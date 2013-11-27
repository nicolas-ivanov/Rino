package ru.rinorecognizer;

import java.util.List;


public abstract class Frame {

	public static enum ActionType {A_CALL, A_SMS, A_EMAIL, A_SEARCH, A_SITE, A_ALARM, A_BALANCE};
	public static enum ParamsType {ACTION, P_NAME, P_NUMBER, P_EMAIL, P_SITE, P_TIME, OTHER, QUOTE, Q_MARK, PREPOS};
	
	protected MainActivity mainActivity;
	protected String response;
	protected ActionType type;
	protected Boolean isComplete;
	
	public Frame(MainActivity main, ActionType type)
	{
		this.mainActivity = main;
		this.type = type;
		this.isComplete = false;
		response = "";
	}
	
	public abstract FramingResult fill(List<String> wgroups, List<ParamsType> labels);
	protected abstract boolean check();	
	
	public String getResponse()	{
		return response;
	}
	
	public Boolean isComplete() {
		return isComplete;
	}
	
	public int getTypeID() {
		
		int typeID;
		
		switch (type) {
		case A_CALL: 	typeID = 1; break;
		case A_SMS: 	typeID = 2; break;
		case A_EMAIL: 	typeID = 3; break;
		case A_SEARCH: 	typeID = 4; break;
		case A_SITE: 	typeID = 5; break;
		case A_ALARM: 	typeID = 6; break;
		case A_BALANCE:	typeID = 7; break;
		default: 		typeID = 0;
		}
		
		return typeID;
	}
}



