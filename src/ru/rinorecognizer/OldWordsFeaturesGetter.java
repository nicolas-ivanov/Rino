package ru.rinorecognizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.rino.R;

import android.util.Log;

public class OldWordsFeaturesGetter {

	private static final Pattern structurePattern = Pattern.compile("([-?\\d,]+)\\t+(\\w+)\\t+([^\\t~]+)");
	private InputStream patternsStream;
	private BufferedReader patternsReader;
	
	
	OldWordsFeaturesGetter(MainActivity main){
		patternsStream = main.getApplicationContext().getResources().openRawResource(R.raw.patterns);
		patternsReader = new BufferedReader(new InputStreamReader(patternsStream));
	}	


	public int[][] getParams(String command) 
	{    	
		try {
			
	    	if (patternsReader.markSupported())
	    		patternsReader.mark(1);
	    	else {
				System.out.println("Mark is not supported");
				throw new IOException();
			}
	    	
	    	// Check "pattern" file for errors and get parameters number
			String rawPattern;
			int paramsNum = 0;
			
			while ((rawPattern = patternsReader.readLine()) != null) {
				
				if (rawPattern.equals(""))
					continue;	// skip empty lines
				
				paramsNum++;
				Matcher structureMatcher = structurePattern.matcher(rawPattern);
				
				if (! structureMatcher.matches()) {
					System.out.println("Pattern '" + rawPattern + "' is incorrect");
					throw new IOException();
				}
			}
			patternsReader.reset();
			
			
			/// Start of main section ///////////////////////////////////////////////////
			
			String[] words = command.split(" ");
			int[][] wordsVectors = new int[words.length][];

			// Get parameters vector for every word of the command
			for (int k = 0; k < words.length; k++) {
				
				int[] wVector = new int[paramsNum];
				
				for (int i=0; i<wVector.length; i++)
					wVector[i] = 0;
				
				String w = words[k];										
				int pNum = 0;
				patternsReader.reset();

				
				// Check if the word is a keyword of a certain set
				while ((rawPattern = patternsReader.readLine()) != null) {

					if (rawPattern.equals(""))
						continue;	// skip empty lines
					
					Matcher structureMatcher = structurePattern.matcher(rawPattern);
					
					// Check if the pattern is correct
					if (! structureMatcher.matches()) {
						System.out.println("Pattern '" + rawPattern + "' is incorrect");
						break;
					}
					
					Pattern typePattern = Pattern.compile(structureMatcher.group(3));
					Matcher typeMatcher = typePattern.matcher(w);
		
					if (typeMatcher.matches())	
						wVector[pNum]++;
					
					pNum++;
				}
				wordsVectors[k] = wVector;		
			}
			
			
			// Get parameters vectors for trigrams of words
			int paramsNum_2 = paramsNum * 2; 
			int paramsNum_3 = paramsNum * 3;
			int[][] trigramsVectors = new int[words.length][];
			
			
			for (int k = 0; k < wordsVectors.length; k++) {
				
				int[] tVector = new int[paramsNum_3];
				
				for (int i = 0; i < paramsNum; i++) {
					tVector[i] = wordsVectors[k][i]; 
					tVector[i + paramsNum] = (k == 0)? 0 : wordsVectors[k-1][i]; // "first word" check
					tVector[i + paramsNum_2] = (k == wordsVectors.length - 1)? 0 : wordsVectors[k+1][i]; // "last word" check
				}
				trigramsVectors[k] = tVector;
			}			
			/// End of main section ///////////////////////////////////////////////////
			
			return trigramsVectors;
	
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
