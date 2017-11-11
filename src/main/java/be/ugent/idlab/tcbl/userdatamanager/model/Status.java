package be.ugent.idlab.tcbl.userdatamanager.model;

import java.util.Map;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class Status {
	public enum Value {
		OK,
		WARNING,
		ERROR
	}

	private final Value value;
	private final String text;

	public Status(Value value, String text) {
		this.value = value;
		this.text = text;
	}

	public Value getValue() {
		return value;
	}

	public String getText() {
		return text;
	}
}
