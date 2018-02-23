package be.ugent.idlab.tcbl.userdatamanager.model;

import org.gluu.oxtrust.model.scim2.User;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@FunctionalInterface
public interface ScimUserProcessor {
	void process(final User user);
}
