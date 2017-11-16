package be.ugent.idlab.tcbl.userdatamanager.controller.support;

import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Controller support for the configuration template.</p>
 * <p>
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
public class ConfirmationTemplate {

	private String title;
	private String utext;
	private List<Link> links;
	private Status status;

	public ConfirmationTemplate() {
		// the template is safe with the following defaults
		title = "";
		utext = "";
		links = null;
		status = null;
	}

	public ConfirmationTemplate(String title) {
		this.title = title;
		utext = "";
		links = null;
		status = null;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUtext() {
		return utext;
	}

	public void setUtext(String utext) {
		this.utext = utext;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public List<Link> getLinks() {
		return links;
	}

	public void addLink(Link link) {
		if (links == null) {
			links = new ArrayList<>();
		}
		links.add(link);
	}

	/**
	 * Prepare a model for this template and return the template path
	 *
	 * @param model the model to be prepared
	 * @return template path
	 */
	public String getPreparedPath(Model model) {
		if (!model.containsAttribute("title")) {
			model.addAttribute("title", title);
		}
		if (!model.containsAttribute("utext")) {
			model.addAttribute("utext", utext);
		}
		if (!model.containsAttribute("links")) {
			model.addAttribute("links", links);
		}
		if (!model.containsAttribute("status")) {
			model.addAttribute("status", status);
		}
		return "/confirmation";
	}
}
