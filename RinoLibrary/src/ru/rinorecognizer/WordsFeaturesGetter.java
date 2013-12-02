package ru.rinorecognizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordsFeaturesGetter {
	
	private static final Pattern structurePattern = Pattern.compile("([-?\\d,]+)\\t+(\\w+)\\t+([^\\t~]+)");

	
	private BufferedReader getBufferedReader() {
		InputStream input = getClass().getResourceAsStream("patterns.txt");
		return new BufferedReader(new InputStreamReader(input));
	}
	
	
	private int getParamsNum() throws IOException {
		BufferedReader patternsReader = getBufferedReader();
		String rawPattern;
		int paramsNum = 0;

		while ((rawPattern = patternsReader.readLine()) != null) {
			if (rawPattern.equals(""))
				continue; // skip empty lines

			paramsNum++;
		}
		patternsReader.close();
		
		return paramsNum;
	}
	

	public int[][] getVectors(String command) 
	{    	
		try {
			int paramsNum = getParamsNum();
//			String command = extCommand.curCommand;
			
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
				
				BufferedReader patternsReader = getBufferedReader();
				String rawPattern;
				
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
				patternsReader.close();	
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
			
			return trigramsVectors;
	
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}	

	public int[] getLabels(String command) 
	{    	
		try {			
			/// Start of main section ///////////////////////////////////////////////////
			
			String[] words = command.split(" ");
			int[] wordsLabels = new int[words.length];

			// Get labels vector for every word of the command
			for (int k = 0; k < words.length; k++) {

				BufferedReader patternsReader = getBufferedReader();
				Boolean wordFound = false;				
				String rawPattern;
				String w = words[k];
				
				if (w.length() > 0) {
					if (w.charAt(0) == '_') {
						w = w.substring(1);
						wordsLabels[k] = -1;
						wordFound = true;
					}
				}
				
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
		
					if (typeMatcher.matches()) {
						
						if (!wordFound) {
							wordsLabels[k] = Integer.parseInt(structureMatcher.group(1));
							wordFound  = true;
						}
					}
				}
				if (!wordFound)
					wordsLabels[k] = 0;
					
				patternsReader.close();	
			}
			
			return wordsLabels;
	
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}

}
