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

package be.ugent.idlab.tcbl.userdatamanager.controller.support;

import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUser;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>Extra data to send in ActivityLogger, in case a new user profile was created.</p>
 *
 * @author Martin Vanbrabant
 */
public class UserProfileData {

	@JsonProperty
	protected final boolean hasPicture;
	@JsonProperty
	protected final boolean acceptedPP;
	@JsonProperty
	protected final boolean allowedMon;
	@JsonProperty
	protected final boolean subscribedNL;

	public UserProfileData(TCBLUser user) {
		this.acceptedPP = user.isAcceptedPP();
		this.allowedMon = user.isAllowedMon();
		this.hasPicture = user.getPictureURL() != null;
		this.subscribedNL = user.isSubscribedNL();
	}

	@Override
	public String toString() {
		return String.format("%s{hasPicture=%b, acceptedPP=%b, allowedMon=%b, subscribedNL=%b}",
				this.getClass().getSimpleName(),
				hasPicture, acceptedPP, allowedMon, subscribedNL);
	}
}
