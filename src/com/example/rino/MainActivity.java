package com.example.rino;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
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


public class MainActivity extends Activity implements OnClickListener {

	public static final int VOICE_RECOGNITION_REQUEST_CODE = 11;
	public static final int SUB_ACTIVITY_REQUEST_CODE = 12;
	
	public static final String TAG = "Rino";
	private ArrayList<String> dialogList;
	private ArrayList<String> newPhraseList;
	private ArrayList<String> commands;
	
	private Button speakButton;
	private Button textButton;
	private EditText textField;
	private ProgressBar progress;
	private ListView dialogListView;
	
	private CommandAnalyser analyserTask;
	private PackageManager packageManager;
	private InputMethodManager inputManager;
	private DialogDBHelper dialogDBHelper;
	
	
	
	// retrieving all contacts at once should be avoided as too expensive operation
	/*private void retrieveContacts() {
		Cursor people = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		people.moveToFirst();
		int i = 0;
		while (people.moveToNext()) {
			i++;
			int nameFieldColumnIndex = people.getColumnIndex(PhoneLookup.DISPLAY_NAME);
			if (nameFieldColumnIndex == -1)	continue;
			String contact = people.getString(nameFieldColumnIndex);
			String contactId = people.getString(people.getColumnIndex(ContactsContract.Contacts._ID)); 
			if (people.getColumnIndex(PhoneLookup.HAS_PHONE_NUMBER) != 0) {
				Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
				phones.moveToFirst();
				Collection<String> numbers = new ArrayList<String>();
				while (phones.moveToNext()) {
					String number = phones.getString(phones.getColumnIndex(
							ContactsContract.CommonDataKinds.Phone.NUMBER));
					Log.d(MainActivity.TAG, this.getLocalClassName()
							+ " " + i 
							+ ": new contact name = " + contact + "; number = "
							+ number);
					numbers.add(number);
				}
				phones.close();
				
				String whereName = ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = ?";
				String[] whereNameParams = new String[] { ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, contactId };
				Cursor nameCur = getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, whereName, whereNameParams, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
				String givenName = null, familyName = null, middleName = null;
				nameCur.moveToFirst();
				//TODO: fix bug
				//Strange: doesn't find all first names and last names which are written and used on phone
				while (nameCur.moveToNext()) {
					givenName = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
					familyName = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
					middleName = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
					Log.d(MainActivity.TAG, this.getLocalClassName()
							+ ": new full contact"
							+ "; FN = " + givenName
							+ "; LN = " + familyName 
							+ "; MN = " + middleName 
							+ "; name = " + contact);
				}
				nameCur.close();
				
				ContactsDatabase.getInstance().addContact(contact, givenName, familyName, middleName, numbers);
			} else {
				Log.d(MainActivity.TAG, this.getLocalClassName()
						+ ": No numbers");
			}
		}

		people.close();
	}*/

	
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
	
	
	public void addRequest(String request) {
		newPhraseList.add(0, request);
		dialogList.add(0, request);
		dialogListView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dialogList));
	}
	
	public void addAnswer(String answer) {
		String str = "- " + answer;
		newPhraseList.add(str);
		dialogList.add(0, str);
		dialogListView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dialogList));
	}
	
	
	private void startVoiceRecognitionActivity() {		
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
		
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);				
	}
	
	
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
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(MainActivity.TAG, this.getLocalClassName() + ": created");
		
		setContentView(R.layout.activity_main);
		speakButton = (Button) findViewById(R.id.speak_button);
		progress = (ProgressBar) findViewById(R.id.progressBar);
		textField = (EditText) findViewById(R.id.text_field);
		textButton = (Button) findViewById(R.id.text_button);
		dialogListView = (ListView) findViewById(R.id.history_list);

		inputManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
		
		dialogDBHelper = new DialogDBHelper(this);
		dialogList = dialogDBHelper.getDialogHistory();
		newPhraseList = new ArrayList<String>();
		dialogListView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dialogList));

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
		
		// Get contacts from phone
//		retrieveContacts();
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
			String str = textField.getText().toString();
			startCommandAnalysing(str);
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

		switch (requestCode) {
		case VOICE_RECOGNITION_REQUEST_CODE:
			
			switch (resultCode) {
			case RESULT_OK:
				// Fill the list view with the strings the recognizer thought it could have heard
				commands = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				String command = commands.get(0);
				Log.d(TAG, this.getLocalClassName() + ": res = '" + command + "'");
				startCommandAnalysing(command);
				break;
				
			case RESULT_CANCELED:
				addAnswer(getStr(R.string.recognition_is_cancelled));
		        break;
			}
			break;

		case SUB_ACTIVITY_REQUEST_CODE:

			switch (resultCode) {
			case RESULT_OK:
				// TODO: find cases, when this code is returned 
				// do something
				break;
				
			case RESULT_CANCELED:
				// TODO: find cases, when this code is returned 
				// do something
				break;
			}
			break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
	
}
