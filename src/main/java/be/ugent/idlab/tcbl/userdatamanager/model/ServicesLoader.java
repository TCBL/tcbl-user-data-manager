package be.ugent.idlab.tcbl.userdatamanager.model;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;

/**
 * <p>A class to load the TCBL services from file.</p>
 * <p>
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
@Component
public class ServicesLoader {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${tudm.tcbl-services.filename}")
	private String filename;

	private Services services;

	public ServicesLoader()
	{
		log.debug("Constructing ServicesLoader.");
	}

	@PostConstruct
	public void refresh() {
		try (Reader in = new FileReader(filename)) {
			services = new Gson().fromJson(in, Services.class);
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
