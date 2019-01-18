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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>Data to send in ActivityLogger.</p>
 *
 * @author Martin Vanbrabant
 */
public class ActivityLoggingDataToSend {
	@JsonProperty("userID")
	private String userName;
	@JsonProperty("type")
	private String logType;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("data")
	private Object extraData;

	public ActivityLoggingDataToSend(String userName, String logType, Object extraData) {
		this.userName = userName;
		this.logType = logType;
		this.extraData = extraData;
	}

	@Override
	public String toString() {
		return String.format("%s{userName='%s', logType='%s', extraData=%s}",
				this.getClass().getSimpleName(),
				userName,
				logType,
				extraData == null ? "null" : "'" + extraData.toString() + "'");
	}
}

