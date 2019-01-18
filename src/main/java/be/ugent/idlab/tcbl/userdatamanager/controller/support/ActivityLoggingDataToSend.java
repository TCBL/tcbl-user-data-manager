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

