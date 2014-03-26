package ru.rinorecognizer.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParser {
	
	public class Time {
		public int hour;
		public int minutes;
	}
	
	private static final Pattern p_simple = Pattern.compile("(\\d{1,2}) (\\w+ )?(\\d{1,2})");
	private static final Pattern p_evening = Pattern.compile("(\\w+ )+[дня|вечер\\p{InCyrillic}*]( \\w+)*");
	private static final Pattern p_tomorrow = Pattern.compile("(\\w+ )+[завтра\\p{InCyrillic}*]\\p{InCyrillic}( \\w+)*");
	
	private static final Pattern p_one = Pattern.compile("час дня( \\w+)*");
	private static final Pattern p_midnight = Pattern.compile("[двенадцать|12] ночи( \\w+)*");
	
	public Time parse(String command) {

		Time time = new Time();
		int hour = -1, minutes = -1;
		Boolean found = false;
		
		Matcher pSimpleMatcher = p_simple.matcher(command);
		Matcher pOneMatcher = p_one.matcher(command);
		Matcher pMidnightMatcher = p_midnight.matcher(command);
		Matcher pEveningMatcher = p_evening.matcher(command);
		Matcher pTomorrowMatcher = p_tomorrow.matcher(command);

		if (pSimpleMatcher.matches()) {
			hour = Integer.parseInt(pSimpleMatcher.group(1));
			minutes = Integer.parseInt(pSimpleMatcher.group(3));
			found = true;			
		}
		else if (pOneMatcher.matches()) {
			hour = 13;
			found = true;			
		}
		else if (pMidnightMatcher.matches()) {
			hour = 24;
			found = true;			
		}
		
		
		if (found) {
			if (pEveningMatcher.matches() && (hour <= 12))
				hour += 12;
			
			if (pTomorrowMatcher.matches())
				hour += 24;
			
			time.hour = hour;
			time.minutes = minutes;
		}
		else
			time = null;
		
		return time;		
	}

}
