package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.controller.support.SrvLink;
import be.ugent.idlab.tcbl.userdatamanager.controller.support.NavLink;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
@ConfigurationProperties(prefix="tudm.tcbl-services")
public class ServicesController {

	private List<SrvLink> srvLinksTCBL;
	private List<SrvLink> srvLinksASP;

	public void setSrvLinksTCBL(List<SrvLink> srvLinksTCBL) {
		this.srvLinksTCBL = srvLinksTCBL;
	}

	public void setSrvLinksASP(List<SrvLink> srvLinksASP) {
		this.srvLinksASP = srvLinksASP;
	}

	@RequestMapping("/services")
	public String services(Model model) {
		List<NavLink> navLinks = new ArrayList<>();
		navLinks.add(new NavLink(NavLink.DisplayCondition.ALWAYS, "Home", "/index"));
		model.addAttribute("navLinks", navLinks);

		model.addAttribute("srvLinksTCBL", srvLinksTCBL);

		model.addAttribute("srvLinksASP", srvLinksASP);

		return "/services";
	}
}
