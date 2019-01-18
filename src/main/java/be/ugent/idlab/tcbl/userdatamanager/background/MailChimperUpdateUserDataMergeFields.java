package be.ugent.idlab.tcbl.userdatamanager.background;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>Data to send in MailChimper when updating a user (contained class).</p>
 *
 * @author Martin Vanbrabant
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MailChimperUpdateUserDataMergeFields {
	@JsonProperty("FNAME")
	private String firstName;
	@JsonProperty("LNAME")
	private String lastName;

	public MailChimperUpdateUserDataMergeFields() {
	}

	public MailChimperUpdateUserDataMergeFields(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
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

	@Override
	public String toString() {
		return String.format("%s{firstName='%s', lastName='%s'}",
				this.getClass().getSimpleName(),
				firstName,
				lastName);
	}
}
