package com.example.rino;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.util.Log;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;

import com.example.rino.MainActivity.Token;


public class TaggerTask extends AsyncTask<String, String, ArrayList<MainActivity.Token>> {

	private MainActivity mainActivity;
	private HmmClassifier hmmClassifier;

	
	TaggerTask(MainActivity main, HmmClassifier hmm){
		mainActivity = main;
		hmmClassifier = hmm; 
	}

    
    @Override
    protected ArrayList<MainActivity.Token> doInBackground(String... commands) 
    {    	
    	NaiveTagger tagger = new NaiveTagger(mainActivity); 
    	ArrayList<ObservationVector> obsSeq = tagger.getTags(commands[0]);
    
    	ArrayList<MainActivity.Token> tokenList = null; 
    	
		tokenList = new ArrayList<MainActivity.Token>();
		int[] states = hmmClassifier.getStatesSequence(obsSeq);
    	String[] words = commands[0].split(" ");
    	
    	for (int i = 0; i < words.length; i++) {
    		MainActivity.Token t = new Token();
    		t.lexem = words[i];
    		t.label = states[i];
    		tokenList.add(t);
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