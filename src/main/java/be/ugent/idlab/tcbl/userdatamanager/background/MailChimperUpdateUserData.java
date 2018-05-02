package be.ugent.idlab.tcbl.userdatamanager.background;

import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>Data to send in MailChimper when updating a user.</p>
 * <p>
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MailChimperUpdateUserData {
	@JsonProperty("email_address")
	private String emailAddress;
	@JsonProperty("status_if_new")
	private String statusIfNew;
	private String status;
	@JsonProperty("merge_fields")
	private MailChimperUpdateUserDataMergeFields mergeFields;

	public MailChimperUpdateUserData() {
	}

	public MailChimperUpdateUserData(TCBLUser user) {
		this.emailAddress = user.getUserName();
		this.statusIfNew = user.isSubscribedNL() ? "subscribed" : "unsubscribed";
		this.status = statusIfNew;
		this.mergeFields = new MailChimperUpdateUserDataMergeFields(user.getFirstName(), user.getLastName());
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getStatusIfNew() {
		return statusIfNew;
	}

	public void setStatusIfNew(String statusIfNew) {
		this.statusIfNew = statusIfNew;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public MailChimperUpdateUserDataMergeFields getMergeFields() {
		return mergeFields;
	}

	public void setMergeFields(MailChimperUpdateUserDataMergeFields mergeFields) {
		this.mergeFields = mergeFields;
	}

	@Override
	public String toString() {
		return String.format("%s{emailAddress='%s', statusIfNew='%s', status='%s', mergeFields='%s'}",
				this.getClass().getSimpleName(),
				emailAddress,
				statusIfNew,
				status,
				mergeFields.toString());
	}
}
