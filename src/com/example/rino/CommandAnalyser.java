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

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.AlarmClock;
import android.util.Log;


public class CommandAnalyser extends AsyncTask<String, String, Intent> {

	private String patternID;
	private String nl;
	
	private InputStream patternsStream;
	private BufferedReader patternsReader;
	private PackageManager packageManager;

	private static final Pattern structurePattern = Pattern
			.compile("(\\w+)\\t+([^\\t~]+)(\\t~\\t(.+))?");

	
	// custom constructor
	CommandAnalyser(InputStream stream, PackageManager manager){
		nl = System.getProperty("line.separator");
		patternsStream = stream;
		packageManager = manager;
		patternsReader = new BufferedReader(new InputStreamReader(patternsStream));
	}
	
	// auxiliary function
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
    protected void onPreExecute() {
    	super.onPreExecute();
    	MainActivity.historyLabel.setText("Begin");  	      
		Log.d(MainActivity.TAG, "AsyncTask: created");	
    }

    
    
    @Override
    protected Intent doInBackground(String... commands) {
		Intent resIntent = null;
		
    	try {            
    		boolean found = false;
    		String line;
    		
			while (!found && ((line = patternsReader.readLine()) != null)) {
				String rawPattern = line;
				Matcher structureMatcher = structurePattern.matcher(rawPattern);
				
				// work only with correct raw patterns
				if (structureMatcher.matches()) {

					// extract command pattern from raw pattern
					Pattern commandPattern = Pattern.compile(structureMatcher.group(2));
					Matcher commandMatcher = commandPattern.matcher(commands[0]);
					
					found = commandMatcher.matches();

					if (found) {
						Log.d(MainActivity.TAG, "AsyncTask: commandMatcher = " 
								+ commandMatcher.matches() + ": '" + structureMatcher.group(2) + "'");
						patternID = structureMatcher.group(1);
						Log.d(MainActivity.TAG, "AsyncTask: pattern ID = '" + patternID + "'");

					    publishProgress("Command is recognized");
					    
						// Applications' launcher:
						if (patternID.equals("call_num")) {
							Integer numberNum, contactNum;
							if ((numberNum = findGroupNum("number", structureMatcher)) != null){
								String number = commandMatcher.group(numberNum);
								//TODO: check that number is proper
								Uri numUri = Uri.parse("tel:" + number);
								resIntent = new Intent(android.content.Intent.ACTION_CALL, numUri);
								return resIntent;

							} else if ((contactNum = findGroupNum("contact", structureMatcher)) != null){
								String name = commandMatcher.group(contactNum);
								Collection<Contact> contacts = ContactsDatabase.getInstance().getContacts(name);
								Contact contact;
								if (contacts.size() == 0) {
									//no contacts found

								    publishProgress("The contact not found");
//									continue;
								} else {
									//found one or more than one contact
									contact = contacts.iterator().next();
									
									Collection<String> numbers = contact.getNumbers();
									if (numbers == null){
									    publishProgress("The contact has no phone numbers");
									} else {
										Iterator<String> iterNumbers = numbers.iterator();
										if (iterNumbers.hasNext()){
											//TODO: take not only first phone number
											String number = iterNumbers.next();
											//TODO: check that number is proper
											if (number != null) {
												Uri numUri = Uri.parse("tel:" + number);
												resIntent = new Intent(
														android.content.Intent.ACTION_CALL, numUri);
												return resIntent;
											}
										} else {
											publishProgress("The contact is found, but it has no phone numbers");
										}
									}
								}
							}
						}
						
/*						else if (patternID.equals("find")){
							Integer contactNum;
							if ((contactNum = findGroupNum("contact", structureMatcher)) != null){
								String name = commandMatcher.group(contactNum);
								Collection<Contact> contacts = ContactsDatabase.getInstance().getContacts(name);
								Contact contact;
								if (contacts.size() == 0) {
									//no contacts found
								    publishProgress("Contact not found");
									break;
								} else {
									//found one or more than one contact
									contact = contacts.iterator().next(); 
								}
							    publishProgress("Contact is found");
								
							    resIntent = new Intent();
//								setResult(RESULT_OK, resIntent);
//								resIntent.putExtra("command", command);
								return resIntent;
								
							}
						}
						*/
						else if (patternID.equals("dial_num")) {
							Integer numberNum;
							if ((numberNum = findGroupNum("number", structureMatcher)) != null){
								String number = commandMatcher.group(numberNum);
								Uri numUri = Uri.parse("tel:" + number);
								resIntent = new Intent(android.content.Intent.ACTION_DIAL, numUri);
								return resIntent;
							}

						}

						else if (patternID.equals("run_ussd")) {
							Integer numberNum;
							if ((numberNum = findGroupNum("number", structureMatcher)) != null){
								Log.d(MainActivity.TAG, "AsyncTask: commandMatcher.group('number') = '"
										+ commandMatcher.group(numberNum) + "'");
	
								String ussd = "*" + commandMatcher.group(numberNum)
										+ Uri.encode("#");
								Uri numUri = Uri.parse("tel:" + ussd);
								resIntent = new Intent(android.content.Intent.ACTION_CALL, numUri);
								return resIntent;
							}
						}

						else if (patternID.equals("balance")) {
							// TODO: check balance not only for MTS users
							String ussd = "*100" + Uri.encode("#");
							Uri numUri = Uri.parse("tel:" + ussd);
							resIntent = new Intent(android.content.Intent.ACTION_CALL, numUri);
							return resIntent;

						}

						else if (patternID.equals("web_page")) {
							Integer webpageNum;
							if ((webpageNum = findGroupNum("webpage", structureMatcher)) != null){
								Log.d(MainActivity.TAG, "AsyncTask: commandMatcher.group('webpage') = '"
										+ commandMatcher.group(webpageNum) + "'");
	
								Uri webpageUri = Uri.parse("http://www."
										+ commandMatcher.group(webpageNum));
								resIntent = new Intent(Intent.ACTION_VIEW, webpageUri);
								return resIntent;
							}
						}

						else if (patternID.equals("send_email")) {
							Integer emailNum, textNum;
							if ((emailNum = findGroupNum("email", structureMatcher)) != null &&
									(textNum = findGroupNum("text", structureMatcher)) != null){
								Log.d(MainActivity.TAG, "AsyncTask: commandMatcher.group('email') = '"
										+ commandMatcher.group(emailNum) + "'");
								Log.d(MainActivity.TAG, "AsyncTask: commandMatcher.group('text') = '"
										+ commandMatcher.group(textNum) + "'");
								
								String email = commandMatcher.group(emailNum);
								String message = commandMatcher.group(textNum);
								Uri emailUri = Uri.parse("mailto:" + email);
								resIntent = new Intent(Intent.ACTION_SENDTO, emailUri);
								resIntent.putExtra(Intent.EXTRA_SUBJECT,
										"Hello from Rino");
								resIntent.putExtra(Intent.EXTRA_TEXT,
										"This is a sample message." + nl + message + nl
												+ "Best regards," + nl + "Rino");
	
								List<ResolveInfo> activities = packageManager
										.queryIntentActivities(resIntent, 0);
	
								// Check, whether the intent can be handled by some
								// activity
								// ! This check should be led for every launch
								// attempt
								if (activities.size() > 0) {
									return resIntent;
								} else {
								    publishProgress("Your phone can not handle this action");
									return null;
								}
							}
						}

						else if (patternID.equals("send_sms")) {
							Integer numberNum, textNum;
							if ((numberNum = findGroupNum("number", structureMatcher)) != null &&
									(textNum = findGroupNum("text", structureMatcher)) != null){
								Log.d(MainActivity.TAG, "AsyncTask: commandMatcher.group('number') = '"
										+ commandMatcher.group(numberNum) + "'");
								Log.d(MainActivity.TAG, "AsyncTask: commandMatcher.group('text') = '"
										+ commandMatcher.group(textNum) + "'");
	
								String tel = commandMatcher.group(numberNum);
								String text = commandMatcher.group(textNum);
								// The standard application is used here
								Uri numUri = Uri.parse("smsto:" + tel);
								resIntent = new Intent(Intent.ACTION_SENDTO, numUri);
								resIntent.putExtra("sms_body", text);
	
								List<ResolveInfo> activities = packageManager
										.queryIntentActivities(resIntent, 0);
	
								// Check, whether the intent can be handled by some
								// activity
								// ! This check should be led for every launch
								// attempt
								if (activities.size() > 0) {
									return resIntent;
								} else {
								    publishProgress("Your phone can not handle this action");
									return null;
								}		
							}
						} else if (patternID.equals("set_alarm")) {
							Integer hoursNum, minutesNum;
							if ((hoursNum = findGroupNum("hours", structureMatcher)) != null) {
							
								Log.d(MainActivity.TAG, "AsyncTask: commandMatcher.group('hours') = '"
										+ commandMatcher.group(hoursNum) + "'");								
	
								int hour = Integer
										.parseInt(commandMatcher.group(3));
								int minutes = 0;
								if ((minutesNum = findGroupNum("minutes", structureMatcher)) != null) {
									Log.d(MainActivity.TAG, "AsyncTask: commandMatcher.group('minutes') = '"
											+ commandMatcher.group(minutesNum) + "'");
									minutes = Integer.parseInt(commandMatcher
											.group(minutesNum));
								}
								
								resIntent = new Intent(AlarmClock.ACTION_SET_ALARM);
								resIntent.putExtra(AlarmClock.EXTRA_HOUR, hour);
								resIntent.putExtra(AlarmClock.EXTRA_MINUTES, minutes);
								resIntent.putExtra(AlarmClock.EXTRA_MESSAGE,
										"Rino alarm");
	
								List<ResolveInfo> activities = packageManager
										.queryIntentActivities(resIntent, 0);
	
								// Check, whether the intent can be handled by some
								// activity
								// ! This check should be led for every launch
								// attempt

								if (activities.size() > 0) {
									return resIntent;
								} else {
								    publishProgress("Your phone can not handle this action");
									return null;
								}
							}
						}

					}
				} else {
					Log.d(MainActivity.TAG, "AsyncTask: Pattern '" + rawPattern + "' is incorrect");
				}
			}

			if (!found) {
			    publishProgress("No matches");
			}

		} catch (IOException e) {
			Log.d(MainActivity.TAG, "Reading file with patterns failed", e);
		} finally {
			try {
				patternsReader.close();
			} catch (IOException e) {
				Log.e(MainActivity.TAG, "Closing file with patterns failed", e);
			}
		}
    	
	    return null;
	}


    protected void onProgressUpdate(String... message) {
    	super.onProgressUpdate(message);
  		MainActivity.historyLabel.setText(message[0]);  	
    }
    
    @Override
    protected void onPostExecute(Intent resIntent) {
    	Log.d(MainActivity.TAG, "AsyncTask: created");
	    super.onPostExecute(resIntent);
//	    MainActivity.historyLabel.setText("End async");  
	    }
  }