package be.ugent.idlab.tcbl.userdatamanager.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

/**
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Component
public interface DatabaseUserRepository extends CrudRepository<TCBLUser, String> {
}
