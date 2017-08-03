package be.ugent.idlab.tcbl.userdatamanager.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class InMemoryTCBLUserRepository implements TCBLUserRepository {

	private final ConcurrentMap<String, TCBLUser> users = new ConcurrentHashMap<>();

	@Override
	public Iterable<TCBLUser> findAll() {
		return users.values();
	}

	@Override
	public TCBLUser save(TCBLUser tcblUser) {
		String userName = tcblUser.getUserName();
		users.put(userName, tcblUser);
		return tcblUser;
	}

	@Override
	public TCBLUser find(String userName) {
		return users.get(userName);
	}

	@Override
	public TCBLUser findByName(String userName) throws Exception {
		return find(userName);
	}

	@Override
	public TCBLUser create(TCBLUser tcblUser) throws Exception {
		return save(tcblUser);
	}

	@Override
	public void deleteTCBLUser(String userName) {
		users.remove(userName);
	}
}
