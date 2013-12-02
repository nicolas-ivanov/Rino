package ru.rinorecognizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;

public class SeparateData {

    Random generator = new Random(2);    

	public void separate(String inFile, String trainFile, String testFile, double ratio) {
		try {
			BufferedReader dataReader = new BufferedReader(new InputStreamReader(new FileInputStream(inFile)));
			BufferedWriter trainWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(trainFile)));
			BufferedWriter testWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(testFile)));

			String line;

			while ((line = dataReader.readLine()) != null) {

				if (line.length() == 0) {
//					trainWriter.write("\n");
//					testWriter.write("\n");
					continue;
				}
				else if (line.charAt(0) == '#')
					continue;

				if (generator.nextDouble() < ratio)
					trainWriter.write(line + "\n");
				else
					testWriter.write(line + "\n");

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

	public static void main(String[] args) 
	{
//		String path = "/home/nicolas/Dropbox/Diploma/svm/train/";
		
		String path = "../train/";
		String in = path + "collection";
		String trainData = path + "train/train";
		String testData = path + "test/test";
		double ratio = 0.5;

		SeparateData s = new SeparateData();
		s.separate(in, trainData, testData, ratio);
	}

}