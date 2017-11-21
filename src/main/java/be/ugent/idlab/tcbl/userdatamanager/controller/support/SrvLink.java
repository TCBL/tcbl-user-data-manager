package be.ugent.idlab.tcbl.userdatamanager.controller.support;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
public class SrvLink {
	private final String text;
	private final String url;

	private final String cssClass; // a specific css class that specifies the layout of the SrvLink

	public SrvLink(String text, String url, String cssClass) {
		this.text = text;
		this.url = url;
		this.cssClass = cssClass;
	}

	public String getText() {
		return text;
	}

	public String getUrl() {
		return url;
	}

	public String getCssClass() {
		return cssClass;
	}
}
