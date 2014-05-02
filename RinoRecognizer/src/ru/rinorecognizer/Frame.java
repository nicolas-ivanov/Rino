package ru.rinorecognizer;

import java.util.List;

import ru.rinorecognizer.IdTranslator.ActionType;
import ru.rinorecognizer.IdTranslator.ParamsType;
import android.content.Intent;


public abstract class Frame {
	
	protected MainActivity mainActivity;
	protected Boolean isComplete;
	protected String response;
	protected Intent intent;
	protected ActionType type;
	protected ParamsType expParameter;
	
	public Frame(MainActivity main, IdTranslator.ActionType type)
	{
		this.mainActivity = main;
		this.type = type;
		this.isComplete = false;
		this.intent = null;
		this.expParameter = ParamsType.OTHER;
		this.response = "";
	}
	
	public abstract Frame fill(List<String> wgroups, List<IdTranslator.LabelsType> labels);
	protected abstract boolean check();	
	
	public String getResponse()	{
		return response;
	}
	
	public Boolean isComplete() {
		return isComplete;
	}
	
	public int getExpParameterID() {
		return IdTranslator.getParamOrdinal(expParameter);
	}
	
	public int getTypeID() {		
		return IdTranslator.getActionID(type);
	}
	
}



