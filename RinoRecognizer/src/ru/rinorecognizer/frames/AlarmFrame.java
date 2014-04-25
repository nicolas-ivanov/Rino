package ru.rinorecognizer.frames;

import java.util.List;

import ru.rinorecognizer.Frame;
import ru.rinorecognizer.IdTranslator;
import ru.rinorecognizer.MainActivity;
import ru.rinorecognizer.parsers.TimeParser;
import android.content.Intent;
import android.provider.AlarmClock;
import android.text.format.Time;



public class AlarmFrame extends Frame {
	private Time time;
	
	public AlarmFrame(MainActivity main){
		super(main, IdTranslator.ActionType.A_ALARM);
	}
	
	public Frame fill(List<String> wgroups, List<IdTranslator.ParamsType> labels)
	{			
		String timePatch = "";
		
		for (int i = 0; i < wgroups.size(); i++) 
		{
			switch (labels.get(i)) {
			case P_TIME:
				TimeParser tp = new TimeParser();
				time = tp.parse(wgroups.get(i));
				
			default: break;
			}
		}

		
		// check
		if (time != null) {
			response = "Ставлю будильник на " + time.hour + " часов " + time.minute + " минут.";
			expParameter = null;
			
			intent = new Intent(AlarmClock.ACTION_SET_ALARM);
			intent.putExtra(AlarmClock.EXTRA_HOUR, time.hour);
			intent.putExtra(AlarmClock.EXTRA_MINUTES, time.minute);
			intent.putExtra(AlarmClock.EXTRA_MESSAGE,"Rino Alarm");
		}
		else {
			response = "Непонятное время: «" + timePatch + "»\n На сколько нужно поставить будильник?";
			expParameter = IdTranslator.ParamsType.P_TIME;
		}
		
		return this;
	}	
	
	
	protected boolean check() {
		return true;
	}
	
}