package ru.rinorecognizer;

import java.util.Locale;

public class IdTranslator {

	public static enum ActionType {A_CHATTER, A_CALL, A_SMS, A_EMAIL, A_SEARCH, A_SITE, A_ALARM, A_BALANCE, A_CANCEL};
	public static enum ParamsType {OTHER, ACTION, P_NAME, P_NUMBER, P_EMAIL, P_SITE, P_TIME, QUOTE, Q_MARK, PREPOS, CHANGE};

	
	public static int getActionsNum() {
		return ActionType.values().length;
	}
	
	public static int getParamsNum() {
		return ParamsType.values().length;
	}
	
	
	public static int getActionID(ActionType actionType) {		
		return actionType.ordinal();
	}	
	
	public static int getActionID(String actionType) {		
		return getActionID(ActionType.valueOf(actionType.toUpperCase(Locale.ENGLISH)));
	}	
	
	public static ActionType getActionEnum(int actionID) {		
		return ActionType.values()[actionID];
	}
	
//	public static int getActionID(ActionType actionType) {
//		
//		int actionID;
//		
//		switch (actionType) {
//		case A_CALL: 	actionID = 1; break;
//		case A_SMS: 	actionID = 2; break;
//		case A_EMAIL: 	actionID = 3; break;
//		case A_SEARCH: 	actionID = 4; break;
//		case A_SITE: 	actionID = 5; break;
//		case A_ALARM: 	actionID = 6; break;
//		case A_BALANCE:	actionID = 7; break;
//		case A_CANCEL:	actionID = 8; break;
//		default: 		actionID = 0;
//		System.out.println("IdTranslator: actionType '" + actionType + "' is incorrect");
//		}
//		
//		return actionID;
//	}	
//	
//	public static int getActionID(String actionType) {		
//		return getActionID(ActionType.valueOf(actionType.toUpperCase(Locale.ENGLISH)));
//	}	
//	
//	public static ActionType getActionEnum(int actionID) {
//		
//		ActionType actionType;
//		
//		switch (actionID) {
//		case 1: 	actionType = ActionType.A_CALL; 	break;
//		case 2: 	actionType = ActionType.A_SMS; 		break;
//		case 3: 	actionType = ActionType.A_EMAIL;	break;
//		case 4: 	actionType = ActionType.A_SEARCH; 	break;
//		case 5: 	actionType = ActionType.A_SITE; 	break;
//		case 6: 	actionType = ActionType.A_ALARM; 	break;
//		case 7: 	actionType = ActionType.A_BALANCE; 	break;
//		case 8: 	actionType = ActionType.A_CANCEL; 	break;
//		default: 	actionType = null;
//		System.out.println("IdTranslator: actionID '" + actionID + "' is incorrect");
//		}
//		
//		return actionType;
//	}
	
	
	public static int getParamOrdinal(ParamsType paramType) {
		return paramType.ordinal();
	}	
	
	public static int getParamOrdinal(String paramType) {		
		return getParamOrdinal(ParamsType.valueOf(paramType.toUpperCase(Locale.ENGLISH)));
	}
		
	public static ParamsType getParamEnum(int paramID) {
		return ParamsType.values()[paramID];
	}
	
	
//	
//	public static int getParamID(ParamsType paramType) {
//		
//		int paramID;
//		
//		switch (paramType) {
//		case P_NAME: 	paramID = 1; break;
//		case P_NUMBER: 	paramID = 2; break;
//		case P_EMAIL: 	paramID = 3; break;
//		case P_SITE: 	paramID = 4; break;
//		case P_TIME: 	paramID = 5; break;
//		case QUOTE: 	paramID = 6; break;
//		default: 		paramID = 0;
//		System.out.println("IdTranslator: paramType '" + paramType + "' is incorrect");
//		}
//		
//		return paramID;
//	}	
//	
//	public static int getParamID(String paramType) {		
//		return getParamID(ParamsType.valueOf(paramType.toUpperCase(Locale.ENGLISH)));
//	}
//	
//	
//	public static ParamsType getParamEnum(int paramID) {
//		
//		ParamsType paramType;
//		
//		switch (paramID) {
//		case 1: 	paramType = ParamsType.P_NAME;	break;
//		case 2: 	paramType = ParamsType.P_NUMBER;break;
//		case 3: 	paramType = ParamsType.P_EMAIL; break;
//		case 4: 	paramType = ParamsType.P_SITE; 	break;
//		case 5: 	paramType = ParamsType.P_TIME; 	break;
//		case 6: 	paramType = ParamsType.QUOTE; 	break;
//		default: 	paramType = null;
//		System.out.println("IdTranslator: paramID '" + paramID + "' is incorrect");
//		}
//		
//		return paramType;
//	}
	

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
			case  0:	paramType = ParamsType.OTHER; 		break;
			case -1:	paramType = ParamsType.QUOTE; 		break;
			case -2:	paramType = ParamsType.Q_MARK; 	break;
			case -3:	paramType = ParamsType.PREPOS;		break;
			case -4:	paramType = ParamsType.CHANGE;		break;
			default: 	paramType = null;
			System.out.println("IdTranslator: paramID '" + paramID + "' is incorrect");
		}
	return paramType;
}
}
