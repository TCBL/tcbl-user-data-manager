package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.model.Stats;
import be.ugent.idlab.tcbl.userdatamanager.model.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Gerald Haesendonck
 */
@Controller
@RequestMapping("stats")
public class StatController {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final UserRepository userRepository;

	public StatController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@GetMapping("/users")
	public String activeUsers(Model model) {
		final Stats stats = new Stats();
		log.debug("Calculating statistics");
		userRepository.processTCBLUsers(stats::add);
		log.debug("calculating statistics done.");
		model.addAttribute("totalCount", stats.totalCount);
		model.addAttribute("invited", stats.invited);
		model.addAttribute("invitedActive", stats.invitedActive);
		model.addAttribute("newUsers", stats.newUsers);
		model.addAttribute("dates", stats.getLabels());
		model.addAttribute("active", stats.getActiveValues());
		model.addAttribute("totalActive", stats.getTotalActiveValues());
		model.addAttribute("testUsers", stats.testUsers);
		return "stats/users";
	}
}
