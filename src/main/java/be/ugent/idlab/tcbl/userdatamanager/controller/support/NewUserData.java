package be.ugent.idlab.tcbl.userdatamanager.controller.support;

import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUser;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>Extra data to send in ActivityLogger, in case a new user was created.</p>
 * <p>
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
public class NewUserData {


	@JsonProperty
	private final boolean hasPicture;
	@JsonProperty
	private final boolean subscribedNL;
	@JsonProperty
	private final boolean acceptedPP;
	@JsonProperty
	private final boolean allowedMon;

	public NewUserData(TCBLUser user) {
		this. hasPicture = user.getPictureURL() != null;
		this.subscribedNL = user.isSubscribedNL();
		this.acceptedPP = user.isAcceptedPP();
		this.allowedMon = user.isAllowedMon();
	}

	@Override
	public String toString() {
		return String.format("%s{hasPicture=%b, subscribedNL=%b, acceptedPP=%b, allowedMon=%b}",
				this.getClass().getSimpleName(),
				hasPicture,
				subscribedNL,
				acceptedPP,
				allowedMon);
	}
}
