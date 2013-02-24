package com.example.rino;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class TextButton extends Activity{

	public static final int TEXT_BUTTON_REQUEST_CODE = 31;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		Log.d(MainActivity.TAG, this.getLocalClassName() + ": created");
		
	    String str = getIntent().getStringExtra("text");

		Log.d(MainActivity.TAG, this.getLocalClassName() + ": str = " + str);
        		
		ArrayList<String> res = new ArrayList<String>();
        res.add(str);

		Intent resIntent = new Intent();
		resIntent.putExtra("res", res);
		setResult(RESULT_OK, resIntent);

		finish();
	}
}
