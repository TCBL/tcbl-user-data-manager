package be.ugent.idlab.tcbl.userdatamanager.background;

import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUser;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.RequestBodyEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Component
public class MailChimper {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${mailchimp.filename}")
	private String filename;

	private String key;
	private String listId;
	private String baseUrl;

	private final Gson gson = new Gson();

	private void load() {
		try (Reader in = new FileReader(filename)) {
			Properties properties = new Properties();
			properties.load(in);
			key = properties.getProperty("key");
			listId = properties.getProperty("list");
			String apiVersion = properties.getProperty("api");
			String dcPart = key.substring(key.lastIndexOf('-') + 1);
			baseUrl = "https://" + dcPart + ".api.mailchimp.com/" + apiVersion;
			log.info("MailChimpLoader's properties (re)loaded");
		} catch (Exception e) {
			log.warn("MailChimpLoader's properties not loaded: ", e);
			key = null;
		}
	}

	@Async
	public void addOrUpdate(final TCBLUser user) {
		load();
		if (key != null) {
			String subscriberHash = getSubscriberHash(user.getUserName());
			String body = new UpdateUserData(user).toJSON();
			RequestBodyEntity request = Unirest.put(baseUrl + "/lists/" + listId + "/members/" + subscriberHash)
					.basicAuth("anystring", key)
					.body(body);
			send(request);
		}
	}

	private void send(final RequestBodyEntity request) {
		send(request, 10);
	}

	private void send(final RequestBodyEntity request, int nrRetry) {
		try {
			HttpResponse<JsonNode> response = request.asJson();
			if (response.getStatus() != 200) {
				log.error("Error sending request to MailChimp API: {}. Trying again in 10 minutes {}", response.getStatusText());
			} else return;
		} catch (UnirestException e) {
			log.error("Error sending request to MailChimp API. Trying again in 10 minutes", e);
		}
		try {
			Thread.sleep(10 * 60 * 1000);
			if (nrRetry > 0) {
				log.warn("Retrying update user request to MailChimp. {} attempts left.", nrRetry - 1);
				send(request, nrRetry - 1);
			}
		} catch (InterruptedException e) {
			log.error("Error sending request to MailChimp, giving up! {}", e);
		}
	}

	private String getSubscriberHash(final String emailAddress) {
		return org.springframework.util.DigestUtils.md5DigestAsHex(emailAddress.toLowerCase().getBytes(StandardCharsets.UTF_8));
	}

	private class UpdateUserData {
		private final String email_address;
		private final String status_if_new;
		private final String status;
		private final MergeFields merge_fields;

		public UpdateUserData(TCBLUser user) {
			this.email_address = user.getUserName();
			this.status_if_new = user.isSubscribedNL() ? "subscribed" : "unsubscribed";
			this.status = status_if_new;
			this.merge_fields = new MergeFields(user.getFirstName(), user.getLastName());
		}

		public String toJSON() {
			return gson.toJson(this);
		}

		private class MergeFields {
			private final String FNAME;
			private final String LNAME;

			public MergeFields(String FNAME, String LNAME) {
				this.FNAME = FNAME;
				this.LNAME = LNAME;
			}
		}
	}

}
