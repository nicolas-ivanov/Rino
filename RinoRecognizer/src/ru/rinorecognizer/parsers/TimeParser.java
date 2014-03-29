package ru.rinorecognizer.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.text.format.Time;


public class TimeParser {
	
	private static final Pattern p_hour_minute_minute = Pattern.compile("(?:\\D+ )*(\\d{1,2}) (?:\\D+ )*(\\d{1,2})(?: \\D+)* (\\d{1,2})(?: \\D+)*");
	private static final Pattern p_hour_minute = Pattern.compile("(?:\\D+ )*(\\d{1,2}) (?:\\D+ )*(\\d{1,2})(?: \\D+)*");
	private static final Pattern p_minute_minute = Pattern.compile("(?:\\D+ )*(\\d{1,2}) (\\d{1,2})( ?:\\D+)* минут\\p{InCyrillic}*(?: \\D+)*");
	private static final Pattern p_minute = Pattern.compile("(?:\\D+ )*(\\d{1,2})( ?:\\D+)* минут\\p{InCyrillic}*(?: \\D+)*");
	private static final Pattern p_one = Pattern.compile("час дня( \\w+)*");
	private static final Pattern p_midnight = Pattern.compile("12 ночи( \\w+)*");
	private static final Pattern p_hour = Pattern.compile("(?:\\D+ )*(\\d{1,2})(?: \\D+)*");
	
	private static final Pattern p_evening = Pattern.compile(".*(дня|вечер\\p{InCyrillic}*).*");
	private static final Pattern p_tomorrow = Pattern.compile(".*завтра\\p{InCyrillic}*.*");
	
	public Time parse(String timePatch) {
	

		Time time = new Time();
		Boolean found = false;

		Matcher pHourMinuteMinuteMatcher = p_hour_minute_minute.matcher(timePatch);
		Matcher pHourMinuteMatcher = p_hour_minute.matcher(timePatch);
		Matcher pMinuteMinuteMatcher = p_minute_minute.matcher(timePatch);
		Matcher pMinuteMatcher = p_minute.matcher(timePatch);
		Matcher pHourMatcher = p_hour.matcher(timePatch);
		
		Matcher pEveningMatcher = p_evening.matcher(timePatch);
		Matcher pTomorrowMatcher = p_tomorrow.matcher(timePatch);
		
		Matcher pOneMatcher = p_one.matcher(timePatch);
		Matcher pMidnightMatcher = p_midnight.matcher(timePatch);
		

		if (pHourMinuteMinuteMatcher.matches()) {
			time.hour = Integer.parseInt(pHourMinuteMinuteMatcher.group(1));
			time.minute = Integer.parseInt(pHourMinuteMinuteMatcher.group(2)) + Integer.parseInt(pHourMinuteMinuteMatcher.group(3));
			found = true;			
		}
		else if (pMinuteMinuteMatcher.matches()) {
			Time now = new Time();
			now.setToNow();

			
			int tmp_minute = now.minute + Integer.parseInt(pMinuteMinuteMatcher.group(1)) + Integer.parseInt(pMinuteMinuteMatcher.group(2));
			time.hour = (int) (now.hour + Math.floor(tmp_minute / 60));
			time.minute = tmp_minute % 60;
			
			found = true;
		}
		else if (pHourMinuteMatcher.matches()) {
			time.hour = Integer.parseInt(pHourMinuteMatcher.group(1));
			time.minute = Integer.parseInt(pHourMinuteMatcher.group(2));
			found = true;			
		}
		else if (pMinuteMatcher.matches()) {
			Time now = new Time();
			now.setToNow();

			
			int tmp_minute = now.minute + Integer.parseInt(pMinuteMatcher.group(1));
			time.hour = (int) (now.hour + Math.floor(tmp_minute / 60));
			time.minute = tmp_minute % 60;
			
			found = true;
		}
		else if (pOneMatcher.matches()) {
			time.hour = 13;
			found = true;			
		}
		else if (pMidnightMatcher.matches()) {
			time.hour = 24;
			found = true;			
		}
		else if (pHourMatcher.matches()) {
			time.hour = Integer.parseInt(pHourMatcher.group(1));
			time.minute = 0;
			found = true;		
		}
		
		
		if (found) {
			if (pEveningMatcher.matches() && (time.hour <= 12))
				time.hour += 12;
			
			if (pTomorrowMatcher.matches())
				time.hour += 24;
		}
		else
			time = null;
		
		return time;		
	}

}
