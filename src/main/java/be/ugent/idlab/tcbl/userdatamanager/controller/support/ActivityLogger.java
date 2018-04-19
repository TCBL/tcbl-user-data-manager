package be.ugent.idlab.tcbl.userdatamanager.controller.support;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.RequestBodyEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
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

	public void log(String userName, ActivityLoggingType logType) {
		String body = gson.toJson(new ActivityLoggingRecord(userName, logType.getValue()));
		if (enabled) {
			log.debug("Activity logging - sending: " + body);
			RequestBodyEntity request = Unirest.post(endpoint)
					.body(body);
			send(request);
		} else {
			log.debug("Activity logging - not sending: " + body);
		}
	}

	private void send(final RequestBodyEntity request) {
		try {
			HttpResponse<JsonNode> response = request.asJson();
			if (response.getStatus() != 200) {
				log.error("Error sending activity logging record: {}.", response.getStatusText());
			}
		} catch (UnirestException e) {
			log.error("Error sending activity logging record: ", e);
		}
	}


	private class ActivityLoggingRecord {
		private String userName;
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
