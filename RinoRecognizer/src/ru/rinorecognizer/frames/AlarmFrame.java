package ru.rinorecognizer.frames;

import java.util.List;
import java.util.Locale;

import ru.rinorecognizer.Frame;
import ru.rinorecognizer.FramingResult;
import ru.rinorecognizer.MainActivity;
import ru.rinorecognizer.Frame.ParamsType;
import ru.rinorecognizer.parsers.TimeParser;

import android.text.format.Time;
import android.content.Intent;
import android.net.Uri;
import android.provider.AlarmClock;



public class AlarmFrame extends Frame {
	private Time time;
	
	public AlarmFrame(MainActivity main){
		super(main, ActionType.A_ALARM);
	}
	
	public FramingResult fill(List<String> wgroups, List<ParamsType> labels)
	{			
		String timePatch = "";
		
		for (int i = 0; i < wgroups.size(); i++) 
		{
			switch (labels.get(i)) {
			case P_TIME:
				TimeParser tp = new TimeParser();
				timePatch = wgroups.get(i);
				time = tp.parse(timePatch);
				
			default: break;
			}
		}

		
		FramingResult framingResult = new FramingResult();

		if (time != null) {
			response = "Ставлю будильник на " + time.hour + " часов " + time.minute + " минут.";	
			
			Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
			intent.putExtra(AlarmClock.EXTRA_HOUR, time.hour);
			intent.putExtra(AlarmClock.EXTRA_MINUTES, time.minute);
			intent.putExtra(AlarmClock.EXTRA_MESSAGE,"Rino Alarm");
			
			framingResult.intent = intent;
		}
		else {
			response = "Непонятное время: «" + timePatch + "»";
			framingResult.intent = null;
		}

		framingResult.savedFrame = this; 
		return framingResult;
	}	
	
	
	protected boolean check() {
		return true;
	}
	
}