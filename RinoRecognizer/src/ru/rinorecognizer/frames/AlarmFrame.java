package ru.rinorecognizer.frames;

import java.util.List;

import ru.rinorecognizer.Frame;
import ru.rinorecognizer.FramingResult;
import ru.rinorecognizer.MainActivity;
import ru.rinorecognizer.parsers.TimeParser;

import android.content.Intent;
import android.provider.AlarmClock;



public class AlarmFrame extends Frame {
	private Integer hour;
	private Integer minutes;
	
	public AlarmFrame(MainActivity main){
		super(main, ActionType.A_ALARM);
	}
	
	public FramingResult fill(List<String> wgroups, List<ParamsType> labels)
	{			
		for (int i = 0; i < wgroups.size(); i++)
			switch (labels.get(i)) {
			case P_TIME:
				TimeParser tp = new TimeParser();
				TimeParser.Time time = tp.parse(wgroups.get(i));

				if (time != null) {	
					hour = time.hour;
					minutes = time.minutes;
				}
				else {
					response = "Непонятное время: «" + wgroups.get(i) + "»";
					return null;
				}
				
			default: break;
			}
		
		response = "Ставлю будильник на " + hour + " часов " + minutes + " минут."; 
		
		Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
		intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
		intent.putExtra(AlarmClock.EXTRA_MINUTES, minutes);
		intent.putExtra(AlarmClock.EXTRA_MESSAGE,"Rino Alarm");
		
		FramingResult framingResult = new FramingResult();
		framingResult.intent = intent;
		framingResult.savedFrame = this;

		return framingResult;
	}	
	
	protected boolean check() {
		return true;
	}
	
}