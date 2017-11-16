package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.model.Link;
import be.ugent.idlab.tcbl.userdatamanager.model.Status;
import org.springframework.ui.Model;

import java.util.List;

/**
 * <p>Helper for common templates.</p>
 * <p>
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
public class ControllerSupport {

	/**
	 * Prepare the model with the necessary attributes and return the path to the configuration.html template.
	 *
	 * @param model
	 * @param title
	 * @param utext
	 * @param links
	 * @param status
	 * @return configuration.html path
	 */
	public static String preparedConfirmationTemplate(Model model, String title, String utext, List<Link> links, Status status) {
		model.addAttribute("title", title);
		model.addAttribute("utext", utext);
		model.addAttribute("links", links);
		model.addAttribute("status", status);
		return "/confirmation";
	}
}
