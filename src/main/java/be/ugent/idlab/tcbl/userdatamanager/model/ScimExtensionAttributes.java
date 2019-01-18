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

package be.ugent.idlab.tcbl.userdatamanager.model;

/**
 * @author Gerald Haesendonck
 */
public enum ScimExtensionAttributes {
	urn("urn:ietf:params:scim:schemas:extension:gluu:2.0:User"),
	subscribedField("gcpSubscribedToTCBLnewsletter"),
	acceptedField("gcpAcceptedTCBLprivacyPolicy"),
	pictureField("gcpPictureURL"),
	allowedMonField("gcpAllowedTCBLactivityMon");

	private final String value;

	ScimExtensionAttributes(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
