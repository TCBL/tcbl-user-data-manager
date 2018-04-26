package be.ugent.idlab.tcbl.userdatamanager.controller.support;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * <p>Activity logging support.</p>
 * <p>
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
@Component
public class ActivityLogger {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final String endpoint;
	private final String jwtKey;
	private final boolean enabled;
	private final RestTemplate restTemplate;
	private final Gson gson = new Gson();

	public ActivityLogger(Environment environment) {
		String endpoint = null;
		String jwtKey = null;
		boolean enabled = false;
		try {
			endpoint = environment.getRequiredProperty("tudm.activity-logging.endpoint");
			jwtKey = environment.getRequiredProperty("tudm.activity-logging.jwtkey");
			log.info(String.format("Activity logging is enabled; endpoint: %s, jwtKey: %s", endpoint, jwtKey));
			enabled = true;
		} catch (Exception e) {
			log.info("Activity logging is not enabled.");
		}
		this.endpoint = endpoint;
		this.jwtKey = jwtKey;
		this.enabled = enabled;
		this.restTemplate = new RestTemplate();
	}

	@Async
	public void log(String userName, ActivityLoggingType logType) {
		String body = gson.toJson(new ActivityLoggingRecord(userName, logType.getValue()));
		send(body);
	}

	private void send(String body) {
		if (enabled) {
			log.debug("Activity logging - start sending: " + body);
			try {
				HttpHeaders headers = new HttpHeaders();
				headers.add("authorization", "Bearer " + jwtKey);
				headers.setContentType(MediaType.APPLICATION_JSON);
				HttpEntity<String> request = new HttpEntity<>(body, headers);

				ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);
				HttpStatus status = response.getStatusCode();
				log.debug(String.format("Activity logging - sending ended with: %d, %s.", status.value(), status.getReasonPhrase()));
			} catch (Exception e) {
				log.error("Activity logging - sending failed: " + e.getMessage());
			}
		} else {
			log.debug("Activity logging - not sending: " + body);
		}
	}


	private class ActivityLoggingRecord {
		@SerializedName("userID")
		private String userName;
		@SerializedName("type")
		private String logType;

		public ActivityLoggingRecord() {
		}

		public String getUserName() {
			return userName;
		}

		public ActivityLoggingRecord(String userName, String logType) {
			this.userName = userName;
			this.logType = logType;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public String getLogType() {
			return logType;
		}

		public void setLogType(String logType) {
			this.logType = logType;
		}
	}

}
