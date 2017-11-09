package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.background.Mail;
import be.ugent.idlab.tcbl.userdatamanager.model.Message;
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
			model.addAttribute("userAttributes", userAttributes);
			model.addAttribute("tcblUser", tcblUser);
			return "/user/index";
		} catch (Exception e) {
			model.addAttribute("message", new Message("Error", "Cannot get your information."));
			log.error("Cannot get user info", e);
			return "/index";
		}
	}

	@PostMapping("/update")
	public String update (TCBLUser user, Model model) {
		try {
			tcblUserRepository.save(user);
			model.addAttribute("tcblUser", user);
			/* activated: */ model.addAttribute("message", new Message("Update successful", "Your information is successfully updated."));
		} catch (Exception e) {
			log.error("Cannot update user info", e);
			model.addAttribute("message", new Message("Error", "Cannot update your information."));
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
		try {
			TCBLUser newUser = tcblUserRepository.create(user);
			String baseUri = getUriOneLevelUp(request);
			sendRegisterMessage(newUser, baseUri);
		} catch (Exception e) {
			log.error("Cannot register user {}", user.getUserName(), e);
			model.addAttribute("message", new Message("Registration failed",
					e.getMessage()));
		}
		return "/user/registered";
	}

	@GetMapping("/confirm/{id}")
	public String confirmRegistration(Model model, @PathVariable String id) {
		String inum = decodeBase64(id);
		try {
			TCBLUser user = tcblUserRepository.find(inum);
			if (!user.isActive()) {
				user.setActive(true);
				tcblUserRepository.save(user);
				/* activated: */ model.addAttribute("message", new Message("Registration completed", "You can now log in."));
			}
		} catch (Exception e) {
			log.error("Cannot confirm registration of {}", inum, e);
			model.addAttribute("message", new Message("Registration failed",
					e.getMessage()));
		}
		return "/user/confirmed";
	}

	@GetMapping("/resetpw")
	public String getResetPassword() {
		return "/user/resetpw";
	}

	@PostMapping("/resetpw")
	public String postResetPassword(HttpServletRequest request, Model model, String mail) {
		try {
			TCBLUser user = tcblUserRepository.findByName(mail);
			String baseUri = getUriOneLevelUp(request);
			sendResetMessage(user, baseUri);
		} catch (Exception e) {
			log.error("Cannot send reset pw mail for {} ", mail);
			model.addAttribute("message", new Message("Reset Password failed", "Cannot send mail."));
		}
		return "/user/pwmailsent";
	}

	@GetMapping("/resetpwform/{rpc}")
	public String resetPasswordForm(Model model, @PathVariable String rpc) {
		String userId = decodeIdForPassword(rpc, 3600000);
		if (userId != null) {
			String encodedId = encodeBase64(userId);
			model.addAttribute("rpc", encodedId);
			return "/user/resetpwform";
		} else {
			model.addAttribute("message", new Message("Reset Password failed", "Link expired."));
			return "/user/pwmailsent";
		}
	}

	@PostMapping("/resetpwform")
	public String resetPasswordForm(Model model, String password, String rpc) {
		String inum = decodeBase64(rpc);
		try {
			TCBLUser user = tcblUserRepository.find(inum);
			user.setPassword(password);
			tcblUserRepository.save(user);
		} catch (Exception e) {
			log.error("Cannot reset password of {}", rpc, e);
			model.addAttribute("message", new Message("Reset Password failed",
					e.getMessage()));
		}
		return "/user/passwordset";
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
