package ru.rinorecognizer;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class RinoServer {

	
	public static void main(String[] args) throws ParseException {

		
		// Set command line arguments
		Options options = new Options();
		options.addOption("trainDir", 	true, "where to look for training files");
		options.addOption("testDir", 	true, "where to look for testing files");
		options.addOption("separate", 	false, "shows if training data should be separated on training and testing sets");
		options.addOption("ratio", 		true, "train/test separation ratio");
		
		options.addOption("allParamsName", 		true, "where to output full-length params' vectors");
		options.addOption("compactName", 		true, "where to output non-zeros params of vectors");
		options.addOption("verboseName", 		true, "where to output non-zeros params with original command");

		options.addOption("rangeName", 		true, "where to output scaling range values");


		
		// Get arguments values
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		String trainSourceDir = cmd.getOptionValue("trainDir");
		String testSourceDir = 	cmd.getOptionValue("testDir");
		Boolean separate = 		cmd.hasOption("separate");
		Double sRatio = 		Double.parseDouble(cmd.getOptionValue("ratio", "0.5"));
		
		String allParamsName = 	cmd.getOptionValue("allParamsName", "allParams");
		String compactName = 	cmd.getOptionValue("compactName", "compact");
		String verboseName = 	cmd.getOptionValue("verboseName", "verbose");
		
		String rangeName = 	cmd.getOptionValue("rangeName", "range");
		String scaledName = cmd.getOptionValue("scaledName", "scaled");


		
		// Get training and testing sets
		String trainDir = trainSourceDir;
		String testDir = testSourceDir;
		
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
				sData.separateFile(sourceFile, trainTmpFile, testTmpFile, sRatio);
		    }
		}

		
		String workingDir;
		String allParamsFile, compactFile, verboseFile;
		
		String rangeFile, scaledFile;
		ScaleData scaleData = new ScaleData();
		

		ActionsConvert actions = new ActionsConvert();
		// Get parameters values for action-type classification
		{			
			// training set
			workingDir = trainSourceDir + "../action/train/";
			new File(workingDir).mkdirs();
			
			allParamsFile = workingDir + allParamsName;
			compactFile = 	workingDir + compactName;
			verboseFile = 	workingDir + verboseName;
			actions.convert(trainDir, allParamsFile, compactFile, verboseFile);

			rangeFile = workingDir + rangeName; 
			scaledFile = workingDir + scaledName; 
			scaleData.scale(allParamsFile, scaledFile, rangeFile);
			
			
			// testing set
			workingDir = trainSourceDir + "../action/test/";
			new File(workingDir).mkdirs();
			
			allParamsFile = workingDir + allParamsName;
			compactFile = 	workingDir + compactName;
			verboseFile = 	workingDir + verboseName;			
			actions.convert(testDir, allParamsFile, compactFile, verboseFile);

			rangeFile = workingDir + rangeName; 
			scaledFile = workingDir + scaledName; 
			scaleData.scale(allParamsFile, scaledFile, rangeFile);
			
		}

//		ParamsConvert params = new ParamsConvert();
//		// Get parameters values for action-type classification
//		{			
//			// training set
//			workingDir = trainSourceDir + "../action/train/";
//			new File(workingDir).mkdirs();
//			
//			allParamsFile = workingDir + allParamsName;
//			compactFile = 	workingDir + compactName;
//			verboseFile = 	workingDir + verboseName;
//			params.convert(modelName, trainDir, outFile, verboseFile, allParamsFile);
//
//			rangeFile = workingDir + rangeName; 
//			scaledFile = workingDir + scaledName; 
//			scaleData.scale(allParamsFile, scaledFile, rangeFile);
//			
//			
//			// testing set
//			workingDir = trainSourceDir + "../action/test/";
//			new File(workingDir).mkdirs();
//			
//			allParamsFile = workingDir + allParamsName;
//			compactFile = 	workingDir + compactName;
//			verboseFile = 	workingDir + verboseName;			
//			params.convert(testDir, allParamsFile, compactFile, verboseFile);
//
//			rangeFile = workingDir + rangeName; 
//			scaledFile = workingDir + scaledName; 
//			scaleData.scale(allParamsFile, scaledFile, rangeFile);
//			
//		}
		
	}

}
