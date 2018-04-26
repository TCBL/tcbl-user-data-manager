package be.ugent.idlab.tcbl.userdatamanager.controller.support;

import java.io.Serializable;

/**
 * <p>Data returned in ActivityLogger.</p>
 * <p>
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
public class ActivityLoggingDataReturned {
	private String uuid;

	public ActivityLoggingDataReturned() {
	}

	public ActivityLoggingDataReturned(String uuid) {
		this.uuid = uuid;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String toString() {
		return String.format("ActivityLoggingDataReturned{uuid='%s'}", uuid);
	}
}

