package ru.rinorecognizer.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.text.format.Time;


public class TimeParser {
	
	private static final Pattern p_full_time = Pattern.compile("(?:\\D+ )*(\\d{1,2}) (?:\\D+ )*(\\d{1,2})(?: \\D+)*");
	private static final Pattern p_minutes = Pattern.compile("(?:\\D+ )*(\\d{1,2})( ?:\\D+)* минут\\p{InCyrillic}*(?: \\D+)*");
	private static final Pattern p_one = Pattern.compile("час дня( \\w+)*");
	private static final Pattern p_midnight = Pattern.compile("12 ночи( \\w+)*");
	private static final Pattern p_hour = Pattern.compile("(?:\\D+ )*(\\d{1,2})(?: \\D+)*");
	
	private static final Pattern p_evening = Pattern.compile(".*(дня|вечер\\p{InCyrillic}*).*");
	private static final Pattern p_tomorrow = Pattern.compile(".*завтра\\p{InCyrillic}*.*");
	
	
	public Time parse(String command) {

		Time time = new Time();
		Boolean found = false;
		
		Matcher pFullTimeMatcher = p_full_time.matcher(command);
		Matcher pMinutesMatcher = p_minutes.matcher(command);
		Matcher pHourMatcher = p_hour.matcher(command);
		
		Matcher pEveningMatcher = p_evening.matcher(command);
		Matcher pTomorrowMatcher = p_tomorrow.matcher(command);
		
		Matcher pOneMatcher = p_one.matcher(command);
		Matcher pMidnightMatcher = p_midnight.matcher(command);
		

		if (pFullTimeMatcher.matches()) {
			time.hour = Integer.parseInt(pFullTimeMatcher.group(1));
			time.minute = Integer.parseInt(pFullTimeMatcher.group(2));
			found = true;			
		}
		else if (pMinutesMatcher.matches()) {
			Time now = new Time();
			now.setToNow();

			
			int tmp_minute = now.minute + Integer.parseInt(pMinutesMatcher.group(1));
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
