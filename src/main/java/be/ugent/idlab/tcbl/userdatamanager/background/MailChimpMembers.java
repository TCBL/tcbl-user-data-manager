package be.ugent.idlab.tcbl.userdatamanager.background;

import java.util.List;

/**
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class MailChimpMembers {
	private List<MailChimpMember> members;
	private boolean update_existing;

	public MailChimpMembers(List<MailChimpMember> members, boolean update_existing) {
		this.members = members;
		this.update_existing = update_existing;
	}

	public List<MailChimpMember> getMembers() {
		return members;
	}

	public void setMembers(List<MailChimpMember> members) {
		this.members = members;
	}

	public void setUpdate_existing(boolean update_existing) {
		this.update_existing = update_existing;
	}
}
