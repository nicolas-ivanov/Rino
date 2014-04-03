package ru.rinorecognizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandFeaturesGetter {

	private static final Pattern structurePattern = Pattern.compile("([-?\\d,]+)\\t+(\\w+)\\t+([^\\t~]+)");

	private int getParamsNum() throws IOException {

		InputStream input = getClass().getResourceAsStream("patterns.txt");
		BufferedReader patternsReader = new BufferedReader(new InputStreamReader(input));

		String rawPattern;
		int paramsNum = 0;

		while ((rawPattern = patternsReader.readLine()) != null) {
			if (rawPattern.equals(""))
				continue; // skip empty lines

			paramsNum++;
		}
		patternsReader.close();

		return paramsNum;
	}
	

	public int[] getVector(ExtendedCommand extCommand) {
		try {
			// if (!checkPatterns())
			// return null;
			
			String command = extCommand.curCommand;

			int paramsNum = getParamsNum() + 3; // Params Num + prevType + prevComplete + expParameter

			String[] words = command.split(" ");
			int[] pVector = new int[paramsNum];

			for (int i = 0; i < pVector.length; i++)
				pVector[i] = 0;

			for (int i = 0; i < words.length; i++) {

				InputStream input = getClass().getResourceAsStream("patterns.txt");
				BufferedReader patternsReader = new BufferedReader(new InputStreamReader(input));

				String w = words[i];
				String rawPattern;

				if (w.length() > 0) {
					w = w.replaceFirst("_", "");
					w = w.replaceFirst("Time:", "");
				}

				int pNum = 0;

				while ((rawPattern = patternsReader.readLine()) != null) {

					if (rawPattern.equals(""))
						continue; // skip empty lines

					Matcher structureMatcher = structurePattern.matcher(rawPattern);
					// work only with correct type patterns
					if (!structureMatcher.matches()) {
						System.out.println("Pattern '" + rawPattern + "' is incorrect");
						break;
					}

					Pattern typePattern = Pattern.compile(structureMatcher.group(3));
					Matcher typeMatcher = typePattern.matcher(w);

					if (typeMatcher.matches())
						pVector[pNum]++;

					pNum++;
				}

				patternsReader.close();
			}
			
			pVector[pVector.length - 3] = extCommand.prevType;
			pVector[pVector.length - 2] = extCommand.prevComplete;
//			pVector[pVector.length - 1] = 77;
			pVector[pVector.length - 1] = extCommand.expParameter;
			
			return pVector;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}