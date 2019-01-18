package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.model.NavLink;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Gerald Haesendonck
 */
@Controller
public class MainController {

	@RequestMapping("/")
	public String root() {
		return "redirect:/index";
	}

	@RequestMapping("/index")
	public String index(Model model) {
		model.addAttribute("nohomelink", true);
		List<NavLink> navLinks = new ArrayList<>();
		navLinks.add(new NavLink(NavLink.DisplayCondition.ANONYMOUS, "Sign up for TCBL", "/user/register"));
		navLinks.add(new NavLink(NavLink.DisplayCondition.ALWAYS, "Manage your profile", "/user/info"));
		navLinks.add(new NavLink(NavLink.DisplayCondition.ALWAYS, "Reset password", "/user/resetpw"));
		navLinks.add(new NavLink(NavLink.DisplayCondition.ALWAYS, "TCBL Services", "/services"));
		model.addAttribute("navLinks", navLinks);
		return "index";
	}
}

