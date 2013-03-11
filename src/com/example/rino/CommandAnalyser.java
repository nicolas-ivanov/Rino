package com.example.rino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
				Log.d(MainActivity.TAG, this.getLocalClassName()
						+ ": structureMatcher = " + structureMatcher.matches()
						+ ": '" + rawPattern + "'");

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

						// ! These variables should rather be declared somewhere
						// else
						String number;
						Uri numUri;
						Intent intent;
						Integer groupNum;

						if (patternID.equals("call_num")) {
							if ((groupNum = findGroupNum("number", structureMatcher)) != null){
								number = commandMatcher.group(groupNum);
								// ! Uri.parse can return rubbish or throw something
								// if the input is incorrect
								// ! Handle this somehow
								// It can't be incorrect, we check it with regexp and know exactly what to find
								numUri = Uri.parse("tel:" + number);
								intent = new Intent(
										android.content.Intent.ACTION_CALL, numUri);
								startActivityForResult(intent,
										SUB_ACTIVITY_REQUEST_CODE);
							} else if ((groupNum = findGroupNum("contact", structureMatcher)) != null){
								String name = commandMatcher.group(groupNum);
								number = ContactsDatabase.getInstance().getContact(name);
								if (number == null) {
									Toast.makeText(this, "Contact not found",
											Toast.LENGTH_LONG).show();
									finish();
								} else {
									numUri = Uri.parse("tel:" + number);
									intent = new Intent(
											android.content.Intent.ACTION_CALL, numUri);
									startActivityForResult(intent,
											SUB_ACTIVITY_REQUEST_CODE);
								}
							}
						}

						else if (patternID.equals("dial_num")) {
							if ((groupNum = findGroupNum("number", structureMatcher)) != null){
								number = commandMatcher.group(groupNum);
								numUri = Uri.parse("tel:" + number);
								intent = new Intent(
										android.content.Intent.ACTION_DIAL, numUri);
								startActivityForResult(intent,
										SUB_ACTIVITY_REQUEST_CODE);
							}

						}

						else if (patternID.equals("run_ussd")) {
							if ((groupNum = findGroupNum("number", structureMatcher)) != null){
								Log.d(MainActivity.TAG, this.getLocalClassName()
										+ ": commandMatcher.group('number') = '"
										+ commandMatcher.group(groupNum) + "'");
	
								String ussd = "*" + commandMatcher.group(groupNum)
										+ Uri.encode("#");
								numUri = Uri.parse("tel:" + ussd);
								intent = new Intent(
										android.content.Intent.ACTION_CALL, numUri);
								startActivityForResult(intent,
										SUB_ACTIVITY_REQUEST_CODE);
	
								// numUri = Uri.fromParts("tel", "*" +
								// commandMatcher.group(4), "");
							}
						}

						else if (patternID.equals("balance")) {
							// for MTS users only
							String ussd = "*100" + Uri.encode("#");
							numUri = Uri.parse("tel:" + ussd);
							intent = new Intent(
									android.content.Intent.ACTION_CALL, numUri);
							startActivityForResult(intent,
									SUB_ACTIVITY_REQUEST_CODE);

						}

						else if (patternID.equals("web_page")) {
							if ((groupNum = findGroupNum("webpage", structureMatcher)) != null){
								Log.d(MainActivity.TAG, this.getLocalClassName()
										+ ": commandMatcher.group('webpage') = '"
										+ commandMatcher.group(groupNum) + "'");
	
								Uri webpageUri = Uri.parse("http://www."
										+ commandMatcher.group(groupNum));
								intent = new Intent(Intent.ACTION_VIEW, webpageUri);
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
								intent = new Intent(Intent.ACTION_SENDTO, emailUri);
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
								numUri = Uri.parse("smsto:" + tel);
								intent = new Intent(Intent.ACTION_SENDTO, numUri);
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
	
								// Send sms directly from your app
								// manager = SmsManager.getDefault();
								// intent = PendingIntent.getActivity(this, 0, new
								// Intent(this, MainActivity.class), 0);
								//
								// manager.sendTextMessage(number, null, message,
								// null, null);
								//
								// Toast.makeText(CommandAnalyzer.this, "SMS sent",
								// Toast.LENGTH_LONG).show();
								//
								// Intent data = new Intent();
								// data.putExtra("matches", matches);
								// setResult(RESULT_OK, data);
								//
								// this.onActivityResult(SUB_ACTIVITY_SMS_REQUEST_CODE,
								// RESULT_OK, data);
							}
						}

						/*
						 * else if (patternID.equals("choose_data_sender")) {
						 * 
						 * intent = new Intent();
						 * intent.setAction(Intent.ACTION_SEND);
						 * intent.putExtra(Intent.EXTRA_SUBJECT,
						 * "Hello from Rino");
						 * intent.putExtra(Intent.EXTRA_TEXT,
						 * "This is a sample message." + nl + nl +
						 * "Best regards," + nl + "Rino");
						 * intent.setType("vnd.android-dir/mms-sms");
						 * 
						 * PackageManager packageManager = getPackageManager();
						 * List<ResolveInfo> activities =
						 * packageManager.queryIntentActivities(intent, 0);
						 * 
						 * // Check, whether the intent can be handled by some
						 * activity //! This check should be led for every
						 * launch attempt if (activities.size() > 0) { //
						 * startActivityForResult(intent,
						 * SUB_ACTIVITY_REQUEST_CODE);
						 * startActivity(Intent.createChooser(intent,
						 * "Send data from Rino")); } else {
						 * Toast.makeText(this,
						 * "Your phone can not handle this action",
						 * Toast.LENGTH_LONG).show(); finish(); }
						 * 
						 * }
						 */

						else if (patternID.equals("set_alarm")) {
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
								
								intent = new Intent(AlarmClock.ACTION_SET_ALARM);
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

			// if (resultCode == RESULT_CANCELED) {
			// Toast.makeText(this, "The app execution process is canceled",
			// Toast.LENGTH_LONG).show();
			// }
		}

		super.onActivityResult(requestCode, resultCode, data);
		finish();
	}

}
