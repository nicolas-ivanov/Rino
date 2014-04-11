package ru.rinorecognizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandFeaturesGetter {

	private static final Pattern structurePattern = Pattern.compile("([-?\\d,]+)\\t+(\\w+)\\t+([^\\t~]+)");

	public float[] getVector(ExtendedCommand extCommand) {
		try {
			// if (!checkPatterns())
			// return null;
			
			String command = extCommand.curCommand;
			
			int semSetsNum = new PatternsHandler().getSemanticSetsNum();
			int actionsNum = IdTranslator.getActionsNum();
			int paramsNum = IdTranslator.getParamsNum();

			int vFeaturesNum = semSetsNum	// number of semantic sets
					+ actionsNum + 1 		// number of action types for prevType
					+ 1 					// for prevComplete value
					+ paramsNum + 1; 		// number of params types for expectedParam

			String[] words = command.split(" ");
			float[] pVector = new float[vFeaturesNum];

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
			
			// write value of prevComplete
			pVector[semSetsNum] = extCommand.prevComplete;
			// encode integer id of prevType as boolean vector
			pVector[semSetsNum + 1 + extCommand.prevType] = 1;
			// encode integer id of expParameter as boolean vector
			pVector[semSetsNum+ 1 + actionsNum + extCommand.expParameter] = 1;	
			
			return pVector;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}