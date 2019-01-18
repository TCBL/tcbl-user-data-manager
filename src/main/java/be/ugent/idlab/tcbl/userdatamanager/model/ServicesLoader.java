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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;

/**
 * <p>A class to load the TCBL services from file.</p>
 *
 * @author Martin Vanbrabant
 */
@Component
public class ServicesLoader {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final String filename;

	private Services services;

	public ServicesLoader(Environment environment)
	{
		this.filename = environment.getRequiredProperty("tudm.tcbl-services.filename");
	}

	@PostConstruct
	public void refresh() {
		try {
			services = new ObjectMapper().readValue(new File(filename), Services.class);
			log.info("ServiceLoader's services refreshed.");
		} catch (Exception e) {
			log.error("ServiceLoader's services no refreshed:", e);
		}
	}

	public List<SrvLink> getSrvLinksTCBL() {
		return services == null ? null : services.getSrvLinksTCBL();
	}

	public List<SrvLink> getSrvLinksASP() {
		return services == null ? null : services.getSrvLinksASP();
	}

}
