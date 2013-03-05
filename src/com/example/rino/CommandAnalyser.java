package com.example.rino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class CommandAnalyser extends Activity {

	public static final int COMMAND_ANALYSER_REQUEST_CODE = 41;
	public static final int SUB_ACTIVITY_CALL_REQUEST_CODE = 401;
	private static final Pattern commonPattern = Pattern.compile("(\\d*)#([^#]*)#(.*)");
	private String command;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(MainActivity.TAG, this.getLocalClassName() + ": created");

	    command = getIntent().getStringExtra("command");
		Log.d(MainActivity.TAG, this.getLocalClassName() + ": command = '" + command + "'");
		
		
		InputStream patternsStream = this.getApplicationContext().getResources().openRawResource(R.raw.patterns);  // Why not to buffer input?
		BufferedReader patternsReader = new BufferedReader(new InputStreamReader(patternsStream));
		
		try {
			String line;
			while ((line = patternsReader.readLine()) != null) {
				String rawPattern = line;
				Matcher commonMatcher = commonPattern.matcher(rawPattern);			
				Log.d(MainActivity.TAG, this.getLocalClassName() + ": commonMatcher = '" + commonMatcher.matches() + "'" + rawPattern);
				
				if (commonMatcher.matches()) {
					Log.d(MainActivity.TAG, this.getLocalClassName() + ": group(1) = '" + commonMatcher.group(1) + 
							"' group(2) = '" + commonMatcher.group(2) + "' group(3) = '" + commonMatcher.group(3) + "'");
					
					Pattern commandPattern = Pattern.compile(commonMatcher.group(2));
					Matcher commandMatcher = commandPattern.matcher(command);
					Log.d(MainActivity.TAG, this.getLocalClassName() + ": commandMatcher = '" + commandMatcher.matches() + "'");
					
					if (commandMatcher.matches()) {
						Log.d(MainActivity.TAG, this.getLocalClassName() + ": group(1) = '" + commandMatcher.group(1) + "'");
						
						Intent resIntent = new Intent();
						setResult(RESULT_OK, resIntent);
						resIntent.putExtra("command", command); 

				        Toast.makeText(this, "Matches some pattern", Toast.LENGTH_LONG).show();
				        
						// Applications' launcher should be here? 
					    
						finish();
					}
				} else {
					Log.d(MainActivity.TAG, this.getLocalClassName() + ": Pattern '" + rawPattern + "' is incorrect");
				}
			}
			
			Intent resIntent = new Intent();
			setResult(RESULT_OK, resIntent);
			resIntent.putExtra("command", command); 

	        Toast.makeText(this, "No matches", Toast.LENGTH_LONG).show();
		    
			finish();
			
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
		
		if (requestCode == COMMAND_ANALYSER_REQUEST_CODE) {
			Log.d(MainActivity.TAG, this.getLocalClassName() + ": got result, resultCode = '" + resultCode + "'");

			Intent resultIntent = new Intent();
			setResult(resultCode, resultIntent);
			resultIntent.putExtra("command", command); 
			
		}
		
		super.onActivityResult(requestCode, resultCode, data);
		finish();
	}

}
