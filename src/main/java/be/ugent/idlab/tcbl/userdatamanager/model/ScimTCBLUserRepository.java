package be.ugent.idlab.tcbl.userdatamanager.model;

import gluu.scim.client.ScimResponse;
import gluu.scim2.client.Scim2Client;
import gluu.scim2.client.util.Util;
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

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class ScimTCBLUserRepository implements TCBLUserRepository {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final Scim2Client client;
	private final UserExtensionSchema userExtensionSchema;

	public ScimTCBLUserRepository(final Environment environment) throws Exception {
		log.debug("Initialising ScimTCBLUserRepository");
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
		log.debug("ScimTCBLUserRepository initialised.");
	}

	@Override
	public Iterable<TCBLUser> findAll() throws Exception {
		try {
			ScimResponse response = client.retrieveAllUsers();
			if (response.getStatusCode() == 200) {
				ListResponse userList = Util.toListResponseUser(response, userExtensionSchema);
				List<TCBLUser> users = new ArrayList<>();
				for (Resource userResource : userList.getResources()) {
					User user = (User) userResource;
					TCBLUser tcblUser = TCBLUser.createFromScimUser(user);
					users.add(tcblUser);
				}
				return users;
			} else {
				String message = "Cannot request user list to OpenID Connect server: " + response.getStatusCode() + ": " + response.getStatus() + ". " + response.getResponseBodyString();
				log.error(message);
				throw new Exception(message);
			}
		} catch (Exception e) {
			String message = "Cannot request user list: " + e.getMessage();
			log.error(message, e);
			throw new Exception(message, e);
		}
	}

	@Override
	public TCBLUser save(TCBLUser tcblUser) throws Exception {
		User user = findUser(tcblUser.getId());
		user.setPassword("");
		tcblUser.updateScimUser(user);
		client.updateUser(user, tcblUser.getId(), new String[0]);
		return tcblUser;
	}

	@Override
	public TCBLUser find(String id) throws Exception {
		User user = findUser(id);
		return TCBLUser.createFromScimUser(user);
	}

	@Override
	public void deleteTCBLUser(String userName) {
		// TODO or not TODO
	}

	private static Map<String, String> resolveScimClientProperties(final Environment environment) {
		Binder binder = Binder.get(environment);
		BindResult<Map<String, String>> result = binder.bind(
				"security.scim", Bindable.mapOf(String.class, String.class));
		return result.get();
	}

	private User findUser(final String id) throws Exception {
		try {
			ScimResponse response = client.retrieveUser(id, new String[0]);
			if (response.getStatusCode() == 200) {
				return Util.toUser(response,userExtensionSchema);
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
