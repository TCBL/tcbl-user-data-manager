package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.controller.support.SrvLink;
import be.ugent.idlab.tcbl.userdatamanager.controller.support.NavLink;
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
public class ServicesController {

	@RequestMapping("/services")
	public String services(Model model) {
		List<NavLink> navLinks = new ArrayList<>();
		navLinks.add(new NavLink(NavLink.DisplayCondition.ALWAYS, "Home", "/index"));
		model.addAttribute("navLinks", navLinks);

		List<SrvLink> srvLinksTCBL = new ArrayList<>();
		srvLinksTCBL.add(new SrvLink("Main TCBL website", "https://tcbl.eu/", "button--srvlink--main"));
		srvLinksTCBL.add(new SrvLink("_ZINE, the TCBL magazine", "https://zine.tcbl.eu/", "button--srvlink--zine"));
		srvLinksTCBL.add(new SrvLink("TCBL Labs platform", "https://labs.tcbl.eu/", "srvlink--labs"));
		model.addAttribute("srvLinksTCBL", srvLinksTCBL);

		List<SrvLink> srvLinksASP = new ArrayList<>();
		srvLinksASP.add(new SrvLink("Thela", "https://thela.cleviria.it/", "button--srvlink--asp"));
		model.addAttribute("srvLinksASP", srvLinksASP);

		return "/services";
	}
}
