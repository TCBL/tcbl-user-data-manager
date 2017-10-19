package be.ugent.idlab.tcbl.userdatamanager.model;

import com.google.gson.Gson;
import org.gluu.oxtrust.model.scim2.Meta;
import org.gluu.oxtrust.model.scim2.User;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
public class Stats implements Serializable {
	private int totalCount = 0;	// total nr of users in the server
	private int invited = 0;	// number of invited (pre-registered) users
	private int invitedInactive = 0;	// number of invited users that never signed in
	private int newUsers = 0;	// number of users that registered themselves
	private Map<Calendar, Integer> activeAtTime = new TreeMap<>();

	
	private /*transient*/ final Date now = new Date();
	private /*transient*/ final Calendar invitation_day = new GregorianCalendar(2017, Calendar.AUGUST, 31);
	private transient final Gson gson = new Gson();
	private transient static DateFormat dateFormat = new SimpleDateFormat("YYYY-LL-dd");


	public void add(final User user) {
		totalCount++;
		Meta meta = user.getMeta();
		Date created = meta.getCreated();
		Date modified = meta.getLastModified();
		Calendar createdCalendar = toCalendarPerDay(created);
		Calendar modifiedCalendar = toCalendarPerDay(modified);
		if (createdCalendar.equals(invitation_day)) {
			invited++;
			if (created.equals(modified)) {
				invitedInactive++;
			} else {
				insertActive(modifiedCalendar);
			}
		} else if (createdCalendar.compareTo(invitation_day) > 0) {
			newUsers++;
			insertActive(createdCalendar);
		}
	}

	private Calendar toCalendarPerDay(final Date date) {
		if (date == null) {
			return new GregorianCalendar(0, 0, 1);
		} else {
			Calendar original = new GregorianCalendar();
			original.setTime(date);
			return new GregorianCalendar(original.get(Calendar.YEAR), original.get(Calendar.MONTH), original.get(Calendar.DAY_OF_MONTH));
		}
	}

	private void insertActive(final Calendar date) {
		activeAtTime.compute(date, (existingDate, nrActive) -> (nrActive == null) ? 1 : ++nrActive);
	}

	public void toFile() throws IOException {
		//String json = gson.toJson(this);
		File file = new File("/tmp", "usermanager_stats_" + dateFormat.format(now) + ".json");
		try (Writer out = new FileWriter(file)) {
			gson.toJson(this, out);
		}
	}

	public boolean exists() {
		File file = new File("/tmp", "usermanager_stats_" + dateFormat.format(now)+ ".json");
		return file.exists();
	}

	public static Stats fromFile() throws FileNotFoundException {
		File file = new File("/tmp", "usermanager_stats_" + dateFormat.format(new Date()) + ".json");
		if (file.exists()) {
			Gson gson = new Gson();
			return gson.fromJson(new FileReader(file), Stats.class);
		} else {
			return null;
		}
	}
}

