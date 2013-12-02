package ru.rinorecognizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionsConvert {

	private static final Pattern sourcePattern = Pattern.compile("(\\w+)\\t([^\\t]+)(\\t+#.*)?");
	
	private void convert(String trainDir, String fullFile, String paramsFile, String verboseFile) {

		BufferedReader dataReader = null;
		BufferedWriter fullWriter = null;
		BufferedWriter paramsWriter = null;
		BufferedWriter verboseWriter = null;
		
		try {
			fullWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fullFile)));
			paramsWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(paramsFile)));
			verboseWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(verboseFile)));
			
		    
		    String line;
			File[] files = new File(trainDir).listFiles();
			LinkedList<int[]> vList = new LinkedList<int[]>();
			
			
			for (File file : files) {
				
			    if (file.isFile())
					dataReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

				
				int prevType = 0;
				int prevComplite = 1;
				

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
						System.out.println("Line '" + line + "' is incorrect");
						break;
					}
					String label = sourceMatcher.group(1);
					int l = getLabelCode(label);
					
					// process commands
					CommandFeaturesGetter cfg = new CommandFeaturesGetter();
					String command = sourceMatcher.group(2);
					
					ExtendedCommand extCommand = new ExtendedCommand();
					extCommand.curCommand = command;
					extCommand.prevType = prevType;
					extCommand.prevComplite = prevComplite;
					
					int[] pVector = cfg.getVector(extCommand);
					
					// check if vector is a duplicate
					Iterator<int[]> vListIterator = vList.iterator();
					Boolean duplicateFound = false;
					
					while (vListIterator.hasNext() && !duplicateFound) {
						int[] v = vListIterator.next();
						if (Arrays.equals(pVector,v))
							duplicateFound = true;
					}
					
					// write down non-duplicate data
					String fullString = l + "";
					String paramsString = l + "";
					String verboseString = String.format("%s\t%-70s\t", l, line);
					
					if (!duplicateFound) {
						vList.addFirst(pVector);

						for (int i = 0; i < pVector.length; i++) {
							fullString += " " + pVector[i];
							
							if (pVector[i] != 0) {
								paramsString += " " + (i + 1) + ":" + pVector[i];
								verboseString += " " + (i + 1) + ":" + pVector[i];
							}
						}

						fullWriter.write(fullString + " \n");
						paramsWriter.write(paramsString + " \n");
						verboseWriter.write(verboseString + "\n");
					}
//					else {
//						System.out.println(Arrays.toString(pVector));
//					}
					
					if (sourceMatcher.group(3) == null) {
						prevComplite = 1;
						prevType = 0;
//						System.out.println(sourceMatcher.group(3));
					}
					else {
						prevComplite = 0;
						prevType = l;
//						System.out.println(sourceMatcher.group(3));
					}

				}
				fullWriter.flush();
				paramsWriter.flush();
				verboseWriter.flush();
			}

		} catch (IOException e) {
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
	
	private int getLabelCode(String label) {
	
		int l = 0;
		
		switch (label) {
		case "a_call":		l = 1; break;
		case "a_sms":		l = 2; break;
		case "a_email":		l = 3; break;
		case "a_search": 	l = 4; break;
		case "a_site":		l = 5; break;
		case "a_alarm":		l = 6; break;
		case "a_balance":	l = 7; break;
		default:
			System.out.println(this.toString() + ": Label '" + label + "' is incorrect");
		}
		
		return l;
	}


	public static void main(String[] args) {

		String path = "/home/nicolas/Dropbox/Diploma/svm/train/";
		String mode = "train";
		String data = "collection";
		
//		if (args.length != 2) {
//			System.out.println("ActionsConvert: wrong parameters number: " + args.length);
//			return;
//		};
//		String data = args[0];
//		String mode = args[1];
//		String path = "../train/";
		
		String trainDir = path + mode + "/";
		String fullFile = path + "action/full_" + data;
		String paramsFile = path + "action/params_" + data;
		String verboseFile = path + "action/verbose_" + data;

		ActionsConvert c = new ActionsConvert();
		c.convert(trainDir, fullFile, paramsFile, verboseFile);
	}

}