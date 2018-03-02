package be.ugent.idlab.tcbl.userdatamanager.model;

import org.springframework.stereotype.Component;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Component
public class UserRepository {
	private final ScimUserRepository scimUserRepository;

	public UserRepository(ScimUserRepository scimUserRepository) {
		this.scimUserRepository = scimUserRepository;
	}

	public TCBLUser find(String inum) throws  Exception {
		return scimUserRepository.find(inum);
	}

	public Iterable<TCBLUser> findAll() throws Exception {
		return scimUserRepository.findAll();
	}
	public TCBLUser save(TCBLUser user) throws Exception {
		return scimUserRepository.save(user);
	}
	public TCBLUser findByName(final String userName) throws Exception {
		return scimUserRepository.findByName(userName);
	}
	public TCBLUser create(TCBLUser user) throws Exception {
		return scimUserRepository.create(user);
	}
	public void deleteTCBLUser(TCBLUser user) throws Exception {
		scimUserRepository.deleteTCBLUser(user);
	}
	public Iterable<TCBLUser> findInactive() throws Exception {
		return scimUserRepository.findInactive();
	}
	public void processScimUsers(final ScimUserProcessor processor) throws Exception {
		scimUserRepository.processScimUsers(processor);
	}
	public void processTCBLUsers(final TCBLUserProcessor processor) throws Exception {
		scimUserRepository.processTCBLUsers(processor);
	}
}
