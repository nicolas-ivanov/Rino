
package com.example.rino;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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


public class MainActivity extends Activity implements OnClickListener{

	public static final String TAG = "Rino";
	private ArrayList<String> commandsHistory;
	private ListView commandsHistoryView;
	private EditText textField;

	public static final SimpleDateFormat format = new SimpleDateFormat("dd_MM_yyyy HH_mm_ss", Locale.US);
	
	private void retrieveContacts() {
		Cursor people = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

		while(people.moveToNext()) {
			int nameFieldColumnIndex = people.getColumnIndex(PhoneLookup.DISPLAY_NAME);
			String contact = people.getString(nameFieldColumnIndex);
			int numberFieldColumnIndex = people.getColumnIndex(PhoneLookup.NUMBER);
			String number = people.getString(numberFieldColumnIndex);
			Log.d(MainActivity.TAG, this.getLocalClassName() + ": new contact name = " + contact
					+ "; number = " + number);
			ContactsDatabase.getInstance().addContact(contact, number);
		}

		people.close();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(MainActivity.TAG, this.getLocalClassName() + ": created");
		setContentView(R.layout.activity_main);

		Button speakButton = (Button) findViewById(R.id.speak_button);
		
		commandsHistory = new ArrayList<String>(); 				
		commandsHistoryView = (ListView) findViewById(R.id.history_list);
		
		Button textButton = (Button) findViewById(R.id.text_button);
		textButton.setOnClickListener(this);
		
		textField = (EditText) findViewById(R.id.text_field);
		textField.requestFocus();
		
		//Get contacts from phone
		retrieveContacts();

		// Check to see if a recognition activity is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0) {
			speakButton.setOnClickListener(this);
		} else {
			speakButton.setEnabled(false);
			speakButton.setText("Recognizer doesn't present\n Use the form below");
		}

	}

	public void onClick(View v) {
		if (v.getId() == R.id.speak_button) {
			Intent recognizeIntent = new Intent(this, SpeakButton.class);
			startActivityForResult(recognizeIntent, SpeakButton.SPEAK_BUTTON_REQUEST_CODE);
		}
		else if (v.getId() == R.id.text_button) {		
			EditText textField = (EditText) findViewById(R.id.text_field);
			String str = textField.getText().toString();
			textField.setText("");
			
			Intent getTextIntent = new Intent(this, TextButton.class);
			getTextIntent.putExtra("text", str);
			startActivityForResult(getTextIntent, TextButton.TEXT_BUTTON_REQUEST_CODE);
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, this.getLocalClassName() + ": got result, requestCode=" + requestCode + ", resultCode=" + resultCode);
		
		switch (requestCode) {
		case SpeakButton.SPEAK_BUTTON_REQUEST_CODE:
		case TextButton.TEXT_BUTTON_REQUEST_CODE:
			
			if (resultCode == RESULT_OK) {	
				ArrayList<String> resList = data.getStringArrayListExtra("res");
				String res = resList.get(0);		
				Log.d(TAG, this.getLocalClassName() + ": res = '" + res + "'");
				
				Intent recognizeIntent = new Intent(this, CommandAnalyser.class);
				recognizeIntent.putExtra("command", res);
				startActivityForResult(recognizeIntent, CommandAnalyser.COMMAND_ANALYSER_REQUEST_CODE);
			}
				
			break;
			
		case CommandAnalyser.COMMAND_ANALYSER_REQUEST_CODE:
			
			if (resultCode == RESULT_OK) {	
				// should be replaced with proper handling
				String command = data.getStringExtra("command");
				commandsHistory.add(0, command);
				// end replace
				commandsHistoryView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, commandsHistory));				
			}
			
			break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
}







