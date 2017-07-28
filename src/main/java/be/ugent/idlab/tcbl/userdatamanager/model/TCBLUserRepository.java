package be.ugent.idlab.tcbl.userdatamanager.model;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public interface TCBLUserRepository {
	Iterable<TCBLUser> findAll() throws Exception;
	TCBLUser save(TCBLUser tcblUser) throws Exception;
	TCBLUser find(String id) throws  Exception;
	TCBLUser create(TCBLUser tcblUser) throws Exception;
	void deleteTCBLUser(String id) throws Exception;
}
