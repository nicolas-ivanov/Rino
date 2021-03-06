package ru.rinorecognizer;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import ru.rinorecognizer.IdTranslator.ActionType;
import ru.rinorecognizer.IdTranslator.LabelsType;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;



public class FramingTask extends AsyncTask<ExtendedCommand, String, Frame> {
	
	private MainActivity mainActivity;
	private ArrayList<SvmClassifier> svmList;
	private Frame savedFrame;
	
	private Boolean debugMode = true;

	
	FramingTask(MainActivity mainActivity, ArrayList<SvmClassifier> svmList, Frame savedFrame)
	{
		this.mainActivity = mainActivity;
		this.svmList = svmList;
		this.savedFrame = savedFrame;
	}
	
		@Override
	    protected Frame doInBackground(ExtendedCommand... extCommandList) 
	    {    	
			ExtendedCommand extCommand = extCommandList[0];
	    		
	    	// step 1: detect command type
	    	CommandFeaturesGetter cfGetter = new CommandFeaturesGetter(); 
	    	float[] cFeatures = cfGetter.getVector(extCommand);
	    	
	    	SvmClassifier svmAction = svmList.get(IdTranslator.ModelsType.ACTION.ordinal());
			int actionID = svmAction.classify(cFeatures);
			
			
			if (actionID == extCommand.prevType)
				publishProgress(extCommand.curCommand);
			else
				publishProgress(extCommand.curCommand, ""); // "" is needed to distinguish addAnswer() and addRequest() cases
			
			
			ActionType a_type = IdTranslator.getActionEnum(actionID);
			
			if (debugMode)
				publishProgress(a_type.toString().toLowerCase(Locale.US));
			
	    	
	    	// step 2: map each word of a command with a label to get parameters
	
	    	WordsFeaturesGetter wfGetter = new WordsFeaturesGetter(); 
	    	float[][] wFeatures = wfGetter.getVectors(extCommand);
	
			List<LabelsType> labels = new ArrayList<LabelsType>();
			List<Integer> labels_id_list = new ArrayList<Integer>();
	    	List<String> words = getWords(extCommand.curCommand);
	    	
	    	
	    	SvmClassifier svm = svmList.get(actionID);
	    	Frame frame = savedFrame;
	
			if ((frame == null) || (!frame.type.equals(a_type))) {
				frame = FrameTranslator.getProperActionFrame(mainActivity, a_type);
			}

			
			int saved_label_id = 0;
			int saved_label_ordinal = 0;
			int startOfPrevLabelBlock = wFeatures[0].length - IdTranslator.getLabelsNum();
			
			for (int i = 0; i < wFeatures.length; i++) {
				 // add the label id of the previous word at the end of feature vector 
				wFeatures[i][startOfPrevLabelBlock + saved_label_ordinal] = 1;
	//			float curFeatures[] = wFeatures[i]; // for debug
				saved_label_id = svm.classify(wFeatures[i]);
				labels_id_list.add(saved_label_id);
//				saved_label_ordinal = IdTranslator.getParamOrdinal(IdTranslator.getParamEnumFromID(saved_label_id));
				saved_label_ordinal = saved_label_id;
			}
			
			labels = convertToEnum(labels_id_list);
			makeGroups(words, labels);
			Frame resultFrame = frame.fill(words, labels);
			publishProgress(resultFrame.getResponse());
			
	    	return resultFrame;
		}
    
    
    @Override
    protected void onProgressUpdate(String... answer) 
    {
    	super.onProgressUpdate(answer);
    	
    	if (answer.length == 1)
    		mainActivity.addAnswer(answer[0]);
    	else
    		mainActivity.addRequest(answer[0]);
    }
    
    
    @Override
    protected void onPostExecute(Frame resultFrame) 
    {
    	Log.d(MainActivity.TAG, "AsyncTask: finished");
	    super.onPostExecute(resultFrame);
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

    
    private void makeGroups(List<String> words, List<LabelsType> labels) 
    {
		if (debugMode) {
			publishLabels(labels);
			group(words, labels);
			publishLabels(labels);		
		}
		else 
			group(words, labels);
    }
    
    
    private List<LabelsType> convertToEnum(List<Integer> p_type_id)
    {
		List<LabelsType> p_type = new ArrayList<LabelsType>();
		
    	for (int i = 0; i < p_type_id.size(); i++) {
//    		p_type.add(IdTranslator.getParamEnum(p_type_id.get(i)));
    		p_type.add(IdTranslator.getLabelEnum(p_type_id.get(i)));
    	}
    	
    	return p_type;
    }
    
    
    private void publishLabels(List<LabelsType>labels)
    {
		String labels_str = "";
		for (int i = 0; i < labels.size(); i++)
			labels_str += labels.get(i).toString() + " ";
		
		publishProgress(labels_str.toLowerCase(Locale.US));
    }
	
    private void group(List<String> words, List<LabelsType> labels)
    {
    	String prevWord = "";
    	LabelsType prevLabel = null;
		
    	String currWord = null;
    	LabelsType currLabel = null;
		
		ListIterator<String> wordsIterator = words.listIterator();
		ListIterator<LabelsType> labelsIterator = labels.listIterator();
		
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
    
    private String swapSpecialWords(String word, LabelsType label)
    {
    	String resWord = word;
    	
		switch (label) {
		
		case P_NAME: 	
			resWord = word.substring(0,1).toUpperCase(new Locale("ru","RU")) + word.substring(1, word.length() - 1);
			break;
			
		case P_NUMBER:
		case P_TIME:
			if (word.equals("плюс"))				resWord = "+";
			else if (word.equals("звездочка"))		resWord = "*";
			else if (word.equals("решетка"))		resWord = Uri.encode("#");

			else if (word.equals("ноль"))			resWord = "0";
			else if (word.equals("один"))			resWord = "1";
			else if (word.equals("два"))			resWord = "2";
			else if (word.equals("три"))			resWord = "3";
			else if (word.equals("четыре"))			resWord = "4";
			else if (word.equals("пять"))			resWord = "5";
			else if (word.equals("шесть"))			resWord = "6";
			else if (word.equals("семь"))			resWord = "7";
			else if (word.equals("восемь"))			resWord = "8";
			else if (word.equals("девять"))			resWord = "9";
			
			else if (word.equals("десять"))			resWord = "10";
			else if (word.equals("одиннадцать"))	resWord = "11";
			else if (word.equals("двенадцать"))		resWord = "12";
			else if (word.equals("тринадцать"))		resWord = "13";
			else if (word.equals("четырнадцать"))	resWord = "14";
			else if (word.equals("пятнадцать"))		resWord = "15";
			else if (word.equals("шестнадцать"))	resWord = "16";
			else if (word.equals("семнадцать"))		resWord = "17";
			else if (word.equals("восемнадцать"))	resWord = "18";
			else if (word.equals("девятнадцать"))	resWord = "19";
			
			else if (word.equals("двадцать"))		resWord = "20";
			else if (word.equals("тридцать"))		resWord = "30";
			else if (word.equals("сорок"))			resWord = "40";
			else if (word.equals("пятьдесят"))		resWord = "50";
			else if (word.equals("шестьдесят"))		resWord = "60";
			else if (word.equals("семьдесят"))		resWord = "70";
			else if (word.equals("восемьдесят"))	resWord = "80";
			else if (word.equals("девяносто"))		resWord = "90";
			
			else if (word.equals("сто"))			resWord = "100";
			else if (word.equals("двести"))			resWord = "200";
			else if (word.equals("триста"))			resWord = "300";
			else if (word.equals("четыреста"))		resWord = "400";
			else if (word.equals("пятьсот"))		resWord = "500";
			else if (word.equals("шестьсот"))		resWord = "600";
			else if (word.equals("семьсот"))		resWord = "700";
			else if (word.equals("восемьсот"))		resWord = "800";
			else if (word.equals("десятьсот"))		resWord = "900";
			
			else if (word.equals("тысяча"))		resWord = "1000";
			
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
			else if (word.matches("рамблер\\w*") 	|| word.equals("rambler"))		resWord = "nova.rambler.ru";
			
			else if (word.equals("джимейл") 		|| word.equals("gmail"))		resWord = "gmail.com";
			else if (word.equals("майл") 			|| word.equals("mail"))			resWord = "mail.ru";
			
			else if (word.equals("вконтакте") 		|| word.equals("vkontakte"))	resWord = "vk.com";
			else if (word.equals("фэйсбук") 		|| word.equals("facebook"))		resWord = "facebook.com";
			
			else if (word.matches("интернет\\w*"))	resWord = "yandex.ru";
			
			break;
		default:
		}
		
		return resWord;
	}

}