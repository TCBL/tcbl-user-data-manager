package be.ugent.idlab.tcbl.userdatamanager.model;

import org.gluu.oxtrust.model.scim2.User;

/**
 * @author Gerald Haesendonck
 */
@FunctionalInterface
public interface ScimUserProcessor {
	void process(final User user);
}
