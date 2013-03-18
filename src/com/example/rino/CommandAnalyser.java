package com.example.rino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.util.Log;
import android.widget.Toast;

public class CommandAnalyser extends Activity {

	public static final int COMMAND_ANALYSER_REQUEST_CODE = 41;
	public static final int SUB_ACTIVITY_REQUEST_CODE = 401;

	private String patternID;
	private String command;

	private static final Pattern structurePattern = Pattern
			.compile("(\\w+)\\t+([^\\t~]+)(\\t~\\t(.+))?");

	// group1 - pattern id
	// group2 - command
	// group3 - parameters list
	// structurePattern example: "call_num позвони (\+?[\d\s]+) ~ number1"

	private Integer findGroupNum(String groupName, Matcher structureMatcher){
		String[] parameterList = structureMatcher.group(4).split(",");
		int cnt = 1;
		for (String parameter : parameterList){
			if (parameter.equals(groupName)) {
				return cnt; 
			}
			cnt++;
		}
		return null;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(MainActivity.TAG, this.getLocalClassName() + ": created");

		String nl = System.getProperty("line.separator");

		command = getIntent().getStringExtra("command");
		Log.d(MainActivity.TAG, this.getLocalClassName() + ": command = '"
				+ command + "'");

		InputStream patternsStream = this.getApplicationContext()
				.getResources().openRawResource(R.raw.patterns);
		BufferedReader patternsReader = new BufferedReader(
				new InputStreamReader(patternsStream));

		try {
			String line;
			boolean found = false;

			while (!found && ((line = patternsReader.readLine()) != null)) {
				String rawPattern = line;
				Matcher structureMatcher = structurePattern.matcher(rawPattern);
				// work only with correct raw patterns
				if (structureMatcher.matches()) {
					/*int groupsNum = structureMatcher.groupCount();
					Log.d(MainActivity.TAG, this.getLocalClassName()
							+ ": groupsNum = " + groupsNum);
					for (int i = 0; i < groupsNum; i++)
						Log.d(MainActivity.TAG,
								this.getLocalClassName() + ": group(" + i
										+ ") = '" + structureMatcher.group(i)
										+ "'");*/

					// extract command pattern from raw pattern
					Pattern commandPattern = Pattern.compile(structureMatcher
							.group(2));
					Matcher commandMatcher = commandPattern.matcher(command);
					
					found = commandMatcher.matches();

					if (found) {
						Log.d(MainActivity.TAG, this.getLocalClassName()
								+ ": commandMatcher = " + commandMatcher.matches()
								+ ": '" + structureMatcher.group(2) + "'");
						patternID = structureMatcher.group(1);
						Log.d(MainActivity.TAG, this.getLocalClassName()
								+ ": pattern ID = '" + patternID + "'");

						Toast.makeText(this, "Command is recognized",
								Toast.LENGTH_LONG).show();

						// Applications' launcher:
						if (patternID.equals("call_num")) {
							Integer numberNum, contactNum;
							if ((numberNum = findGroupNum("number", structureMatcher)) != null){
								String number = commandMatcher.group(numberNum);
								//TODO: check that number is proper
								Uri numUri = Uri.parse("tel:" + number);
								Intent intent = new Intent(
										android.content.Intent.ACTION_CALL, numUri);
								startActivityForResult(intent,
										SUB_ACTIVITY_REQUEST_CODE);
							} else if ((contactNum = findGroupNum("contact", structureMatcher)) != null){
								String name = commandMatcher.group(contactNum);
								Collection<Contact> contacts = ContactsDatabase.getInstance().getContacts(name);
								Contact contact;
								if (contacts.size() == 0) {
									//no contacts found
									Toast.makeText(this, "Contact not found",
											Toast.LENGTH_LONG).show();
									finish();
									continue;
								} else {
									//found one or more than one contact
									contact = contacts.iterator().next(); 
								}
								Collection<String> numbers = contact.getNumbers();
								if (numbers == null){
									Toast.makeText(this, "Contact has no numbers",
											Toast.LENGTH_LONG).show();
									finish();
								} else {
									Iterator<String> iterNumbers = numbers.iterator();
									if (iterNumbers.hasNext()){
										//TODO: take not only first phone number
										String number = iterNumbers.next();
										//TODO: check that number is proper
										if (number != null) {
											Uri numUri = Uri.parse("tel:" + number);
											Intent intent = new Intent(
													android.content.Intent.ACTION_CALL, numUri);
											startActivityForResult(intent,
													SUB_ACTIVITY_REQUEST_CODE);
										}
									} else {
										Toast.makeText(this, "Contact is found, but it has no phone numbers",
												Toast.LENGTH_LONG).show();
										finish();
									}
								}
							}
						}
						
						else if (patternID.equals("find")){
							Integer contactNum;
							if ((contactNum = findGroupNum("contact", structureMatcher)) != null){
								String name = commandMatcher.group(contactNum);
								Collection<Contact> contacts = ContactsDatabase.getInstance().getContacts(name);
								Contact contact;
								if (contacts.size() == 0) {
									//no contacts found
									Toast.makeText(this, "Contact not found",
											Toast.LENGTH_LONG).show();
									finish();
									break;
								} else {
									//found one or more than one contact
									contact = contacts.iterator().next(); 
								}
								Toast.makeText(this, "Contact is found: " + contact.getName(),
										Toast.LENGTH_LONG).show();
								
								Intent resIntent = new Intent();
								setResult(RESULT_OK, resIntent);
								resIntent.putExtra("command", command);
								
								finish();
							}
						}
						
						else if (patternID.equals("dial_num")) {
							Integer numberNum;
							if ((numberNum = findGroupNum("number", structureMatcher)) != null){
								String number = commandMatcher.group(numberNum);
								Uri numUri = Uri.parse("tel:" + number);
								Intent intent = new Intent(
										android.content.Intent.ACTION_DIAL, numUri);
								startActivityForResult(intent,
										SUB_ACTIVITY_REQUEST_CODE);
							}

						}

						else if (patternID.equals("run_ussd")) {
							Integer numberNum;
							if ((numberNum = findGroupNum("number", structureMatcher)) != null){
								Log.d(MainActivity.TAG, this.getLocalClassName()
										+ ": commandMatcher.group('number') = '"
										+ commandMatcher.group(numberNum) + "'");
	
								String ussd = "*" + commandMatcher.group(numberNum)
										+ Uri.encode("#");
								Uri numUri = Uri.parse("tel:" + ussd);
								Intent intent = new Intent(
										android.content.Intent.ACTION_CALL, numUri);
								startActivityForResult(intent,
										SUB_ACTIVITY_REQUEST_CODE);
							}
						}

						else if (patternID.equals("balance")) {
							// TODO: check balance not only for MTS users
							String ussd = "*100" + Uri.encode("#");
							Uri numUri = Uri.parse("tel:" + ussd);
							Intent intent = new Intent(
									android.content.Intent.ACTION_CALL, numUri);
							startActivityForResult(intent,
									SUB_ACTIVITY_REQUEST_CODE);

						}

						else if (patternID.equals("web_page")) {
							Integer webpageNum;
							if ((webpageNum = findGroupNum("webpage", structureMatcher)) != null){
								Log.d(MainActivity.TAG, this.getLocalClassName()
										+ ": commandMatcher.group('webpage') = '"
										+ commandMatcher.group(webpageNum) + "'");
	
								Uri webpageUri = Uri.parse("http://www."
										+ commandMatcher.group(webpageNum));
								Intent intent = new Intent(Intent.ACTION_VIEW, webpageUri);
								startActivityForResult(intent,
										SUB_ACTIVITY_REQUEST_CODE);
							}
						}

						else if (patternID.equals("send_email")) {
							Integer emailNum, textNum;
							if ((emailNum = findGroupNum("email", structureMatcher)) != null &&
									(textNum = findGroupNum("text", structureMatcher)) != null){
								Log.d(MainActivity.TAG, this.getLocalClassName()
										+ ": commandMatcher.group('email') = '"
										+ commandMatcher.group(emailNum) + "'");
								Log.d(MainActivity.TAG, this.getLocalClassName()
										+ ": commandMatcher.group('text') = '"
										+ commandMatcher.group(textNum) + "'");
								
								String email = commandMatcher.group(emailNum);
								String message = commandMatcher.group(textNum);
								Uri emailUri = Uri.parse("mailto:" + email);
								Intent intent = new Intent(Intent.ACTION_SENDTO, emailUri);
								intent.putExtra(Intent.EXTRA_SUBJECT,
										"Hello from Rino");
								intent.putExtra(Intent.EXTRA_TEXT,
										"This is a sample message." + nl + message + nl
												+ "Best regards," + nl + "Rino");
	
								PackageManager packageManager = getPackageManager();
								List<ResolveInfo> activities = packageManager
										.queryIntentActivities(intent, 0);
	
								// Check, whether the intent can be handled by some
								// activity
								// ! This check should be led for every launch
								// attempt
								if (activities.size() > 0)
									startActivityForResult(intent,
											SUB_ACTIVITY_REQUEST_CODE);
								else {
									Toast.makeText(
											this,
											"Your phone can not handle this action",
											Toast.LENGTH_LONG).show();
									finish();
								}
							}
						}

						else if (patternID.equals("send_sms")) {
							Integer numberNum, textNum;
							if ((numberNum = findGroupNum("number", structureMatcher)) != null &&
									(textNum = findGroupNum("text", structureMatcher)) != null){
								Log.d(MainActivity.TAG, this.getLocalClassName()
										+ ": commandMatcher.group('number') = '"
										+ commandMatcher.group(numberNum) + "'");
								Log.d(MainActivity.TAG, this.getLocalClassName()
										+ ": commandMatcher.group('text') = '"
										+ commandMatcher.group(textNum) + "'");
	
								String tel = commandMatcher.group(numberNum);
								String text = commandMatcher.group(textNum);
								// The standard application is used here
								Uri numUri = Uri.parse("smsto:" + tel);
								Intent intent = new Intent(Intent.ACTION_SENDTO, numUri);
								intent.putExtra("sms_body", text);
	
								PackageManager packageManager = getPackageManager();
								List<ResolveInfo> activities = packageManager
										.queryIntentActivities(intent, 0);
	
								// Check, whether the intent can be handled by some
								// activity
								// ! This check should be led for every launch
								// attempt
								if (activities.size() > 0)
									startActivityForResult(intent,
											SUB_ACTIVITY_REQUEST_CODE);
								else {
									Toast.makeText(
											this,
											"Your phone can not handle this action",
											Toast.LENGTH_LONG).show();
									finish();
								}								
							}
						} else if (patternID.equals("set_alarm")) {
							Integer hoursNum, minutesNum;
							if ((hoursNum = findGroupNum("hours", structureMatcher)) != null) {
							
								Log.d(MainActivity.TAG, this.getLocalClassName()
										+ ": commandMatcher.group('hours') = '"
										+ commandMatcher.group(hoursNum) + "'");								
	
								int hour = Integer
										.parseInt(commandMatcher.group(3));
								int minutes = 0;
								if ((minutesNum = findGroupNum("minutes", structureMatcher)) != null) {
									Log.d(MainActivity.TAG, this.getLocalClassName()
											+ ": commandMatcher.group('minutes') = '"
											+ commandMatcher.group(minutesNum) + "'");
									minutes = Integer.parseInt(commandMatcher
											.group(minutesNum));
								}
								
								Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
								intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
								intent.putExtra(AlarmClock.EXTRA_MINUTES, minutes);
								intent.putExtra(AlarmClock.EXTRA_MESSAGE,
										"Rino alarm");
	
								PackageManager packageManager = getPackageManager();
								List<ResolveInfo> activities = packageManager
										.queryIntentActivities(intent, 0);
	
								// Check, whether the intent can be handled by some
								// activity
								// ! This check should be led for every launch
								// attempt
								if (activities.size() > 0)
									startActivityForResult(intent,
											SUB_ACTIVITY_REQUEST_CODE);
								else {
									Toast.makeText(
											this,
											"Your phone can not handle this action",
											Toast.LENGTH_LONG).show();
									finish();
								}
							}
						}

					}
				} else {
					Log.d(MainActivity.TAG, this.getLocalClassName()
							+ ": Pattern '" + rawPattern + "' is incorrect");
				}
			}

			if (!found) {
				Toast.makeText(this, "No matches :(", Toast.LENGTH_LONG).show();

				Intent resIntent = new Intent();
				setResult(RESULT_OK, resIntent);
				resIntent.putExtra("command", command);

				finish();
			}

		} catch (IOException e) {
			Intent resIntent = new Intent();
			setResult(RESULT_CANCELED, resIntent);
			resIntent.putExtra("command", command);

			Log.d(MainActivity.TAG, "Reading file with patterns failed", e);

			finish();

		} finally {
			try {
				patternsReader.close();
			} catch (IOException e) {
				Intent resIntent = new Intent();
				setResult(RESULT_CANCELED, resIntent);
				resIntent.putExtra("command", command);

				Log.e(MainActivity.TAG, "Closing file with patterns failed", e);

				finish();
			}
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(MainActivity.TAG, this.getLocalClassName()
				+ ": got result, requestCode=" + requestCode + ", resultCode="
				+ resultCode);

		if (requestCode == SUB_ACTIVITY_REQUEST_CODE) {
			Intent resultIntent = new Intent();
			setResult(RESULT_OK, resultIntent);
			resultIntent.putExtra("command", command);

			// TODO: check if RESULT_CANCELED is needed to process 
			// if (resultCode == RESULT_CANCELED) {
			// Toast.makeText(this, "The app execution process is canceled",
			// Toast.LENGTH_LONG).show();
			// }
		}

		super.onActivityResult(requestCode, resultCode, data);
		finish();
	}

}
