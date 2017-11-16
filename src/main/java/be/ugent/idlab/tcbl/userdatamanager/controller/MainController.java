package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.controller.support.Link;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Controller
public class MainController {

	@RequestMapping("/")
	public String root() {
		return "redirect:index";
	}

	@RequestMapping("/index")
	public String index(Model model) {
		List<Link> links = new ArrayList<>();
		links.add(new Link(Link.DisplayCondition.ANONYMOUS, "Sign up", "/user/register"));
		links.add(new Link(Link.DisplayCondition.ANONYMOUS, "Log in with TCBL", "/oiclogin"));
		//TODO links.add(new Link(Link.DisplayCondition.AUTHENTICATED, "Manage my profile info", "/user/index"));
		links.add(new Link(Link.DisplayCondition.ALWAYS, "Reset password", "/user/resetpw"));
		links.add(new Link(Link.DisplayCondition.AUTHENTICATED, "TCBL applications", "/applications"));
		model.addAttribute("links", links);
		return "/index";
	}
}

