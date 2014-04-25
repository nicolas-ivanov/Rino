package ru.rinorecognizer;

import java.util.List;

import ru.rinorecognizer.IdTranslator.ActionType;
import ru.rinorecognizer.IdTranslator.ParamsType;
import ru.rinorecognizer.frames.AlarmFrame;
import ru.rinorecognizer.frames.BalanceFrame;
import ru.rinorecognizer.frames.CallFrame;
import ru.rinorecognizer.frames.CancelFrame;
import ru.rinorecognizer.frames.EmailFrame;
import ru.rinorecognizer.frames.SearchFrame;
import ru.rinorecognizer.frames.SiteFrame;
import ru.rinorecognizer.frames.SmsFrame;
import android.content.Intent;


public abstract class Frame {
	
	protected MainActivity mainActivity;
	protected Boolean isComplete;
	protected String response;
	protected Intent intent;
	protected IdTranslator.ActionType type;
	protected IdTranslator.ParamsType expParameter;
	
	public Frame(MainActivity main, IdTranslator.ActionType type)
	{
		this.mainActivity = main;
		this.type = type;
		this.isComplete = false;
		this.intent = null;
		this.expParameter = ParamsType.OTHER;
		this.response = "";
	}
	
	public abstract Frame fill(List<String> wgroups, List<IdTranslator.ParamsType> labels);
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



