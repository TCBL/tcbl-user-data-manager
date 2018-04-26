package be.ugent.idlab.tcbl.userdatamanager.controller.support;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>Data to send in ActivityLogger.</p>
 * <p>
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
public class ActivityLoggingDataToSend {
	@JsonProperty("userID")
	private String userName;
	@JsonProperty("type")
	private String logType;

	public ActivityLoggingDataToSend() {
	}

	public String getUserName() {
		return userName;
	}

	public ActivityLoggingDataToSend(String userName, String logType) {
		this.userName = userName;
		this.logType = logType;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	@Override
	public String toString() {
		return String.format("ActivityLoggingDataToSend{userName='%s', logType='%s}'", userName, logType);
	}
}

