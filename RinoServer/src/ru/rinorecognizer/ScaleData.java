package ru.rinorecognizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ScaleData {

	private static final Pattern paramsPattern = Pattern.compile("(\\d+) (.*)");
	
	
	private void getRange(String fullFile, String rangePath) throws IOException {
		
//		File rangeFile = new File(rangePath);
		BufferedReader fullReader = new BufferedReader(new InputStreamReader(new FileInputStream(fullFile)));
		
		int[] paramsMin;
		int[] paramsMax;
		
		// initialize arrays paramsMin and paramsMax
	    String line = fullReader.readLine();
	    
	    if (line != null) {
			Matcher paramsMatcher = paramsPattern.matcher(line);
			if (!paramsMatcher.matches()) {
				System.out.println("Line '" + line + "' is incorrect\n");
				fullReader.close();
				return;
			}
			String params = paramsMatcher.group(2);
			String[] paramsStr = params.split(" ");
			paramsMin = new int[paramsStr.length];
			paramsMax = new int[paramsStr.length];
			
			for (int i = 0; i < paramsStr.length; i++) { 
				paramsMin[i] = Integer.parseInt(paramsStr[i]);
				paramsMax[i] = Integer.parseInt(paramsStr[i]);
			} 	
	    }	    	
	    else {
	    	System.out.print("File '" + fullFile + "' is empty\n");
			fullReader.close();
	    	return;
	    }
	   
	    
		// find paramsMin and paramsMax
		while ((line = fullReader.readLine()) != null) {
			Matcher paramsMatcher = paramsPattern.matcher(line);
			if (!paramsMatcher.matches()) {
				System.out.println("Line '" + line + "' is incorrect\n");
				break;
			}
			String params = paramsMatcher.group(2);
			String[] paramsStr = params.split(" ");
			
			for (int i = 0; i < paramsStr.length; i++) {
				int curNum = Integer.parseInt(paramsStr[i]);
				
				if (curNum < paramsMin[i])
					paramsMin[i] = curNum;

				if (curNum > paramsMax[i])
					paramsMax[i] = curNum;
			} 
		}
		fullReader.close();
		
		
		// write the results to a file
		BufferedWriter scaledWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rangePath)));
		
		scaledWriter.write("x\n");
		scaledWriter.write("0 1\n");
		
		for (int i = 0; i < paramsMax.length; i++)
			scaledWriter.write((i+1) + " " + paramsMin[i] + " " + paramsMax[i] + "\n");
		
		scaledWriter.close();
	}
	
	
	private void scale(String fullFile, String scaledFile, String rangeFile) {
		
		BufferedReader paramsReader = null;
		BufferedWriter scaledWriter = null;
		
		try {
			paramsReader = new BufferedReader(new InputStreamReader(new FileInputStream(fullFile)));
			scaledWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(scaledFile)));

			getRange(fullFile, rangeFile);
			VectorScaler vScaler = new VectorScaler(rangeFile);
			
		    String line;
			
			while ((line = paramsReader.readLine()) != null) {

				// parse line
				Matcher paramsMatcher = paramsPattern.matcher(line);
				if (!paramsMatcher.matches()) {
					System.out.println("Line '" + line + "' is incorrect\n");
					break;
				}
				String label = paramsMatcher.group(1);
				String params = paramsMatcher.group(2);
				
				String[] paramsStr = params.split(" ");
				int[] paramsInt = new int[paramsStr.length];
				
				for (int i = 0; i < paramsInt.length; i++)
					paramsInt[i] = Integer.parseInt(paramsStr[i]);

				double[] scaledParams = vScaler.scale(paramsInt);
				
				String scaledString = label + "";
				
				for (int i = 0; i < scaledParams.length; i++) {					
					if (scaledParams[i] != 0) {
						scaledString += " " + (i + 1) + ":" + scaledParams[i];
					}
				}
				scaledWriter.write(scaledString + " \n");
			}
			scaledWriter.flush();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally {
			try {
				paramsReader.close();
				scaledWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	public static void main(String[] args) {

		String path = "/home/nicolas/Dropbox/Diploma/svm/train/";
		String data = "collection";
		
//		if (args.length != 1) {
//			System.out.println("ScaleData: wrong parameters number: " + args.length);
//			return;
//		};
//		String data = args[0];
//		String path = "../train/";

		String fullFile = path + "action/full_" + data;
		String rangeFile = path + "action/range_" + data;
		String scaledFile = path + "action/scaled_" + data;

		ScaleData scaler = new ScaleData();
		scaler.scale(fullFile, scaledFile, rangeFile);
	}

}