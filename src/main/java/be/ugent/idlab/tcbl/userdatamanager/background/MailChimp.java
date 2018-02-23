package be.ugent.idlab.tcbl.userdatamanager.background;

import be.ugent.idlab.tcbl.userdatamanager.model.MailChimpLoader;
import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Component
public class MailChimp {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final MailChimpLoader loader;
	private final TCBLUserRepository userRepository;


	public MailChimp(MailChimpLoader loader, TCBLUserRepository userRepository) {
		this.loader = loader;
		this.userRepository = userRepository;
	}

	@Scheduled(cron = "0 * * * * *")
	public void listMembers() {
		log.debug("Listing MailChimp list subscriptions");
		String key = loader.getKey();
		String listId = loader.getListId();
		if (key == null || listId == null) {
			log.warn("No valid MailChimp configuration detected. Doing nothing.");
		} else {

			try {
				log.debug("Initialising MailChimp client");
				String dcPart = key.substring(key.lastIndexOf('-') + 1);
				String auth = "anystring:" + key;
				String b64Key = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.US_ASCII));

				WebClient webClient = WebClient.builder()
						.baseUrl("https://" + dcPart + ".api.mailchimp.com/" + loader.getApiVersion())
						.defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + b64Key)
						.build();

				// first get list of TCBL users
				log.debug("Retrieving users from the Gluu server");
				Set<String> subscribedUsers = new HashSet<>();
				Set<String> unsubscribedUsers = new HashSet<>();
				userRepository.processTCBLUsers(user -> {
					if (user.isSubscribedNL()) {
						subscribedUsers.add(user.getUserName());
					} else {
						unsubscribedUsers.add(user.getUserName());
					}
				});

				// then get MailChimp users
				log.debug("Retrieving users from MailChimp list {}", listId);
				MailChimpMembers members = webClient
						.get()
						//.uri("/lists/" + listId + "/members?fields=members.id,members.email_address,members.status")
						.uri("/lists/" + listId + "/members?fields=members.email_address,members.status")
						.accept(MediaType.APPLICATION_JSON)
						.retrieve()
						.bodyToMono(MailChimpMembers.class)
						.block();

				// now compare the two sets of users and categorise
				Set<MailChimpMember> updateMembers = new HashSet<>();
				Set<MailChimpMember> deleteMembers = new HashSet<>();
				for (MailChimpMember mailChimpMember : members.getMembers()) {
					String userName = mailChimpMember.getEmail_address();
					switch (mailChimpMember.getStatus()) {
						case "cleaned":
						case "unsubscribed":
							if (subscribedUsers.contains(userName)) {
								updateMembers.add(mailChimpMember);
								subscribedUsers.remove(userName);
							} else if (!unsubscribedUsers.contains(userName)) {
								deleteMembers.add(mailChimpMember);
							}
							break;
						case "subscribed" :
							if (unsubscribedUsers.contains(userName)) {
								updateMembers.add(mailChimpMember);
							} else if (subscribedUsers.contains(userName)) {
								subscribedUsers.remove(userName);
							} else {
								deleteMembers.add(mailChimpMember);
							}
							break;
						default:	// "pending" state
							break;
					}
				}

				// for now, treat deleted the same as unsubscribed...
				updateMembers.addAll(deleteMembers);

				// now add rest of subscribed to newMembers
				Set<MailChimpMember> newMembers = new HashSet<>();
				for (String subscribedUser : subscribedUsers) {
					newMembers.add(new MailChimpMember(subscribedUser, "subscribed"));
				}

				// perform the update / delete / new members requests!
				/*if (!updateMembers.isEmpty()) {
					webClient.post()
							.uri("/lists/" + listId )
							.contentType(MediaType.APPLICATION_JSON)
							.body()
							
				}*/


			} catch (Exception e) {
				log.error("Something went wrong updating MailChimp subscriptions", e);
			}
		}
	}


}
