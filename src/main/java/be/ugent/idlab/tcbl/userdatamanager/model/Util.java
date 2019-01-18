package be.ugent.idlab.tcbl.userdatamanager.model;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Gerald Haesendonck
 */
public class Util {
	transient static final Calendar invitationDay = new GregorianCalendar(2017, Calendar.AUGUST, 31);

	public static Calendar toCalendarPerDay(final Date date) {
		if (date == null) {
			return new GregorianCalendar(0, 0, 1);
		} else {
			Calendar original = new GregorianCalendar();
			original.setTime(date);
			return new GregorianCalendar(original.get(Calendar.YEAR), original.get(Calendar.MONTH), original.get(Calendar.DAY_OF_MONTH));
		}
	}
}
