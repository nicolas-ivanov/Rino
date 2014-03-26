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
			
			mistakesWriter.write(String.format("%-50s %-10s %-30s %-10s " +
					"%6s %6s %6s %6s %6s %6s %6s\n", 
					"Original text", "O.label", "Original params", "P.label", 
					"call", "sms", "mail", "search", "site", "alrm", "blnc"));
			
			String originalsLine;
			String predictionsLine = predictionsReader.readLine(); // labels 1 2 3 4 5 ...
			
			while ((originalsLine = originalsReader.readLine()) != null) {
				
				if (originalsLine.length() == 0)
					continue;
				
				predictionsLine = predictionsReader.readLine();

				Matcher originalsMatcher = originalsPattern.matcher(originalsLine);
				Matcher predictionsMatcher = predictionsPattern.matcher(predictionsLine);
				
				if (!originalsMatcher.matches()) {
					System.out.println("Line '" + originalsLine + "' is incorrect");
					break;
				}
				else if (!predictionsMatcher.matches()) {
					System.out.println("Line '" + predictionsLine + "' is incorrect");
					break;
				}

				String originalsID = originalsMatcher.group(1);
				String originalsLabel = originalsMatcher.group(2);
				String originalsText = originalsMatcher.group(3);
				String originalsParams = originalsMatcher.group(4);
				
				String predictionsID = predictionsMatcher.group(1);
				String predictionsProb = predictionsMatcher.group(2);
				String predictionsLabel = "";
				
				if (! originalsID.equals(predictionsID)) {
					mistakesWriter.write(String.format("%-50s %-10s %-30s ", originalsText, originalsLabel, originalsParams));

					Integer pID = new Integer(predictionsID);
					
					switch (pID) {
					case 1:	predictionsLabel = "a_call"; break;
					case 2:	predictionsLabel = "a_sms"; break;
					case 3:	predictionsLabel = "a_email"; break;
					case 4:	predictionsLabel = "a_search"; break;
					case 5:	predictionsLabel = "a_site"; break;
					case 6:	predictionsLabel = "a_alarm"; break;
					case 7:	predictionsLabel = "a_balance"; break;
					default:
						System.out.println("Label '" + pID + "' is incorrect");
						return;
					}
	
					mistakesWriter.write(String.format("%-10s", predictionsLabel));
					
					String[] probs = predictionsProb.split(" ");
					for (int i = 0; i < probs.length; i++)
						mistakesWriter.write(String.format(" %6.2f", new Float(probs[i])));
					
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