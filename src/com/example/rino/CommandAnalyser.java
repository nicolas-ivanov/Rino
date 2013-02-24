package com.example.rino;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class CommandAnalyser extends Activity {

	public static final int COMMAND_ANALYSER_REQUEST_CODE = 41;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(MainActivity.TAG, this.getLocalClassName() + ": created");

	    String command = getIntent().getStringExtra("command");
		Log.d(MainActivity.TAG, this.getLocalClassName() + ": command = '" + command + "'");
	    
		// Applications' launcher should be here 
		// Classes 'pattern' and 'matcher' should be used for command analyzing 
		
		String[] words = command.split(" "); // should be replaced with proper handling
	
		Intent resIntent = new Intent();
		setResult(RESULT_OK, resIntent);
		resIntent.putExtra("words", words); 

		finish();
	    
	}

}
