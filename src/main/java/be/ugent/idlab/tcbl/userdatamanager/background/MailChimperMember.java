package be.ugent.idlab.tcbl.userdatamanager.background;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MailChimperMember {
	@JsonProperty("email_address")
	private String emailAddress;

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	@Override
	public String toString() {
		return String.format("%s{emailAddress='%s'}",
				this.getClass().getSimpleName(),
				emailAddress);
	}
}
