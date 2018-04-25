package be.ugent.idlab.tcbl.userdatamanager.model;

/**
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public enum ScimExtensionAttributes {
	urn("urn:ietf:params:scim:schemas:extension:gluu:2.0:User"),
	subscribedField("gcpSubscribedToTCBLnewsletter"),
	acceptedField("gcpAcceptedTCBLprivacyPolicy"),
	pictureField("gcpPictureURL"),
	allowedMonField("gcpAllowedTCBLactivityMon");

	private final String value;

	ScimExtensionAttributes(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
