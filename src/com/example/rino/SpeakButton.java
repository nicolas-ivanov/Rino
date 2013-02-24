package com.example.rino;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;

public class SpeakButton extends Activity{

	public static final int SPEAK_BUTTON_REQUEST_CODE = 21;
	public static final int VOICE_RECOGNITION_REQUEST_CODE = 22;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		Log.d(MainActivity.TAG, this.getLocalClassName() + ": created");
		startVoiceRecognitionActivity();
	}

	public void startVoiceRecognitionActivity() {		
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
		
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);				
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(MainActivity.TAG, this.getLocalClassName() + ": got result, requestCode=" + requestCode + ", resultCode=" + resultCode);
		
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
			// Fill the list view with the strings the recognizer thought it could have heard
			ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			Log.d("Speak Button", "recognitionResults.get(0)" + res.get(0));

			Intent resultIntent = new Intent();
			resultIntent.putExtra("res", res);
			setResult(RESULT_OK, resultIntent);		
		}

		super.onActivityResult(requestCode, resultCode, data);
		finish();
	}
}




