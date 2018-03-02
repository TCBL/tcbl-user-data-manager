package be.ugent.idlab.tcbl.userdatamanager.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Component
public interface DatabaseUserRepository extends CrudRepository<TCBLUser, String> {
	TCBLUser findByInum(String inum);
	boolean existsByInum(String inum);
	List<TCBLUser> findByActive(boolean active);
}
