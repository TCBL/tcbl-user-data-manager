package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.model.Stats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Controller
@RequestMapping("stats")
public class StatController {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@GetMapping("/users")
	public String activeUsers(Model model) {
		try {
			Stats stats = Stats.fromLatestFile();
			if (stats != null) {
				model.addAttribute("stats", stats);
				model.addAttribute("dates", stats.getLabels());
				model.addAttribute("active", stats.getActiveValues());
				model.addAttribute("totalActive", stats.getTotalActiveValues());
			} else {
				log.error("Stats not calculated yet.");
				model.addAttribute("stats", null);
			}
		} catch (IOException e) {
			log.error("Error while getting stats or stats not calculated yet.", e);
			model.addAttribute("stats", null);
		}
		return "/stats/users";
	}

}
