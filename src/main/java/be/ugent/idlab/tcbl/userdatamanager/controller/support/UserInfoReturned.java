package be.ugent.idlab.tcbl.userdatamanager.controller.support;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfoReturned {
	private String inum;

	public String getInum() {
		return inum;
	}

	public void setInum(String inum) {
		this.inum = inum;
	}
}
