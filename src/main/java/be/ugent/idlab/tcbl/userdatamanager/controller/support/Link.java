package be.ugent.idlab.tcbl.userdatamanager.controller.support;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
public class Link {
	public enum DisplayCondition {
		ALWAYS,
		ANONYMOUS,
		AUTHENTICATED
	}

	private final DisplayCondition displayCondition;
	private final String text;
	private final String location;

	public Link(DisplayCondition displayCondition, String text, String location) {
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
