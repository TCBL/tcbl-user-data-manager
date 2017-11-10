package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.model.Stats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@RestController
@RequestMapping("/rest")
public class RESTController {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final DateFormat sinceFormat = new SimpleDateFormat("yyyyMMdd");

	@GetMapping("/new_users")
	public List<String> newUsers(@RequestParam(name = "since", defaultValue = "20170831") String since) {
		final ArrayList<String> newUsers = new ArrayList<>();
		try {
			final Date sinceDate = sinceFormat.parse(since);
			Stats stats = Stats.fromLatestFile();
			if (stats != null) {
				stats.getSelfRegisteredUsers().entrySet()
						.stream()
						.filter(entry -> entry.getKey() >= sinceDate.getTime())
						.forEach(entry -> newUsers.addAll(entry.getValue()));
			} else {
				log.warn("Stats not calculated yet.");
				// TODO: return decent error code
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			log.error("Error while getting stats or stats not calculated yet.", e);
			// TODO: return decent error code
		} catch (ParseException e) {
			log.error("Invalid request: cannot parse date {}", since);
			e.printStackTrace();
		}
		return newUsers;
	}

}
