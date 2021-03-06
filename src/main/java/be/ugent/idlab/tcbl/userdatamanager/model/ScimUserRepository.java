/*
 *  Copyright 2019 imec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package be.ugent.idlab.tcbl.userdatamanager.model;

import gluu.scim.client.ScimResponse;
import gluu.scim2.client.Scim2Client;
import gluu.scim2.client.util.Util;
import org.apache.commons.collections.map.SingletonMap;
import org.gluu.oxtrust.model.scim2.*;
import org.gluu.oxtrust.model.scim2.schema.extension.UserExtensionSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;

import static be.ugent.idlab.tcbl.userdatamanager.model.Util.invitationDay;
import static be.ugent.idlab.tcbl.userdatamanager.model.Util.toCalendarPerDay;
import static org.gluu.oxtrust.model.scim2.Constants.MAX_COUNT;

/**
 * @author Gerald Haesendonck
 */
@Component
public class ScimUserRepository {

	// SCIM query language: see https://tools.ietf.org/html/rfc7644#section-3.4.2.2

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final Scim2Client client;
	private final UserExtensionSchema userExtensionSchema;

	public ScimUserRepository(final Environment environment) throws Exception {
		log.debug("Initialising ScimUserRepository");
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
		log.debug("ScimUserRepository initialised.");
	}


	public void save(final TCBLUser tcblUser) throws Exception {
		log.debug("Saving user {}", tcblUser.getUserName() );
		User user = findUser(tcblUser.getInum());
		user.setPassword("");
		updateScimUser(user, tcblUser);
		client.updateUser(user, tcblUser.getInum(), new String[0]);
	}

	public TCBLUser create(final TCBLUser tcblUser) throws Exception {
		log.debug("Creating user {}", tcblUser.getUserName());
		try {
			User user = new User();
			updateScimUser(user, tcblUser);
			ScimResponse response = client.createUser(user, new String[0]);
			if (response.getStatusCode() == 201) {
				user = Util.toUser(response, userExtensionSchema);
				tcblUser.setInum(user.getId());
				tcblUser.setCreated(user.getMeta().getCreated());
				tcblUser.setLastModified(user.getMeta().getLastModified());
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

	private void updateScimUser(final User scimUser, final TCBLUser tcblUser) {
		if (scimUser.getUserName() == null) {
			scimUser.setUserName(tcblUser.getUserName());
		}
		if (scimUser.getPassword().isEmpty() && tcblUser.getPassword() != null) {
			scimUser.setPassword(tcblUser.getPassword());
		}
		if (scimUser.getEmails().isEmpty()) {
			Email email = new Email();
			email.setValue(scimUser.getUserName());
			scimUser.setEmails(Collections.singletonList(email));
		}
		String displayName = tcblUser.getFirstName() + " " + tcblUser.getLastName();
		Name newName = new Name();
		newName.setGivenName(tcblUser.getFirstName());
		newName.setFamilyName(tcblUser.getLastName());
		newName.setFormatted(displayName);
		scimUser.setName(newName);
		scimUser.setDisplayName(displayName);
		scimUser.setActive(tcblUser.isActive());
		String pictureURL = tcblUser.getPictureURL() == null ? "_" : tcblUser.getPictureURL();
		Extension extension = new Extension.Builder(ScimExtensionAttributes.urn.getValue())
				.setField(ScimExtensionAttributes.subscribedField.getValue(), Boolean.toString(tcblUser.isSubscribedNL()))
				.setField(ScimExtensionAttributes.acceptedField.getValue(), Boolean.toString(tcblUser.isAcceptedPP()))
				.setField(ScimExtensionAttributes.pictureField.getValue(), pictureURL)
				.setField(ScimExtensionAttributes.allowedMonField.getValue(), Boolean.toString(tcblUser.isAllowedMon()))
				.build();
		if (scimUser.isExtensionPresent(ScimExtensionAttributes.urn.getValue())) {
			scimUser.setExtensions(new SingletonMap(ScimExtensionAttributes.urn.getValue(), extension));
		} else {
			scimUser.addExtension(extension);
		}
	}

	public void deleteTCBLUser(final TCBLUser user) throws Exception {
		log.debug("Deleting user {}", user.getUserName());
		try {
			ScimResponse response = client.deletePerson(user.getInum());
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

	public void processTCBLUsers(TCBLUserProcessor processor) throws Exception {
		processScimUsers(scimUser -> {
			log.debug("Processing user {}", scimUser.getUserName());

			// fix passwordReset date
			Meta meta = scimUser.getMeta();
			Date created = meta.getCreated();
			Date modified = meta.getLastModified();
			Calendar createdCalendar = toCalendarPerDay(created);

			boolean invited = false;

			// Very important note about behaviour of the SCIM client (observed in version 3.0.2),
			// concerning User.active (SCIM client class property) vs.
			// gluuPerson.gluuStatus (display name 'User Status' as seen in the Gluu GUI):
			// - Changes to User.active are reflected in gluuPerson.gluuStatus after calling client.updateUser() [as expected],
			// - Changes to gluuPerson.gluuStatus through the Gluu GUI are not reflected in User.active after calling
			//   client.searchUsers() and other client methods that read back users [not as expected].
			// Conclusion:
			//   NEVER modify the 'User Status' in the Gluu GUI!!!!!!!!!!!!!!!!!!
			boolean active = scimUser.isActive() == null ? false : scimUser.isActive();

			Date passwordResetAt = null;
			Date activeSince;

			if (createdCalendar.equals(invitationDay)) {
				invited = true;
				if (!created.equals(modified)) {
					passwordResetAt = modified;
					activeSince = modified;
				} else {
					activeSince = null;
				}
			} else {
				activeSince = created;
			}

			// Default values for the TCBLUser properties that 'can' be available in the SCIM extension attributes
			// Note that there is no need to write back missing SCIM extension attributes to the Gluu server...
			String pictureURL = null;
			boolean subscribedNL = false;
			boolean acceptedPP = false;
			boolean allowedMon = false;
			if (scimUser.isExtensionPresent(ScimExtensionAttributes.urn.getValue())) {
				Extension extension = scimUser.getExtension(ScimExtensionAttributes.urn.getValue());
				pictureURL = getScimExtensionFieldStringWithDefault(extension, ScimExtensionAttributes.pictureField.getValue(), pictureURL);
				if ("_".equals(pictureURL)) {
					pictureURL = null;
				}
				subscribedNL = getScimExtensionFieldBooleanWithDefault(extension, ScimExtensionAttributes.subscribedField.getValue(), subscribedNL);
				acceptedPP = getScimExtensionFieldBooleanWithDefault(extension, ScimExtensionAttributes.acceptedField.getValue(), acceptedPP);
				allowedMon = getScimExtensionFieldBooleanWithDefault(extension, ScimExtensionAttributes.allowedMonField.getValue(), allowedMon);
			}

			TCBLUser tcblUser = new TCBLUser(
					scimUser.getId(),	// inum
					scimUser.getUserName(),
					scimUser.getName().getGivenName(),
					scimUser.getName().getFamilyName(),
					active,
					invited,
					pictureURL,
					subscribedNL,
					acceptedPP,
					allowedMon,
					created,
					modified,
					passwordResetAt,
					activeSince);

			processor.process(tcblUser);

		});
	}

	private static String getScimExtensionFieldStringWithDefault(Extension extension, String field, String defaultValue) {
		return extension.isFieldPresent(field) ? extension.getFieldAsString(field) : defaultValue;
	}

	private static boolean getScimExtensionFieldBooleanWithDefault(Extension extension, String field, boolean defaultValue) {
		String s = getScimExtensionFieldStringWithDefault(extension, field, Boolean.toString(defaultValue));
		return Boolean.parseBoolean(s);
	}

	private static Map<String, String> resolveScimClientProperties(final Environment environment) {
		Binder binder = Binder.get(environment);
		BindResult<Map<String, String>> result = binder.bind(
				"scim", Bindable.mapOf(String.class, String.class));
		return result.get();
	}

	private User findUser(final String inum) throws Exception {
		try {
			ScimResponse response = client.retrieveUser(inum, new String[0]);
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
