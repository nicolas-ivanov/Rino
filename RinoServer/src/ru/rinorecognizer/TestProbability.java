package ru.rinorecognizer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestProbability {

	private static final Pattern predictedPattern = Pattern.compile("(-?\\d+) (.*)");

	private void collectProbabilities(String predictedFile) 
	{    	
		BufferedReader predictedReader = null;
		
		try {
			predictedReader = new BufferedReader(new InputStreamReader(new FileInputStream(predictedFile)));
			
			float probSum = 0;
			float probMax = 0;
			float probMin = 1;
			int probNum = 0;

			String predictedLine;
			
			// get correspondence between classes' indexes and probability's order
			predictedLine = predictedReader.readLine();
			predictedLine = predictedLine.replaceFirst("labels ", "");
			String[] corr = predictedLine.split(" "); 
			
			
			while ((predictedLine = predictedReader.readLine()) != null) {
										
				Matcher predictedMatcher = predictedPattern.matcher(predictedLine);
				if (!predictedMatcher.matches()) {
					System.out.println(this.toString()+ ": Line '" + predictedLine + "' is incorrect");
					break;
				}
				String predicted_id = predictedMatcher.group(1);
				String[] predicted_probs = predictedMatcher.group(2).split(" ");
				
				// find probability for the current word
				Boolean found = false;
				int i = 0;
				while ((i < corr.length) && !(found = predicted_id.equals(corr[i]))) {
					i++;
				}
				
				if (!found) {
					System.out.println(this.toString() + ": No matching: " + predicted_id + " in " + Arrays.toString(corr));
					break;
				}
				else {						
					Float probability = new Float(predicted_probs[i]);				

					probNum++;
					probSum += probability;
					
					if (probability > probMax)
						probMax = probability;
					else if (probability < probMin)
						probMin = probability;
				}
			}
			System.out.print(String.format("%4.2f %4.2f %4.2f", probSum / probNum, probMin, probMax));
			
		}	
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	 
		finally {
			try {
				predictedReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public static void main (String[] args) 
	{		
//		String path = "/home/nicolas/Dropbox/Diploma/svm/train/";
//		String data = "test";
//		String modelName = "action";
		
		if (args.length != 2) {
			System.out.println("ParamsSupport: wrong parameters number: " + args.length);
			return;
		}		
		String data = args[0];
		String modelName = args[1];
		String path = "../train/";
		
		String predicted = path + modelName + "/predicted_" + data;
		
		TestProbability c = new TestProbability();
		c.collectProbabilities(predicted);
	}

}