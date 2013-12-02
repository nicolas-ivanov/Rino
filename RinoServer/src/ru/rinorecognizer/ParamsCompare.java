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

public class ParamsCompare {

	private static final Pattern originalsPattern = Pattern.compile("(-?\\d{1,2}) +(.*)# (.*)");
	private static final Pattern predictionsPattern = Pattern.compile("(-?\\d{1,2}) (.*)");

	private void compare(String originalsFile, String predictionsFile, String mistakesFile) 
	{    	
		BufferedReader originalsReader = null;
		BufferedReader predictionsReader = null;
		BufferedWriter mistakesWriter = null;
		
		try {
			originalsReader = new BufferedReader(new InputStreamReader(new FileInputStream(originalsFile)));
			predictionsReader = new BufferedReader(new InputStreamReader(new FileInputStream(predictionsFile)));
			mistakesWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mistakesFile)));
 
			mistakesWriter.write(String.format("  %-16s %-8s %-31s %-9s ",
					"Original text", "O.label", "Original params", "P.label"));			
			
			String originalsLine;
			String savedLines = "";
			Boolean mistakeIsFound = false;
			
			// Process the first line of predictions file ("labels 1 2 3 4 5 ...")
			String predictionsLine = predictionsReader.readLine();
			String[] prIDs = predictionsLine.split(" ");
			
			for (int i = 1; i < prIDs.length; i++)
				mistakesWriter.write(String.format(" %-6s", "[" + prIDs[i] + "]"));
			
			mistakesWriter.write("\n\n");
			
			
			while ((originalsLine = originalsReader.readLine()) != null) {
				
				if (originalsLine.length() == 0) {
					
					if (mistakeIsFound) {  
						mistakesWriter.write(savedLines + "\n\n");
						mistakeIsFound = false;
					}
					savedLines = "";
					continue;	
				}
				
				predictionsLine = predictionsReader.readLine();

				// Check lines structure
				Matcher originalsMatcher = originalsPattern.matcher(originalsLine);
				Matcher predictionsMatcher = predictionsPattern.matcher(predictionsLine);
				
				if (!originalsMatcher.matches()) {
					System.out.println("OriginalsFile: line'" + originalsLine + "' is incorrect");
					break;
				}
				else if (!predictionsMatcher.matches()) {
					System.out.println("PredictedFile: line '" + predictionsLine + "' is incorrect");
					break;
				}
				

				String originalsID = originalsMatcher.group(1);
				String originalsParams = originalsMatcher.group(2);
				String originalsText = originalsMatcher.group(3);
				
				String predictionsID = predictionsMatcher.group(1);
				String predictionsProb = predictionsMatcher.group(2);
				
				// Compare indices
				if (!originalsID.equals(predictionsID)) {
					mistakeIsFound = true;
					savedLines += "! ";
				}
//				else if (mistakeIsFound) {
//					savedLines += "  ";
//				}
				else
					savedLines += "  ";
				
				
				savedLines += String.format("%-15s %3s %5s %-30s %3s %5s", 
						originalsText, originalsID, "", originalsParams, predictionsID, "");

				String[] probs = predictionsProb.split(" ");
				for (int i = 0; i < probs.length; i++)
					savedLines += String.format(" %6.2f", new Float(probs[i]));
				
				savedLines += "\n";			
					
			}
			mistakesWriter.flush();
			
		}	
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	 
		finally {
			try {
				originalsReader.close();
				predictionsReader.close();
				mistakesWriter.close();
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
			System.out.println("ParamsCompare 1: wrong parameters number: " + args.length);
			return;
		}		
		String data = args[0];
		String modelName = args[1];		
		String path = "../train/";
		
		String original = path + modelName + "/verbose_" + data;
		String predicted = path + modelName + "/predicted_" + data;
		String mistakes = path + modelName + "/mistakes_" + data;
		
		ParamsCompare c = new ParamsCompare();
		c.compare(original, predicted, mistakes);
	}

}