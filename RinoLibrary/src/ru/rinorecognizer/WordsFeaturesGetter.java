package ru.rinorecognizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import ru.rinorecognizer.IdTranslator;

public class WordsFeaturesGetter {
	
	private static final Pattern structurePattern = Pattern.compile("([-?\\d,]+)\\t+(\\w+)\\t+([^\\t~]+)");

	
	public int[] getLabels(ExtendedCommand extCommand) 
	{    	
		try {			
			/// Start of main section ///////////////////////////////////////////////////
			
			String command = extCommand.curCommand;
			
			String[] words = command.split(" ");
			int[] wordsLabels = new int[words.length];

			// Get a label value for every word of the command
			for (int k = 0; k < words.length; k++) {

				BufferedReader patternsReader = new PatternsHandler().getPatternsReader();

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
	
	

	public float[][] getVectors(ExtendedCommand extCommand) 
	{    	
		try {			
			String command = extCommand.curCommand;
			int expParam = extCommand.expParameter;
			
			int semSetsNum = new PatternsHandler().getSemanticSetsNum();
			int paramsNum = IdTranslator.getParamsNum();
			int wBlockLength = semSetsNum + 1; // number of semantic groups + a value relative position in command
			
			final int leftLeafSize = 2;
			final int rightLeafSize = 2;
			int windowSize = 1 + leftLeafSize + rightLeafSize;
			
			int windowLength = windowSize * wBlockLength;
			int vLength = 
					windowLength
					+ paramsNum 	// for encoding expected parameter index
					+ paramsNum;	// for encoding label id of the previous word

			
			/// Start of main section ///////////////////////////////////////////////////
			
			String[] words = command.split(" ");
			float[][] wordsVectors = new float[words.length][];

			// Get parameters vector for every word of the command
			for (int wNum = 0; wNum < words.length; wNum++) {
				
				float[] wVector = new float[wBlockLength];
				
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
				BufferedReader patternsReader = new PatternsHandler().getPatternsReader();
				
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
			
			
			for (int curr_word_num = 0; curr_word_num < wordsVectors.length; curr_word_num++) {
				
				float[] tVector = new float[vLength]; 

				// get word's own parameters
				for (int i = 0; i < wBlockLength; i++) {
					tVector[i] = wordsVectors[curr_word_num][i];
				}
				
			
				// get left leaf parameters
				for (int leaf_word_num = 0; leaf_word_num < leftLeafSize; leaf_word_num++) {
					
					int leaf_word_pos = curr_word_num - leftLeafSize + leaf_word_num;
					
					if (leaf_word_pos >= 0)
						for (int p = 0; p < wBlockLength; p++)
							tVector[wBlockLength * (leaf_word_num + 1) + p] = wordsVectors[leaf_word_pos][p];
				}
				
				
				// get right leaf parameters
				for (int leaf_word_num = 0; leaf_word_num < rightLeafSize; leaf_word_num++) {
					
					int leaf_word_pos = curr_word_num + leaf_word_num + 1;
					
					if (leaf_word_pos < words.length)
						for (int p = 0; p < wBlockLength; p++)
							tVector[wBlockLength * (1 + leftLeafSize + leaf_word_num) + p] = wordsVectors[leaf_word_pos][p];
				}
				
				// encode the expected parameter index
				tVector[windowLength - 1 + expParam] = 1;
				
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
