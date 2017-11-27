package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.model.NavLink;
import be.ugent.idlab.tcbl.userdatamanager.model.ServicesLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
@Controller
public class ServicesController {

	private final ServicesLoader servicesLoader;

	public ServicesController(ServicesLoader servicesLoader) {
		this.servicesLoader = servicesLoader;
	}

	@RequestMapping("/services")
	public String services(Model model,
						   @RequestParam(value = "refresh", required = false) boolean refresh) {

	 	// Admins can append ?refresh=true to install and immediately view the result
		// of an update to the services json file.
		// This is better than having to restart the application...
		if (refresh) {
			servicesLoader.refresh();
		}

		model.addAttribute("srvLinksTCBL", servicesLoader.getSrvLinksTCBL());

		model.addAttribute("srvLinksASP", servicesLoader.getSrvLinksASP());

		return "services";
	}

	/**
	 * This endpoint triggers the refresh of the ServicesLoader.
	 *
	 * It is not reachable using the normal navigation controls.
	 * Admins can use it to install and immediately view the result of an update to the services json file.
	 * This is better than having to restart the application...
	 */
	@RequestMapping("/services/refresh")
	public String refresh() {

		return "redirect:/services";
	}

}
