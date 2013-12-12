package ru.rinorecognizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ParamsConvert {

	private void convert(String modelName, String trainDir, String outFile, String verboseFile, String allParamsFile)  
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
					
					if ((line.length() == 0) || (line.charAt(0) == '#'))
						continue;
					
					String[] parts = line.split("\t");
	
					if (parts.length < 2) {
						System.out.println("ActionConvert: line '" + line + "' is incorrect");
						break;
					}
					
					if (!modelName.equals(parts[0])) 
						continue;
					
	
					/// Main section ///////////////////////////////////////////////////
					
					WordsFeaturesGetter wfg = new WordsFeaturesGetter();
	
					String command = parts[1];
					String[] words = parts[1].split(" ");
					int[] wordsLabels = wfg.getLabels(command);
					int[][] wordsVectors = wfg.getVectors(command);
	
					
					
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
	
	
	public static void main (String[] args) 
	{		
		if (args.length != 2) {
			System.out.println("ParamsConvert: wrong parameters number: " + args.length);
			return;
		}
		String path = "../main/";
		String data = args[0];
		String modelName = args[1];
		
//		String path = "/home/nicolas/Dropbox/Diploma/svm/train/";
//		String data = "collection";
//		String modelName = "a_call";

		
		String trainDir = path + data;
		String outFile = path + modelName + "/params_" + data;
		String verboseFile = path + modelName + "/verbose_" + data;
		String allParamsFile = path + modelName + "/full_" + data;
		
		ParamsConvert c = new ParamsConvert();
		c.convert(modelName, trainDir, outFile, verboseFile, allParamsFile);
	}
}