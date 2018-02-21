package be.ugent.idlab.tcbl.userdatamanager.background;

import be.ugent.idlab.tcbl.userdatamanager.model.MailChimpLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Component
public class MailChimp {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private MailChimpLoader loader;


	public MailChimp(MailChimpLoader loader) {
		this.loader = loader;
	}

	@Scheduled(cron = "0 * * * * *")
	public void listMembers() {
		log.debug("Listing MailChimp list subscriptions");
		String key = loader.getKey();
		String listId = loader.getListId();
		if (key == null || listId == null) {
			log.warn("No valid MailChimp configuration detected. Doing nothing.");
		} else {
			log.debug("Listing users of list {}", listId);
			String dcPart = key.substring(key.lastIndexOf('-') + 1);
			String auth = "anystring:" + key;
			String b64Key = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.US_ASCII));
			String response = WebClient.builder()
					.baseUrl("https://" + dcPart + ".api.mailchimp.com/3.0")
					.defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + b64Key)
					.build()
					.get()
					.uri("/lists/" + listId + "/members?fields=members.id,members.email_address,members.status")
					.retrieve()
					.bodyToMono(String.class)
					.block();
			System.out.println(response);
		}
	}


}
