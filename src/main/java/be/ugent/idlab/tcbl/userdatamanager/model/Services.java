/*
 *  Copyright 2019 imec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package be.ugent.idlab.tcbl.userdatamanager.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * <p>A class to contain the TCBL services.</p>
 * <p>
 * <p>Fixed structure for json file compatibility.</p>
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
