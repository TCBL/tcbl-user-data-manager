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

package be.ugent.idlab.tcbl.userdatamanager.controller;

import be.ugent.idlab.tcbl.userdatamanager.model.Stats;
import be.ugent.idlab.tcbl.userdatamanager.model.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Gerald Haesendonck
 */
@RestController
@RequestMapping("/rest")
public class RESTController {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final DateFormat sinceFormat = new SimpleDateFormat("yyyyMMdd");
	private final UserRepository userRepository;

	public RESTController(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@GetMapping("/new_users")
	public ResponseEntity<?> newUsers(@RequestParam(name = "since", defaultValue = "20170831") String since) {
		final ArrayList<String> newUsers = new ArrayList<>();
		try {
			final Date sinceDate = sinceFormat.parse(since);
			final Stats stats = new Stats();
			userRepository.processTCBLUsers(stats::add);
			stats.getSelfRegisteredUsers().entrySet()
					.stream()
					.filter(entry -> entry.getKey() >= sinceDate.getTime())
					.forEach(entry -> newUsers.addAll(entry.getValue()));
		} catch (ParseException e) {
			log.error("Invalid request: cannot parse date {}", since, e);
			return new ResponseEntity<>("Invalid request: cannot parse date '" + since + "'\n", HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.ok(newUsers);
	}

}
