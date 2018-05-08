package be.ugent.idlab.tcbl.userdatamanager.controller.support;

import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUser;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>Extra data to send in ActivityLogger, in case of a user profile update.</p>
 * <p>
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
public class UserProfileUpdateData {


	@JsonProperty
	private final List<String> updates;

	public UserProfileUpdateData(TCBLUser oldUser, TCBLUser newUser, boolean samePicture) {
		this.updates = new ArrayList<>();
		addUpdatedString(oldUser.getFirstName(), newUser.getFirstName(), "firstName", false);
		addUpdatedString(oldUser.getLastName(), newUser.getLastName(), "lastName", false);
		addUpdatedPicture(oldUser.getPictureURL(), newUser.getPictureURL(), "picture", samePicture);
		addUpdatedBoolean(oldUser.isSubscribedNL(), newUser.isSubscribedNL(), "subscribedNL");
		addUpdatedBoolean(oldUser.isAcceptedPP(), newUser.isAcceptedPP(), "acceptedPP");
		addUpdatedBoolean(oldUser.isAllowedMon(), newUser.isAllowedMon(), "allowedMon");
	}

	private void addUpdatedString(String oldValue, String newValue, String label, boolean showNewValue) {
		if (!Objects.equals(oldValue, newValue)) {
			if (showNewValue) {
				updates.add(String.format(label + " (%s)", newValue));
			} else {
				updates.add(label);
			}
		}
	}

	private void addUpdatedBoolean(boolean oldValue, boolean newValue, String label) {
		if (!Objects.equals(oldValue, newValue)) {
			updates.add(String.format(label + " (%b)", newValue));
		}
	}

	private void addUpdatedPicture(String oldPictureURL, String newPictureURL, String label, boolean samePicture) {
		// for a given user, the pictureURL will allways be null or one userName-dependent fixed value
		// if neither the old nor the new pictureURL is null, we need the hint from samePicture to detect actual updates to the picture
		if (oldPictureURL == null) {
			if (newPictureURL != null) {
				updates.add(label + " (added)");
			}
		} else {
			if (newPictureURL == null) {
				updates.add(label + " (deleted)");
			} else {
				if (!samePicture) {
					updates.add(label + " (updated)");
				}
			}
		}
	}

	@Override
	public String toString() {
		return String.format("%s{updates=%s}",
				this.getClass().getSimpleName(),
				updates.toString());
	}
}
