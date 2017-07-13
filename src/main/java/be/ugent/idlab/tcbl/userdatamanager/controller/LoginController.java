package be.ugent.idlab.tcbl.userdatamanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Controller
public class LoginController {

	@RequestMapping("/oiclogin")
	public String login() {
		return "redirect:/oauth2/authorization/code/tcbl-manager";
	}
}
