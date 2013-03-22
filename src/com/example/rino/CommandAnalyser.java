package com.example.rino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.AlarmClock;
import android.util.Log;


public class CommandAnalyser extends AsyncTask<String, String, Intent> {

	private String patternID;
	private String nl;
	
	private MainActivity mainActivity;
	private InputStream patternsStream;
	private BufferedReader patternsReader;

	private static final Pattern structurePattern = Pattern
			.compile("(\\w+)\\t+([^\\t~]+)(\\t~\\t(.+))?");

	private String getStr(int strCode)	{
		return String.format(mainActivity.getResources().getString(strCode));
	}
	
	CommandAnalyser(MainActivity main, InputStream stream){
		nl = System.getProperty("line.separator");
		mainActivity = main;
		patternsStream = stream;
		patternsReader = new BufferedReader(new InputStreamReader(patternsStream));
	}
	
	
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
		Log.d(MainActivity.TAG, "AsyncTask: created");	
    }

    
    @Override
    protected Intent doInBackground(String... commands) {
		Intent resIntent = null;

		// Imitate a heavy task that needs a couple of seconds to run 
        try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
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
					    
						
						// Command analysis block:
						
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

								    publishProgress(getStr(R.string.contact_is_not_found));
									continue; // why to continue?
								} else {
									//found one or more than one contact
									contact = contacts.iterator().next();
									
									Collection<String> numbers = contact.getNumbers();
									if (numbers == null){
									    publishProgress(getStr(R.string.no_pnohes_in_contact));
									} else {
										Iterator<String> iterNumbers = numbers.iterator();
										if (iterNumbers.hasNext()){
											//TODO: take not only first phone number
											String number = iterNumbers.next();
											//TODO: check that number is proper
											if (number != null) {
												Uri numUri = Uri.parse("tel:" + number);

												publishProgress(getStr(R.string.calling_number) 
														+ numUri.getSchemeSpecificPart());

												resIntent = new Intent(
														android.content.Intent.ACTION_CALL, numUri);
												return resIntent;
											}
										} else {
										    publishProgress(getStr(R.string.no_pnohes_in_contact));
										}
									}
								}
							}
						}
						
						else if (patternID.equals("find")){
							Integer contactNum;
							if ((contactNum = findGroupNum("contact", structureMatcher)) != null){
								String name = commandMatcher.group(contactNum);
								Collection<Contact> contacts = ContactsDatabase.getInstance().getContacts(name);
//								Contact contact;
								if (contacts.size() == 0) {
								    publishProgress(getStr(R.string.command_is_not_found));
									break;
								} else {
									//found one or more than one contact
//									contact = contacts.iterator().next(); 
								}
							    publishProgress(getStr(R.string.contact_is_found));
								
							    resIntent = new Intent();
								return resIntent;
							}
						}
						
						else if (patternID.equals("dial_num")) {
							Integer numberNum;
							if ((numberNum = findGroupNum("number", structureMatcher)) != null){
								String number = commandMatcher.group(numberNum);
								Uri numUri = Uri.parse("tel:" + number);

								publishProgress(getStr(R.string.dialing_number)
										+ numUri.getSchemeSpecificPart());
							    
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

								publishProgress(getStr(R.string.running_ussd)
										+ numUri.getSchemeSpecificPart());
								
								resIntent = new Intent(android.content.Intent.ACTION_CALL, numUri);
								return resIntent;
							}
						}

						else if (patternID.equals("balance")) {
							// TODO: check balance not only for MTS users
							String ussd = "*100" + Uri.encode("#");
							Uri numUri = Uri.parse("tel:" + ussd);
							
							publishProgress(getStr(R.string.checking_balance));
							
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

								publishProgress(getStr(R.string.loading_webpage)
										+ webpageUri.getSchemeSpecificPart());
								
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

								publishProgress(getStr(R.string.sending_email)
										+ emailUri.getSchemeSpecificPart());
								
								resIntent = new Intent(Intent.ACTION_SENDTO, emailUri);
								resIntent.putExtra(Intent.EXTRA_SUBJECT,
										"Hello from Rino");
								resIntent.putExtra(Intent.EXTRA_TEXT, "This is a sample message." 
										+ nl + message + nl	+ "Best regards," + nl + "Rino");
								return resIntent;
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
								

								publishProgress(getStr(R.string.sending_sms)
										+ numUri.getSchemeSpecificPart()
										+ getStr(R.string.with_text) + text);
								
								resIntent = new Intent(Intent.ACTION_SENDTO, numUri);
								resIntent.putExtra("sms_body", text);
								return resIntent;
							}
						} 
						
						else if (patternID.equals("set_alarm")) {
							Integer hoursNum, minutesNum;
							if ((hoursNum = findGroupNum("hours", structureMatcher)) != null) {
							
								Log.d(MainActivity.TAG, "AsyncTask: commandMatcher.group('hours') = '"
										+ commandMatcher.group(hoursNum) + "'");								
	
								Integer hour = Integer.parseInt(commandMatcher.group(3));
								Integer minutes = 0;
								if ((minutesNum = findGroupNum("minutes", structureMatcher)) != null) {
									Log.d(MainActivity.TAG, "AsyncTask: commandMatcher.group('minutes') = '"
											+ commandMatcher.group(minutesNum) + "'");
									minutes = Integer.parseInt(commandMatcher.group(minutesNum));
								}

								publishProgress(getStr(R.string.setting_alarm)
										+ hour.toString() + ":" + minutes.toString());
								
								resIntent = new Intent(AlarmClock.ACTION_SET_ALARM);
								resIntent.putExtra(AlarmClock.EXTRA_HOUR, hour);
								resIntent.putExtra(AlarmClock.EXTRA_MINUTES, minutes);
								resIntent.putExtra(AlarmClock.EXTRA_MESSAGE,"Rino alarm");
								return resIntent;
							}
						}

					} // if (found()) ends
					
				} // if (structureMatcher.matches()) ends	
				else {
					Log.d(MainActivity.TAG, "AsyncTask: Pattern '" + rawPattern + "' is incorrect");
					return null;
				}
			} // while (!found && ((line = patternsReader.readLine()) != null)) ends

			// the command can't be recognized with existing patterns
		    publishProgress(getStr(R.string.command_is_not_found));
			return null;

		} catch (IOException e) {
			Log.d(MainActivity.TAG, "Reading file with patterns failed", e);
		} finally {
			try {
				patternsReader.close();
				patternsStream.close();
			} catch (IOException e) {
				Log.e(MainActivity.TAG, "Closing patternsReader or patternsStream failed", e);
			}
		}
		return null;
	}

    
    @Override
    protected void onProgressUpdate(String... answer) {
    	super.onProgressUpdate(answer);
  		mainActivity.addAnswer(answer[0]);
    }
    
    
    @Override
    protected void onPostExecute(Intent resIntent) {
    	Log.d(MainActivity.TAG, "AsyncTask: finished");
	    super.onPostExecute(resIntent);
	    mainActivity.endCommandAnalysing();
	}
    
}