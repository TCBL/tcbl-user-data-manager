package be.ugent.idlab.tcbl.userdatamanager.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * <p>A class to contain the TCBL services.</p>
 * <p>
 * <p>Fixed structure for json file compatibility.</p>
 * <p>
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Services {

	private List<SrvLink> srvLinksTCBL;
	private List<SrvLink> srvLinksASP;

	public List<SrvLink> getSrvLinksTCBL() {
		return srvLinksTCBL;
	}

	public void setSrvLinksTCBL(List<SrvLink> srvLinksTCBL) {
		this.srvLinksTCBL = srvLinksTCBL;
	}

	public List<SrvLink> getSrvLinksASP() {
		return srvLinksASP;
	}

	public void setSrvLinksASP(List<SrvLink> srvLinksASP) {
		this.srvLinksASP = srvLinksASP;
	}
}
