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

import javax.persistence.*;
import java.util.Date;

/**
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
	private boolean allowedMon;		// did the user allow TCBL activity monitoring?

	private Date created;
	private Date lastModified;
	private Date passwordReset;
	private Date activeSince;	// only for invited users;


	public TCBLUser() {
		active = false;
	}

	public TCBLUser(String inum,
					String userName,
					String firstName,
					String lastName,
					boolean active,
					boolean invited,
					String pictureURL,
					boolean subscribedNL,
					boolean acceptedPP,
					boolean allowedMon,
					Date created,
					Date lastModified,
					Date passwordReset,
					Date activeSince) {
		this.inum = inum;
		this.userName = userName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.active = active;
		this.invited = invited;
		this.pictureURL = pictureURL;
		this.subscribedNL = subscribedNL;
		this.acceptedPP = acceptedPP;
		this.allowedMon = allowedMon;
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

	public boolean isAllowedMon() {
		return allowedMon;
	}

	public void setAllowedMon(boolean allowedMon) {
		this.allowedMon = allowedMon;
	}

	@Override
	public String toString() {
		return String.format("%s{userName='%s', firstName='%s', lastName='%s', pictureURL=%s, subscribedNL=%b, acceptedPP=%b, allowedMon=%b}",
				this.getClass().getSimpleName(),
				userName,
				firstName,
				lastName,
				pictureURL == null ? "null" : pictureURL,
				subscribedNL,
				acceptedPP,
				allowedMon);
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
