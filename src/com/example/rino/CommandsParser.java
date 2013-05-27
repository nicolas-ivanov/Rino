package com.example.rino;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class CommandsParser {
	private BufferedReader reader;
	private static final Pattern structurePattern = Pattern
			.compile("([^#]+)#([\\w\\s]+)");
	private static final String TAG = "CommandsParser: ";

	public CommandsParser(BufferedReader reader) {
		this.reader = reader;
	}

	public String[] getCommandType(String input) {
		String line;
		boolean found = false;
		try {
			reader.mark(1000000);
			while (!found && (line = reader.readLine()) != null) {
				String rawPattern = line;
				Matcher structureMatcher = structurePattern.matcher(rawPattern);
				if (structureMatcher.matches()) {
					/*Pattern commandPattern = Pattern.compile(structureMatcher
							.group(1));
					Matcher commandMatcher = commandPattern.matcher(input);
					found = commandMatcher.matches();
					*/
					found = structureMatcher.group(1).equals(input);
					if (found) {
						reader.reset();
						return structureMatcher.group(2).split(" ");
					}
				} else {
					Log.d(MainActivity.TAG, TAG + "Command keyword pattern '"
							+ rawPattern + "' is incorrect");
					return new String[]{"null"};
				}
			}
			reader.reset();
		} catch (IOException e) {
			Log.d(MainActivity.TAG, TAG + "Command keyword IOException", e);
		}
		return new String[]{"null"};
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
