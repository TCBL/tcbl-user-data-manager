package be.ugent.idlab.tcbl.userdatamanager.controller.support;

/**
 * <p>Possible types for activity logging.</p>
 * <p>
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
public enum ActivityLoggingType {
	login                  ("usermanager.user/login"),
	logout                 ("usermanager.user/logout"),
	registration_mailsent  ("usermanager.user/registration.mailsent"),
	registration_completed ("usermanager.user/registration.completed"),
	resetpassword_mailsent ("usermanager.user/resetpassword.mailsent"),
	resetpassword_completed("usermanager.user/resetpassword.completed"),
	profile_updated        ("usermanager.user/profile.updated");

	private final String value;

	ActivityLoggingType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
