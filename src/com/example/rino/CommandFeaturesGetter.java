package com.example.rino;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class CommandFeaturesGetter {

	private static final Pattern structurePattern = Pattern.compile("([\\d,]+)\\t+(\\w+)\\t+([^\\t~]+)");
	private InputStream patternsStream;
	private BufferedReader patternsReader;
	
	
	CommandFeaturesGetter(MainActivity main){
		patternsStream = main.getApplicationContext().getResources().openRawResource(R.raw.psvm);
		patternsReader = new BufferedReader(new InputStreamReader(patternsStream));
	}	


	public double[] getParams(String command) 
	{    	
		try {
			
	    	if (patternsReader.markSupported())
	    		patternsReader.mark(1);
	    	else{
				System.out.println("Mark is not supported");
				return null;
			}
	    	
	    	// Check "psvm" file for errors and get parameters number
			String rawPattern;
			int paramsNum = 0;
			
			while ((rawPattern = patternsReader.readLine()) != null) {
				
				if (rawPattern.equals(""))
					continue;	// skip empty lines
				
				paramsNum++;
				Matcher structureMatcher = structurePattern.matcher(rawPattern);
				
				if (! structureMatcher.matches()) {
					System.out.println("Pattern '" + rawPattern + "' is incorrect");
					return null;
				}
			}
			patternsReader.reset();
			
			
				
			String[] words = command.split(" ");
			double[] pVector = new double[paramsNum];
			
			for (int i=0; i<pVector.length; i++)
				pVector[i] = 0.0;
			
			for (int i=0; i<words.length; i++) {	
				String w = words[i];			
				int pNum = 0;
				
				while ((rawPattern = patternsReader.readLine()) != null) {

					if (rawPattern.equals(""))
						continue;	// skip empty lines
					
					Matcher structureMatcher = structurePattern.matcher(rawPattern);
					// work only with correct type patterns
					if (! structureMatcher.matches()) {
						System.out.println("Pattern '" + rawPattern + "' is incorrect");
						break;
					}					

					Pattern typePattern = Pattern.compile(structureMatcher.group(3));
					Matcher typeMatcher = typePattern.matcher(w);
								
					if (typeMatcher.matches())						
						pVector[pNum]++;

					pNum++;
				}

				patternsReader.reset();
			}
			return pVector;	
	
		} 
		catch (IOException e) {
			Log.d(MainActivity.TAG, "Reading file with patterns failed", e);
		} 
		finally {
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
