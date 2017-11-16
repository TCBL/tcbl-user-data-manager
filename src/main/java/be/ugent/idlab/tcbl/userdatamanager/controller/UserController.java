package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.background.Mail;
import be.ugent.idlab.tcbl.userdatamanager.model.Link;
import be.ugent.idlab.tcbl.userdatamanager.model.Status;
import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUser;
import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
	private WebClient webClient = WebClient.create();
	private final TCBLUserRepository tcblUserRepository;
	private final Mail mail;
	private final static Base64.Encoder encoder = Base64.getUrlEncoder();
	private final static Base64.Decoder decoder = Base64.getUrlDecoder();

	/**
	 * Creates a UserController; Spring injects the TCBLUserRepository.
	 *
	 * @param tcblUserRepository A repository where TCBLUsers are stored.
	 * @param mail The background mail sender.
	 */
	public UserController(TCBLUserRepository tcblUserRepository, Mail mail) {
		this.tcblUserRepository = tcblUserRepository;
		this.mail = mail;
	}

	/**
	 * Gets info from the currently authenticated / authorized user, using OpenID Connect to get the inum (Gluu specific)
	 * and SCIM to get other attributes.
	 *
	 * @param model				Gets updated with a TCBLUser object.
	 * @param authentication	Required to perform a UserInfo request.
	 * @return					The path of the view to be rendered.
	 */
	@GetMapping("/index")
	public String userinfo(Model model, OAuth2AuthenticationToken authentication) {
		Map userAttributes = this.webClient
				.mutate().filter(oauth2Credentials(authentication)).build()
				.get()
				.uri(authentication.getClientRegistration().getProviderDetails().getUserInfoUri())
				.retrieve()
				.bodyToMono(Map.class)
				.block();
		String id = userAttributes.get("inum").toString();
		try {
			TCBLUser tcblUser = tcblUserRepository.find(id);
			if (tcblUser == null) {
				tcblUser = new TCBLUser();
				tcblUser.setId(id);
			}
			//model.addAttribute("userAttributes", userAttributes);
			model.addAttribute("tcblUser", tcblUser);
		} catch (Exception e) {
			log.error("Cannot get user info", e);
			model.addAttribute("status", new Status(Status.Value.ERROR, "Your information could not be found."));
		}
		return "/user/index";
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
		return "/user/index";
	}

	@GetMapping("/register")
	public String getRegister(Model model) {
		TCBLUser user = new TCBLUser();
		model.addAttribute("tcblUser", user);
		return "/user/register";
	}

	@PostMapping("/register")
	public String postRegister(HttpServletRequest request, Model model, TCBLUser user) {
		String utext = null;
		List<Link> links = new ArrayList<Link>();
		links.add(new Link(Link.DisplayCondition.ALWAYS, "Home", "/index"));
		Status status = null;

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
			utext = "<p>Registration of '" + user.getUserName() + "' is almost complete.</p>" +
					"<p>We've sent you an email containing a link to activate your account. Please check your mailbox.</p>" +
					"<p>If you don't find the email within a few minutes, check your spam folder too before retrying.</p>";
			links.add(new Link(Link.DisplayCondition.ALWAYS, "Try again", "/user/register"));
			status = new Status(Status.Value.OK, "Email sent. You can close this browser tab.");
		} catch (Exception e) {
			log.error("Cannot register user {}", user.getUserName(), e);
			if (oldActive) {
				utext = "<p>User '" + user.getUserName() + "' was signed up earlier.</p>" +
						"<p>You may use it as-is, reset your password, or try again with a different email address.</p>";
				links.add(new Link(Link.DisplayCondition.ALWAYS, "Recover password", "/user/resetpw"));
				links.add(new Link(Link.DisplayCondition.ALWAYS, "Try again", "/user/register"));
				status = new Status(Status.Value.WARNING, "User signed up earlier.");
			} else {
				utext = "<p>We could not send you an email to complete sign up at this moment.</p>" +
						"<p>Please try again.</p>";
				links.add(new Link(Link.DisplayCondition.ALWAYS, "Try again", "/user/register"));
				status = new Status(Status.Value.ERROR, "Could not send email.");
			}
		}

		return ControllerSupport.preparedConfirmationTemplate(model, "Sign up", utext, links, status);
	}

	// reached when the user clicked the link in the confirmation email
	@GetMapping("/confirm/{id}")
	public String confirmRegistration(Model model, @PathVariable String id) {
		String utext = null;
		List<Link> links = new ArrayList<Link>();
		links.add(new Link(Link.DisplayCondition.ALWAYS, "Home", "/index"));
		Status status = null;

		try {
			String inum = decodeBase64(id);
			TCBLUser user = tcblUserRepository.find(inum);
			if (!user.isActive()) {
				user.setActive(true);
				tcblUserRepository.save(user);
			}
			utext = "<p>You're successfully signed up! You can now use your new account to log in at TCBL related sites.</p>" +
					"<p>See our home page for more options...</p>";
			status = new Status(Status.Value.OK, "Sign up completed.");
		} catch (Exception e) {
			// reached when the url was maniplulated or when the user was deleted in the mean time...
			log.error("Cannot confirm registration of {} ", id, e);
			utext = "<p>We are sorry to tell you that the sign up process failed.</p>" +
					"<p>Are you sure you used the appropriate link?</p>" +
					"<p>Please try again.</p>";
			links.add(new Link(Link.DisplayCondition.ALWAYS, "Try again", "/user/register"));
			status = new Status(Status.Value.ERROR, "Sign up failed.");
		}

		return ControllerSupport.preparedConfirmationTemplate(model, "Sign up completion", utext, links, status);
	}

	@GetMapping("/resetpw")
	public String getResetPassword() {
		return "/user/resetpw";
	}

	@PostMapping("/resetpw")
	public String postResetPassword(HttpServletRequest request, Model model, String mail) {
		String utext = null;
		List<Link> links = new ArrayList<Link>();
		links.add(new Link(Link.DisplayCondition.ALWAYS, "Home", "/index"));
		Status status = null;

		boolean userExists = false;
		try {
			TCBLUser user = tcblUserRepository.findByName(mail);
			userExists = true;
			String baseUri = getUriOneLevelUp(request);
			sendResetMessage(user, baseUri);
			utext = "<p>We've sent an email containing a link to reset your password to '" + mail +
					"'. The link is <b>only valid for one hour</b>, starting now. Please check your mailbox.</p>" +
					"<p>If you don't find the email within a few minutes, check your spam folder too before retrying.</p>";
			links.add(new Link(Link.DisplayCondition.ALWAYS, "Try again", "/user/resetpw"));
			status = new Status(Status.Value.OK, "Email sent. You can close this browser tab.");
		} catch (Exception e) {
			log.error("Cannot send reset pw mail for {}", mail, e);
			if (userExists) {
				utext = "<p>We could not send you an email to complete resetting your password at this moment.</p>" +
						"<p>Please try again.</p>";
				links.add(new Link(Link.DisplayCondition.ALWAYS, "Try again", "/user/resetpw"));
				status = new Status(Status.Value.ERROR, "Could not send email.");
			} else {
				utext = "<p>We could not find a user with the given email address '" + mail + "'.</p>" +
						"<p>Please try again.</p>";
				links.add(new Link(Link.DisplayCondition.ALWAYS, "Try again", "/user/resetpw"));
				status = new Status(Status.Value.ERROR, "User not found.");
			}
		}

		return ControllerSupport.preparedConfirmationTemplate(model, "Reset password", utext, links, status);
	}

	// reached when the user clicked the link in the password reset email
	@GetMapping("/resetpwform/{rpc}")
	public String resetPasswordForm(Model model, @PathVariable String rpc) {
		boolean expired = false;
		try {
			String userId = decodeIdForPassword(rpc, 3600000);
			if (userId == null) {
				expired = true;
				throw new Exception("Link expired.");
			}
			String encodedId = encodeBase64(userId);
			model.addAttribute("rpc", encodedId);

			return "/user/resetpwform";
		} catch (Exception e) {
			String utext = null;
			List<Link> links = new ArrayList<Link>();
			links.add(new Link(Link.DisplayCondition.ALWAYS, "Home", "/index"));
			Status status = null;

			if (expired) {
				utext = "<p>We are sorry to tell you that the reset password process failed.</p>" +
						"<p>The link has expired.</p>" +
						"<p>Please try again and use the link within one hour.</p>";
				links.add(new Link(Link.DisplayCondition.ALWAYS, "Try again", "/user/resetpw"));
				status = new Status(Status.Value.ERROR, "Link expired.");
			} else {
				// reached when the url was maniplulated...
				utext = "<p>We are sorry to tell you that the reset password process failed.</p>" +
						"<p>Please try again.</p>";
				links.add(new Link(Link.DisplayCondition.ALWAYS, "Try again", "/user/resetpw"));
				status = new Status(Status.Value.ERROR, "Reset password failed.");
			}

			return ControllerSupport.preparedConfirmationTemplate(model,"Reset password", utext, links, status);
		}
	}

	@PostMapping("/resetpwform")
	public String resetPasswordForm(Model model, String password, String rpc) {
		String utext = null;
		List<Link> links = new ArrayList<Link>();
		links.add(new Link(Link.DisplayCondition.ALWAYS, "Home", "/index"));
		Status status = null;

		try {
			String inum = decodeBase64(rpc);
			TCBLUser user = tcblUserRepository.find(inum);
			user.setPassword(password);
			tcblUserRepository.save(user);
			utext = "<p>You've successfully updated your password! You can now use your account again to log in at TCBL related sites.</p>" +
					"<p>See our home page for more options...</p>";
			status = new Status(Status.Value.OK, "Password updated.");
		} catch (Exception e) {
			// reached when the user was deleted in the mean time...
			log.error("Cannot reset password of {}", rpc, e);
			utext = "<p>The new password couldn't be saved.</p>" +
					"<p>Please try again.</p>";
			links.add(new Link(Link.DisplayCondition.ALWAYS, "Try again", "/user/resetpw"));
			status = new Status(Status.Value.ERROR, "Could not save the new password.");
		}

		return ControllerSupport.preparedConfirmationTemplate(model, "Reset password", utext, links, status);
	}

	private ExchangeFilterFunction oauth2Credentials(OAuth2AuthenticationToken authentication) {
		return ExchangeFilterFunction.ofRequestProcessor(
				clientRequest -> {
					ClientRequest authorizedRequest = ClientRequest.from(clientRequest)
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + authentication.getAccessToken().getTokenValue())
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

}
