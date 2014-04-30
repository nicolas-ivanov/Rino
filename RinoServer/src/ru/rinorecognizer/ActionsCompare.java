package ru.rinorecognizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionsCompare {

	private static final Pattern originalsPattern = Pattern.compile("(\\d{1,2})\\t(\\w*)\\t(.*)\\t (.*)");
	private static final Pattern predictionsPattern = Pattern.compile("(\\d{1,2}) (.*)");

	private void compare(String originalsFile, String predictionsFile, String mistakesFile) 
	{    	
		BufferedReader originalsReader = null;
		BufferedReader predictionsReader = null;
		BufferedWriter mistakesWriter = null;
		
		try {
			originalsReader = new BufferedReader(new InputStreamReader(new FileInputStream(originalsFile)));
			predictionsReader = new BufferedReader(new InputStreamReader(new FileInputStream(predictionsFile)));
			mistakesWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mistakesFile)));
			
			mistakesWriter.write(String.format("%-125s %-10s %-80s %-13s ", 
					"Original text", "O.label", "Original params", "P.label"));
			
			String originalsLine;
			
			// Process the first line of predictions file ("labels 1 2 3 4 5 ...")
			String predictionsLine = predictionsReader.readLine();
			String[] prIDs = predictionsLine.split(" ");
			
			for (int i = 1; i < prIDs.length; i++)
				mistakesWriter.write(String.format(" %-8s", 
						IdTranslator.getActionEnum(Integer.parseInt(prIDs[i])).toString().toLowerCase(Locale.ENGLISH)));
			
			mistakesWriter.write("\n\n");
			
			
			while ((originalsLine = originalsReader.readLine()) != null) {
				
				if (originalsLine.length() == 0)
					continue;
				
				predictionsLine = predictionsReader.readLine();

				Matcher originalsMatcher = originalsPattern.matcher(originalsLine);
				Matcher predictionsMatcher = predictionsPattern.matcher(predictionsLine);
				
				if (!originalsMatcher.matches()) {
					System.out.println(this.toString()+ ": Line '" + originalsLine + "' is incorrect");
					break;
				}
				else if (!predictionsMatcher.matches()) {
					System.out.println(this.toString()+ ": Line '" + predictionsLine + "' is incorrect");
					break;
				}

				String originalsID = originalsMatcher.group(1).trim();
				String originalsLabel = originalsMatcher.group(2).trim();
				String originalsText = originalsMatcher.group(3).trim();
				String originalsParams = originalsMatcher.group(4).trim();
				
				String predictionsID = predictionsMatcher.group(1);
				String predictionsProb = predictionsMatcher.group(2);
				IdTranslator.ActionType predictionsLabel;
				
				if (! originalsID.equals(predictionsID)) {
					mistakesWriter.write(String.format("%-125s %-10s %-80s ", originalsText, originalsLabel, originalsParams));

					Integer pID = new Integer(predictionsID);
					predictionsLabel = IdTranslator.getActionEnum(pID);
	
					mistakesWriter.write(String.format("%-10s", predictionsLabel.toString().toLowerCase(Locale.ENGLISH)));
					
					String[] probs = predictionsProb.split(" ");
					for (int i = 0; i < probs.length; i++)
						mistakesWriter.write(String.format(" %8.2f", new Float(probs[i])));
					
					mistakesWriter.write("\n");
					
				}			
					
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
//		String path = "/home/nicolas/Dropbox/Diploma/svm/train/action/";
//		String data = "collection";
		
		if (args.length != 1) {
			System.out.println("ActionCompare: wrong parameters number: " + args.length);
			return;
		}		
		String data = args[0];
		String path = "../main/action/";
		
		String original = path + "verbose_" + data;
		String predicted = path + "predicted_" + data;
		String mistakes = path + "mistakes_" + data;
		
		ActionsCompare c = new ActionsCompare();
		c.compare(original, predicted, mistakes);
	}

}