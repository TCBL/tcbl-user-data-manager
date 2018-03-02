package be.ugent.idlab.tcbl.userdatamanager.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Component
public class UserRepository {
	private final ScimUserRepository scimUserRepository;
	private final DatabaseUserRepository databaseUserRepository;

	private final Logger log = LoggerFactory.getLogger(TCBLUser.class);

	public UserRepository(ScimUserRepository scimUserRepository, DatabaseUserRepository databaseUserRepository) {
		this.scimUserRepository = scimUserRepository;
		this.databaseUserRepository = databaseUserRepository;
	}

	public TCBLUser find(String inum) throws  Exception {
		if (databaseUserRepository.existsByInum(inum)) {
			return databaseUserRepository.findByInum(inum);
		} else {
			TCBLUser user = scimUserRepository.find(inum);
			databaseUserRepository.save(user);
			return user;
		}
	}

	/*public Iterable<TCBLUser> findAll() throws Exception {
		return scimUserRepository.findAll();
	}*/
	public TCBLUser save(TCBLUser user) throws Exception {
		TCBLUser savedUser = scimUserRepository.save(user);
		databaseUserRepository.save(savedUser);
		return savedUser;
	}
	public TCBLUser findByName(final String userName) throws Exception {
		Optional<TCBLUser> userOption = databaseUserRepository.findById(userName);
		if (userOption.isPresent()) {
			return userOption.get();
		} else {
			TCBLUser user = scimUserRepository.findByName(userName);
			databaseUserRepository.save(user);
			return user;
		}
	}
	public TCBLUser create(TCBLUser user) throws Exception {
		TCBLUser resultUser = scimUserRepository.create(user);
		databaseUserRepository.save(resultUser);
		return resultUser;
	}
	public void deleteTCBLUser(TCBLUser user) throws Exception {
		scimUserRepository.deleteTCBLUser(user);
		databaseUserRepository.delete(user);
	}
	public Iterable<TCBLUser> findInactive() {
		return databaseUserRepository.findByActive(false);
	}
	public void processScimUsers(final ScimUserProcessor processor) throws Exception {
		scimUserRepository.processScimUsers(processor);
	}
	public void processTCBLUsers(final TCBLUserProcessor processor) throws Exception {
		scimUserRepository.processTCBLUsers(processor);
	}

	@Async
	public void synchronise() {
		try {
			for (TCBLUser tcblUser : scimUserRepository.findAll()) {
				databaseUserRepository.save(tcblUser);
			}
			log.info("Database synchronised!");
		} catch (Exception e) {
			log.error("Something went wrong synchronising the databases!");
		}
	}
}
