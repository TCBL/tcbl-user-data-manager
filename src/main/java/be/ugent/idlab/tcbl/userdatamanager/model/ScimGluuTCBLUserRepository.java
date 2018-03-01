package be.ugent.idlab.tcbl.userdatamanager.model;

import gluu.scim.client.ScimResponse;
import gluu.scim2.client.Scim2Client;
import gluu.scim2.client.util.Util;
import org.gluu.oxtrust.model.scim2.Constants;
import org.gluu.oxtrust.model.scim2.ListResponse;
import org.gluu.oxtrust.model.scim2.Resource;
import org.gluu.oxtrust.model.scim2.User;
import org.gluu.oxtrust.model.scim2.schema.extension.UserExtensionSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.gluu.oxtrust.model.scim2.Constants.MAX_COUNT;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class ScimGluuTCBLUserRepository implements GluuTCBLUserRepository {

	// SCIM query language: see https://tools.ietf.org/html/rfc7644#section-3.4.2.2

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final Scim2Client client;
	private final UserExtensionSchema userExtensionSchema;

	public ScimGluuTCBLUserRepository(final Environment environment) throws Exception {
		log.debug("Initialising ScimGluuTCBLUserRepository");
		Map<String, String> clientProperties = resolveScimClientProperties(environment);
		String domain = clientProperties.get("domain");
		String metaDataUrl = clientProperties.get("meta-data-url");
		String clientId = clientProperties.get("aat-client-id");
		String jksPath = clientProperties.get("aat-client-jks-path");
		String jksPassword = clientProperties.get("aat-client-jks-password");
		String keyId = clientProperties.get("aat-client-key-id");
		client = Scim2Client.umaInstance(domain, metaDataUrl, clientId, jksPath, jksPassword, keyId);
		try {
			userExtensionSchema = client.getUserExtensionSchema();
		} catch (Exception e) {
			String message = "Cannot get the user extension schema from the OpenID Connect server.";
			log.error(message, e);
			throw new Exception(message, e);
		}
		log.debug("ScimGluuTCBLUserRepository initialised.");
	}

	@Override
	public Iterable<TCBLUser> findAll() throws Exception {
		log.debug("Find all users.");
		List<TCBLUser> users = new ArrayList<>();
		processTCBLUsers(users::add);
		return users;
	}

	@Override
	public TCBLUser save(final TCBLUser tcblUser) throws Exception {
		log.debug("Saving user {}", tcblUser.getUserName() );
		User user = findUser(tcblUser.getId());
		user.setPassword("");
		tcblUser.updateScimUser(user, userExtensionSchema.getId());
		client.updateUser(user, tcblUser.getId(), new String[0]);
		return tcblUser;
	}

	@Override
	public TCBLUser find(final String id) throws Exception {
		log.debug("Finding user with id {}", id);
		User user = findUser(id);
		return TCBLUser.createFromScimUser(user, userExtensionSchema.getId());
	}

	@Override
	public TCBLUser findByName(final String userName) throws Exception {
		log.debug("Finding user by userName {}", userName);
		String usernameQuery = "userName eq \"" + userName + "\"";
		ScimResponse existsResponse = client.searchUsers(usernameQuery, 1, 1, "", "", null);
		ListResponse userList = Util.toListResponseUser(existsResponse, userExtensionSchema);
		if (userList.getTotalResults() == 1) {
			User user = (User) userList.getResources().get(0);
			return TCBLUser.createFromScimUser(user, userExtensionSchema.getId());
		} else {
			String message = "Did not find user " + userName;
			log.error(message);
			throw new Exception(message);
		}
	}

	@Override
	public TCBLUser create(final TCBLUser tcblUser) throws Exception {
		log.debug("Creating user {}", tcblUser.getUserName());
		try {
			User user = new User();
			tcblUser.updateScimUser(user, userExtensionSchema.getId());
			ScimResponse response = client.createUser(user, new String[0]);
			if (response.getStatusCode() == 201) {
				user = Util.toUser(response, userExtensionSchema);
				tcblUser.setId(user.getId());
				return tcblUser;
			} else if (response.getStatusCode() == 409) {
				String message = "user with username " + tcblUser.getUserName() + " already exists.";
				throw new Exception(message);
			} else {
				String message = "the OpenID Connect server returned: " + response.getStatusCode() + ": " + response.getStatus() + ". " + response.getResponseBodyString();
				throw new Exception(message);
			}
		} catch (Exception e) {
			String message = "Cannot create user: " + e.getMessage();
			log.error(message, e);
			throw new Exception(message, e);
		}
	}

	@Override
	public void deleteTCBLUser(final TCBLUser user) throws Exception {
		log.debug("Deleting user {}", user.getUserName());
		try {
			ScimResponse response = client.deletePerson(user.getId());
			if (response.getStatusCode() != 204) {
				String message = "Cannot delete user on OpenID Connect server: " + response.getStatusCode() + ": " + response.getStatus() + ". " + response.getResponseBodyString();
				log.error(message);
				throw new Exception(message);
			}
		} catch (Exception e) {
				String message = "Cannot create user: " + e.getMessage();
				log.error(message, e);
				throw new Exception(message, e);
		}
	}

	@Override
	public Iterable<TCBLUser> findInactive() throws Exception {
		log.debug("Finding inactive users.");
		String query = "active eq \"false\"";
		try {
			boolean ready = false;
			int startIndex = 1;
			List<TCBLUser> users = new ArrayList<>();
			while (!ready) {
				ScimResponse inactiveUsersResponse = client.searchUsers(query, startIndex, Constants.MAX_COUNT, "", "", null);
				if (inactiveUsersResponse.getStatusCode() == 200) {
					ListResponse userListResponse = Util.toListResponseUser(inactiveUsersResponse, userExtensionSchema);
					List<Resource> userList = userListResponse.getResources();
					if (!userList.isEmpty()) {
						for (Resource userResource : userListResponse.getResources()) {
							User user = (User) userResource;
							TCBLUser tcblUser = TCBLUser.createFromScimUser(user, userExtensionSchema.getId());
							users.add(tcblUser);
						}
						startIndex += Constants.MAX_COUNT;
					} else {
						ready = true;
					}
				} else {
					String message = "Cannot search for inactive users on the OpenID Connect server: " + inactiveUsersResponse.getStatusCode() + ": " + inactiveUsersResponse.getStatus() + ". " + inactiveUsersResponse.getResponseBodyString();
					log.error(message);
					throw new Exception(message);
				}
			}
			return users;
		} catch (Exception e) {
			String message = "Cannot search for inactive users: " + e.getMessage();
			log.error(message, e);
			throw new Exception(message, e);
		}
	}

	@Override
	public void processScimUsers(ScimUserProcessor processor) throws Exception {
		// Iterating all users does not work with Gluu Scim, because of pagination does not work.
		// To work around this, we request users by each letter of the alphabet and some other characters, as to limit
		// the risk of getting over the maximum of 200 users per request. This is dirty, but what else is there to do...
		String abc = "abcdefghijklmnopqrstuvwxyz0123456789_-+*";
		for (char c : abc.toCharArray()) {
			ScimResponse listResponse = client.searchUsers("userName sw \"" + c + "\"", 1, MAX_COUNT, "", "", new String[]{});
			ListResponse userListResponse = Util.toListResponseUser(listResponse, client.getUserExtensionSchema());
			List<Resource> userList = userListResponse.getResources();
			userList.stream()
					.map(user -> (User)user)
					.filter(user -> !user.getUserName().equals("admin"))
					.forEach(processor::process);
		}
	}

	@Override
	public void processTCBLUsers(TCBLUserProcessor processor) throws Exception {
		processScimUsers(scimUser -> processor.process(TCBLUser.createFromScimUser(scimUser, userExtensionSchema.getId())));
	}

	private static Map<String, String> resolveScimClientProperties(final Environment environment) {
		Binder binder = Binder.get(environment);
		BindResult<Map<String, String>> result = binder.bind(
				"scim", Bindable.mapOf(String.class, String.class));
		return result.get();
	}

	private User findUser(final String id) throws Exception {
		try {
			ScimResponse response = client.retrieveUser(id, new String[0]);
			if (response.getStatusCode() == 200) {
				return Util.toUser(response, userExtensionSchema);
			} else {
				String message = "Cannot request user info to OpenID Connect server: " + response.getStatusCode() + ": " + response.getStatus() + ". " + response.getResponseBodyString();
				log.error(message);
				throw new Exception(message);
			}
		} catch (Exception e) {
			String message = "Cannot request user info: " + e.getMessage();
			log.error(message, e);
			throw new Exception(message, e);
		}
	}
}
