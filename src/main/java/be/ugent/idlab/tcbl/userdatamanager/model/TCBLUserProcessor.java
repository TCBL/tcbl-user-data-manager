package be.ugent.idlab.tcbl.userdatamanager.model;

/**
 * @author Gerald Haesendonck
 */
@FunctionalInterface
public interface TCBLUserProcessor {
	void process(TCBLUser user);
}
