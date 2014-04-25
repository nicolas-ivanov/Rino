package ru.rinorecognizer;

import ru.rinorecognizer.frames.AlarmFrame;
import ru.rinorecognizer.frames.BalanceFrame;
import ru.rinorecognizer.frames.CallFrame;
import ru.rinorecognizer.frames.CancelFrame;
import ru.rinorecognizer.frames.EmailFrame;
import ru.rinorecognizer.frames.SearchFrame;
import ru.rinorecognizer.frames.SiteFrame;
import ru.rinorecognizer.frames.SmsFrame;

public class FrameTranslator {
	
	public static Frame getProperActionFrame(MainActivity mainActivity, IdTranslator.ActionType a_type)
	{		
		Frame frame = null;
		
		switch (a_type) {
		case A_CALL: 	frame = new CallFrame(mainActivity); 	break;			
		case A_SMS: 	frame = new SmsFrame(mainActivity); 	break;			
		case A_EMAIL: 	frame = new EmailFrame(mainActivity); 	break;			
		case A_SITE: 	frame = new SiteFrame(mainActivity); 	break;		
		case A_SEARCH: 	frame = new SearchFrame(mainActivity); 	break;			
		case A_ALARM: 	frame = new AlarmFrame(mainActivity); 	break;
		case A_BALANCE: frame = new BalanceFrame(mainActivity); break;
		case A_CANCEL: 	frame = new CancelFrame(mainActivity); 	break;
		default:
			break;
		}
		
		return frame;
	}
}
