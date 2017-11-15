package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.model.Stats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	public ResponseEntity<?> newUsers(@RequestParam(name = "since", defaultValue = "20170831") String since) {
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
				return new ResponseEntity<>("Stats not calculated yet. Try again later\n", HttpStatus.NOT_FOUND);
			}
		} catch (FileNotFoundException e) {
			log.error("Error while getting stats or stats not calculated yet.", e);
			return new ResponseEntity<>("Stats not calculated yet. Try again later\n", HttpStatus.NOT_FOUND);
		} catch (ParseException e) {
			log.error("Invalid request: cannot parse date {}", since, e);
			return new ResponseEntity<>("Invalid request: cannot parse date '" + since + "'\n", HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.ok(newUsers);
	}

}
