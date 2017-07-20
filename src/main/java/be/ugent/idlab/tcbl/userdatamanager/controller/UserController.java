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
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Controller
public class UserController {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private WebClient webClient = WebClient.create();
	private final TCBLUserRepository tcblUserRepository;

	public UserController(TCBLUserRepository tcblUserRepository) {
		this.tcblUserRepository = tcblUserRepository;
	}

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

	@RequestMapping("/user/update")
	public String update (TCBLUser user, Model model) {
		try {
			tcblUserRepository.save(user);
			model.addAttribute("tcblUser", user);
		} catch (Exception e) {
			// TODO
		}
		return "/user/index";
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
