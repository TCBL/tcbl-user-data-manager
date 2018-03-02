package be.ugent.idlab.tcbl.userdatamanager.model;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public interface GluuTCBLUserRepository {
	Iterable<TCBLUser> findAll() throws Exception;
	TCBLUser save(TCBLUser user) throws Exception;
	TCBLUser find(String inum) throws  Exception;
	TCBLUser findByName(final String userName) throws Exception;
	TCBLUser create(TCBLUser user) throws Exception;
	void deleteTCBLUser(TCBLUser user) throws Exception;
	Iterable<TCBLUser> findInactive() throws Exception;
	void processScimUsers(final ScimUserProcessor processor) throws Exception;
	void processTCBLUsers(final TCBLUserProcessor processor) throws Exception;
}
