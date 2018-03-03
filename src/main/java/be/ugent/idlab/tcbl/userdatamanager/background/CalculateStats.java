package be.ugent.idlab.tcbl.userdatamanager.background;

import be.ugent.idlab.tcbl.userdatamanager.model.Stats;
import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUser;
import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUserProcessor;
import be.ugent.idlab.tcbl.userdatamanager.model.UserRepository;
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
public class CalculateStats implements TCBLUserProcessor {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final UserRepository userRepository;
	private Stats stats = new Stats();

	public CalculateStats(UserRepository userRepository) {
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
				userRepository.processTCBLUsers(this);
				stats.toFile();
			} catch (Exception e) {
				log.warn("Calculating statistics went wrong. ", e);
			}
		}
		log.info("calculating statistics done.");
	}

	@Override
	public void process(final TCBLUser user) {
		stats.add(user);
	}
}
