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

	private static final Pattern sourcePattern = Pattern.compile("(\\w+)\\t([^\\t]+)(\\t+#.*)?");

    Random generator = new Random(2);    

	public void separateFile(String inFile, String trainFile, String testFile, double ratio) {
		try {
			BufferedReader dataReader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			BufferedWriter trainWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(trainFile)));
			BufferedWriter testWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(testFile)));

			String line;
			String outLine = "";

			while ((line = dataReader.readLine()) != null) {
				
				if (line.length() == 0) {
//					trainWriter.write("\n");
//					testWriter.write("\n");
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
					outLine += line + "\n\n";
					
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

	
	
	public void separateFolder(String trainSourceDir, String testSourceDir, double sRatio) {

		String trainDir = trainSourceDir;
		String testDir = testSourceDir;
		
		File folder = new File(trainSourceDir);
		File[] listOfFiles = folder.listFiles();

	    for (File f: listOfFiles) {

			String sourceFile = f.getAbsolutePath();
			trainDir = trainSourceDir + "../../train/";
			testDir = trainSourceDir + "../../test/";
			String trainTmpFile = trainDir + f.getName();
			String testTmpFile = testDir + f.getName();
			
			new File(trainDir).mkdirs();
			new File(testDir).mkdirs();
			
			SeparateData sData = new SeparateData();
			sData.separateFile(sourceFile, trainTmpFile, testTmpFile, sRatio);
	    }
	}

	
	
	public static void main(String[] args) 
	{		
		if (args.length != 3) {
			System.out.println("SeparateData: wrong parameters number: " + args.length);
			return;
		};
		
//		String path = "../main/saved/";
//		String trainDir = path + args[0] + "/"; 
//		String testDir = path + args[1] + "/";
//		double ratio = new Double(args[2]);
//		
		String path = "../main/saved/";
		String trainDir = path + "train/";
		String testDir = path + "test/";
		double ratio = 0.5;

		SeparateData s = new SeparateData();
		s.separateFolder(trainDir, testDir, ratio);
	}

}