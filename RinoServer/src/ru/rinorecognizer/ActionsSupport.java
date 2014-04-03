package ru.rinorecognizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionsSupport {

	private static final Pattern scaledPattern = Pattern.compile(".*?(\\d+:.*)");
	private static final Pattern supportPattern = Pattern.compile(".*?(\\d+:.*)");
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
			String savedVLine = "0";
			float probSum = 0;
			float probMax = 0;
			float probMin = 1;
			int probNum = 0;
			
			
			while ( (supportLine = modelReader.readLine()) != null) {

				Matcher supportMatcher = supportPattern.matcher(supportLine);
				if (!supportMatcher.matches()) {
					System.out.println(this.toString()+ ": Line '" + supportLine + "' is incorrect");
					break;
				}
				String support_v = supportMatcher.group(1);
				
				// find support vector and corresponding command

				scaledReader = new BufferedReader(new InputStreamReader(new FileInputStream(scaledFile)));
				verboseReader = new BufferedReader(new InputStreamReader(new FileInputStream(verboseFile)));
				predictedReader = new BufferedReader(new InputStreamReader(new FileInputStream(predictedFile)));
				
				String scaledLine = null;
				String verboseLine = null;
				String predictedLine = null;				
				
				// skip unnecessary lines
				predictedLine = predictedReader.readLine();
				
				
				while ((scaledLine = scaledReader.readLine()) != null) {
					
					// get corresponding line in verboseFile
					do {
						verboseLine = verboseReader.readLine();
					}
					while (	(verboseLine != null) && (verboseLine.length() == 0) );

					// get corresponding line in predictedFile
					predictedLine = predictedReader.readLine();
					
					Matcher scaledMatcher = scaledPattern.matcher(scaledLine);
					if (!scaledMatcher.matches()) {
						System.out.println(this.toString()+ ": Line '" + scaledLine + "' is incorrect");
						break;
					}
					String scaled_v = scaledMatcher.group(1);
					
					if (scaled_v.equals(support_v)) {
						if (savedVLine.charAt(0) != verboseLine.charAt(0))
							supportWriter.write("\n");
						
						Matcher predictedMatcher = predictedPattern.matcher(predictedLine);
						if (!predictedMatcher.matches()) {
							System.out.println(this.toString() + ": Line '" + predictedLine + "' is incorrect");
							break;
						}
						Integer predicted_class = new Integer(predictedMatcher.group(1));
						String[] predicted_probs = predictedMatcher.group(2).split(" ");
						Float probability = new Float(predicted_probs[predicted_class - 1]);
						
						supportWriter.write(String.format("%6.4f \t", probability));						
						supportWriter.write(verboseLine + "\n");
						supportWriter.flush();

						probNum++;
						probSum += probability;
						
						if (probability > probMax)
							probMax = probability;
						else if (probability < probMin)
							probMin = probability;
						
						savedVLine = verboseLine;
						break;
					}

				}
				
				scaledReader.close();
				verboseReader.close();
				predictedReader.close();
			}
			System.out.print(String.format("%4.2f %4.2f %4.2f", probSum / probNum, probMin, probMax));
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
				predictedReader.close();
				supportWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	
	
	public static void main (String[] args) 
	{		
//		String path = "/home/nicolas/Dropbox/Diploma/svm/train/action/";
//		String data = "train";
		
		if (args.length != 1) {
			System.out.println("ActionSupport: wrong parameters number: " + args.length);
			return;
		}		
		String data = args[0];
		String path = "../main/action/";
		
		String model = path + "model_action";
		String scaled = path + "scaled_" + data;
		String verbose = path + "verbose_" + data;
		String predicted = path + "predicted_" + data;
		String support = path + "support_" + data;
		
		ActionsSupport c = new ActionsSupport();
		c.collectSupportCommands(model, scaled, verbose, predicted, support);
	}

}