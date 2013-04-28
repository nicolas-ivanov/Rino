package com.example.rino;

import java.io.IOException;
import java.util.ArrayList;

import android.os.AsyncTask;
import android.util.Log;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;

import com.example.rino.MainActivity.Token;


public class TaggerTask extends AsyncTask<String, String, ArrayList<MainActivity.Token>> {

	
	private MainActivity mainActivity;

	
	TaggerTask(MainActivity main){
		mainActivity = main;
	}
		
	
    @Override
    protected void onPreExecute() {
    	super.onPreExecute();      
		Log.d(MainActivity.TAG, "AsyncTask: created");
    }

    
    @Override
    protected ArrayList<MainActivity.Token> doInBackground(String... commands) 
    {    	
    	NaiveTagger tagger = new NaiveTagger(mainActivity); 
    	ArrayList<ObservationVector> obsSeq = tagger.getTags(commands[0]);
    	
    	HmmClassifier hmm = new HmmClassifier();
    	ArrayList<MainActivity.Token> tokenList = null; 
    	
    	try {
			tokenList = new ArrayList<MainActivity.Token>();
			int[] states = hmm.getStatesSequence(obsSeq);
	    	String[] words = commands[0].split(" ");
	    	
	    	for (int i = 0; i < words.length; i++) {
	    		MainActivity.Token t = new Token();
	    		t.lexem = words[i];
	    		t.label = states[i];
	    		tokenList.add(t);
	    	}
	    	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return tokenList;
	}
    
    
    @Override
    protected void onPostExecute(ArrayList<MainActivity.Token> res) {
    	Log.d(MainActivity.TAG, "AsyncTask: finished");
	    super.onPostExecute(res);
	    mainActivity.endTaggerTask();
	}
    
}