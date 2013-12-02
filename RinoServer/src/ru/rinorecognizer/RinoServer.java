package ru.rinorecognizer;

import org.apache.commons.cli.*;

public class RinoServer {

	
	public static void main(String[] args) throws ParseException {

		Options options = new Options();
		options.addOption("train", true, "set the file for training");
		options.addOption("test", true, "set the file for testing");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		String trainFile = cmd.getOptionValue("train");
		String testFile = cmd.getOptionValue("test");

		System.out.println(trainFile);
		System.out.println(testFile);

//		SeparateData sData = new SeparateData();
		

	}

}
