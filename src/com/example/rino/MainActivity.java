package com.example.rino;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements OnClickListener {

	public static final int VOICE_RECOGNITION_REQUEST_CODE = 11;
	public static final int SUB_ACTIVITY_REQUEST_CODE = 12;
	
	public static final String TAG = "Rino";
	private ArrayList<String> dialogList;
	private ArrayList<String> commands;
	private ListView dialogListView;
	private EditText textField;
	public static TextView historyLabel;
	
	private CommandAnalyser analyserTask;
	private PackageManager packageManager;

	private void retrieveContacts() {
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
	}

	
	private String getStr(int strCode)	{
		return String.format(getResources().getString(strCode));
	}
	
	
	public void addRequest(String request) {
		dialogList.add(0, request);
		dialogListView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dialogList));
	}
	
	public void addAnswer(String answer) {
		dialogList.add(0, answer);
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
	
	
	private void analyseCommand(String command) {	
	    try {	
			addRequest(command);    
			
			if (analyserTask != null) {
			      analyserTask.cancel(true);
		    }
			InputStream patternsStream = this.getApplicationContext().getResources().
					openRawResource(R.raw.patterns);
			
		    analyserTask = new CommandAnalyser(this, patternsStream);
		    analyserTask.execute(command);
	    	Intent intent = analyserTask.get();
	    	
	    	if(intent != null) {
				
				// Check, whether the intent can be handled by some activity
				List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
				if (activities.size() == 0) {
				    addAnswer(getStr(R.string.unknown_action));
				}
				
	    		startActivityForResult(intent, SUB_ACTIVITY_REQUEST_CODE);	
	    	}
	    } 
	    catch (InterruptedException e) {
			addAnswer(getStr(R.string.analyzing_is_stopped));
	    	e.printStackTrace();
	    } 
	    catch (ExecutionException e) {
	    	e.printStackTrace();
	    }
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(MainActivity.TAG, this.getLocalClassName() + ": created");
		
		setContentView(R.layout.activity_main);
		Button speakButton = (Button) findViewById(R.id.speak_button);
		Button textButton = (Button) findViewById(R.id.text_button);
		textField = (EditText) findViewById(R.id.text_field);
		historyLabel = (TextView) findViewById(R.id.history_label);
		dialogListView = (ListView) findViewById(R.id.history_list);
		dialogList = new ArrayList<String>();

		// Get contacts from phone
//		retrieveContacts();

		packageManager = getPackageManager();

		// Check to see if a recognition activity is present
		List<ResolveInfo> activities = packageManager.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0) {
			speakButton.setOnClickListener(this);
		} else {
			speakButton.setEnabled(false);
			speakButton.setText("@string/recognizer_is_absent");
		}
		textButton.setOnClickListener(this);
	}

	
	public void onClick(View v) {
		if (v.getId() == R.id.speak_button) {
			startVoiceRecognitionActivity();
		} 
		else if (v.getId() == R.id.text_button) {
			String str = textField.getText().toString();
			analyseCommand(str);
			textField.setText("");
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
				analyseCommand(command);
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
