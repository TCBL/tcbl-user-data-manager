package be.ugent.idlab.tcbl.userdatamanager.background;

import be.ugent.idlab.tcbl.userdatamanager.model.MailChimpLoader;
import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUserRepository;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
	private final Gson gson = new Gson();


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

				String baseUrl = "https://" + dcPart + ".api.mailchimp.com/" + loader.getApiVersion();

				HttpResponse<JsonNode> response = Unirest.get(baseUrl + "/lists/" + listId + "/members")
						.queryString("fields", "members.email_address,members.status")
						.basicAuth("anystring", key)
						.asJson();
				if (response.getStatus() != HttpStatus.OK.value()) {
					log.error("Could not retrieve list of members from MailChimp! Response: {}", response.getStatusText());
					return;
				}
				String responseStr = response.getBody().getObject().toString();
				MailChimpMembers members = gson.fromJson(responseStr, MailChimpMembers.class);

				// now compare the two sets of users and categorise
				Set<MailChimpMember> updateMembers = new HashSet<>();
				Set<MailChimpMember> deleteMembers = new HashSet<>();
				for (MailChimpMember mailChimpMember : members.getMembers()) {
					String userName = mailChimpMember.getEmail_address();
					switch (mailChimpMember.getStatus()) {
						case "cleaned":
						case "unsubscribed":
							if (subscribedUsers.contains(userName)) {
								mailChimpMember.setStatus("subscribed");
								updateMembers.add(mailChimpMember);
								subscribedUsers.remove(userName);
							} else if (!unsubscribedUsers.contains(userName)) {
								deleteMembers.add(mailChimpMember);
							}
							break;
						case "subscribed" :
							if (unsubscribedUsers.contains(userName)) {
								mailChimpMember.setStatus("unsubscribed");
								updateMembers.add(mailChimpMember);
							} else if (subscribedUsers.contains(userName)) {
								subscribedUsers.remove(userName);
							} else {
								mailChimpMember.setStatus("unsubscribed");
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
				if (!updateMembers.isEmpty()) {
					MailChimpMembers membersToUpdate = new MailChimpMembers(new ArrayList<>(updateMembers), true);
					HttpResponse<JsonNode> updateResponse = Unirest.post(baseUrl + "/lists/" + listId)
							.basicAuth("anystring", key)
							.body(new JsonNode(gson.toJson(membersToUpdate)))
							.asJson();
					if (updateResponse.getStatus() != HttpStatus.OK.value()) {
						log.error("Could not update list of members from MailChimp! Response: {}", response.getStatusText());
						return;
					}
				}
				if (!newMembers.isEmpty()) {
					MailChimpMembers membersToAdd = new MailChimpMembers(new ArrayList<>(newMembers), false);
					HttpResponse<JsonNode> newResponse = Unirest.post(baseUrl + "/lists/" + listId)
							.basicAuth("anystring", key)
							.body(new JsonNode(gson.toJson(membersToAdd)))
							.asJson();
					if (newResponse.getStatus() != HttpStatus.OK.value()) {
						log.error("Could not add members to list in MailChimp! Response: {}", response.getStatusText());
					}
				}
			} catch (Exception e) {
				log.error("Something went wrong updating MailChimp subscriptions", e);
			}
		}
	}


}
