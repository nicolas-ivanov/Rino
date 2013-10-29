package ru.rinorecognizer;

import java.util.List;

import ru.rinorecognizer.FramingTask.ActionType;
import ru.rinorecognizer.FramingTask.ParamsType;

import android.content.Intent;


public abstract class Frame {
	protected MainActivity mainActivity;
	protected String response;
	protected ActionType type;
	
	public Frame(MainActivity main, ActionType type)
	{
		this.mainActivity = main;
		this.type = type;
		response = "";
	}
	
	public abstract FramingResult fill(List<String> wgroups, List<ParamsType> labels);
	protected abstract boolean check();	
	
	public String getResponse()	{
		return response;
	}
}

