package be.ugent.idlab.tcbl.userdatamanager.controller.support;

import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUser;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>Extra data to send in ActivityLogger, in case of a user profile was updated.</p>
 *
 * @author Martin Vanbrabant
 */
public class UserProfileUpdateData extends UserProfileData {

	@JsonProperty
	private final List<String> updates;

	public UserProfileUpdateData(TCBLUser oldUser, TCBLUser newUser, boolean pictureUpdated) {
		super(newUser);
		List<String> updates = new ArrayList<>();
		if (!Objects.equals(oldUser.getFirstName(), newUser.getFirstName())) {
			updates.add("firstName");
		}
		if (!Objects.equals(oldUser.getLastName(), newUser.getLastName())) {
			updates.add("lastName");
		}
		if (!Objects.equals(oldUser.getPictureURL(), newUser.getPictureURL())) {
			updates.add("hasPicture");
		}
		// picture updates cannot be deducted from the pictureURL. Get this info from the user interface...
		if (pictureUpdated) {
			updates.add("picture");
		}
		if (!Objects.equals(oldUser.isAcceptedPP(), newUser.isAcceptedPP())) {
			updates.add("acceptedPP");
		}
		if (!Objects.equals(oldUser.isAllowedMon(), newUser.isAllowedMon())) {
			updates.add("allowedMon");
		}
		if (!Objects.equals(oldUser.isSubscribedNL(), newUser.isSubscribedNL())) {
			updates.add("subscribedNL");
		}
		this.updates = updates;
	}

	@Override
	public String toString() {
		return String.format("%s{hasPicture=%b, acceptedPP=%b, allowedMon=%b, subscribedNL=%b, updates=%s}",
				this.getClass().getSimpleName(),
				hasPicture,	subscribedNL, acceptedPP, allowedMon, updates.toString());
	}
}
