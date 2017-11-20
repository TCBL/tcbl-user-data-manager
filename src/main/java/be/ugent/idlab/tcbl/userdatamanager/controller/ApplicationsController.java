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
 * @author Martin Vanbrabant
 */
@Controller
public class ApplicationsController {

	@RequestMapping("/applications")
	public String applications(Model model) {
		List<Link> navLinks = new ArrayList<>();
		navLinks.add(new Link(Link.DisplayCondition.ALWAYS, "Home", "/index"));
		model.addAttribute("navLinks", navLinks);
		return "/applications";
	}
}

