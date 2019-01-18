package be.ugent.idlab.tcbl.userdatamanager.controller.support;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * <p>Data returned in ActivityLogger.</p>
 *
 * @author Martin Vanbrabant
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivityLoggingDataReturned {
	private String uuid;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String toString() {
		return String.format("%s{uuid='%s'}",
				this.getClass().getSimpleName(),
				uuid);
	}
}

