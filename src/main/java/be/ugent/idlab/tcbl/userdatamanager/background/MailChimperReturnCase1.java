package be.ugent.idlab.tcbl.userdatamanager.background;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author Gerald Haesendonck
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MailChimperReturnCase1 {
	@JsonProperty("total_items")
	private int totalItems;
	private List<MailChimperMember> members;

	public int getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(int totalItems) {
		this.totalItems = totalItems;
	}

	public List<MailChimperMember> getMembers() {
		return members;
	}

	public void setMembers(List<MailChimperMember> members) {
		this.members = members;
	}

	@Override
	public String toString() {
		return String.format("%s{totalItems=%d, members='%s'}",
				this.getClass().getSimpleName(),
				totalItems,
				members.toString());
	}
}
