package com.example.rino;

import java.io.InputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class MainActivity extends Activity implements OnClickListener {

	public static final int VOICE_RECOGNITION_REQUEST_CODE = 11;
	public static final int SUB_ACTIVITY_REQUEST_CODE = 12;

	public static final String TAG = "Rino";
	public static final String HMM = "hmmModel";
	private ArrayList<String> dialogList;
	private ArrayList<String> newPhraseList;
	private ArrayList<String> commands;
	
	private Button speakButton;
	private Button textButton;
	private EditText textField;
	private ProgressBar progress;
	private TextView coloredTextView;
	private ListView dialogListView;

	private CommandAnalyser analyserTask;
	private TaggerTask taggerTask;
	private PackageManager packageManager;
	private InputMethodManager inputManager;
	private DialogDBHelper dialogDBHelper;
	
	private File hmmModel;
	private HmmClassifier hmmClassifier;
	private InputStream trainDataStream;
	
	static public class Token {
		String lexem;
		Integer label;
	};
	

//////////////// Common Methods //////////////////////////////////////////////////////////////////////
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(MainActivity.TAG, this.getLocalClassName() + ": created");
		
		setContentView(R.layout.activity_main);
		speakButton = (Button) findViewById(R.id.speak_button);
		progress = (ProgressBar) findViewById(R.id.progressBar);
		textField = (EditText) findViewById(R.id.text_field);
		textButton = (Button) findViewById(R.id.text_button);
		coloredTextView = (TextView) findViewById(R.id.colored_textview);
		coloredTextView.setVisibility(View.GONE);
		dialogListView = (ListView) findViewById(R.id.history_list);

		inputManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
		
		dialogDBHelper = new DialogDBHelper(this);
		dialogList = dialogDBHelper.getDialogHistory();
		newPhraseList = new ArrayList<String>();
		dialogListView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dialogList));

 		hmmModel = getHmmModelFile(HMM);
		hmmClassifier = new HmmClassifier();
		hmmClassifier.load(hmmModel);
		
		trainDataStream = getApplicationContext().getResources().openRawResource(R.raw.train);
		hmmClassifier.train(trainDataStream);
		
		// Check to see if a recognition activity is present
		packageManager = getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0) {
			speakButton.setOnClickListener(this);
		} else {
			speakButton.setEnabled(false);
			speakButton.setText("@string/recognizer_is_absent");
		}
		
		textButton.setOnClickListener(this);
		textField.setOnClickListener(this);
	}

	
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, this.getLocalClassName() + ": on pause");
		dialogDBHelper.saveDialogHistory(newPhraseList);
		newPhraseList.clear();
		hmmClassifier.save(hmmModel);
	}
	
	
	public void onClick(View v) {
		if (v.getId() == R.id.speak_button) {
			startVoiceRecognitionActivity();
			hideSoftKeyboard();
		} 
		else if (v.getId() == R.id.text_button) {
			String str = textField.getText().toString();
//			startCommandAnalysing(str);
			startTaggerTask(str);
			textField.setText("");
			hideSoftKeyboard();
		}
		else if (v.getId() == R.id.text_field) {
			showSoftKeyboard(textField);
		}
	}

	  
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, this.getLocalClassName() + ": got result, requestCode="
				+ requestCode + ", resultCode=" + resultCode);

		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
			switch (resultCode) {
			case RESULT_OK:
				// Fill the list view with the strings the recognizer thought it could have heard
				commands = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				String command = commands.get(0);
				Log.d(TAG, this.getLocalClassName() + ": res = '" + command + "'");
//				startCommandAnalysing(command);
				startTaggerTask(command);
				break;
				
			case RESULT_CANCELED:
				addAnswer(getStr(R.string.recognition_is_cancelled));
		        break;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
//////////////// Voice Recognition Activity ////////////////////////////////////////////////////////
	
	private void startVoiceRecognitionActivity() {		
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
		
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);				
	}
	
	
////////////////Dialog List Updating //////////////////////////////////////////////////////////////

	public void addRequest(String request) {
		newPhraseList.add(0, request);
		dialogList.add(0, request);
		
		dialogListView.setAdapter(new ArrayAdapter<String>(this,
			android.R.layout.simple_list_item_1, dialogList));
	}
	
	/*	public void addRequest(Token request) {
		newPhraseList.add(0, request.lexem);
		dialogList.add(0, request.lexem);
		
		TextView tv = new TextView(this);
		tv.setText(request.lexem);
		
		int label = request.label;
		if (label == 0)
		tv.setTextColor(Color.RED);
		else if (label == 1)
		tv.setTextColor(Color.YELLOW);
		else if (label == 2)
		tv.setTextColor(Color.GREEN);
		else if (label == 3)
		tv.setTextColor(Color.BLUE);
		else if (label == 4)
		tv.setTextColor(Color.CYAN);
		else if (label == 5)
		tv.setTextColor(Color.MAGENTA);
		
		dialogTVList.add(0, tv);
		dialogListView.setAdapter(new ArrayAdapter<TextView>(this,
			android.R.layout.simple_list_item_1, dialogTVList));
	}*/
	
	public void addAnswer(String answer) {
		String str = "- " + answer;
		newPhraseList.add(str);
		dialogList.add(0, str);
		dialogListView.setAdapter(new ArrayAdapter<String>(this,
			android.R.layout.simple_list_item_1, dialogList));
	}

	
//////////////// Command Analyzing Task ////////////////////////////////////////////////////////////
	
	private void startCommandAnalysing(String command) {	
		addRequest(command);    
		
		if (analyserTask != null) {
			analyserTask.cancel(true);
	    }
		InputStream patternsStream = this.getApplicationContext().getResources().
				openRawResource(R.raw.patterns);

	    textButton.setVisibility(View.GONE);
	    progress.setVisibility(View.VISIBLE);
	    
	    analyserTask = new CommandAnalyser(this, patternsStream);
	    analyserTask.execute(command);
	}
	
	public void endCommandAnalysing() {
		try {
			Intent intent = analyserTask.get();
			
		    progress.setVisibility(View.GONE);
		    textButton.setVisibility(View.VISIBLE);
	    	
	    	if(intent != null) {				
				// Check, whether the intent can be handled by some activity
				List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
				if (activities.size() == 0) {
				    addAnswer(getStr(R.string.unknown_action));
				} else {
					startActivityForResult(intent, SUB_ACTIVITY_REQUEST_CODE);
				}
	    	}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
//////////////// Tagging Task //////////////////////////////////////////////////////////////////////
	
	private void startTaggerTask(String command) {	
//		addRequest(command);    
		
		if (taggerTask != null) {
			taggerTask.cancel(true);
	    }

	    textButton.setVisibility(View.GONE);
	    progress.setVisibility(View.VISIBLE);
	    
	    taggerTask = new TaggerTask(this, hmmClassifier);
	    taggerTask.execute(command);
	}
	
	public void endTaggerTask() {
		try {			
			ArrayList<Token> list = taggerTask.get();
			String textRequest = "";
			String coloredRequest = "";
			
			for (Token t: list) 
			{
				Log.d(TAG, this.getLocalClassName() + ": Token: " + t.lexem + ", " + t.label);
				textRequest += t.lexem;
				
				int label = t.label;
				if (label == 0) {
					textRequest += ":COM ";
					coloredRequest += "<font color='#EE0000'>";
				} else if (label == 1) {
					textRequest += ":NAME ";
					coloredRequest += "<font color='#EEEE00'>";
				} else if (label == 2) {
					textRequest += ":TEL ";
					coloredRequest += "<font color='#00EE00'>";
				} else if (label == 3) {
					textRequest += ":EMAIL ";
					coloredRequest += "<font color='#00EEEE'>";
				} else if (label == 4) {
					textRequest += ":URL ";
					coloredRequest += "<font color='#0000EE'>";
				} else if (label == 5) {
					textRequest += ":TIME ";
					coloredRequest += "<font color='#EE00EE'>";
				}
				
				coloredRequest += t.lexem + "</font> ";
			}

			addRequest(textRequest);		
			coloredTextView.setText(Html.fromHtml(coloredRequest));
				
		    progress.setVisibility(View.GONE);
		    textButton.setVisibility(View.VISIBLE);
	   
		
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
//////////////// HMM Initialization ////////////////////////////////////////////////////////////////
	
	public File getHmmModelFile(String fileName) {		
	    File file = getBaseContext().getFileStreamPath(fileName);
	    Boolean exists = file.exists();
	    
	    File hmmFile = new File(this.getFilesDir(), fileName);
    	HmmClassifier.saveInit(hmmFile);
    	return hmmFile;
    	
//	    if(exists) {
//	    	return file;
//	    }
//	    else {
//	    	File hmmFile = new File(this.getFilesDir(), fileName);
//	    	HmmClassifier.saveInit(hmmFile);
//	    	return hmmFile;
//	    }
	}
	

////////////////Auxiliary Methods /////////////////////////////////////////////////////////////////

	private void hideSoftKeyboard() {
		InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),0);
	}
		
	private void showSoftKeyboard(EditText editText) {
		inputManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
	}
		
	private String getStr(int strCode)	{
		return String.format(getResources().getString(strCode));
	}
	
}
