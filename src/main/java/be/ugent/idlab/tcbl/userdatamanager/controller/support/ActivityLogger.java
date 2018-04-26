package be.ugent.idlab.tcbl.userdatamanager.controller.support;

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

	public ActivityLogger(Environment environment) {
		String endpoint = null;
		String jwtKey = null;
		boolean enabled = false;
		try {
			endpoint = environment.getRequiredProperty("tudm.activity-logging.endpoint");
			jwtKey = environment.getRequiredProperty("tudm.activity-logging.jwtkey");
			log.info(String.format("Activity logging is enabled; endpoint: '%s', jwtKey: '%s'", endpoint, jwtKey));
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
		send(new ActivityLoggingDataToSend(userName, logType.getValue()));
	}

	private void send(ActivityLoggingDataToSend record) {
		if (enabled) {
			log.debug(String.format("Activity logging - sending %s.", record.toString()));
			try {
				HttpHeaders headers = new HttpHeaders();
				headers.add("authorization", "Bearer " + jwtKey);
				headers.setContentType(MediaType.APPLICATION_JSON);
				HttpEntity<ActivityLoggingDataToSend> request = new HttpEntity<>(record, headers);

				ResponseEntity<ActivityLoggingDataReturned> response = restTemplate.postForEntity(endpoint, request, ActivityLoggingDataReturned.class);
				HttpStatus status = response.getStatusCode();
				ActivityLoggingDataReturned result = response.getBody();
				log.debug(String.format("Activity logging - sent %s: status %d (%s); uuid '%s'.", record.toString(), status.value(), status.getReasonPhrase(), result.getUuid()));
			} catch (Exception e) {
				log.error(String.format("Activity logging - failed sending %s: %s.", record.toString(), e.getMessage()));
			}
		} else {
			log.debug(String.format("Activity logging - not sending %s.", record.toString()));
		}
	}
}
