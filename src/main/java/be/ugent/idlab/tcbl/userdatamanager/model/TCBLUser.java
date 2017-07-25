package be.ugent.idlab.tcbl.userdatamanager.model;

import org.gluu.oxtrust.model.scim2.Email;
import org.gluu.oxtrust.model.scim2.Name;
import org.gluu.oxtrust.model.scim2.User;

import java.util.Collections;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class TCBLUser {

	private String id;
	private String userName;
	private String firstName;
	private String lastName;
	private String password;

	static TCBLUser createFromScimUser(final User scimUser) {
		TCBLUser user = new TCBLUser();
		user.setId(scimUser.getId());
		user.setUserName(scimUser.getUserName());
		user.setFirstName(scimUser.getName().getGivenName());
		user.setLastName(scimUser.getName().getFamilyName());
		return user;
	}

	void updateScimUser(final User scimUser) {
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
		//scimUser.setUserName(userName);
		String displayName = firstName + " " + lastName;
		Name newName = new Name();
		newName.setGivenName(firstName);
		newName.setFamilyName(lastName);
		newName.setFormatted(displayName);
		scimUser.setName(newName);
		scimUser.setDisplayName(displayName);
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

	@Override
	public String toString() {
		return "TCBLUser{" +
				"userName='" + userName + '\'' +
				", firstName='" + firstName + '\'' +
				", lastName='" + lastName + '\'' +
				'}';
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
