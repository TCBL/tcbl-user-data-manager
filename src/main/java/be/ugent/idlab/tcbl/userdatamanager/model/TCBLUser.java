package be.ugent.idlab.tcbl.userdatamanager.model;

import org.apache.commons.collections.map.SingletonMap;
import org.gluu.oxtrust.model.scim2.Email;
import org.gluu.oxtrust.model.scim2.Extension;
import org.gluu.oxtrust.model.scim2.Name;
import org.gluu.oxtrust.model.scim2.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.Collections;
import java.util.NoSuchElementException;

;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Entity
@Table(indexes = @Index(columnList = "inum"))
public class TCBLUser {
	private static Logger log = LoggerFactory.getLogger(TCBLUser.class);

	@Column(length = 128)
	private String inum;

	@Id
	@Column(length = 128)
	private String userName;
	private String firstName;
	private String lastName;
	private transient String password;
	private boolean active;
	private boolean subscribedNL;	// is the user subscribed to the TCBL newsletter?
	private boolean acceptedPP;		// did the user accept the TCBL privacy policy?


	private final transient static String subscribedField = "gcpSubscribedToTCBLnewsletter";
	private final transient static String acceptedField = "gcpAcceptedTCBLprivacyPolicy";


	public TCBLUser() {
		active = false;
	}

	static TCBLUser createFromScimUser(final User scimUser, String extensionUrn) {
		TCBLUser user = new TCBLUser();
		user.setInum(scimUser.getId());
		user.setUserName(scimUser.getUserName());
		user.setFirstName(scimUser.getName().getGivenName());
		user.setLastName(scimUser.getName().getFamilyName());
		user.setActive(scimUser.isActive() == null ? false : scimUser.isActive());

		if (scimUser.isExtensionPresent(extensionUrn)) {
			Extension extension = scimUser.getExtension(extensionUrn);
			try {
				user.setSubscribedNL(Boolean.parseBoolean(extension.getFieldAsString(subscribedField)));
				user.setAcceptedPP(Boolean.parseBoolean(extension.getFieldAsString(acceptedField)));
			} catch (NoSuchElementException e) {
				log.warn("(One of) the fields {} and {} do not exist. Setting subscribedNL and acceptedPP to 'false'",subscribedField, acceptedField);
				user.setSubscribedNL(false);
				user.setAcceptedPP(false);
			}
		} else {
			log.warn("No extension URN '{}' found on the Gluu server", extensionUrn);
		}
		return user;
	}

	void updateScimUser(final User scimUser, String extensionUrn) {
		if (scimUser.getUserName() == null) {
			scimUser.setUserName(userName);
		}
		if (scimUser.getPassword().isEmpty()) {
			scimUser.setPassword(password);
		}
		if (scimUser.getEmails().isEmpty()) {
			Email email = new Email();
			email.setValue(scimUser.getUserName());
			scimUser.setEmails(Collections.singletonList(email));
		}
		String displayName = firstName + " " + lastName;
		Name newName = new Name();
		newName.setGivenName(firstName);
		newName.setFamilyName(lastName);
		newName.setFormatted(displayName);
		scimUser.setName(newName);
		scimUser.setDisplayName(displayName);
		scimUser.setActive(active);

		Extension extension = new Extension.Builder(extensionUrn)
				.setField(subscribedField, Boolean.toString(subscribedNL))
				.setField(acceptedField, Boolean.toString(acceptedPP))
				.build();
		if (scimUser.isExtensionPresent(extensionUrn)) {
			scimUser.setExtensions(new SingletonMap(extensionUrn, extension));
		} else {
			scimUser.addExtension(extension);
		}
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	public boolean isSubscribedNL() {
		return subscribedNL;
	}

	public void setSubscribedNL(boolean subscribedNL) {
		this.subscribedNL = subscribedNL;
	}

	public boolean isAcceptedPP() {
		return acceptedPP;
	}

	public void setAcceptedPP(boolean acceptedPP) {
		this.acceptedPP = acceptedPP;
	}

	@Override
	public String toString() {
		return "TCBLUser{" +
				"userName='" + userName + '\'' +
				", firstName='" + firstName + '\'' +
				", lastName='" + lastName + '\'' +
				", subscribedNL='" + subscribedNL + '\'' +
				", acceptedPP='" + acceptedPP + '\'' +
				'}';
	}

	public String getInum() {
		return inum;
	}

	public void setInum(String inum) {
		this.inum = inum;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
