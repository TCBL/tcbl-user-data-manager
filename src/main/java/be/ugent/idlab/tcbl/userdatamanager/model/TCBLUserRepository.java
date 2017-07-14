package be.ugent.idlab.tcbl.userdatamanager.model;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public interface TCBLUserRepository {
	Iterable<TCBLUser> findAll() throws Exception;
	TCBLUser save(TCBLUser tcblUser) throws Exception;
	TCBLUser find(String userName) throws  Exception;
	void deleteTCBLUser(String userName) throws Exception;
}
