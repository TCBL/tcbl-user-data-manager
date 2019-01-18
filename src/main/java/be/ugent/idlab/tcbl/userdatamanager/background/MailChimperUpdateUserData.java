/*
 *  Copyright 2019 imec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package be.ugent.idlab.tcbl.userdatamanager.background;

import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>Data to send in MailChimper when updating a user.</p>
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
