package ru.rinorecognizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeparateData {

	private static final Pattern sourcePattern = Pattern.compile("(\\w+)\\t([^\\t]+)(?:\\t+([#&] .+))?");	

    Random generator = new Random(1);    

	public void separateFile(String inFile, String trainFile, String testFile, double ratio) {
		try {
			BufferedReader dataReader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			BufferedWriter trainWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(trainFile)));
			BufferedWriter testWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(testFile)));

			String line;
			String outLine = "";

			while ((line = dataReader.readLine()) != null) {
				
				if (line.length() == 0) {
					outLine += line + "\n";
				
					if (generator.nextDouble() < ratio)
						trainWriter.write(outLine);
					else
						testWriter.write(outLine);
					
					outLine = "";
					continue;
				}
				else if (line.charAt(0) == '#')
					continue;

				// parse line
				Matcher sourceMatcher = sourcePattern.matcher(line);
				if (!sourceMatcher.matches()) {
					System.out.println(this.toString()+ ": Line '" + line + "' is incorrect");
					break;
				}
				
				if (sourceMatcher.group(3) != null) {
					outLine += line + "\n";
				}
				else {
					outLine += line + "\n";
					
					if (generator.nextDouble() < ratio)
						trainWriter.write(outLine);
					else
						testWriter.write(outLine);
					
					outLine = "";
				}	

			}
			trainWriter.flush();
			testWriter.flush();

			dataReader.close();
			trainWriter.close();
			testWriter.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	public void separateFolder(String trainSourceDir, double sRatio, String trainPartDir, String testPartDir) {
				
		File folder = new File(trainSourceDir);
		File[] listOfFiles = folder.listFiles();

	    for (File f: listOfFiles) {

			String sourceFile = f.getAbsolutePath();
			String trainTmpFile = trainPartDir + f.getName() + String.format("_%.1f", sRatio);
			String testTmpFile = testPartDir + f.getName() + String.format("_%.1f", 1 - sRatio);
			
			new File(trainPartDir).mkdirs();
			new File(testPartDir).mkdirs();
			
			SeparateData sData = new SeparateData();
			sData.separateFile(sourceFile, trainTmpFile, testTmpFile, sRatio);
	    }
	}

	
	
	public static void main(String[] args) 
	{		
//		String path = "/home/nicolas/Develop/workspace/RinoServer/models/main/";
//		String trainSourceDir = path + "data/train/"; 
//		String trainPartDir = path + "separated/train/"; 
//		String testPartDir = path + "separated/test/"; 
//		double ratio = 0.5;
		
		if (args.length != 4) {
			System.out.println("SeparateData: wrong parameters number: " + args.length);
			return;
		}	
//		String path = "../main/data/";
		String trainSourceDir = args[0] + "/"; 
		String trainPartDir = args[2] + "/"; 
		String testPartDir = args[3] + "/"; 
		double ratio = new Double(args[1].replaceFirst(",", "."));

		SeparateData s = new SeparateData();
		s.separateFolder(trainSourceDir, ratio, trainPartDir, testPartDir);
	}

}