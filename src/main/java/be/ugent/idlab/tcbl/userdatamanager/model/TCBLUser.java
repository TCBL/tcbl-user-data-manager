package be.ugent.idlab.tcbl.userdatamanager.model;

import org.apache.commons.collections.map.SingletonMap;
import org.gluu.oxtrust.model.scim2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.*;

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
	public transient static final Calendar invitationDay = new GregorianCalendar(2017, Calendar.AUGUST, 31);

	@Column(length = 128, nullable = false)
	private String inum;

	@Id
	@Column(length = 128, nullable = false)
	private String userName;

	@Column(nullable = false)
	private String firstName;

	@Column(nullable = false)
	private String lastName;

	private transient String password; // this is only relevant for gluu

	private boolean active;
	private boolean invited;
	private String pictureURL;
	private boolean subscribedNL;	// is the user subscribed to the TCBL newsletter?
	private boolean acceptedPP;		// did the user accept the TCBL privacy policy?

	private Date created;
	private Date lastModified;
	private Date passwordReset;
	private Date activeSince;	// only for invited users;


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

		Meta meta = scimUser.getMeta();
		user.setCreated(meta.getCreated());
		user.setLastModified(meta.getLastModified());

		// fix passwordReset date
		Date created = meta.getCreated();
		Date modified = meta.getLastModified();
		Calendar createdCalendar = toCalendarPerDay(created);
		if (createdCalendar.equals(invitationDay)) {
			user.setInvited(true);
			if (!created.equals(modified)) {
				user.setPasswordReset(user.getLastModified());
				user.setActiveSince(user.getLastModified());
			}
		} else {
			user.setInvited(false);
			user.setActiveSince(user.getCreated());
		}

		// TODO set pictureURL
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
		// TODO set pictureURL
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

	public String getPictureURL() {
		return pictureURL;
	}

	public void setPictureURL(String pictureURL) {
		this.pictureURL = pictureURL;
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
				", pictureURL='" + pictureURL + '\'' +
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

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public static Calendar toCalendarPerDay(final Date date) {
		if (date == null) {
			return new GregorianCalendar(0, 0, 1);
		} else {
			Calendar original = new GregorianCalendar();
			original.setTime(date);
			return new GregorianCalendar(original.get(Calendar.YEAR), original.get(Calendar.MONTH), original.get(Calendar.DAY_OF_MONTH));
		}
	}

	public Date getPasswordReset() {
		return passwordReset;
	}

	public void setPasswordReset(Date passwordReset) {
		this.passwordReset = passwordReset;
	}

	public boolean isInvited() {
		return invited;
	}

	public void setInvited(boolean invited) {
		this.invited = invited;
	}

	public Date getActiveSince() {
		return activeSince;
	}

	public void setActiveSince(Date activeSince) {
		this.activeSince = activeSince;
	}
}
