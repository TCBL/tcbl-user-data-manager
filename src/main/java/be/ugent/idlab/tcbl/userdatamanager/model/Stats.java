package be.ugent.idlab.tcbl.userdatamanager.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
	public int totalCount = 0;	// total nr of users in the server
	public int invited = 0;	// number of invited (pre-registered) users
	public int invitedActive = 0;	// number of invited users that never signed in
	public int newUsers = 0;	// number of users that registered themselves
	private Map<Long, Integer> activeAtTime = new TreeMap<>();

	private Map<Long, List<String>> selfRegisteredUsers = new TreeMap<>();

	
	private final long now = new Date().getTime();
	private transient static Gson gson = new GsonBuilder().setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").create();
	private transient static DateFormat dateFormat = new SimpleDateFormat("YYYY-LL-dd");


	public void add(final TCBLUser user) {
		totalCount++;
		if (user.isInvited()) {
			invited++;
			if (user.isActive()) {
				invitedActive++;
				insertActive(Util.toCalendarPerDay(user.getActiveSince()));
			}
		} else {
			newUsers++;
			Calendar createdCalendar = Util.toCalendarPerDay(user.getCreated());
			insertActive(createdCalendar);
			insertSelfRegisteredUser(createdCalendar, user.getUserName());
		}
	}

	private void insertActive(final Calendar date) {
		activeAtTime.compute(date.getTimeInMillis(), (existingDate, nrActive) -> (nrActive == null) ? 1 : ++nrActive);
	}

	private void insertSelfRegisteredUser(final Calendar date, final String user) {
		long registerDate = date.getTimeInMillis();
		List<String> userList;
		if (selfRegisteredUsers.containsKey(registerDate)) {
			userList = selfRegisteredUsers.get(registerDate);
		} else {
			userList = new ArrayList<>(1);
			selfRegisteredUsers.put(registerDate, userList);
		}
		userList.add(user);
	}

	public void toFile() throws IOException {
		File file = new File("/tmp", "usermanager_stats_" + dateFormat.format(new Date(now)) + ".json");
		try (Writer out = new FileWriter(file)) {
			gson.toJson(this, out);
		}
	}

	public boolean exists() {
		File file = new File("/tmp", "usermanager_stats_" + dateFormat.format(new Date(now))+ ".json");
		return file.exists();
	}

	public static Stats fromLatestFile() throws FileNotFoundException {
		File tmp = new File("/tmp");
		File[] possibleFiles = tmp.listFiles((dir, fileName) -> fileName.startsWith("usermanager_stats_"));
		if (possibleFiles != null && possibleFiles.length > 0) {
			Arrays.sort(possibleFiles);
			File file = possibleFiles[possibleFiles.length - 1];
			if (file.exists()) {
				return gson.fromJson(new FileReader(file), Stats.class);
			} else {
				return null;
			}
		}
		return null;
	}

	public Map<Long, List<String>> getSelfRegisteredUsers() {
		return selfRegisteredUsers;
	}

	////// methods to ease drawing of charts: prepare data labels, values, etc

	public String[] getLabels() {
		List<String> labels = new ArrayList<>();
		for (Long timestamp : activeAtTime.keySet()) {
			Date date = new Date(timestamp);
			labels.add(dateFormat.format(date));
		}
		return labels.toArray(new String[labels.size()]);
	}

	public Integer[] getActiveValues() {
		List<Integer> values = new ArrayList<>();
		values.addAll(activeAtTime.values());
		return values.toArray(new Integer[values.size()]);
	}

	public Integer[] getTotalActiveValues() {
		List<Integer> values = new ArrayList<>();
		int totals = 0;
		for (Integer totalActive : activeAtTime.values()) {
			totals += totalActive;
			values.add(totals);
		}
		return values.toArray(new Integer[values.size()]);
	}
}

