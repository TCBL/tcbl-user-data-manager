package be.ugent.idlab.tcbl.userdatamanager.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Martin Vanbrabant
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SrvLink {
	private String text;
	private String url;
	private String style; // appended to "button--srvlink--" to define a css class for display purposes

	public SrvLink() {
		this.style = "asp";
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}
}
