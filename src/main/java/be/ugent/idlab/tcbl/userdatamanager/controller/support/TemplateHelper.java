package be.ugent.idlab.tcbl.userdatamanager.controller.support;

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
public class TemplateHelper {

	/**
	 * Get path to the configuration template, for which the model was prepared with the attributes (see parameters).
	 *
	 * @param model
	 * @param title
	 * @param utext
	 * @param links
	 * @param status
	 * @return path to the template
	 */
	public static String getPreparedConfirmationTemplate(Model model, String title, String utext, List<Link> links, Status status) {
		model.addAttribute("title", title);
		model.addAttribute("utext", utext);
		model.addAttribute("links", links);
		model.addAttribute("status", status);
		return "/confirmation";
	}
}
