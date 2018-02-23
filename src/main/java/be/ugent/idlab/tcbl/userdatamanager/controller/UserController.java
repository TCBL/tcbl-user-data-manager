package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.background.Mail;
import be.ugent.idlab.tcbl.userdatamanager.controller.support.ConfirmationTemplate;
import be.ugent.idlab.tcbl.userdatamanager.model.NavLink;
import be.ugent.idlab.tcbl.userdatamanager.model.Status;
import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUser;
import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * Controller for actions where (TCBL) users are involved.
 *
 * @author Gerald Haesendonck
 */
@Controller
@RequestMapping("user")
public class UserController {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final TCBLUserRepository tcblUserRepository;
	private final Mail mail;
	private final static Base64.Encoder encoder = Base64.getUrlEncoder();
	private final static Base64.Decoder decoder = Base64.getUrlDecoder();
	private OAuth2AuthorizedClientService authorizedClientService;

	@Value("${tudm.tcbl-privacy-url}")
	private String privacyUrl;


	/**
	 * Creates a UserController; Spring injects the TCBLUserRepository.
	 *
	 * @param tcblUserRepository A repository where TCBLUsers are stored.
	 * @param mail The background mail sender.
	 */
	public UserController(TCBLUserRepository tcblUserRepository, Mail mail, OAuth2AuthorizedClientService authorizedClientService) {
		this.tcblUserRepository = tcblUserRepository;
		this.mail = mail;
		this.authorizedClientService = authorizedClientService;
	}

	@ModelAttribute("privacyUrl")
	public String getPrivacyUrl() {
		return privacyUrl;
	}

	/**
	 * Gets info from the currently authenticated / authorized user, using OpenID Connect to get the inum (Gluu specific)
	 * and SCIM to get other attributes.
	 *
	 * @param model				Gets updated with a TCBLUser object.
	 * @param authentication	Required to perform a UserInfo request.
	 * @return					The path of the view to be rendered.
	 */
	@GetMapping("/info")
	public String userinfo(Model model, OAuth2AuthenticationToken authentication) {
		try {
			OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(), authentication.getName());
			String userInfoEndpointUri = authorizedClient.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri();


			//WebClient webClient = WebClient.create(authentication.getClientRegistration().getProviderDetails().getUserInfoUri());
			Map userAttributes = WebClient.builder()
					.filter(oauth2Credentials(authorizedClient))
					.build()
					.get()
					.uri(userInfoEndpointUri)
					.retrieve()
					.bodyToMono(Map.class)
					.block();
			String id = userAttributes.get("inum").toString();
			TCBLUser tcblUser = tcblUserRepository.find(id);
			// note: if no user found, tcblUser is null, and this is covered nicely by the view
			model.addAttribute("tcblUser", tcblUser);
		} catch (Exception e) {
			log.error("Cannot get user info. Possible causes: wrong OP configured; scope 'inum' is not defined or assigned to the user manager client.", e);
			model.addAttribute("status", new Status(Status.Value.ERROR, "Your information could not be found."));
		}
		return "user/info";
	}

	@PostMapping("/update")
	public String update (TCBLUser user, Model model) {
		try {
			tcblUserRepository.save(user);
			model.addAttribute("tcblUser", user);
			model.addAttribute("status", new Status(Status.Value.OK, "Your information is updated."));
		} catch (Exception e) {
			log.error("Cannot update user info", e);
			model.addAttribute("status", new Status(Status.Value.ERROR, "Your information could not be updated."));
		}
		return "user/info";
	}

	@GetMapping("/register")
	public String getRegister(Model model) {
		TCBLUser user = new TCBLUser();

		// set defaults for new user:
		user.setSubscribedNL(true);

		model.addAttribute("tcblUser", user);
		return "user/register";
	}

	@PostMapping("/register")
	public String postRegister(HttpServletRequest request, Model model, TCBLUser user) {
		ConfirmationTemplate ct = new ConfirmationTemplate("Sign up for TCBL");

		boolean oldActive = false;
		try {
			TCBLUser oldUser;
			try {
				oldUser = tcblUserRepository.findByName(user.getUserName());
			} catch (Exception e) {
				oldUser = null;
			}
			if (oldUser != null) {
				if (oldUser.isActive()) {
					oldActive = true;
					throw new Exception("User already exists.");
				} else {
					// a previous registration attempt on this username was not completed...
					// let's delete this old entry.
					tcblUserRepository.deleteTCBLUser(oldUser);
				}
			}
			TCBLUser newUser = tcblUserRepository.create(user);
			String baseUri = getUriOneLevelUp(request);
			sendRegisterMessage(newUser, baseUri);
			ct.setUtext(getEmailInformationText(user.getUserName()));
			ct.addNavLink(new NavLink(NavLink.DisplayCondition.ALWAYS, "Try again", "/user/register"));
			ct.setStatus(new Status(Status.Value.OK, "Please check your mailbox."));
		} catch (Exception e) {
			log.error("Cannot register user {}", user.getUserName(), e);
			if (oldActive) {
				ct.setUtext("<p>User '" + user.getUserName() + "' was signed up earlier.</p>" +
						"<p>You may use it as-is, reset your password, or try again with a different email address.</p>");
				ct.addNavLink(new NavLink(NavLink.DisplayCondition.ALWAYS, "Reset password", "/user/resetpw"));
				ct.addNavLink(new NavLink(NavLink.DisplayCondition.ALWAYS, "Try again", "/user/register"));
				ct.setStatus(new Status(Status.Value.WARNING, "User signed up earlier."));
			} else {
				ct.setUtext("<p>We could not send you an email to complete sign up for TCBL at this moment.</p>" +
						"<p>Please try again.</p>");
				ct.addNavLink(new NavLink(NavLink.DisplayCondition.ALWAYS, "Try again", "/user/register"));
				ct.setStatus(new Status(Status.Value.ERROR, "Could not send email."));
			}
		}

		return ct.getPreparedPath(model);
	}

	// reached when the user clicked the link in the confirmation email
	@GetMapping("/confirm/{id}")
	public String confirmRegistration(Model model, @PathVariable String id) {
		ConfirmationTemplate ct = new ConfirmationTemplate("Sign up for TCBL completion");

		try {
			String inum = decodeBase64(id);
			TCBLUser user = tcblUserRepository.find(inum);
			if (!user.isActive()) {
				user.setActive(true);
				tcblUserRepository.save(user);
			}
			ct.setUtext("<p>You're successfully signed up!</p>" +
					"<p>You can now use your new account to login.</p>" +
					"<p>See our home page for more options...");
			ct.setStatus(new Status(Status.Value.OK, "Sign up for TCBL completed."));
		} catch (Exception e) {
			// reached when the url was manipulated or when the user was deleted in the mean time...
			log.error("Cannot confirm registration of {} ", id, e);
			ct.setUtext("<p>We are sorry to tell you that the sign up for TCBL process failed.</p>" +
					"<p>Are you sure you used the appropriate link?</p>" +
					"<p>Please try again.</p>");
			ct.addNavLink(new NavLink(NavLink.DisplayCondition.ALWAYS, "Try again", "/user/register"));
			ct.setStatus(new Status(Status.Value.ERROR, "Sign up for TCBL failed."));
		}

		return ct.getPreparedPath(model);
	}

	@GetMapping("/resetpw")
	public String getResetPassword(Model model) {
		return "user/resetpw";
	}

	@PostMapping("/resetpw")
	public String postResetPassword(HttpServletRequest request, Model model, String mail) {
		ConfirmationTemplate ct = new ConfirmationTemplate("Reset password");

		boolean userExists = false;
		try {
			TCBLUser user = tcblUserRepository.findByName(mail);
			userExists = true;
			String baseUri = getUriOneLevelUp(request);
			sendResetMessage(user, baseUri);
			ct.setUtext(getEmailInformationText(mail) +
					"<p>The link is only valid for <b>one hour</b>, starting now.</p>");
			ct.addNavLink(new NavLink(NavLink.DisplayCondition.ALWAYS, "Try again", "/user/resetpw"));
			ct.setStatus(new Status(Status.Value.OK, "Please check your mailbox."));
		} catch (Exception e) {
			log.error("Cannot send reset pw mail for {}", mail, e);
			if (userExists) {
				ct.setUtext("<p>We could not send you an email to complete resetting your password at this moment.</p>" +
						"<p>Please try again.</p>");
				ct.addNavLink(new NavLink(NavLink.DisplayCondition.ALWAYS, "Try again", "/user/resetpw"));
				ct.setStatus(new Status(Status.Value.ERROR, "Could not send email."));
			} else {
				ct.setUtext("<p>We could not find a user with the given email address '" + mail + "'.</p>" +
						"<p>Please try again.</p>");
				ct.addNavLink(new NavLink(NavLink.DisplayCondition.ALWAYS, "Try again", "/user/resetpw"));
				ct.setStatus(new Status(Status.Value.ERROR, "User not found."));
			}
		}

		return ct.getPreparedPath(model);
	}

	// reached when the user clicked the link in the password reset email
	@GetMapping("/resetpwform/{rpc}")
	public String resetPasswordForm(Model model, @PathVariable String rpc) {
		boolean expired = false;
		try {
			String userId = decodeIdForPassword(rpc, 3600000);
			if (userId == null) {
				expired = true;
				throw new Exception("NavLink expired.");
			}
			String encodedId = encodeBase64(userId);
			model.addAttribute("rpc", encodedId);

			return "user/resetpwform";
		} catch (Exception e) {
			ConfirmationTemplate ct = new ConfirmationTemplate("Reset password");

			if (expired) {
				ct.setUtext("<p>We are sorry to tell you that the reset password process failed.</p>" +
						"<p>The link has expired.</p>" +
						"<p>Please try again and use the link within one hour.</p>");
				ct.addNavLink(new NavLink(NavLink.DisplayCondition.ALWAYS, "Try again", "/user/resetpw"));
				ct.setStatus(new Status(Status.Value.ERROR, "NavLink expired."));
			} else {
				// reached when the url was maniplulated...
				ct.setUtext("<p>We are sorry to tell you that the reset password process failed.</p>" +
						"<p>Are you sure you used the appropriate link?</p>" +
						"<p>Please try again.</p>");
				ct.addNavLink(new NavLink(NavLink.DisplayCondition.ALWAYS, "Try again", "/user/resetpw"));
				ct.setStatus(new Status(Status.Value.ERROR, "Reset password failed."));
			}

			return ct.getPreparedPath(model);
		}
	}

	@PostMapping("/resetpwform")
	public String resetPasswordForm(Model model, String password, String rpc) {
		ConfirmationTemplate ct = new ConfirmationTemplate("Reset password");

		try {
			String inum = decodeBase64(rpc);
			TCBLUser user = tcblUserRepository.find(inum);
			user.setPassword(password);
			tcblUserRepository.save(user);
			ct.setUtext("<p>You've successfully updated your password!</p>");
			ct.setStatus(new Status(Status.Value.OK, "Password updated."));
		} catch (Exception e) {
			// reached when the user was deleted in the mean time...
			log.error("Cannot reset password of {}", rpc, e);
			ct.setUtext("<p>The new password couldn't be saved.</p>" +
					"<p>Please try again.</p>");
			ct.addNavLink(new NavLink(NavLink.DisplayCondition.ALWAYS, "Try again", "/user/resetpw"));
			ct.setStatus(new Status(Status.Value.ERROR, "Could not save the new password."));
		}

		return ct.getPreparedPath(model);
	}

	/*private ExchangeFilterFunction oauth2Credentials(OAuth2AuthenticationToken authentication) {
		return ExchangeFilterFunction.ofRequestProcessor(
				clientRequest -> {
					ClientRequest authorizedRequest = ClientRequest.from(clientRequest)
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + authentication.getAccessToken().getTokenValue())
							.build();
					return Mono.just(authorizedRequest);
				});
	}*/
	private ExchangeFilterFunction oauth2Credentials(OAuth2AuthorizedClient authorizedClient) {
		return ExchangeFilterFunction.ofRequestProcessor(
				clientRequest -> {
					ClientRequest authorizedRequest = ClientRequest.from(clientRequest)
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + authorizedClient.getAccessToken().getTokenValue())
							.build();
					return Mono.just(authorizedRequest);
				});
	}

	static String encodeBase64(final String input) {
		return encoder.encodeToString(input.getBytes(StandardCharsets.UTF_8));
	}

	static String decodeBase64(final String input) {
		return new String(decoder.decode(input), StandardCharsets.UTF_8);
	}

	static String generateResetPasswordCode(final String id) {
		// Generate a date as string to append to the id, which represents the current time
		long now = new Date().getTime();
		String encodedDate = Long.toString(now, Character.MAX_RADIX);
		return encodeBase64(id + '|' + encodedDate);
	}

	static String decodeIdForPassword(final String resetPasswordCode, final long maxTime) {
		String unbase64 = decodeBase64(resetPasswordCode);
		String[] data = unbase64.split("\\|");
		String encodedDate = data[1];
		long requestDate = Long.parseLong(encodedDate, Character.MAX_RADIX);
		// compare current time with requested time
		long now = new Date().getTime();
		if (now - requestDate <= maxTime) {
			return data[0]; // clicked within one hour, so return user id
		} else {
			return null;	// not valid anymore
		}
	}

	/**
	 * Sends the mail for registration asynchronously. We don't want the app to block.
	 * @param user	The user to send a mail to.
	 */
	private void sendRegisterMessage(final TCBLUser user, final String baseUri) {
		String encodedId = encodeBase64(user.getId());
		String text = "<p>Thank you for becoming a TCBL member. Click <a href=\""
				+ baseUri + "/confirm/" + encodedId
				+ "\">here</a> to activate your account.</p>";
		mail.send(user.getUserName(), "Registration TCBL", text);
	}

	private void sendResetMessage(final TCBLUser user, final String baseUri) {
		String passwordResetCode = generateResetPasswordCode(user.getId());
		String text = "<p>You receive this mail because you want to reset your TCBL password. Click <a href=\""
				+ baseUri + "/resetpwform/" + passwordResetCode
				+ "\">here</a> to do so.</p>" +
				"<p>If you didn't request to reset your password, you can just ignore this e-mail.</p>";
		mail.send(user.getUserName(), "Reset password for TCBL", text);
	}

	private String getUriOneLevelUp(final HttpServletRequest request) {
		String reqUri = request.getRequestURL().toString();
		return reqUri.substring(0, reqUri.lastIndexOf('/'));
	}

	private String getEmailInformationText(String addressee) {
		return	String.format("<p>We've sent an email with further instructions to <b>%s</b>.</p>", addressee) +
				"<p>If you don't find the email within a few minutes, check your spam folder too before retrying.</p>" +
				"<p>When you follow the link in the email, a new browser tab will open. You can close this browser tab at that time.</p>";
	}

}
