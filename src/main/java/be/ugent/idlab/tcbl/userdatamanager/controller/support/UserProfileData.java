package be.ugent.idlab.tcbl.userdatamanager.controller.support;

import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUser;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>Extra data to send in ActivityLogger, in case a new user profile was created.</p>
 * <p>
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
public class UserProfileData {

	@JsonProperty
	protected final boolean hasPicture;
	@JsonProperty
	protected final boolean acceptedPP;
	@JsonProperty
	protected final boolean allowedMon;
	@JsonProperty
	protected final boolean subscribedNL;

	public UserProfileData(TCBLUser user) {
		this.acceptedPP = user.isAcceptedPP();
		this.allowedMon = user.isAllowedMon();
		this.hasPicture = user.getPictureURL() != null;
		this.subscribedNL = user.isSubscribedNL();
	}

	@Override
	public String toString() {
		return String.format("%s{hasPicture=%b, acceptedPP=%b, allowedMon=%b, subscribedNL=%b}",
				this.getClass().getSimpleName(),
				hasPicture, acceptedPP, allowedMon, subscribedNL);
	}
}
