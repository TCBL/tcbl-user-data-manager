package be.ugent.idlab.tcbl.userdatamanager.controller.support;

/**
 * <p>Authentication details we set in the OAuth2AuthenticationToken object.</p>
 * <p>
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
public class MyAuthenticationDetails {
	private final String id;
	private final String userName;

	public MyAuthenticationDetails(String id, String userName) {
		this.id = id;
		this.userName = userName;
	}

	public String getId() {
		return id;
	}

	public String getUserName() {
		return userName;
	}
}
