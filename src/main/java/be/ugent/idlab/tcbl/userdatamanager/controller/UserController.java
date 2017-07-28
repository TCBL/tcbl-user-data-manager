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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
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
	@RequestMapping("/user/index")
	public String userinfo(Model model, OAuth2AuthenticationToken authentication) {
		Map userAttributes = this.webClient
				.filter(oauth2Credentials(authentication))
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

	@RequestMapping(value = "/user/update", method = RequestMethod.POST)
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

	@RequestMapping(value = "/user/register", method = RequestMethod.GET)
	public String getRegister(Model model) {
		TCBLUser user = new TCBLUser();
		model.addAttribute("tcblUser", user);
		return "/user/register";
	}

	@RequestMapping(value = "/user/register", method = RequestMethod.POST)
	public String postRegister(Model model, TCBLUser user) {
		try {
			TCBLUser newUser = tcblUserRepository.create(user);
			sendRegisterMessage(newUser);
			model.addAttribute("message", new Message("Registration almost complete",
					"An e-mail containing a link to confirm the registration will arrive soon."));
		} catch (Exception e) {
			log.error("Cannot register user {}", user.getUserName(), e);
			model.addAttribute("message", new Message("Registration failed",
					e.getMessage()));
		}
		return "/index";
	}

	@RequestMapping(value = "/user/confirm/{id}", method = RequestMethod.GET)
	public String confirmRegistration(Model model, @PathVariable String id) {
		String inum = decodeBase64(id);
		try {
			TCBLUser user = tcblUserRepository.find(inum);
			if (!user.isActive()) {
				user.setActive(true);
				tcblUserRepository.save(user);
				model.addAttribute("message", new Message("Registration completed",
						"You can now log in."));
			}
		} catch (Exception e) {
			log.error("Cannot confirm registration of {}", inum, e);
			model.addAttribute("message", new Message("Registration failed",
					e.getMessage()));
		}
		return "/index";
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
	private void sendRegisterMessage(final TCBLUser user) {
		executor.execute(() -> {
			try {
				String encodedId = encodeBase64(user.getId());
				MimeMessage message = mailSender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(message);
				helper.setSubject("Registration TCBL");
				helper.setFrom("no-reply@tcbl.eu");
				helper.setTo(user.getUserName());
				helper.setText("<p>Thank you for becoming a TCBL member. Click <a href=\""
						+ "https://ravel.elis.ugent.be:8443/user/confirm/" + encodedId
						+ "\">here</a> to activate your account.</p>", true);
				mailSender.send(message);
				log.debug("Mail sent to " + user.getUserName());
			} catch (MessagingException e) {
				log.error("Could not send mail. ", e);
			}
		});
	}

}
