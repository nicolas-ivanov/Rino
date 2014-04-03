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
	

	public int[] getLabels(ExtendedCommand extCommand) 
	{    	
		try {			
			/// Start of main section ///////////////////////////////////////////////////
			
			String command = extCommand.curCommand;
			
			String[] words = command.split(" ");
			int[] wordsLabels = new int[words.length];

			// Get labels vector for every word of the command
			for (int k = 0; k < words.length; k++) {

				BufferedReader patternsReader = getBufferedReader();
				Boolean wordFound = false;				
				String rawPattern;
				String w = words[k];
				
				if (w.length() > 0) {
					if (w.startsWith("_")) {
						w = w.replaceFirst("_", "");
						wordsLabels[k] = -1;
						wordFound = true;
					}
					if (w.startsWith("Time:")) {
						w = w.replaceFirst("Time:", "");
						wordsLabels[k] = 5;
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
	

	public float[][] getVectors(ExtendedCommand extCommand) 
	{    	
		try {
			int paramsNum = getParamsNum() + 1; // number of semantic groups + relative position in command 
			
			String command = extCommand.curCommand;
			int expParam = extCommand.expParameter;
			
			/// Start of main section ///////////////////////////////////////////////////
			
			String[] words = command.split(" ");
			float[][] wordsVectors = new float[words.length][];

			// Get parameters vector for every word of the command
			for (int wNum = 0; wNum < words.length; wNum++) {
				
				float[] wVector = new float[paramsNum];
				
				for (int i=0; i<wVector.length; i++)
					wVector[i] = 0;
				
				// Replace special marking from words
				String w = words[wNum];
				
				if (w.length() > 0) {
					w = w.replaceFirst("_", "");
					w = w.replaceFirst("Time:", "");
				}
				
				int pNum = 0;
				String rawPattern;				
				BufferedReader patternsReader = getBufferedReader();
				
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
				
				// add info about relative position in command
				{
					wVector[wVector.length - 1] = (float)wNum / words.length;
				}				
				
				wordsVectors[wNum] = wVector;	
				patternsReader.close();	
			}
			
			
			// Get parameters vectors for trigrams of words
			float[][] ngramsVectors = new float[words.length][];
			
			final int left_leaf_size = 2;
			final int right_leaf_size = 2;
			int window_size = 1 + left_leaf_size + right_leaf_size;
			
			for (int curr_word_num = 0; curr_word_num < wordsVectors.length; curr_word_num++) {
				
				float[] tVector = new float[window_size * paramsNum + 1]; // last +1 is for expected parameter index

				// get word's own params
				for (int i = 0; i < paramsNum; i++) {
					tVector[i] = wordsVectors[curr_word_num][i];
				}
				
			
				// get left leaf params
				for (int leaf_word_num = 0; leaf_word_num < left_leaf_size; leaf_word_num++) {
					
					int leaf_word_pos = curr_word_num - left_leaf_size + leaf_word_num;
					
					if (leaf_word_pos >= 0)
						for (int p = 0; p < paramsNum; p++)
							tVector[paramsNum * (leaf_word_num + 1) + p] = wordsVectors[leaf_word_pos][p];
				}
				
				
				// get right leaf params
				for (int leaf_word_num = 0; leaf_word_num < right_leaf_size; leaf_word_num++) {
					
					int leaf_word_pos = curr_word_num + leaf_word_num + 1;
					
					if (leaf_word_pos < words.length)
						for (int p = 0; p < paramsNum; p++)
							tVector[paramsNum * (1 + left_leaf_size + leaf_word_num) + p] = wordsVectors[leaf_word_pos][p];
				}
				
				// get the expected parameter index
				tVector[tVector.length - 1] = expParam;
				
				ngramsVectors[curr_word_num] = tVector;
			}	
			
			return ngramsVectors;
	
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}	

}
