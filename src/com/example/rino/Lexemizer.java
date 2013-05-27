package com.example.rino;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class Lexemizer {
	private BufferedReader reader;
	private static final Pattern structurePattern = Pattern
			.compile("(\\w+)#(.*)");
	private static final String TAG = "Lexemizer: ";

	public Lexemizer(BufferedReader reader){
		this.reader = reader;
	}
	
	public String getLexemeType(String input){
		String line;
		boolean found = false;
		try {
			reader.mark(1000000);
			while (!found && (line = reader.readLine()) != null) {
				String rawPattern = line;
				Matcher structureMatcher = structurePattern.matcher(rawPattern);
				if (structureMatcher.matches()) {
					Pattern commandPattern = Pattern.compile(structureMatcher
							.group(2));
					Matcher commandMatcher = commandPattern
							.matcher(input);
					found = commandMatcher.matches();
					
					if (found) {
						reader.reset();
						return structureMatcher.group(1);
					}
				} else {
					Log.d(MainActivity.TAG, TAG + "Simple pattern '" + rawPattern
							+ "' is incorrect");
					return "text";
				}
			}
			reader.reset();
		} catch (IOException e) {
			Log.d(MainActivity.TAG, TAG + "Simple patterns IOException", e);
		}
		return "text";
	}
	
	@Override
	public void finalize(){
		try {
			reader.close();
		} catch (IOException e) {
			Log.d(MainActivity.TAG, TAG + "Closing IOException", e);
		}
	}
}
