package com.example.rino;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
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

public class MainActivity extends Activity implements OnClickListener {

	public static final String TAG = "Rino";
	private ArrayList<String> commandsHistory;
	private ListView commandsHistoryView;
	private EditText textField;

	public static final SimpleDateFormat format = new SimpleDateFormat(
			"dd_MM_yyyy HH_mm_ss", Locale.US);

	private void retrieveContacts() {
		Cursor people = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
		people.moveToFirst();
		while (people.moveToNext()) {
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

		// Get contacts from phone
		retrieveContacts();

		// Check to see if a recognition activity is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0) {
			speakButton.setOnClickListener(this);
		} else {
			speakButton.setEnabled(false);
			speakButton
					.setText("Recognizer doesn't present\n Use the form below");
		}

	}

	public void onClick(View v) {
		if (v.getId() == R.id.speak_button) {
			Intent recognizeIntent = new Intent(this, SpeakButton.class);
			startActivityForResult(recognizeIntent,
					SpeakButton.SPEAK_BUTTON_REQUEST_CODE);
		} else if (v.getId() == R.id.text_button) {
			EditText textField = (EditText) findViewById(R.id.text_field);
			String str = textField.getText().toString();
			textField.setText("");

			Intent getTextIntent = new Intent(this, TextButton.class);
			getTextIntent.putExtra("text", str);
			startActivityForResult(getTextIntent,
					TextButton.TEXT_BUTTON_REQUEST_CODE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, this.getLocalClassName() + ": got result, requestCode="
				+ requestCode + ", resultCode=" + resultCode);

		switch (requestCode) {
		case SpeakButton.SPEAK_BUTTON_REQUEST_CODE:
		case TextButton.TEXT_BUTTON_REQUEST_CODE:

			if (resultCode == RESULT_OK) {
				ArrayList<String> resList = data.getStringArrayListExtra("res");
				String res = resList.get(0);
				Log.d(TAG, this.getLocalClassName() + ": res = '" + res + "'");

				Intent recognizeIntent = new Intent(this, CommandAnalyser.class);
				recognizeIntent.putExtra("command", res);
				startActivityForResult(recognizeIntent,
						CommandAnalyser.COMMAND_ANALYSER_REQUEST_CODE);
			}

			break;

		case CommandAnalyser.COMMAND_ANALYSER_REQUEST_CODE:

			if (resultCode == RESULT_OK) {
				// should be replaced with proper handling
				String command = data.getStringExtra("command");
				commandsHistory.add(0, command);
				// end replace
				commandsHistoryView.setAdapter(new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, commandsHistory));
			}

			break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
}
