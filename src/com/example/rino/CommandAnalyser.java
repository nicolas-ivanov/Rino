package com.example.rino;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class CommandAnalyser extends Activity {

	public static final int COMMAND_ANALYSER_REQUEST_CODE = 41;
	public static final int SUB_ACTIVITY_CALL_REQUEST_CODE = 401;
	
	private String command;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(MainActivity.TAG, this.getLocalClassName() + ": created");

	    command = getIntent().getStringExtra("command");
		Log.d(MainActivity.TAG, this.getLocalClassName() + ": command = '" + command + "'");
	    
		// Applications' launcher should be here 
		// Classes 'pattern' and 'matcher' should be used for command analyzing 
		
		
		// Direct use of Pattern:
		Pattern phoneCallPattern_1 = Pattern.compile("hi (\\S+)");
		Matcher m = phoneCallPattern_1.matcher(command);

		Log.d(MainActivity.TAG, this.getLocalClassName() + ": m.matches() = '" + m.matches() + "'");
		
		if (m.matches() && m.group(1).equals("позвони")) {
			Log.d(MainActivity.TAG, this.getLocalClassName() + ": m.group(1) = '" + m.group(1) + "'");
			
			Uri number = Uri.parse("tel:100");
			Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
			startActivityForResult(callIntent, COMMAND_ANALYSER_REQUEST_CODE);
			
		}
		else {
			Intent resIntent = new Intent();
			setResult(RESULT_OK, resIntent);
			resIntent.putExtra("command", command); 
		    
			finish();	
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(MainActivity.TAG, this.getLocalClassName() + ": got result, requestCode=" + requestCode + ", resultCode=" + resultCode);
		
		switch (requestCode) {
		case COMMAND_ANALYSER_REQUEST_CODE:
			Log.d(MainActivity.TAG, this.getLocalClassName() + ": got result, resultCode = '" + resultCode + "'");

			
			break;
		}

		Intent resIntent = new Intent();
		setResult(RESULT_OK, resIntent);
		resIntent.putExtra("command", command); 
		
		super.onActivityResult(requestCode, resultCode, data);
		finish();
	}

}
