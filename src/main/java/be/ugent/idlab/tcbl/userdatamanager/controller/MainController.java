
/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
		links.add(new Link(Link.DisplayCondition.ALWAYS, "Reset password", "/user/resetpw"));
		links.add(new Link(Link.DisplayCondition.ANONYMOUS, "Log in with TCBL", "/oiclogin"));
		links.add(new Link(Link.DisplayCondition.AUTHENTICATED, "Manage your information", "/user/index"));
		model.addAttribute("links", links);
		model.addAttribute("status", new Status(Status.Value.WARNING, "This is work in progress, more to come soon!"));
		return "/index";
	}
}

