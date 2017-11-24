package be.ugent.idlab.tcbl.userdatamanager.model;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
public class NavLink {
	public enum DisplayCondition {
		// enum names are used in ThymeLeaf, don't change
		ALWAYS,
		ANONYMOUS,
		AUTHENTICATED
	}

	private final DisplayCondition displayCondition;
	private final String text;
	private final String location; // to be interpreted by ThymeLeaf's @{}

	public NavLink(DisplayCondition displayCondition, String text, String location) {
		this.displayCondition = displayCondition;
		this.text = text;
		this.location = location;
	}

	public DisplayCondition getDisplayCondition() {
		return displayCondition;
	}

	public String getText() {
		return text;
	}

	public String getLocation() {
		return location;
	}
}
