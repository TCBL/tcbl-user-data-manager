package be.ugent.idlab.tcbl.userdatamanager.model;

/**
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@FunctionalInterface
public interface TCBLUserProcessor {
	void process(TCBLUser user);
}
