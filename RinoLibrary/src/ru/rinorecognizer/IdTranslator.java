package ru.rinorecognizer;

import java.util.Locale;

public class IdTranslator {

	public static enum ActionType {ACTION, A_CALL, A_SMS, A_EMAIL, A_SEARCH, A_SITE, A_ALARM, A_BALANCE, A_CANCEL};
	public static enum ParamsType {OTHER, ACTION, P_NAME, P_NUMBER, P_EMAIL, P_SITE, P_TIME, QUOTE, Q_MARK, PREPOS, CHANGE};

	
	// GetNum Section
	
	public static int getActionsNum() {
		return ActionType.values().length;
	}
	
	public static int getParamsNum() {
		return ParamsType.values().length;
	}
	
	

	// Actions Section
	
	public static int getActionID(ActionType actionType) {
		return actionType.ordinal();
	}	
	
	public static int getActionID(String actionType) {		
		return getActionID(ActionType.valueOf(actionType.toUpperCase(Locale.ENGLISH)));
	}	
	
	public static ActionType getActionEnum(int actionID) {		
		return ActionType.values()[actionID];
	}
	


	// Params Section
	
	public static int getParamOrdinal(ParamsType paramType) {
		if (paramType != null)
			return paramType.ordinal();
		else
			return 0;
	}	
	
	public static int getParamOrdinal(String paramType) {		
		return getParamOrdinal(ParamsType.valueOf(paramType.toUpperCase(Locale.ENGLISH)));
	}
		
	public static ParamsType getParamEnum(int paramID) {
		return ParamsType.values()[paramID];
	}
	

	public static ParamsType getParamEnumFromID(int paramID) {

		ParamsType paramType;
		
		switch (paramID) {	
			case 10: 	paramType = ParamsType.ACTION;	break;
			case 1: 	paramType = ParamsType.P_NAME;	break;
			case 2: 	paramType = ParamsType.P_NUMBER;break;
			case 3: 	paramType = ParamsType.P_EMAIL; break;
			case 4: 	paramType = ParamsType.P_SITE; 	break;
			case 5: 	paramType = ParamsType.P_TIME; 	break;
			case 6: 	paramType = ParamsType.QUOTE; 	break;
			case  0:	paramType = ParamsType.OTHER; 	break;
			case -1:	paramType = ParamsType.QUOTE; 	break;
			case -2:	paramType = ParamsType.Q_MARK; 	break;
			case -3:	paramType = ParamsType.PREPOS;	break;
			case -4:	paramType = ParamsType.CHANGE;	break;
			default: 	paramType = null;
			System.out.println("IdTranslator: paramID '" + paramID + "' is incorrect");
		}
		return paramType;
	}
}
