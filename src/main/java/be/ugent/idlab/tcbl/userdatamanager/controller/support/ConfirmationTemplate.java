/*
 *  Copyright 2019 imec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package be.ugent.idlab.tcbl.userdatamanager.controller.support;

import be.ugent.idlab.tcbl.userdatamanager.model.NavLink;
import be.ugent.idlab.tcbl.userdatamanager.model.Status;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Controller support for the configuration template.</p>
 *
 * @author Martin Vanbrabant
 */
public class ConfirmationTemplate {

	private String title;
	private String utext;
	private List<NavLink> navLinks;
	private Status status;

	public ConfirmationTemplate() {
		// the template is safe with the following defaults
		title = "";
		utext = "";
		navLinks = null;
		status = null;
	}

	public ConfirmationTemplate(String title) {
		this.title = title;
		utext = "";
		navLinks = null;
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

	public List<NavLink> getNavLinks() {
		return navLinks;
	}

	public void addNavLink(NavLink link) {
		if (navLinks == null) {
			navLinks = new ArrayList<>();
		}
		navLinks.add(link);
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
		if (!model.containsAttribute("navLinks")) {
			model.addAttribute("navLinks", navLinks);
		}
		if (!model.containsAttribute("status")) {
			model.addAttribute("status", status);
		}
		return "confirmation";
	}
}
