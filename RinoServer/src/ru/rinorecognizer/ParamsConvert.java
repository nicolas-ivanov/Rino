package ru.rinorecognizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParamsConvert {

	private static final Pattern sourcePattern = Pattern.compile("(\\w+)\\t([^\\t]+)(?:\\t+([#&] .+))?");	
	
	DecimalFormat cuteFormat = new DecimalFormat("#.#");

	public void convert(String modelName, String trainDir, String outFile, String verboseFile, String fullFile, String allParamsFile)  
	{   
		
		BufferedReader dataReader = null;
		BufferedWriter fullWriter = null;
		BufferedWriter paramsWriter = null;
		BufferedWriter verboseWriter = null;
		BufferedWriter allParamsWriter = null;
		
		try {			
			fullWriter = new BufferedWriter(new FileWriter(fullFile));
			paramsWriter = new BufferedWriter(new FileWriter(outFile));
			verboseWriter = new BufferedWriter(new FileWriter(verboseFile));
			allParamsWriter = new BufferedWriter(new FileWriter(allParamsFile, true));
			
		
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
						System.out.println(this.toString() + ": Line '" + line + "' is incorrect");
						break;
					}
					String label = sourceMatcher.group(1);
					
					if (!modelName.equals(label)) 
						continue;
					
					
					// process commands
					String command = sourceMatcher.group(2);
					String comment = sourceMatcher.group(3);
					
					ExtendedCommand extCommand = new ExtendedCommand();
					extCommand.curCommand = command;
					extCommand.expParameter = 0;
					
					if (comment != null) {
						if (comment.startsWith("& "))
							extCommand.expParameter = IdTranslator.getParamOrdinal(comment.replaceFirst("& ", ""));
					}
					
	
					/// Main section ///////////////////////////////////////////////////
					
					WordsFeaturesGetter wfg = new WordsFeaturesGetter();
	
					String[] words = command.split(" ");
					int[] wordsLabels = wfg.getLabels(extCommand);
					float[][] wordsVectors = wfg.getVectors(extCommand);
	
					int saved_label_ordinal = 0;

					// Write parameters and labels to files
					for (int k = 0; k < wordsVectors.length; k++) {
	
						float[] tVector = wordsVectors[k];
						int startOfPrevLabelBlock = tVector.length - IdTranslator.getLabelsNum();

						// add encoded label id of the previous word at the end of each vector
						tVector[startOfPrevLabelBlock + saved_label_ordinal] = 1;
						saved_label_ordinal = wordsLabels[k];
						
						String fullString = wordsLabels[k] + "";
						String paramsString = wordsLabels[k] + "";
						String verboseString = String.format("%-5s", wordsLabels[k]);
	
						
						for (int j = 0; j < tVector.length; j++) {
							fullString += " " + tVector[j];
							
							if (tVector[j] != 0) {
								paramsString += " " + (j+1) + ":" + cuteFormat.format(tVector[j]);
								verboseString += " " + (j+1) + ":" + cuteFormat.format(tVector[j]);
							}
						}
						
						fullWriter.write(fullString + " \n");
						paramsWriter.write(paramsString + "\n");
						verboseWriter.write(String.format("%-70s # %s%n",verboseString, words[k]));
						allParamsWriter.write(fullString + " \n");
					}
					verboseWriter.newLine();
	
					/// End of main section ///////////////////////////////////////////////////
	
	//				dataWriter.flush();
	//				verboseWriter.flush();
					
				}
				fullWriter.flush();
				paramsWriter.flush();
				verboseWriter.flush();
				allParamsWriter.flush();
			
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
				allParamsWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	
	
	public static void main (String[] args) 
	{		
//		String path = "/home/nicolas/Develop/workspace/RinoServer/models/main/";
//		String data = "train";
//		String modelName = "a_cancel";
		
		if (args.length != 2) {
			System.out.println("ParamsConvert: wrong parameters number: " + args.length);
			return;
		}
		String path = "../main/";
		String data = args[0];
		String modelName = args[1];
		

		String trainDir = path + data;
		String outFile = path + modelName + "/params_" + data;
		String verboseFile = path + modelName + "/verbose_" + data;
		String fullFile = path + modelName + "/full_" + data;
		String allParamsFile = path + "full_" + data;
		
		ParamsConvert c = new ParamsConvert();
		c.convert(modelName, trainDir, outFile, verboseFile, fullFile, allParamsFile);
	}
}