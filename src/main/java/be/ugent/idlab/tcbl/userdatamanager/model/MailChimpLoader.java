package be.ugent.idlab.tcbl.userdatamanager.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.Reader;
import java.util.Properties;

/**
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Component
public class MailChimpLoader {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${mailchimp.filename}")
	private String filename;

	private String key;
	private String listId;
	private String apiVersion;

	@PostConstruct
	public void refresh() {
		try (Reader in = new FileReader(filename)) {
			Properties properties = new Properties();
			properties.load(in);
			key = properties.getProperty("key");
			listId = properties.getProperty("list");
			apiVersion = properties.getProperty("api");
			log.info("MailChimpLoader's properties rerfreshed");
		} catch (Exception e) {
			log.error("MailChimpLoader's properties not rerfreshed: ", e);
		}
	}

	public String getKey() {
		return key;
	}

	public String getListId() {
		return listId;
	}

	public String getApiVersion() {
		return apiVersion;
	}
}
