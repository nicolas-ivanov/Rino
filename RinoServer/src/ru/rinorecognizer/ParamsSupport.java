package ru.rinorecognizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParamsSupport {

	private static final Pattern scaledPattern = Pattern.compile(".*?(\\d+:.*)?");
	private static final Pattern supportPattern = Pattern.compile(".*?(\\d+:.*)?");
	private static final Pattern predictedPattern = Pattern.compile("(-?\\d+) (.*)");

	private void collectSupportCommands(
			String modelFile, 
			String scaledFile, 
			String verboseFile, 
			String predictedFile, 
			String supportFile) 
	{    	

		BufferedReader modelReader = null;
		BufferedReader scaledReader = null;
		BufferedReader verboseReader = null;
		BufferedReader predictedReader = null;
		BufferedWriter supportWriter = null;
		
		try {

			modelReader = new BufferedReader(new InputStreamReader(new FileInputStream(modelFile)));
			supportWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(supportFile)));
			
			// read unnecessary lines
			for (int i = 0; i < 11; i++)
				modelReader.readLine();
			
			String supportLine;
			float probSum = 0;
			float probMax = 0;
			float probMin = 1;
			int probNum = 0;
			
			
			while ( (supportLine = modelReader.readLine()) != null) {

				Matcher supportMatcher = supportPattern.matcher(supportLine);
				if (!supportMatcher.matches()) {
					System.out.println(this.toString() + ": Line '" + supportLine + "' is incorrect");
					break;
				}
				String support_v = supportMatcher.group(1);
				
				// find a support vector and the corresponding command

				scaledReader = new BufferedReader(new InputStreamReader(new FileInputStream(scaledFile)));
				verboseReader = new BufferedReader(new InputStreamReader(new FileInputStream(verboseFile)));
				predictedReader = new BufferedReader(new InputStreamReader(new FileInputStream(predictedFile)));
				
				String scaledLine;
				String verboseLine;
				String predictedLine;
				String savedLines = "";
				
				// get correspondence between classes' indexes and probability's order
				predictedLine = predictedReader.readLine();
				predictedLine = predictedLine.replaceFirst("labels ", "");
				String[] corr = predictedLine.split(" "); 
				
				
				while ((scaledLine = scaledReader.readLine()) != null) {
					verboseLine = verboseReader.readLine();
					predictedLine = predictedReader.readLine();
					
					if (verboseLine.length() == 0) {
						savedLines = "";
						verboseLine = verboseReader.readLine();
					}


					Matcher scaledMatcher = scaledPattern.matcher(scaledLine);
					if (!scaledMatcher.matches()) {
						System.out.println(this.toString()+ ": Line '" + scaledLine + "' is incorrect");
						break;
					}
					String scaled_v = scaledMatcher.group(1);
					
					if (scaled_v == null)
						continue;
					
					if (scaled_v.equals(support_v)) {

						savedLines += String.format("%n%-60s", verboseLine);
						
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
							savedLines += String.format("%8.2f probability", probability);
							
							// save the rest of command words
							while ((verboseLine = verboseReader.readLine()).length() != 0)
								savedLines += "\n" + verboseLine;
								
							supportWriter.write(savedLines + "\n");
	//						supportWriter.flush();

							probNum++;
							probSum += probability;
							
							if (probability > probMax)
								probMax = probability;
							else if (probability < probMin)
								probMin = probability;
							
							break;
						}
					}
					else {
						savedLines += "\n" + verboseLine;
					}

				}
				
				scaledReader.close();
				verboseReader.close();
			}
			System.out.print(String.format("%4.2f %4.2f %4.2f", probSum / probNum, probMin, probMax));
			supportWriter.flush();
			
			supportWriter.flush();
			
		}	
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	 
		finally {
			try {
				scaledReader.close();
				verboseReader.close();
				supportWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	
	
	public static void main (String[] args) 
	{		
//		String path = "/home/nicolas/Dropbox/Diploma/svm/train/";
//		String data = "collection";
//		String modelName = "a_call";
		
		if (args.length != 2) {
			System.out.println("ParamsSupport: wrong parameters number: " + args.length);
			return;
		}		
		String data = args[0];
		String modelName = args[1];
		String path = "../main/";
		
		String model = path + modelName + "/model_" + modelName;
		String scaled = path + modelName + "/scaled_" + data;
		String verbose = path + modelName + "/verbose_" + data;
		String predicted = path + modelName + "/predicted_" + data;
		String support = path + modelName + "/support_" + data;
		
		ParamsSupport c = new ParamsSupport();
		c.collectSupportCommands(model, scaled, verbose, predicted, support);
	}

}