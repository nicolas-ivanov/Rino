package com.example.rino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.AsyncTask;
import android.util.Log;


public class TypeTagger extends AsyncTask<String, String, ArrayList<ArrayList<Boolean>>> {

	
	private MainActivity mainActivity;
	private InputStream patternsStream;
	private BufferedReader patternsReader;

	private static final Pattern structurePattern = Pattern.compile("(\\w+)\\t+([^\\t]+)");
	
	TypeTagger(MainActivity main, InputStream stream){
		mainActivity = main;
		patternsStream = stream;
		patternsReader = new BufferedReader(new InputStreamReader(patternsStream));
	}
		
	
    @Override
    protected void onPreExecute() {
    	super.onPreExecute();      
		Log.d(MainActivity.TAG, "AsyncTask: created");
    }

    
    @Override
    protected ArrayList<ArrayList<Boolean>> doInBackground(String... commands) 
    {    	
    	try {
        	String[] lexSeq = commands[0].split(" ");
        	ArrayList<ArrayList<Boolean>> obsSeq = new ArrayList<ArrayList<Boolean>>();

        	if (patternsReader.markSupported())
        		patternsReader.mark(1);
    		
    		for (String lex: lexSeq) 
    		{
				String rawPattern;
    			patternsReader.reset();
		    	ArrayList<Boolean> obs = new ArrayList<Boolean>();
				
    			while ((rawPattern = patternsReader.readLine()) != null) 
    			{
    				Matcher structureMatcher = structurePattern.matcher(rawPattern);
    				
    				// work only with correct type patterns
    				if (structureMatcher.matches()) 
    				{
    					Pattern typePattern = Pattern.compile(structureMatcher.group(2));
    					Matcher typeMatcher = typePattern.matcher(lex);
    					obs.add(typeMatcher.matches());
    					
    				}	
    				else {
    					Log.d(MainActivity.TAG, "AsyncTask: Pattern '" + rawPattern + "' is incorrect");
    					return null;
    				}
    			}
				obsSeq.add(obs);
    		}
    		return obsSeq;

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

    
/*    @Override
    protected void onProgressUpdate(String... answer) {
    	super.onProgressUpdate(answer);
  		mainActivity.addAnswer(answer[0]);
    }*/
    
    
    @Override
    protected void onPostExecute(ArrayList<ArrayList<Boolean>> res) {
    	Log.d(MainActivity.TAG, "AsyncTask: finished");
	    super.onPostExecute(res);
	    mainActivity.endTypeTagger();
	}
    
}