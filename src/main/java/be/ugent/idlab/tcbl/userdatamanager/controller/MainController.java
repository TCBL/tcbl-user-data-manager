package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.model.Link;
import be.ugent.idlab.tcbl.userdatamanager.model.Status;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		List<Link> links = new ArrayList<Link>();
		links.add(new Link(Link.DisplayCondition.ANONYMOUS, "Sign up", "/user/register"));
		links.add(new Link(Link.DisplayCondition.ANONYMOUS, "Log in with TCBL", "/oiclogin"));
		//TODO links.add(new Link(Link.DisplayCondition.AUTHENTICATED, "Manage my profile info", "/user/index"));
		links.add(new Link(Link.DisplayCondition.ALWAYS, "Recover password", "/user/resetpw"));
		links.add(new Link(Link.DisplayCondition.AUTHENTICATED, "TCBL applications", "/applications"));
		model.addAttribute("links", links);
		model.addAttribute("status", new Status(Status.Value.WARNING, "This is work in progress, more to come soon!"));
		return "/index";
	}
}

