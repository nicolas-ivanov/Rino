package com.example.rino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class CommandAnalyser extends Activity {

	public static final int COMMAND_ANALYSER_REQUEST_CODE = 41;
	public static final int SUB_ACTIVITY_CALL_REQUEST_CODE = 401;
	
	private static final Pattern commonPattern = Pattern.compile("(\\d+)\\t(.+)\\t~\\t(.+)");
	// group1 - pattern id
	// group2 - command
	// group3 - parameters list
	// common pattern example: "1	позвони (\+?[\d\s]+)	~	number1 number2"
	
	private int patternID;
	private String command;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(MainActivity.TAG, this.getLocalClassName() + ": created");

	    command = getIntent().getStringExtra("command");
		Log.d(MainActivity.TAG, this.getLocalClassName() + ": command = '" + command + "'");
		
		InputStream patternsStream = this.getApplicationContext().getResources().openRawResource(R.raw.patterns);  // Why not to use FileInputStream or BufferedInputStream instead?
		BufferedReader patternsReader = new BufferedReader(new InputStreamReader(patternsStream));
		
		try {
			String line;
			boolean found = false;
			
			while (!found && ((line = patternsReader.readLine()) != null)) 
			{
				String rawPattern = line;
				Matcher commonMatcher = commonPattern.matcher(rawPattern);			
				Log.d(MainActivity.TAG, this.getLocalClassName() + ": commonMatcher = '" + commonMatcher.matches() + "'" + rawPattern);
				
				// work only with correct raw patterns 
				if (commonMatcher.matches()) 
				{
					Log.d(MainActivity.TAG, this.getLocalClassName() + ": group(1) = '" + commonMatcher.group(1) + 
							"' group(2) = '" + commonMatcher.group(2) + "' group(3) = '" + commonMatcher.group(3) + "'");
					
					// extract command pattern from raw pattern
					Pattern commandPattern = Pattern.compile(commonMatcher.group(2));
					Matcher commandMatcher = commandPattern.matcher(command);
					Log.d(MainActivity.TAG, this.getLocalClassName() + ": commandMatcher = '" + commandMatcher.matches() + "'");
					
					found = commandMatcher.matches();
					
					if (found) 
					{
						patternID = Integer.parseInt(commonMatcher.group(1));
						Log.d(MainActivity.TAG, this.getLocalClassName() + ": pattern ID = '" + patternID + "'");
						
				        Toast.makeText(this, "Command is recognized", Toast.LENGTH_LONG).show();
				        			        
						// Applications' launcher:	        
				        switch (patternID) {
				        case 1:
				        	String number = commandMatcher.group(1);
							Log.d(MainActivity.TAG, this.getLocalClassName() + ": number = '" + number + "'");
							
				        	Uri numUri = Uri.parse("tel:" + number);
							Intent callIntent = new Intent(Intent.ACTION_DIAL, numUri);
							startActivityForResult(callIntent, COMMAND_ANALYSER_REQUEST_CODE);
							
				        	break;
				        }
					 
					}
				} 
				else {
					Log.d(MainActivity.TAG, this.getLocalClassName() + ": Pattern '" + rawPattern + "' is incorrect");
				}
			}
			
			if (!found)	
			{
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
			
			Log.e(MainActivity.TAG, "Reading file with patterns failed", e);
			
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
		Log.d(MainActivity.TAG, this.getLocalClassName() + ": got result, requestCode=" + requestCode + ", resultCode=" + resultCode);

		if (requestCode == COMMAND_ANALYSER_REQUEST_CODE) 
		{			
			Intent resultIntent = new Intent();
			setResult(RESULT_OK, resultIntent);
			resultIntent.putExtra("command", command); 
			
			if (resultCode == RESULT_CANCELED) {
		        Toast.makeText(this, "The app execution process is canceled", Toast.LENGTH_LONG).show();
			}		
		}
		
		super.onActivityResult(requestCode, resultCode, data);
		finish();
	}

}
