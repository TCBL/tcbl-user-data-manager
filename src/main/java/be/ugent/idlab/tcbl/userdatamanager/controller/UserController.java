package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.TCBLUserDataManager;
import be.ugent.idlab.tcbl.userdatamanager.model.Message;
import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUser;
import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ExecutorService;

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
	private final JavaMailSender mailSender;
	private final Base64.Encoder encoder;
	private final Base64.Decoder decoder;

	private final ExecutorService executor;

	/**
	 * Creates a UserController; Spring injects the TCBLUserRepository.
	 *
	 * @param tcblUserRepository A repository where TCBLUsers are stored.
	 * @param mailSender The Spring library to send mails.
	 * @param executor	 An executor service to send mails asynchronously. Configured in {@link TCBLUserDataManager}.
	 */
	public UserController(TCBLUserRepository tcblUserRepository, JavaMailSender mailSender, ExecutorService executor) {
		this.executor = executor;
		this.tcblUserRepository = tcblUserRepository;
		this.mailSender = mailSender;
		encoder = Base64.getUrlEncoder();
		decoder = Base64.getUrlDecoder();
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
			//model.addAttribute("message", new Message("Update successful", "Your information is successfully updated."));
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
				/*model.addAttribute("message", new Message("Registration completed",
						"You can now log in."));*/
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
		return "user/resetpw";
	}

	@PostMapping("/user/resetpw")
	public String postResetPassword(HttpServletRequest request, Model model, String mail) {
		try {
			TCBLUser user = tcblUserRepository.findByName(mail);
			String baseUri = getUriOneLevelUp(request);
			sendResetMessage(user, baseUri);
		} catch (Exception e) {
			log.error("Cannot send reset pw mail for {} ", mail);
			model.addAttribute("message", new Message("Reset Password failed", "Cannot reset password."));
		}
		return "/user/pwmailsent";
	}

	@GetMapping("/resetpwform/{rpc}")
	public String resetPasswordForm(Model model, @PathVariable String rpc) {
		model.addAttribute("rpc", rpc);
		return "/user/resetpwform";
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
			model.addAttribute("message", new Message("Resetting password failed",
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

	private String encodeBase64(final String input) {
		return encoder.encodeToString(input.getBytes(StandardCharsets.UTF_8));
	}

	private String decodeBase64(final String input) {
		return new String(decoder.decode(input), StandardCharsets.UTF_8);
	}

	/**
	 * Sends the mail for registration asynchronously. We don't want the app to block.
	 * @param user	The user to send a mail to.
	 */
	private void sendRegisterMessage(final TCBLUser user, final String baseUri) {
		executor.execute(() -> {
			try {
				String encodedId = encodeBase64(user.getId());
				MimeMessage message = mailSender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(message);
				helper.setSubject("Registration TCBL");
				helper.setFrom("no-reply@tcbl.eu");
				helper.setTo(user.getUserName());
				helper.setText("<p>Thank you for becoming a TCBL member. Click <a href=\""
						+ baseUri + "/confirm/" + encodedId
						+ "\">here</a> to activate your account.</p>", true);
				mailSender.send(message);
				log.debug("Mail sent to " + user.getUserName());
			} catch (MessagingException e) {
				log.error("Could not send mail to {}. ", user.getUserName(), e);
			}
		});
	}

	private void sendResetMessage(final TCBLUser user, final String baseUri) {
		executor.execute(() -> {
			try {
				String encodedId = encodeBase64(user.getId());
				MimeMessage message = mailSender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(message);
				helper.setSubject("Reset password for TCBL");
				helper.setFrom("no-reply@tcbl.eu");
				helper.setTo(user.getUserName());
				helper.setText("<p>You receive this mail because you want to reset your TCBL password. Click <a href=\""
						+ baseUri + "/resetpwform/" + encodedId
						+ "\">here</a> to do so.</p>" +
						"<p>If you didn't request to reset your password, you can just ignore this e-mail.</p>", true);
				mailSender.send(message);
				log.debug("Mail sent to " + user.getUserName());
			} catch (MessagingException e) {
				log.error("Could not send mail to {}. ", user.getUserName(), e);
			}
		});
	}

	private String getUriOneLevelUp(final HttpServletRequest request) {
		String reqUri = request.getRequestURL().toString();
		return reqUri.substring(0, reqUri.lastIndexOf('/'));
	}

}
