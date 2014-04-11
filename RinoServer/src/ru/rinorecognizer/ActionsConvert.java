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

	private static final Pattern sourcePattern = Pattern.compile("(\\w+)\\t([^\\t]+)(?:\\t+([#&] .+))?");	
	
	public void convert(String trainDir, String allParamsFile, String compactFile, String verboseFile) {

		BufferedReader dataReader = null;
		BufferedWriter fullWriter = null;
		BufferedWriter paramsWriter = null;
		BufferedWriter verboseWriter = null;
		
		try {
			fullWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(allParamsFile)));
			paramsWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(compactFile)));
			verboseWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(verboseFile)));
			
		    
		    String line;
			File[] files = new File(trainDir).listFiles();
			LinkedList<float[]> vList = new LinkedList<float[]>();
			
			
			for (File file : files) {

//				System.out.println("Action: " + file.toString());
				
			    if (file.isFile())
					dataReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

				
				int prevType = 0;
				int prevComplete = 1;
				

				while ((line = dataReader.readLine()) != null) {

					// skip empty lines and comments
					if (line.length() == 0 || line.charAt(0) == '#') {
						prevType = 0;
						prevComplete = 1;
						
//						if (line.length() != 0)
//							verboseWriter.write("\n");
						
						continue;
					}

					// parse line
					Matcher sourceMatcher = sourcePattern.matcher(line);
					if (!sourceMatcher.matches()) {
						System.out.println("ActionConvert: Line '" + line + "' is incorrect");
						break;
					}
					String label = sourceMatcher.group(1);
					int l = IdTranslator.getActionID(label);
					
					// process commands
					String command = sourceMatcher.group(2);
					String comment = sourceMatcher.group(3);
					
					if (comment == null)
						comment = "";					
					
					
					ExtendedCommand extCommand = new ExtendedCommand();
					extCommand.curCommand = command;
					extCommand.prevType = prevType;
					extCommand.prevComplete = prevComplete;
					
					if (comment.startsWith("&"))
						extCommand.expParameter = IdTranslator.getParamOrdinal(comment.replaceFirst("& ", ""));
					else
						extCommand.expParameter = 0;

					CommandFeaturesGetter cfg = new CommandFeaturesGetter();
					float[] pVector = cfg.getVector(extCommand);
					
					// check if vector is a duplicate
					Iterator<float[]> vListIterator = vList.iterator();
					Boolean duplicateFound = false;
					
					while (vListIterator.hasNext() && !duplicateFound) {
						float[] v = vListIterator.next();
						if (Arrays.equals(pVector,v))
							duplicateFound = true;
					}
					
					// write down non-duplicate data
					String fullString = l + "";
					String paramsString = l + "";
					String verboseString = String.format("%s\t%s\t%-70s %-25s\t", l, label, command, comment);
					
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
					
					if (comment.equals("")) {
						prevComplete = 1;
						prevType = 0;
//						System.out.println(sourceMatcher.group(3));
					}
					else {
						prevComplete = 0;
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
	


	public static void main(String[] args) {

//		String path = "/home/nicolas/Dropbox/Diploma/svm/train/";
//		String mode = "train";
//		String data = "all";
		
		if (args.length != 2) {
			System.out.println("ActionsConvert: wrong parameters number: " + args.length);
			return;
		};
		String data = args[0];
		String mode = args[1];
		String path = "../main/";
		
		String trainDir = path + mode + "/";
		String allParamsFile = path + "action/full_" + data;
		String compactFile = path + "action/params_" + data;
		String verboseFile = path + "action/verbose_" + data;

		ActionsConvert c = new ActionsConvert();
		c.convert(trainDir, allParamsFile, compactFile, verboseFile);
	}

}