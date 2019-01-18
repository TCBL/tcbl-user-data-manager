package be.ugent.idlab.tcbl.userdatamanager.controller.support;

/**
 * <p>Possible types for activity logging.</p>
 *
 * @author Martin Vanbrabant
 */
public enum ActivityLoggingType {
	registration_mailsent  ("usermanager.user/registration.mailsent"),
	registration_completed ("usermanager.user/registration.completed"),
	login                  ("usermanager.user/login"),
	profile_updated        ("usermanager.user/profile.updated"),
	logout                 ("usermanager.user/logout"),
	resetpassword_mailsent ("usermanager.user/resetpassword.mailsent"),
	resetpassword_completed("usermanager.user/resetpassword.completed");

	private final String value;

	ActivityLoggingType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
