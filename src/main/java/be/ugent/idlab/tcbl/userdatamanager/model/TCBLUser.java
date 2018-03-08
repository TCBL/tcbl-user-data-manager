package be.ugent.idlab.tcbl.userdatamanager.model;

import javax.persistence.*;
import java.util.Date;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Entity
@Table(indexes = @Index(columnList = "inum"))
public class TCBLUser {

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


	public TCBLUser() {
		active = false;
	}

	public TCBLUser(String inum, String userName, String firstName, String lastName, boolean active, boolean invited, String pictureURL, boolean subscribedNL, boolean acceptedPP, Date created, Date lastModified, Date passwordReset, Date activeSince) {
		this.inum = inum;
		this.userName = userName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.active = active;
		this.invited = invited;
		this.pictureURL = pictureURL;
		this.subscribedNL = subscribedNL;
		this.acceptedPP = acceptedPP;
		this.created = created;
		this.lastModified = lastModified;
		this.passwordReset = passwordReset;
		this.activeSince = activeSince;
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
