package be.ugent.idlab.tcbl.userdatamanager.model;

import org.gluu.oxtrust.model.scim2.Name;
import org.gluu.oxtrust.model.scim2.User;

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

	static TCBLUser createFromScimUser(final User scimUser) {
		TCBLUser user = new TCBLUser();
		user.setId(scimUser.getId());
		user.setUserName(scimUser.getUserName());
		user.setFirstName(scimUser.getName().getGivenName());
		user.setLastName(scimUser.getName().getFamilyName());
		return user;
	}

	void updateScimUser(final User scimUser) {
		//scimUser.setUserName(userName);
		Name newName = new Name();
		newName.setGivenName(firstName);
		newName.setFamilyName(lastName);
		newName.setFormatted(firstName + " " + lastName);
		scimUser.setName(newName);
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
}
