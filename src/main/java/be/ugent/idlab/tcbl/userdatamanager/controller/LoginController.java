package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.controller.support.ActivityLogger;
import be.ugent.idlab.tcbl.userdatamanager.controller.support.ActivityLoggingType;
import be.ugent.idlab.tcbl.userdatamanager.model.NavLink;
import be.ugent.idlab.tcbl.userdatamanager.model.Status;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Controller
public class LoginController {
	private final String redirectDirective;
	private final ActivityLogger activityLogger;

	public LoginController(final Environment environment, ActivityLogger activityLogger) throws Exception {
		// find the redirect path; we need this to do a correct forwarding. The path is composed
		Set<String> clientPropertyKeys = resolveClientPropertyKeys(environment);
		if (!clientPropertyKeys.isEmpty()) {
			String clientPropertyKey = clientPropertyKeys.iterator().next();
			redirectDirective = "redirect:/oauth2/authorization/" + clientPropertyKey; // we assume authorization_code as grant type.
		} else {
			throw new Exception("No client defined.");
		}
		this.activityLogger = activityLogger;
	}

	@RequestMapping("/gluulogout")
	public String gluuLogout(@RequestParam String op,
							 @RequestParam String id_token_hint,
							 @RequestParam String post_logout_redirect_uri,
							 @RequestParam String un) {
		activityLogger.log(un, ActivityLoggingType.logout);
		return "redirect:" + op + "/oxauth/seam/resource/restv1/oxauth/end_session?id_token_hint=" + id_token_hint + "&post_logout_redirect_uri=" + post_logout_redirect_uri;
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

	private static Set<String> resolveClientPropertyKeys(Environment environment) {
		Binder binder = Binder.get(environment);
		BindResult<Map<String, Object>> result = binder.bind(
				"spring.security.oauth2.client.registration", Bindable.mapOf(String.class, Object.class));
		return result.get().keySet();
	}
}
