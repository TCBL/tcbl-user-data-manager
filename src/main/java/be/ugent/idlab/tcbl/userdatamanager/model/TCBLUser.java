package be.ugent.idlab.tcbl.userdatamanager.model;

import org.gluu.oxtrust.model.scim2.User;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class TCBLUser {

	private String userName;	// TODO: should be set in constructor

	static TCBLUser createFromScimUser(final User scimUser) {
		TCBLUser user = new TCBLUser();
		user.setUserName(scimUser.getUserName());
		return user;
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
				'}';
	}
}
