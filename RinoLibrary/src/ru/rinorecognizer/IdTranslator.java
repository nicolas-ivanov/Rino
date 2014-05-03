package ru.rinorecognizer;

import java.util.Locale;

public class IdTranslator {

	public static enum LabelsType {OTHER, ACTION, P_NAME, P_NUMBER, P_EMAIL, P_SITE, P_TIME, QUOTE, Q_MARK, PREPOS, CHANGE};
	public static enum ModelsType {ACTION, A_CALL, A_SMS, A_EMAIL, A_SEARCH, A_SITE, A_ALARM, A_BALANCE, A_CANCEL};

	public static enum ActionType {ANY, A_CALL, A_SMS, A_EMAIL, A_SEARCH, A_SITE, A_ALARM, A_BALANCE, A_CANCEL};
	public static enum ParamsType {OTHER, P_NAME, P_NUMBER, P_EMAIL, P_SITE, P_TIME, QUOTE};

	

////// GetNum Section ////////////////////////////////////////////////////////////////
	
	public static int getModelsNum() {
		return ModelsType.values().length;
	}
	
	public static int getLabelsNum() {
		return LabelsType.values().length;
	}
	
	public static int getActionsNum() {
		return ActionType.values().length;
	}
	
	public static int getParamsNum() {
		return ParamsType.values().length;
	}
	
	

////// Actions Section ////////////////////////////////////////////////////////////////
	
	public static int getActionID(ActionType actionType) {
		return actionType.ordinal();
	}	
	
	public static int getActionID(String actionType) {		
		return getActionID(ActionType.valueOf(actionType.toUpperCase(Locale.ENGLISH)));
	}	
	
	public static ActionType getActionEnum(int actionID) {		
		return ActionType.values()[actionID];
	}
	


////// Params Section ////////////////////////////////////////////////////////////////
	
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
	
	

////// Labels Section ////////////////////////////////////////////////////////////////
	
	public static LabelsType getLabelEnum(int labelID) {
		return LabelsType.values()[labelID];
	}
	
	public static int getLabelOrdinal(LabelsType labelType) {
		if (labelType != null)
			return labelType.ordinal();
		else
			return 0;
	}	
	
	public static int getLabelOrdinal(String labelType) {		
		return getLabelOrdinal(LabelsType.valueOf(labelType.toUpperCase(Locale.ENGLISH)));
	}
}
