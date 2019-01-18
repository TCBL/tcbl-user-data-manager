/*
 *  Copyright 2019 imec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package be.ugent.idlab.tcbl.userdatamanager.model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static be.ugent.idlab.tcbl.userdatamanager.model.Util.invitationDay;

/**
 * @author Gerald Haesendonck
 */
public class Stats implements Serializable {
	public int totalCount = 0;	// total nr of users in the server
	public int invited = 0;	// number of invited (pre-registered) users
	public int invitedActive = 0;	// number of invited users that never signed in
	public int newUsers = 0;	// number of users that registered themselves
	public int testUsers = 0;
	private Map<Long, Integer> activeAtTime = new TreeMap<>();
	private Map<Long, List<String>> selfRegisteredUsers = new TreeMap<>();

	private transient static DateFormat dateFormat = new SimpleDateFormat("YYYY-LL-dd");


	public void add(final TCBLUser user) {
		totalCount++;
		if (user.isInvited()) {
			invited++;
			if (user.getActiveSince() != null) {
				invitedActive++;
				insertActive(Util.toCalendarPerDay(user.getActiveSince()));
			}
		} else {
			if (user.getCreated() != null) {
				Calendar createdCalendar = Util.toCalendarPerDay(user.getCreated());
				if (createdCalendar.after(invitationDay)) {
					newUsers++;
					insertActive(createdCalendar);
					insertSelfRegisteredUser(createdCalendar, user.getUserName());
				} else {
					testUsers++;
				}
			}
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
		List<Integer> values = new ArrayList<>(activeAtTime.values());
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

