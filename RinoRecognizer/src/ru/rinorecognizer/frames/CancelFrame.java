package ru.rinorecognizer.frames;

import java.util.List;

import ru.rinorecognizer.Frame;
import ru.rinorecognizer.FramingResult;
import ru.rinorecognizer.IdTranslator;
import ru.rinorecognizer.MainActivity;


public class CancelFrame extends Frame {
	
	public CancelFrame(MainActivity main){
		super(main, IdTranslator.ActionType.A_CANCEL);
	}
	
	public FramingResult fill(List<String> wgroups, List<IdTranslator.ParamsType> labels)
	{			
		response = "Действие отменено";
		expParameter = null;		
		
		FramingResult framingResult = new FramingResult();
		framingResult.intent = null;
		framingResult.savedFrame = null;

		return framingResult;
	}	
	
	protected boolean check() {
		return true;
	}
}