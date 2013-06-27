package com.example.rino;

import android.os.AsyncTask;
import android.util.Log;


public class FramingTask extends AsyncTask<String, String, String> {

	private MainActivity mainActivity;
	private SvmClassifier svmClassifier;

	
	FramingTask(MainActivity main, SvmClassifier svm){
		mainActivity = main;
		svmClassifier = svm; 
	}

    
    @Override
    protected String doInBackground(String... commands) 
    {    	
    	ParamsGetter pGetter = new ParamsGetter(mainActivity); 
    	double[] params = pGetter.getParams(commands[0]);

    	return svmClassifier.getCommandType(params);
	}
    
    
    @Override
    protected void onPostExecute(String res) {
    	Log.d(MainActivity.TAG, "AsyncTask: finished");
	    super.onPostExecute(res);
	    mainActivity.endFramingTask();
	}
	
}