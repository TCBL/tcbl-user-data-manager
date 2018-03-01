package be.ugent.idlab.tcbl.userdatamanager.background;

import be.ugent.idlab.tcbl.userdatamanager.model.GluuTCBLUserRepository;
import be.ugent.idlab.tcbl.userdatamanager.model.ScimUserProcessor;
import be.ugent.idlab.tcbl.userdatamanager.model.Stats;
import org.gluu.oxtrust.model.scim2.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Component
public class CalculateStats implements ScimUserProcessor {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final GluuTCBLUserRepository userRepository;
	private Stats stats = new Stats();

	public CalculateStats(GluuTCBLUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Scheduled(cron = "0 0 3 * * *")
	//@Scheduled(cron = "0 * * * * *")
	public void go() {
		log.info("Calculating statistics");
		stats = new Stats();
		if (stats.exists()) {
			log.info("Stats already calculated. Skipping");
		} else {
			try {
				userRepository.processScimUsers(this);
				stats.toFile();
			} catch (Exception e) {
				log.warn("Calculating statistics went wrong. ", e);
			}
		}
		log.info("calculating statistics done.");
	}

	@Override
	public void process(final User user) {
		stats.add(user);
	}
}
