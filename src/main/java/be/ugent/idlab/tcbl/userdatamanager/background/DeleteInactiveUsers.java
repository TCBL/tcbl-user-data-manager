package be.ugent.idlab.tcbl.userdatamanager.background;

import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUser;
import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUserRepository;
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
public class DeleteInactiveUsers {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final TCBLUserRepository userRepository;

	public DeleteInactiveUsers(TCBLUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	//@Scheduled(fixedDelay = 5000)

	/**
	 * Deletes all inactive users every morning at 2:00. Users are inactive if they are registered but not activated
	 * yet.
	 * @throws Exception Something goes wrong. Should not occur, since Exceptons are catched.
	 */
	@Scheduled(cron = "0 0 2 * * *")
	public void deleteInactiveUsers() throws Exception {
		log.debug("Deleting inactive users in background...");
		Iterable<TCBLUser> inactiveUserIds = userRepository.findInactive();
		try {
			for (TCBLUser user : inactiveUserIds) {
				userRepository.deleteTCBLUser(user);
			}
		} catch (Exception e) {
			log.warn("Deleting inactive user failed. Still continuing", e);
		}
	}
}
