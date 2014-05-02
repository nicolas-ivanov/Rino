package ru.rinorecognizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PatternsHandler {

	public BufferedReader getPatternsReader() {
		InputStream input = getClass().getResourceAsStream("patterns.txt");
		return new BufferedReader(new InputStreamReader(input));
	}
	
	public int getSemanticSetsNum() throws IOException {
		BufferedReader patternsReader = getPatternsReader();
		String rawPattern;
		int paramsNum = 0;

		while ((rawPattern = patternsReader.readLine()) != null) {
			
			if (rawPattern.equals("") || rawPattern.startsWith("#"))
				continue; // skip empty lines and comments

			paramsNum++;
		}
		patternsReader.close();
		
		return paramsNum;
	}
}
