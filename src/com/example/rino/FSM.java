package com.example.rino;

import java.util.Collection;
import java.util.HashSet;

public class FSM {
	
	private Collection<State> states;
	
	public Collection<State> getNextState(State start, String word, String wordType, Collection<String> keywordTypes){
		int startId = start.getId();
		Collection<State> resStates = new HashSet<State>();
		/*if (start.isFinite()) {
			resStates.add(start);
		}*/
		if (keywordTypes.contains("skip")){
			resStates.add(start);
			return resStates;
		}
		switch (startId) {
		case 0:
			if (keywordTypes.contains("call")){
				resStates.add(new State(1, "call_num"));
			}
			if (keywordTypes.contains("sms_skip") || 
					keywordTypes.contains("email_skip")){
				resStates.add(new State(2));
			}
			break;
		case 1:
			if (keywordTypes.contains("num") || 
					keywordTypes.contains("num_skip")){
				resStates.add(start);
			}
			if (wordType.equals("num_start") || wordType.equals("number")){
				State st = new State(3, true, start.getCommandType());
				st.addParameter("number", word);
				resStates.add(st);
			}
			if (wordType.equals("text")){
				State st = new State(4, true, start.getCommandType());
				st.addParameter("contact", word);
				resStates.add(st);
			}
			break;
		case 2:
			break;
		case 3:
			if (wordType.equals("number")){
				State st = new State(3, true, start.getCommandType());
				String num = start.getParameter("number");
				st.addParameter("number", num + word);
				resStates.add(st);
			}
			break;
		case 4:
			if (wordType.equals("text")){
				State st = new State(5, true, start.getCommandType());
				String name = start.getParameter("contact");
				st.addParameter("contact", name + " " + word);
				resStates.add(st);
			}
			break;
		case 5:
			if (wordType.equals("text")){
				State st = new State(6, true, start.getCommandType());
				String name = start.getParameter("contact");
				st.addParameter("contact", name + " " + word);
				resStates.add(st);
			}
			break;
		case 6:
			break;
		default:
			if (keywordTypes.contains("null") && wordType.equals("text")){
				resStates.add(start);
			}			
		}
		return resStates;
	}
}
