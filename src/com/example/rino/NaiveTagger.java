package com.example.rino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import be.ac.ulg.montefiore.run.jahmm.ObservationVector;

import android.util.Log;

public class NaiveTagger {

	private InputStream patternsStream;
	private BufferedReader patternsReader;

	private static final Pattern structurePattern = Pattern.compile("(\\w+)\\t+([^\\t]+)");
	
	NaiveTagger(MainActivity main){
		patternsStream = main.getApplicationContext().getResources().openRawResource(R.raw.phmm);
		patternsReader = new BufferedReader(new InputStreamReader(patternsStream));
	}	


	public ArrayList<ObservationVector> getTags(String command) 
	{    	
		try {
	    	String[] lexSeq = command.split(" ");
	    	ArrayList<ObservationVector> obsSeq = new ArrayList<ObservationVector>();
	
	    	if (patternsReader.markSupported())
	    		patternsReader.mark(1);
			
			for (int i=0; i<lexSeq.length; i++) 
			{
				String lex = lexSeq[i];
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
				
				double[] target = new double[obs.size()];
				for (int j = 0; j < target.length; j++) {
					target[j] = obs.get(j)? 1 : 0;
				}
				obsSeq.add(new ObservationVector(target));
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

}