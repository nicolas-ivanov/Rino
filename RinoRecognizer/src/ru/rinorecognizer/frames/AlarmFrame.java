package ru.rinorecognizer.frames;

import java.util.List;

import ru.rinorecognizer.Frame;
import ru.rinorecognizer.FramingResult;
import ru.rinorecognizer.MainActivity;

import android.content.Intent;
import android.provider.AlarmClock;



public class AlarmFrame extends Frame {
	private Integer hour;
	private Integer minutes;
	
	public AlarmFrame(MainActivity main){
		super(main, ActionType.A_ALARM);
		hour = 12;
		minutes = 0;
	}
	
	public FramingResult fill(List<String> wgroups, List<ParamsType> labels)
	{					
		for (int i = 0; i < wgroups.size(); i++)
			switch (labels.get(i)) {
			case P_HOUR: 
				hour = Integer.parseInt(wgroups.get(i));
			case P_MINUTES: 
				minutes = Integer.parseInt(wgroups.get(i));
			default: break;
			}

		response = "Ставлю будильник на " + hour + " часов " + minutes + "минут."; 
		
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