package be.ugent.idlab.tcbl.userdatamanager.background;

/**
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class MailChimpMember {
	private String email_address;
	private String status;

	public MailChimpMember(String email_address, String status) {
		this.email_address = email_address;
		this.status = status;
	}

	public String getEmail_address() {
		return email_address;
	}

	public void setEmail_address(String email_address) {
		this.email_address = email_address;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
