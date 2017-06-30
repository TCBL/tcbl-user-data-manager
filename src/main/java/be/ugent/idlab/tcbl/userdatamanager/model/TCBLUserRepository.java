package be.ugent.idlab.tcbl.userdatamanager.model;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public interface TCBLUserRepository {
	Iterable<TCBLUser> findAll();
	TCBLUser save(TCBLUser tcblUser);
	TCBLUser find(String userName);
	void deleteTCBLUser(String userName);
}
