package ru.rinorecognizer;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import ru.rinorecognizer.Frame.ActionType;
import ru.rinorecognizer.Frame.ParamsType;
import ru.rinorecognizer.frames.AlarmFrame;
import ru.rinorecognizer.frames.BalanceFrame;
import ru.rinorecognizer.frames.CallFrame;
import ru.rinorecognizer.frames.EmailFrame;
import ru.rinorecognizer.frames.SearchFrame;
import ru.rinorecognizer.frames.SiteFrame;
import ru.rinorecognizer.frames.SmsFrame;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;



public class FramingTask extends AsyncTask<ExtendedCommand, String, FramingResult> {
	
	private MainActivity mainActivity;
	private MainActivity.SvmBunch svm_bunch;
	private Frame savedFrame;
	
	private Boolean debugMode = true;

	
	FramingTask(MainActivity mainActivity, MainActivity.SvmBunch svmBunch, Frame savedFrame){
		this.mainActivity = mainActivity;
		this.svm_bunch = svmBunch;
		this.savedFrame = savedFrame;
	}
	
	@Override
    protected FramingResult doInBackground(ExtendedCommand... extCommandList) 
    {    	
		ExtendedCommand extCommand = extCommandList[0];
    		
    	// step 1: detect command type
    	CommandFeaturesGetter cfGetter = new CommandFeaturesGetter(); 
    	int[] cFeatures = cfGetter.getVector(extCommand);
		int c_id = svm_bunch.svm_A.classify(cFeatures);
		
		if (c_id == extCommand.prevType)
			publishProgress(extCommand.curCommand);
		else
			publishProgress(extCommand.curCommand, "");
		
		ActionType a_type = ActionType.A_CALL;
		
		switch (c_id) {
		case 1: a_type = ActionType.A_CALL; 	break;
		case 2: a_type = ActionType.A_SMS; 		break;
		case 3: a_type = ActionType.A_EMAIL; 	break;
		case 4: a_type = ActionType.A_SEARCH; 	break;
		case 5: a_type = ActionType.A_SITE; 	break;
		case 6: a_type = ActionType.A_ALARM; 	break;
		case 7: a_type = ActionType.A_BALANCE; 	break;
		default:
				System.out.println("Command ID '" + c_id + "' is incorrect");
		}

		
		if (debugMode)
			publishProgress("Command type: " + a_type.toString().toLowerCase(Locale.US));
		
    	
    	// step 2: map each word of a command with a label to get parameters

    	WordsFeaturesGetter wfGetter = new WordsFeaturesGetter(); 
    	int[][] wFeatures = wfGetter.getVectors(extCommand.curCommand);

		List<ParamsType> labels = new ArrayList<ParamsType>();
		List<Integer> labels_id = new ArrayList<Integer>();
    	List<String> words = getWords(extCommand.curCommand);
    	
    	Frame frame = savedFrame;
    	SvmClassifier svm = null;
    	
    	
		switch (a_type) {
		case A_CALL:
			if ((frame == null) || (!frame.type.equals(a_type)))
				frame = new CallFrame(mainActivity);
			svm = svm_bunch.svm_call;
			break;
			
		case A_SMS:
			if ((frame == null) || (!frame.type.equals(a_type)))
				frame = new SmsFrame(mainActivity);
			svm = svm_bunch.svm_sms;
			break;
			
		case A_EMAIL:
			if ((frame == null) || (!frame.type.equals(a_type)))
				frame = new EmailFrame(mainActivity);
			svm = svm_bunch.svm_email;
			break;
			
		case A_SITE:
			if ((frame == null) || (!frame.type.equals(a_type)))
				frame = new SiteFrame(mainActivity);
			svm = svm_bunch.svm_site;
			break;		
		
		case A_SEARCH:
			if ((frame == null) || (!frame.type.equals(a_type)))
				frame = new SearchFrame(mainActivity);
			svm = svm_bunch.svm_search;
			break;
			
		case A_ALARM:
			if ((frame == null) || (!frame.type.equals(a_type)))
				frame = new AlarmFrame(mainActivity);
			svm = svm_bunch.svm_alarm;
			break;

		case A_BALANCE:
			if ((frame == null) || (!frame.type.equals(a_type)))
				frame = new BalanceFrame(mainActivity);
			svm = svm_bunch.svm_balance;
			break;
		}

		
		for (int i = 0; i < wFeatures.length; i++)
			labels_id.add(svm.classify(wFeatures[i]));
		
		labels = convertToEnum(labels_id);
		makeGroups(words, labels);
		FramingResult framingResult = frame.fill(words, labels);
		publishProgress(frame.getResponse());
		
    	return framingResult;
	}
    
    
    @Override
    protected void onProgressUpdate(String... answer) {
    	super.onProgressUpdate(answer);
    	
    	if (answer.length == 1)
    		mainActivity.addAnswer(answer[0]);
    	else
    		mainActivity.addRequest(answer[0]);
    }
    
    
    @Override
    protected void onPostExecute(FramingResult framingResult) {
    	Log.d(MainActivity.TAG, "AsyncTask: finished");
	    super.onPostExecute(framingResult);
	    mainActivity.endFramingTask();
	}
    
    
    private List<String> getWords(String str) 
    {
    	String[] words = str.split(" ");
    	List<String> wList = new ArrayList<String>();
    	
    	for (int i = 0; i < words.length; i++)
    		wList.add(words[i]);
    	
    	return wList;
    }

    
    private void makeGroups(List<String> words, List<ParamsType> labels) 
    {
		if (debugMode) {
			publishLabels(labels);
			group(words, labels);
			publishLabels(labels);		
		}
		else 
			group(words, labels);
    }
    
    
    private List<ParamsType> convertToEnum(List<Integer> p_type_id)
    {
		List<ParamsType> p_type = new ArrayList<ParamsType>();
		
    	for (int i = 0; i < p_type_id.size(); i++)
    		switch (p_type_id.get(i)) {
    		case 10: p_type.add(ParamsType.ACTION); 	break;
    		case  1: p_type.add(ParamsType.P_NAME); 	break;
    		case  2: p_type.add(ParamsType.P_NUMBER); 	break;
    		case  3: p_type.add(ParamsType.P_EMAIL); 	break;
    		case  4: p_type.add(ParamsType.P_SITE); 	break;
    		case  5: p_type.add(ParamsType.P_TIME);		break;
    		case  0: p_type.add(ParamsType.OTHER); 		break;
    		case -1: p_type.add(ParamsType.QUOTE); 		break;
    		case -2: p_type.add(ParamsType.Q_MARK); 	break;
    		case -3: p_type.add(ParamsType.PREPOS);		break;
    		case -4: p_type.add(ParamsType.CHANGE);		break;
    		default:
    				System.out.println("Parameter's ID '" + p_type_id.get(i) + "' is incorrect");
    		}
    	
    	return p_type;
    }
    
    
    private void publishLabels(List<ParamsType>labels)
    {
		String labels_str = "Labels:" + System.getProperty("line.separator");
		for (int i = 0; i < labels.size(); i++)
			labels_str += " " + labels.get(i).toString();
		
		publishProgress(labels_str.toLowerCase(Locale.US));
    }
	
    private void group(List<String> words, List<ParamsType> labels)
    {
    	String prevWord = "";
		ParamsType prevLabel = null;
		
    	String currWord = null;
		ParamsType currLabel = null;
		
		ListIterator<String> wordsIterator = words.listIterator();
		ListIterator<ParamsType> labelsIterator = labels.listIterator();
		
		while (wordsIterator.hasNext()) {
			currWord = wordsIterator.next();
			currLabel = labelsIterator.next();
			
			currWord = swapSpecialWords(currWord, currLabel);
			wordsIterator.set(currWord);
			
			if (currLabel == prevLabel)
			{
				wordsIterator.previous();
				wordsIterator.previous();
				prevWord += " " + currWord;
				wordsIterator.set(prevWord);
				wordsIterator.next();
				wordsIterator.next();
				wordsIterator.remove();
				labelsIterator.remove();
			}
			else {
				prevWord = currWord;
				prevLabel = currLabel;
			}
		}		
    }
    
    private String swapSpecialWords(String word, ParamsType label)
    {
    	String resWord = word;
    	
		switch (label) {
		
		case P_NAME: 	
			resWord = word.substring(0,1).toUpperCase(new Locale("ru","RU")) + word.substring(1, word.length() - 1);
			break;
			
		case P_NUMBER:
			if (word.equals("плюс"))			resWord = "+";
			else if (word.equals("звездочка"))	resWord = "*";
			else if (word.equals("решетка"))	resWord = Uri.encode("#");

			else if (word.equals("ноль"))	resWord = "0";
			else if (word.equals("один"))	resWord = "1";
			else if (word.equals("два"))	resWord = "2";
			else if (word.equals("три"))	resWord = "3";
			else if (word.equals("четыре"))	resWord = "4";
			else if (word.equals("пять"))	resWord = "5";
			else if (word.equals("шесть"))	resWord = "6";
			else if (word.equals("семь"))	resWord = "7";
			else if (word.equals("восемь"))	resWord = "8";
			else if (word.equals("девять"))	resWord = "9";
			
			break;
			
			
		case P_EMAIL: 	
			if (word.equals("собака"))		resWord = "@";
			else if (word.equals("точка"))	resWord = ".";
			else if (word.equals("дот"))	resWord = ".";
			
			else if (word.equals("ру"))		resWord = "ru";
			else if (word.equals("ком"))	resWord = "com";
			else if (word.equals("орг"))	resWord = "org";
			else if (word.equals("су"))		resWord = "su";
			
			break;
			
			
		case P_SITE: 	
			if	(word.matches("википед\\w+") 		|| word.equals("wikipedia") 	||
				 word.equals("вики") 				|| word.equals("wiki"))			resWord = "ru.wikipedia.org";
			else if (word.matches("яндекс\\w*") 	|| word.equals("yandex"))		resWord = "yandex.ru";
			else if (word.matches("гугл\\w*") 		|| word.equals("google"))		resWord = "google.ru";
			else if (word.matches("рамблер\\w*") 	|| word.equals("rambler"))		resWord = "rambler.ru";
			
			else if (word.equals("джимейл") 		|| word.equals("gmail"))		resWord = "gmail.com";
			else if (word.equals("майл") 			|| word.equals("mail"))			resWord = "mail.ru";
			
			else if (word.matches("интернет\\w*"))	resWord = "yandex.ru";
			
			break;
		default:
		}
		
		return resWord;
	}

}