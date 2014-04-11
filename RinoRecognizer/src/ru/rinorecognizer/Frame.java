package ru.rinorecognizer;

import java.util.List;


public abstract class Frame {
	
	protected MainActivity mainActivity;
	protected Boolean isComplete;
	protected String response;
	protected IdTranslator.ActionType type;
	protected IdTranslator.ParamsType expParameter;
	
	public Frame(MainActivity main, IdTranslator.ActionType type)
	{
		this.mainActivity = main;
		this.type = type;
		this.isComplete = false;
		this.expParameter = null;
		this.response = "";
	}
	
	public abstract FramingResult fill(List<String> wgroups, List<IdTranslator.ParamsType> labels);
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



