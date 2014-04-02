package ru.rinorecognizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParamsConvert {

	private static final Pattern sourcePattern = Pattern.compile("(\\w+)\\t([^\\t]+)(?:\\t+([#&] .+))?");	

	public void convert(String modelName, String trainDir, String outFile, String verboseFile, String allParamsFile)  
	{   
		
		BufferedReader dataReader = null;
		BufferedWriter fullWriter = null;
		BufferedWriter paramsWriter = null;
		BufferedWriter verboseWriter = null;
		
		try {			
			fullWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(allParamsFile)));
			paramsWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
			verboseWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(verboseFile)));
			
		
			// Process every command
			String line;
			File[] files = new File(trainDir).listFiles();
			
			for (File file : files) {
				
//				System.out.println("Params: " + file.toString());
				
			    if (file.isFile())
					dataReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

			
				while ((line = dataReader.readLine()) != null) {
					
					// skip empty lines and comments
					if (line.length() == 0) {
//						verboseWriter.write("\n");
						continue;
					} else if (line.charAt(0) == '#')
						continue;

					
					// parse line
					Matcher sourceMatcher = sourcePattern.matcher(line);
					if (!sourceMatcher.matches()) {
						System.out.println("ParamsConvert: Line '" + line + "' is incorrect");
						break;
					}
					String label = sourceMatcher.group(1);
					
					if (!modelName.equals(label)) 
						continue;
					
					
					// process commands
					String command = sourceMatcher.group(2);
					String comment = sourceMatcher.group(3);
					int expParam = 0;
					
					if (comment!=null) {
						if (comment.startsWith("& "))
							expParam = getExpParameterID(comment.replaceFirst("& ", ""));
					}
					
					ExtendedCommand extCommand = new ExtendedCommand();
					extCommand.curCommand = command;
					extCommand.expParameter = expParam;
					
	
					/// Main section ///////////////////////////////////////////////////
					
					WordsFeaturesGetter wfg = new WordsFeaturesGetter();
	
					String[] words = command.split(" ");
					int[] wordsLabels = wfg.getLabels(extCommand);
					int[][] wordsVectors = wfg.getVectors(extCommand);
	
					
					
					// Write parameters and labels to files
					
					for (int k = 0; k < wordsVectors.length; k++) {
	
						int[] tVector = wordsVectors[k];
						String fullString = wordsLabels[k] + "";
						String paramsString = wordsLabels[k] + "";
						String verboseString = String.format("%-5s", wordsLabels[k]);
	
						
						for (int j = 0; j < tVector.length; j++) {
							fullString += " " + tVector[j];
							
							if (tVector[j] != 0) {
								paramsString += " " + (j+1) + ":" + tVector[j];
								verboseString += " " + (j+1) + ":" + tVector[j];
							}
						}
						
						fullWriter.write(fullString + " \n");
						paramsWriter.write(paramsString + "\n");
						verboseWriter.write(String.format("%-40s # %s%n",verboseString, words[k]));
					}
					verboseWriter.newLine();
	
					/// End of main section ///////////////////////////////////////////////////
	
	//				dataWriter.flush();
	//				verboseWriter.flush();
					
				}
				fullWriter.flush();
				paramsWriter.flush();
				verboseWriter.flush();
			
			}
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	 
		finally {
			try {
				dataReader.close();
				fullWriter.close();
				paramsWriter.close();
				verboseWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public int getExpParameterID(String expParameter) {
		
		int paramID;
		
		switch (expParameter) {
		case "p_name": 		paramID = 1; break;
		case "p_number": 	paramID = 2; break;
		case "p_email": 	paramID = 3; break;
		case "p_site": 		paramID = 4; break;
		case "p_time": 		paramID = 5; break;
		case "p_quote": 	paramID = 6; break;
		default: 			paramID = 0;
		}
		
		return paramID;
	}
	
	
	
	public static void main (String[] args) 
	{		
		if (args.length != 2) {
			System.out.println("ParamsConvert: wrong parameters number: " + args.length);
			return;
		}
		String path = "../main/";
		String data = args[0];
		String modelName = args[1];
		
//		String path = "/home/nicolas/Develop/workspace/RinoServer/models/main/";
//		String data = "train";
//		String modelName = "a_alarm";

		
		String trainDir = path + data;
		String outFile = path + modelName + "/params_" + data;
		String verboseFile = path + modelName + "/verbose_" + data;
		String allParamsFile = path + modelName + "/full_" + data;
		
		ParamsConvert c = new ParamsConvert();
		c.convert(modelName, trainDir, outFile, verboseFile, allParamsFile);
	}
}