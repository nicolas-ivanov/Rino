package ru.rinorecognizer;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class RinoServer {

	
	public static void main(String[] args) throws ParseException {

		Options options = new Options();
		options.addOption("trainDir", 	true, "where to look for training files");
		options.addOption("testDir", 	true, "where to look for testing files");
		options.addOption("separate", 	false, "shows if training data should be separated on training and testing sets");
		options.addOption("ratio", 		true, "train/test separation ratio");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		String trainSourceDir = 	cmd.getOptionValue("trainDir");
		String testSourceDir = 	cmd.getOptionValue("testDir");
		Boolean separate = 	cmd.hasOption("separate");
		Double sRatio = 	Double.parseDouble(cmd.getOptionValue("ratio"));


		String trainDir, testDir;
		
		if (separate) {			
			File folder = new File(trainSourceDir);
			File[] listOfFiles = folder.listFiles();

		    for (File f: listOfFiles) {

				String sourceFile = f.getAbsolutePath();
				trainDir = trainSourceDir + "../separated/train/";
				testDir = trainSourceDir + "../separated/test/";
				String trainTmpFile = trainDir + f.getName();
				String testTmpFile = testDir + f.getName();
				
				new File(trainDir).mkdirs();
				new File(testDir).mkdirs();
				
				SeparateData sData = new SeparateData();
				sData.separate(sourceFile, trainTmpFile, testTmpFile, sRatio);
				
				System.out.println(sourceFile);
		    }
		}
		else {
			trainDir = trainSourceDir;
			testDir = testSourceDir;
		}
		

	}

}
