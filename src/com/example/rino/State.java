package com.example.rino;

import java.util.HashMap;
import java.util.Map;

public class State {
	private int id;
	private boolean fin = false;
	private String commandType;
	private Map<String, String> parameters;
	
	public Map<String, String> getParameters() {
		return parameters;
	}

	public String getCommandType() {
		return commandType;
	}

	public int getId() {
		return id;
	}

	public boolean isFinite() {
		return fin;
	}

	public State(int id){
		this.id = id;
	}
	
	public State(int id, boolean isFinite){
		this.id = id;
		this.fin = isFinite;
	}

	public State(int id, String commandType) {
		this.id = id;
		this.commandType = commandType;
	}
	
	public State(int id, boolean isFinite, String commandType) {
		this.id = id;
		this.commandType = commandType;
		this.fin = isFinite;
	}

	public void addParameter(String key, String value){
		if (parameters == null) {
			parameters = new HashMap<String, String>();
		}
		parameters.put(key, value);
	}
	
	public String getParameter(String key){
		return parameters.get(key);
	}

	@Override
	public String toString() {
		String start = "State [id=" + id + ", fin=" + fin + ", commandType="
				+ commandType + ", parameters= ";
		for (String key : parameters.keySet()){
			start += "key: " + key + " value: " + parameters.get(key) + ", "; 
		}
		return start + "]";
	}
}
