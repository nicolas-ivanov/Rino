package com.example.rino;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.AlarmClock;
import android.util.Log;


public class FramingTask extends AsyncTask<String, String, Intent> {

	public static enum ActionType {a_call, a_sms, a_site, a_email, a_look, a_alarm, a_balance};
	public static enum ParamsType {action, p_name, p_number, p_email, p_site, p_time, other, quote, q_mark};
	
	private class CallFrame {
		private Uri numUri;
		
		public CallFrame() {
			numUri = null;
		}
		
		public void fill(List<String> wgroups, List<ParamsType> labels)
		{			
			for (int i = 0; i < wgroups.size(); i++)
				switch (labels.get(i)) {
				case p_name: 
					//numUri = getContact;
					break;
					
				case p_number: 
					numUri = Uri.parse("tel:" + wgroups.get(i));
					break;
					
				default: break;
				}
		}
	}	
	
	private class SmsFrame {
		private Uri numUri;
		private String text;
		
		public SmsFrame() {
			numUri = null;
		}
		
		public void fill(List<String> wgroups, List<ParamsType> labels)
		{			
			for (int i = 0; i < wgroups.size(); i++)
				switch (labels.get(i)) {
				case p_name: 
					//numUri = getContact;
					break;
					
				case p_number: 
					numUri = Uri.parse("tel:" + wgroups.get(i));
					break;
					
				case quote: 
					text = wgroups.get(i);
					break;
					
				default: break;
				}
		}
	}
	
	private class EmailFrame {
		private Uri emailUri;
		private String text;
		
		public EmailFrame() {
			emailUri = null;
		}
		
		public void fill(List<String> wgroups, List<ParamsType> labels)
		{			
			for (int i = 0; i < wgroups.size(); i++)
				switch (labels.get(i)) {
				case p_name: 
					//numUri = getContact;
					break;
					
				case p_number: 
					emailUri = Uri.parse("mailto:" + wgroups.get(i));
					break;
					
				case quote: 
					text = wgroups.get(i);
					break;
					
				default: break;
				}
		}
	}	
	
	private class SiteFrame {
		private Uri wwwUri;
		
		public SiteFrame() {
			wwwUri = null;
		}
		
		public void fill(List<String> wgroups, List<ParamsType> labels)
		{			
			for (int i = 0; i < wgroups.size(); i++)
				switch (labels.get(i)) {
				case p_site: 
					wwwUri = Uri.parse("http://www." + wgroups.get(i));
					break;
					
				default: break;
				}
		}
	}	
	
	private class LookFrame {
		private Uri wwwUri;
		private String text;
		
		public LookFrame() {
			wwwUri = null;
		}
		
		public Intent fill(List<String> wgroups, List<ParamsType> labels)
		{			
			for (int i = 0; i < wgroups.size(); i++)
				switch (labels.get(i)) {
				case p_site: 
					wwwUri = Uri.parse("http://yandex.ru/yandsearch?text=");
					break;
					
				case quote: 
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
				case p_time: 
					
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
	
	
	private MainActivity mainActivity;
	private MainActivity.SvmBunch svm_bunch;
	private String nl;

	
	FramingTask(MainActivity main, MainActivity.SvmBunch bunch){
		mainActivity = main;
		svm_bunch = bunch;
		nl = System.getProperty("line.separator");
	}

    
    @Override
    protected Intent doInBackground(String... str_list) 
    {    	
    	// step 1: detect command type
    	String request = str_list[0];
    	
    	CommandFeaturesGetter cfGetter = new CommandFeaturesGetter(mainActivity); 
    	double[] cFeatures = cfGetter.getParams(request);
		int c_id = svm_bunch.svm_A.classify(cFeatures);
		ActionType a_type = ActionType.a_call;
		
		switch (c_id) {
		case 1: a_type = ActionType.a_call; break;
		case 2: a_type = ActionType.a_sms; break;
		case 3: a_type = ActionType.a_email; break;
		case 4: a_type = ActionType.a_look; break;
		case 5: a_type = ActionType.a_site; break;
		case 6: a_type = ActionType.a_balance; break;
		case 7: a_type = ActionType.a_alarm; break;
		default:
				System.out.println("Command ID '" + c_id + "' is incorrect");
		}
		publishProgress("Command type: " + a_type.toString());
		
    	
    	// step 2: map each word of a command with a label to get parameters

    	WordsFeaturesGetter wfGetter = new WordsFeaturesGetter(mainActivity); 
    	double[][] wFeatures = wfGetter.getParams(request);

		List<ParamsType> labels = new ArrayList<ParamsType>();
		List<Integer> labels_id = new ArrayList<Integer>();
    	List<String> words = getWords(request);
    	Intent resIntent = null;
		
		switch (a_type) {
		case a_call:
			for (int i = 0; i < wFeatures.length; i++)
				labels_id.add(svm_bunch.svm_call.classify(wFeatures[i]));
			
			labels = convertToEnum(labels_id);
			publish(labels);
			makeGroups(words, labels);
			publish(labels);
			
			CallFrame callFrame = new CallFrame();
			callFrame.fill(words, labels);
			
			// step 3: form Intent
			resIntent = new Intent(android.content.Intent.ACTION_CALL, callFrame.numUri);
			break;
			
			
		case a_sms:
			for (int i = 0; i < wFeatures.length; i++)
				labels_id.add(svm_bunch.svm_sms.classify(wFeatures[i]));
			
			labels = convertToEnum(labels_id);
			publish(labels);	
			makeGroups(words, labels);
			publish(labels);
			
			SmsFrame smsFrame = new SmsFrame();
			smsFrame.fill(words, labels);
			
			// step 3: form Intent
			resIntent = new Intent(Intent.ACTION_SENDTO, smsFrame.numUri);
			resIntent.putExtra("sms_body", smsFrame.text);
			break;
			
			
		case a_email:
			for (int i = 0; i < wFeatures.length; i++)
				labels_id.add(svm_bunch.svm_email.classify(wFeatures[i]));
			
			labels = convertToEnum(labels_id);
			publish(labels);
			makeGroups(words, labels);
			publish(labels);
			
			EmailFrame emailFrame = new EmailFrame();
			emailFrame.fill(words, labels);
			
			// step 3: form Intent
			resIntent = new Intent(Intent.ACTION_SENDTO, emailFrame.emailUri);			
			resIntent.putExtra(Intent.EXTRA_SUBJECT, "Hello from Rino");
			resIntent.putExtra(Intent.EXTRA_TEXT, "This is a sample message." 
					+ nl + emailFrame.text + nl	+ "Best regards," + nl + "Rino");
			break;
			
			
		case a_site:
			for (int i = 0; i < wFeatures.length; i++)
				labels_id.add(svm_bunch.svm_site.classify(wFeatures[i]));
			
			labels = convertToEnum(labels_id);
			publish(labels);
			makeGroups(words, labels);
			publish(labels);
			
			SiteFrame siteFrame = new SiteFrame();
			siteFrame.fill(words, labels);
			
			// step 3: form Intent
			resIntent = new Intent(Intent.ACTION_VIEW, siteFrame.wwwUri);
			break;
		
		
		case a_look:
			for (int i = 0; i < wFeatures.length; i++)
				labels_id.add(svm_bunch.svm_look.classify(wFeatures[i]));
			
			labels = convertToEnum(labels_id);
			publish(labels);
			makeGroups(words, labels);
			publish(labels);

			// step 3: form Intent
			LookFrame lookFrame = new LookFrame();			
			resIntent = lookFrame.fill(words, labels);
			break;
			
			
		case a_alarm:
			for (int i = 0; i < wFeatures.length; i++)
				labels_id.add(svm_bunch.svm_alarm.classify(wFeatures[i]));
			
			labels = convertToEnum(labels_id);
			publish(labels);
			makeGroups(words, labels);
			publish(labels);

			// step 3: form Intent
			AlarmFrame alarmFrame = new AlarmFrame();			
			resIntent = alarmFrame.fill(words, labels);
			break;

			
		case a_balance:
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
    		case 10: p_type.add(ParamsType.action); 	break;
    		case  1: p_type.add(ParamsType.p_name); 	break;
    		case  2: p_type.add(ParamsType.p_number); 	break;
    		case  3: p_type.add(ParamsType.p_email); 	break;
    		case  4: p_type.add(ParamsType.p_site); 	break;
    		case  5: p_type.add(ParamsType.p_time); 	break;
    		case  0: p_type.add(ParamsType.other); 		break;
    		case -1: p_type.add(ParamsType.quote); 		break;
    		case -2: p_type.add(ParamsType.q_mark); 	break;
    		default:
    				System.out.println("Parameter's ID '" + p_type_id.get(i) + "' is incorrect");
    		}
    	
    	return p_type;
    }
    
    private void publish(List<ParamsType>labels)
    {
		String labels_str = "Labels:" + System.getProperty("line.separator");
		for (int i = 0; i < labels.size(); i++)
			labels_str += " " + labels.get(i).toString();
		
		publishProgress(labels_str);
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
    
}