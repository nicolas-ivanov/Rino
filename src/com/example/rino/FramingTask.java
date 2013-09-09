package com.example.rino;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.AlarmClock;
import android.provider.ContactsContract;
import android.util.Log;


public class FramingTask extends AsyncTask<String, String, Intent> {

	public static enum ActionType {A_CALL, A_SMS, A_SITE, A_EMAIL, A_SEARCH, A_ALARM, A_BALANCE};
	public static enum ParamsType {ACTION, P_NAME, P_NUMBER, P_EMAIL, P_SITE, P_TIME, OTHER, QUOTE, Q_MARK, PREPOS};
	
	private MainActivity mainActivity;
	private MainActivity.SvmBunch svm_bunch;

	
	FramingTask(MainActivity main, MainActivity.SvmBunch bunch){
		mainActivity = main;
		svm_bunch = bunch;
	}
	
	
	
	private class CallFrame {
		private Uri numUri;
		
		public CallFrame() {
			numUri = null;
		}
		
		public Intent fill(List<String> wgroups, List<ParamsType> labels)
		{			
			for (int i = 0; i < wgroups.size(); i++)
				switch (labels.get(i)) {
				case P_NAME: 
					numUri = Uri.parse("tel:" + getPhoneNumber(wgroups.get(i), mainActivity));
					break;
					
				case P_NUMBER:
					numUri = Uri.parse("tel:" + wgroups.get(i));
					break;
					
				default: break;
				}

			return new Intent(Intent.ACTION_CALL, numUri);
		}		
	}	
	
	
	private class SmsFrame {
		private Uri numUri;
		private String text;
		
		public SmsFrame() {
			numUri = null;
			text = "";
		}
		
		public Intent fill(List<String> wgroups, List<ParamsType> labels)
		{			
			for (int i = 0; i < wgroups.size(); i++)
				switch (labels.get(i)) {
				case P_NAME: 
					numUri = Uri.parse("smsto:" + getPhoneNumber(wgroups.get(i), mainActivity));
					break;
					
				case P_NUMBER: 
					numUri = Uri.parse("smsto:" + wgroups.get(i));
					break;
					
				case QUOTE: 
					text = wgroups.get(i);
					break;
					
				default: break;
				}
			
			Intent resIntent = new Intent(Intent.ACTION_SENDTO, numUri);
			resIntent.putExtra("sms_body", text);
			return resIntent;
		}
		
		private boolean check() {
			return false;
		}
	}
	
	
	private class EmailFrame {
		private Uri emailUri;
		private String text;
		
		public EmailFrame() {
			emailUri = null;
			text = "";
		}
		
		public Intent fill(List<String> wgroups, List<ParamsType> labels)
		{			
			for (int i = 0; i < wgroups.size(); i++)
				switch (labels.get(i)) {
				case P_NAME: 
					emailUri = Uri.parse("mailto:" + getEmail(wgroups.get(i), mainActivity));
//					emailUri = Uri.parse("mailto:" + wgroups.get(i));
					break;
					
				case QUOTE: 
					text = wgroups.get(i);
					break;
					
				default: break;
				}

			Intent resIntent = new Intent(Intent.ACTION_SENDTO, emailUri);			
			resIntent.putExtra(Intent.EXTRA_SUBJECT, "Hello from Rino");
			resIntent.putExtra(Intent.EXTRA_TEXT, text + "\n\nBest regards,\nRinoRecognizer");
			return resIntent;
		}
	}	
	
	
	private class SiteFrame {
		private Uri wwwUri;
		
		public SiteFrame() {
			wwwUri = null;
		}
		
		public Intent fill(List<String> wgroups, List<ParamsType> labels)
		{			
			for (int i = 0; i < wgroups.size(); i++)
				switch (labels.get(i)) {
				case P_SITE: 
					wwwUri = Uri.parse("http://www." + wgroups.get(i));
					break;
					
				default: break;
				}
			
			return new Intent(Intent.ACTION_VIEW, wwwUri);
		}
	}	
	
	private class SearchFrame {
		private Uri wwwUri;
		private String text;
		
		public SearchFrame() {
			wwwUri = Uri.parse("http://yandex.ru/yandsearch?text=");
			text = "";
		}
		
		public Intent fill(List<String> wgroups, List<ParamsType> labels)
		{			
			for (int i = 0; i < wgroups.size(); i++)
				switch (labels.get(i)) {
				case P_SITE: 
					wwwUri = Uri.parse("http://yandex.ru/yandsearch?text=");
					break;
					
				case QUOTE: 
					text = wgroups.get(i);
					break;
					
				default: break;
				}
			
			return new Intent(Intent.ACTION_VIEW, Uri.parse(wwwUri.getPath() + text));
		}
	}
	
	
	private class AlarmFrame {
		private Integer hour;
		private Integer minutes;
		
		public AlarmFrame() {
			hour = 12;
			minutes = 0;
		}
		
		public Intent fill(List<String> wgroups, List<ParamsType> labels)
		{			
			for (int i = 0; i < wgroups.size(); i++)
				switch (labels.get(i)) {
				case P_TIME: 
					
					break;	
					
				default: break;
				}
			
			Intent resIntent = new Intent(AlarmClock.ACTION_SET_ALARM);
			resIntent.putExtra(AlarmClock.EXTRA_HOUR, hour);
			resIntent.putExtra(AlarmClock.EXTRA_MINUTES, minutes);
			resIntent.putExtra(AlarmClock.EXTRA_MESSAGE,"Rino alarm");
			
			return resIntent;
		}
	}
	
    
	
	
    @Override
    protected Intent doInBackground(String... str_list) 
    {    	
    	// step 1: detect command type
    	String request = str_list[0];
    	
    	CommandFeaturesGetter cfGetter = new CommandFeaturesGetter(mainActivity); 
    	int[] cFeatures = cfGetter.getParams(request);
		int c_id = svm_bunch.svm_A.classify(cFeatures);
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
		publishProgress("Command type: " + a_type.toString().toLowerCase());
		
    	
    	// step 2: map each word of a command with a label to get parameters

    	WordsFeaturesGetter wfGetter = new WordsFeaturesGetter(mainActivity); 
    	int[][] wFeatures = wfGetter.getParams(request);

		List<ParamsType> labels = new ArrayList<ParamsType>();
		List<Integer> labels_id = new ArrayList<Integer>();
    	List<String> words = getWords(request);
    	Intent resIntent = null;
		
		switch (a_type) {
		case A_CALL:
			for (int i = 0; i < wFeatures.length; i++)
				labels_id.add(svm_bunch.svm_call.classify(wFeatures[i]));
			
			labels = convertToEnum(labels_id);
			publishLabels(labels);
			makeGroups(words, labels);
			publishLabels(labels);
			
			CallFrame callFrame = new CallFrame();
			resIntent = callFrame.fill(words, labels);
			break;
			
			
		case A_SMS:
			for (int i = 0; i < wFeatures.length; i++)
				labels_id.add(svm_bunch.svm_sms.classify(wFeatures[i]));
			
			labels = convertToEnum(labels_id);
			publishLabels(labels);	
			makeGroups(words, labels);
			publishLabels(labels);
			
			SmsFrame smsFrame = new SmsFrame();
			resIntent = smsFrame.fill(words, labels);
			break;
			
			
		case A_EMAIL:
			for (int i = 0; i < wFeatures.length; i++)
				labels_id.add(svm_bunch.svm_email.classify(wFeatures[i]));
			
			labels = convertToEnum(labels_id);
			publishLabels(labels);
			makeGroups(words, labels);
			publishLabels(labels);
			
			EmailFrame emailFrame = new EmailFrame();
			resIntent = emailFrame.fill(words, labels);
			
			break;
			
			
		case A_SITE:
			for (int i = 0; i < wFeatures.length; i++)
				labels_id.add(svm_bunch.svm_site.classify(wFeatures[i]));
			
			labels = convertToEnum(labels_id);
			publishLabels(labels);
			makeGroups(words, labels);
			publishLabels(labels);
			
			SiteFrame siteFrame = new SiteFrame();
			resIntent = siteFrame.fill(words, labels);

			break;
		
		
		case A_SEARCH:
			for (int i = 0; i < wFeatures.length; i++)
				labels_id.add(svm_bunch.svm_search.classify(wFeatures[i]));
			
			labels = convertToEnum(labels_id);
			publishLabels(labels);
			makeGroups(words, labels);
			publishLabels(labels);

			// step 3: form Intent
			SearchFrame searchFrame = new SearchFrame();			
			resIntent = searchFrame.fill(words, labels);
			break;
			
			
		case A_ALARM:
			for (int i = 0; i < wFeatures.length; i++)
				labels_id.add(svm_bunch.svm_alarm.classify(wFeatures[i]));
			
			labels = convertToEnum(labels_id);
			publishLabels(labels);
			makeGroups(words, labels);
			publishLabels(labels);

			// step 3: form Intent
			AlarmFrame alarmFrame = new AlarmFrame();			
			resIntent = alarmFrame.fill(words, labels);
			break;

			
		case A_BALANCE:
			// step 3: form Intent
			String ussd = "*100" + Uri.encode("#");
			Uri numUri = Uri.parse("tel:" + ussd);				
			resIntent = new Intent(android.content.Intent.ACTION_CALL, numUri);
			break;
			
		}
    	
    	return resIntent;
	}
    
    
    @Override
    protected void onProgressUpdate(String... answer) {
    	super.onProgressUpdate(answer);
  		mainActivity.addAnswer(answer[0]);
    }
    
    
    @Override
    protected void onPostExecute(Intent res) {
    	Log.d(MainActivity.TAG, "AsyncTask: finished");
	    super.onPostExecute(res);
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
    		case  5: p_type.add(ParamsType.P_TIME); 	break;
    		case  0: p_type.add(ParamsType.OTHER); 		break;
    		case -1: p_type.add(ParamsType.QUOTE); 		break;
    		case -2: p_type.add(ParamsType.Q_MARK); 	break;
    		case -3: p_type.add(ParamsType.PREPOS);		break;
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
		
		publishProgress(labels_str.toLowerCase());
    }
	
    private void makeGroups(List<String> words, List<ParamsType> labels)
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
			resWord = word.substring(0,1).toUpperCase() + word.substring(1, word.length() - 1);
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
			else if (word.matches("яндекс\\w+") 	|| word.equals("yandex"))		resWord = "yandex.ru";
			else if (word.matches("гугл\\w+") 		|| word.equals("google"))		resWord = "google.ru";
			else if (word.matches("рамблер\\w+") 	|| word.equals("rambler"))		resWord = "rambler.ru";
			
			else if (word.equals("джимейл") 		|| word.equals("gmail"))		resWord = "gmail.com";
			else if (word.equals("майл") 			|| word.equals("mail"))			resWord = "mail.ru";
			
			else if (word.matches("интернет\\w+"))	resWord = "yandex.ru";
			
			break;
		default:
		}
		
		return resWord;
    }
    
    
	public String getPhoneNumber(String name, Context context) 
	{
		String numStr = null;
		String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like'%" + name +"%'";
		String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER };
		
		Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
				projection, selection, null, null);
		
		if (c.moveToFirst()) {
		    numStr = c.getString(0);
		}
		c.close();

		return numStr;
	}    
    
	public String getEmail(String name, Context context) 
	{
		String emailStr = null;
		String selection = ContactsContract.CommonDataKinds.Email.DISPLAY_NAME + " like'%" + name +"%'";
		String[] projection = new String[] { ContactsContract.CommonDataKinds.Email.ADDRESS };
		
		Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
				projection, selection, null, null);
		
		if (c.moveToFirst()) {
		    emailStr = c.getString(0);
		}
		c.close();

		return emailStr;
	}
	
    
	public Cursor getContactInfo(String name, Context context) 
	{
		String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like'%" + name +"%'";
		String[] projection = new String[] { 
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.NUMBER,
				ContactsContract.CommonDataKinds.Email.ADDRESS // doesn't work
			};
		
		Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				projection, selection, null, null);
		
		return c;
	}
	
	
	
	
	
}