package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUser;
import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

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

	/**
	 * Creates a UserController; Spring injects the TCBLUserRepository.
	 *
	 * @param tcblUserRepository A repository where TCBLUsers are stored.
	 */
	public UserController(TCBLUserRepository tcblUserRepository) {
		this.tcblUserRepository = tcblUserRepository;
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
				tcblUser.setUserName(id);
			}
			model.addAttribute("userAttributes", userAttributes);
			model.addAttribute("tcblUser", tcblUser);
		} catch (Exception e) {
			log.error("Cannot get user info", e);
			// TODO
		}
		return "user/index";
	}

	@RequestMapping(value = "/user/update", method = RequestMethod.POST)
	public String update (TCBLUser user, Model model) {
		try {
			tcblUserRepository.save(user);
			model.addAttribute("tcblUser", user);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO
		}
		return "/user/index";
	}

	@RequestMapping(value = "/user/register", method = RequestMethod.GET)
	public String getRegister(Model model) {
		TCBLUser user = new TCBLUser();
		model.addAttribute("tcblUser", user);
		// TODO: send id (sub) in mail to activate user.
		return "/user/register";
	}

	@RequestMapping(value = "/user/register", method = RequestMethod.POST)
	public String postRegister(TCBLUser user) {
		try {
			tcblUserRepository.create(user);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO
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

}
