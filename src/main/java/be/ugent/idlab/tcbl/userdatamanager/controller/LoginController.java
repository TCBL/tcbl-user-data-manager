package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.model.NavLink;
import be.ugent.idlab.tcbl.userdatamanager.model.Status;
import org.springframework.boot.autoconfigure.security.oauth2.client.ClientRegistrationAutoConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Controller
public class LoginController {
	private final String redirectDirective;

	public LoginController(final Environment environment) throws Exception {
		
		// find the redirect path; we need this to do a correct forwarding. The path is composed
		// with the authorized-grand-type and the client-alias, which can be found in the configuration.

		Set<String> clientPropertyKeys = ClientRegistrationAutoConfiguration.resolveClientPropertyKeys(environment);
		if (!clientPropertyKeys.isEmpty()) {
			String clientPropertyKey = clientPropertyKeys.iterator().next();
			String fullClientPropertyKey = ClientRegistrationAutoConfiguration.CLIENT_PROPERTY_PREFIX + "." + clientPropertyKey;
			String authGrantTypeKey = fullClientPropertyKey + ".authorization-grant-type";
			String clientAliasKey = fullClientPropertyKey + ".client-alias";
			if (environment.containsProperty(clientAliasKey) && environment.containsProperty(authGrantTypeKey)) {
				String authGrantTypeDirective = environment.getProperty(authGrantTypeKey).replace('_', '/');
				String clientAlias = environment.getProperty(clientAliasKey);
				redirectDirective = "redirect:/oauth2/" + authGrantTypeDirective + "/" + clientAlias;
			} else {
				throw new Exception("authorization-grant-type or client-alias not specified.");
			}
		} else {
			throw new Exception("No client defined.");
		}
	}

	@RequestMapping("/oiclogin")
	public String login() {
		return redirectDirective;
	}

	@RequestMapping("/loginrequired")
	public String loginPlease(Model model) {
		model.addAttribute("nohomelink", true);
		List<NavLink> navLinks = new ArrayList<>();
		navLinks.add(new NavLink(NavLink.DisplayCondition.ALWAYS, "Login with TCBL", "/oiclogin"));
		navLinks.add(new NavLink(NavLink.DisplayCondition.ALWAYS, "Sign up for TCBL", "/user/register"));
		model.addAttribute("navLinks", navLinks);
		model.addAttribute("status", new Status(Status.Value.ERROR, "Login required."));
		return "loginrequired";
	}
}
