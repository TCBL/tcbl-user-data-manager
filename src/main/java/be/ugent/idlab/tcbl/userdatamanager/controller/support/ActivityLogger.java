package be.ugent.idlab.tcbl.userdatamanager.controller.support;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

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

	private String endpoint;
	private String jwtKey;
	private final boolean enabled;

	private final Gson gson = new Gson();

	public ActivityLogger(Environment environment) {
		boolean enabled = false;
		try {
			this.endpoint = environment.getRequiredProperty("tudm.activity-logging.endpoint");
			this.jwtKey = environment.getRequiredProperty("tudm.activity-logging.jwtkey");
			log.info("Activity logging is enabled; endpoint: %s, jwtKey: %s".format(endpoint, jwtKey));
			enabled = true;
		} catch (Exception e) {
			log.info("Activity logging is not enabled.");
		}
		this.enabled = enabled;
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
				HttpResponse<JsonNode> response = Unirest.post(endpoint)
						.header("authorization", "Bearer " + jwtKey)
						.header("content-type", "application/json")
						.body(body)
						.asJson();
				int statusCode = response.getStatus();
				String statusText = response.getStatusText();
				if (statusCode == HttpStatus.OK.value()) {
					log.debug("Activity logging - sending done.");
				} else {
					log.error("Activity logging - sending ended with: %d, %s.", statusCode, statusText);
				}
			} catch (UnirestException e) {
				log.error("Activity logging - sending failed.", e);
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
