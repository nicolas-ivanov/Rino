package ru.rinorecognizer.frames;

import java.util.List;

import ru.rinorecognizer.Frame;
import ru.rinorecognizer.IdTranslator.ActionType;
import ru.rinorecognizer.IdTranslator.LabelsType;
import ru.rinorecognizer.MainActivity;


public class CancelFrame extends Frame {
	
	public CancelFrame(MainActivity main){
		super(main, ActionType.A_CANCEL);
	}
	
	public Frame fill(List<String> wgroups, List<LabelsType> labels)
	{			
		response = "Действие отменено";
		expParameter = null;		
		
		return this;
	}	
	
	protected boolean check() {
		return true;
	}
}