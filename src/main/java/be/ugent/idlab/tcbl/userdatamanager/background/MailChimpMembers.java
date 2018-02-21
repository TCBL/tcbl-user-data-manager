package be.ugent.idlab.tcbl.userdatamanager.background;

import java.util.List;

/**
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class MailChimpMembers {
	private List<MailChimpMember> members;

	public List<MailChimpMember> getMembers() {
		return members;
	}

	public void setMembers(List<MailChimpMember> members) {
		this.members = members;
	}
}
