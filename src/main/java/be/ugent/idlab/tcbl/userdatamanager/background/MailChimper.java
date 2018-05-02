package be.ugent.idlab.tcbl.userdatamanager.background;

import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUser;
import be.ugent.idlab.tcbl.userdatamanager.model.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.FileReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

/**
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Component
public class MailChimper {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final String filename;
	private final UserRepository userRepository;
	private final RestTemplate restTemplate;

	private String listId;
	private String baseUrl;
	private String authorizationHeaderValue;

	public MailChimper(Environment environment, UserRepository userRepository) {
		this.filename = environment.getProperty("tudm.mailchimp.filename");
		this.userRepository = userRepository;
		this.restTemplate = new RestTemplate();
	}

	private boolean load() {
		try (Reader in = new FileReader(filename)) {
			Properties properties = new Properties();
			properties.load(in);
			String key = properties.getProperty("key");
			listId = properties.getProperty("list");
			String apiVersion = properties.getProperty("api");
			String dcPart = key.substring(key.lastIndexOf('-') + 1);
			baseUrl = "https://" + dcPart + ".api.mailchimp.com/" + apiVersion;

			// see http://www.baeldung.com/how-to-use-resttemplate-with-basic-authentication-in-spring
			String auth = "anystring:" + key;
			byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(Charset.forName("UTF-8")));
			authorizationHeaderValue = "Basic " + new String(encodedAuth);

			log.info("MailChimpLoader's properties (re)loaded");
			return true;
		} catch (Exception e) {
			log.warn("MailChimpLoader's properties not loaded. Doing nothing ", e);
			return false;
		}
	}

	@Async
	public void addOrUpdate(final TCBLUser user) {
		if (load()) {
			String url = baseUrl + "/lists/" + listId + "/members/" + getSubscriberHash(user.getUserName());
			MailChimperUpdateUserData updateUserData = new MailChimperUpdateUserData(user);
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", authorizationHeaderValue);
			HttpEntity<MailChimperUpdateUserData> request = new HttpEntity<>(updateUserData, headers);
			put(url, request);
		}
	}

	private void put(final String url, final HttpEntity<?> request) {
		put(url, request, 42);
	}

	private void put(final String url, final HttpEntity<?> request, int nrRetry) {
		try {
			ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
			HttpStatus status = response.getStatusCode();
			if (status.value() != HttpStatus.OK.value()) {
				log.error("Error sending request to MailChimp API: {}. Trying again in 30 minutes {}", status.getReasonPhrase());
			} else {
				return;
			}
		} catch (Exception e) {
			log.error("Error sending request to MailChimp API. Trying again in 30 minutes", e);
		}
		try {
			Thread.sleep(30 * 60 * 1000);
			if (nrRetry > 0) {
				log.warn("Retrying update user request to MailChimp. {} attempts left.", nrRetry - 1);
				put(url, request, nrRetry - 1);
			}
		} catch (InterruptedException e) {
			log.error("Error sending request to MailChimp, giving up! {}", e);
		}
	}

	private String getSubscriberHash(final String emailAddress) {
		return org.springframework.util.DigestUtils.md5DigestAsHex(emailAddress.toLowerCase().getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Sets the field subscribedNL of all users that are unsubscribed to the newsletter in MailChimp to false
	 * and update the Gluu server accordingly.
	 */
	// @Scheduled(cron = "0 * * * * *") // every minute, good to test
	@Scheduled(cron = "0 0 3 * * *")	// every day at 3
	public void synchronise() {
		log.debug("Synchronising MailChimp list subscriptions");

		if (load()) {
			// get unsubscribed users, and update these in the gluu server
			int offset = 0;
			int totalCount = 100000000;
			try {
				while (totalCount > 0) {
					URI url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/lists/" + listId + "/members")
							.queryParam("fields", "members.email_address,total_items")
							.queryParam("status", "unsubscribed")
							.queryParam("offset", offset)
							.queryParam("count", 100)
							.build().toUri();
					HttpHeaders headers = new HttpHeaders();
					headers.add("Authorization", authorizationHeaderValue);
					HttpEntity<Object> request = new HttpEntity<>(headers);
					ResponseEntity<MailChimperReturnCase1> response = restTemplate.exchange(url,  HttpMethod.GET, request, MailChimperReturnCase1.class);
					HttpStatus status = response.getStatusCode();
					if (status.value() != HttpStatus.OK.value()) {
						log.error("Could not retrieve list of members from MailChimp! Response: {}", status.getReasonPhrase());
						return;
					}
					MailChimperReturnCase1 members = response.getBody();
					if (totalCount == 100000000) {
						totalCount = members.getTotalItems();
					}
					totalCount -= 100;
					offset += 100;
					for (MailChimperMember mailChimperMember : members.getMembers()) {
						String userName = mailChimperMember.getEmailAddress();
						try {
							TCBLUser user = userRepository.findByName(userName);
							if (user.isSubscribedNL()) {
								user.setSubscribedNL(false);
								userRepository.save(user);
							}
						} catch (Exception e) {
							// normal if user is not found
						}
					}

				}
			} catch (Exception e) {
				log.error("Could not retrieve list of members from MailChimp!", e);
			}
		}
	}

}
