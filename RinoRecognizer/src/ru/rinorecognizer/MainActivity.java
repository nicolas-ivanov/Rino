package ru.rinorecognizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import com.example.rino.R;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
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
	public static final String SVM = "svmModel";
	
	private ArrayList<String> dialogList;
	private ArrayList<String> newPhraseList;
	private ArrayList<String> commands;
	
	private Button speakButton;
	private Button textButton;
	private EditText textField;
	private ProgressBar progress;
	private TextView coloredTextView;
	private ListView dialogListView;

	private FramingTask framingTask;
	private Frame savedFrame;
	
	private PackageManager packageManager;
	private InputMethodManager inputManager;
	private DialogDBHelper dialogDBHelper;

	
	public static class SvmBunch {
		SvmClassifier svm_A;
		SvmClassifier svm_call;
		SvmClassifier svm_sms;
		SvmClassifier svm_email;
		SvmClassifier svm_search;
		SvmClassifier svm_site;
		SvmClassifier svm_alarm;
		SvmClassifier svm_balance;
	}
	
	private SvmBunch svm_bunch;
	
	

//////////////// Common Methods ////////////////////////////////////////////////////////////////////
	
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

		
		svm_bunch = new SvmBunch();
		svm_bunch.svm_A = new SvmClassifier(getPath("model_action"), getPath("range_action"));
		svm_bunch.svm_call = new SvmClassifier(getPath("model_a_call"), getPath("range_a_call"));
		svm_bunch.svm_sms = new SvmClassifier(getPath("model_a_sms"), getPath("range_a_sms"));
		svm_bunch.svm_site = new SvmClassifier(getPath("model_a_site"), getPath("range_a_site"));
		svm_bunch.svm_email = new SvmClassifier(getPath("model_a_email"), getPath("range_a_email"));
		svm_bunch.svm_search = new SvmClassifier(getPath("model_a_search"), getPath("range_a_search"));
		svm_bunch.svm_alarm = new SvmClassifier(getPath("model_a_alarm"), getPath("range_a_alarm"));
		svm_bunch.svm_balance = new SvmClassifier(getPath("model_a_balance"), getPath("range_a_balance"));
		
		
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
	}
	
	
	public void onClick(View v) {
		if (v.getId() == R.id.speak_button) {
			startVoiceRecognitionActivity();
			hideSoftKeyboard();
		} 
		else if (v.getId() == R.id.text_button) {
			String command = textField.getText().toString();
			startFramingTask(command);
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
				startFramingTask(command);
				break;
				
			case RESULT_CANCELED:
//				addAnswer(getStr(R.string.recognition_is_cancelled));
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
	
	
//////////////// Dialog List Updating //////////////////////////////////////////////////////////////

	public void addRequest(String request) {
//		newPhraseList.add(0, "«" + request + "»");
		String str = " - " + request + "\n";
//		String str = " - " + request.substring(0, 1).toUpperCase(Locale.US) + request.substring(1) + ".\n";
		dialogList.add(0, str);
		
		dialogListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dialogList));
	}
	
	
	public void addAnswer(String answer) {
		String request = dialogList.get(0);
		dialogList.remove(0);
		
//		String str = request + System.getProperty("line.separator") + "- " + answer 
//				+ System.getProperty("line.separator");		
		
		String str = request + "- " + answer + "\n";		
		

//		String str = " - " + request;
//		String str = " - " + request.substring(0, 1).toUpperCase(Locale.US) + request.substring(1) + "- " + answer + ".\n";
		newPhraseList.add(str);
		dialogList.add(0, str);
		dialogListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dialogList));
	}

	
	
//////////////// Framing Task //////////////////////////////////////////////////////////////////////
	
	private void startFramingTask(String command) {	
//		addRequest(command);    
		
		if (framingTask != null) {
			framingTask.cancel(true);
	    }

	    textButton.setVisibility(View.GONE);
	    progress.setVisibility(View.VISIBLE);
	    
	    framingTask = new FramingTask(this, svm_bunch, savedFrame);
	    ExtendedCommand extCommand = new ExtendedCommand();
	    extCommand.curCommand = command;
	    
	    if (savedFrame != null) {
		    extCommand.prevType = savedFrame.getTypeID();
		    extCommand.prevComplite = savedFrame.isComplete()? 1 : 0;	    	
	    }
	    else {
		    extCommand.prevType = 0;
		    extCommand.prevComplite = 1;
	    }
	    	
	    framingTask.execute(extCommand);
	}
	
	
	public void endFramingTask() {
		try {			
			FramingResult result = framingTask.get();
			Intent intent = result.intent;
			this.savedFrame = result.savedFrame;
		    
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
				this.savedFrame = null;
	    	}
		} 
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

//////////////// Auxiliary Methods /////////////////////////////////////////////////////////////////

	private void hideSoftKeyboard() {
		InputMethodManager inputManager = (InputMethodManager) this.
				getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),0);
	}
		
	private void showSoftKeyboard(EditText editText) {
		inputManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
	}
		
	public String getStr(int strCode)	{
		return String.format(getResources().getString(strCode));
	}
	
	
	
//////////////// SVM Initialization ////////////////////////////////////////////////////////////////
	
	public String getPath(String fileName) 
	{
	    try {
	    	String path = Environment.getExternalStorageDirectory().getPath();
	    	File dir = new File(path, "Rino");
	    	
	    	if (!dir.exists())
	    		dir.mkdir();
	    	
	        File file = new File(dir, fileName);
//	        file.lastModified();
//	        Date lastModDate = new Date(file.lastModified());

		    if (file.exists())
		    	return file.getPath();
		    else {		    	
	            int rID = getResources().getIdentifier(fileName, "raw", getPackageName()); 

		        InputStream is = getResources().openRawResource(rID);  
		        OutputStream os = new FileOutputStream(file);

		        byte[] data = new byte[is.available()];
		        is.read(data);
		        os.write(data);
		        is.close();
		        os.close();
				return file.getPath();
		    }
	    } 
	    catch (IOException e) {
	        Log.w("ExternalStorage", "Error writing to file", e);
	    }
		return null;
	}
	
	
}

