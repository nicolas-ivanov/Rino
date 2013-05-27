package com.example.rino;

import android.util.Log;

public class CommandTokenizer {
	// private String input;
	private String[] tokens;
	private int iter = 0;

	public CommandTokenizer(String command) {
		setInput(command);
	}

	public void setInput(String input) {
		// this.input = input;
		iter = 0;
		tokens = input.split(" ");
		Log.d(MainActivity.TAG, "CommandTokenizer: " + input + " " + tokens.length);
	}

	public String getNextToken() {
		Log.d(MainActivity.TAG, "CommandTokenizer: " + "gNT " + tokens.length + " " + iter);
		if (tokens.length == iter)
			throw new IndexOutOfBoundsException("No more tokens");
		return tokens[iter++];
	}

	public boolean hasNextToken() {
		Log.d(MainActivity.TAG, "CommandTokenizer: " + "hNT " + tokens.length + " " + iter);
		return !(tokens.length == iter);
	}
}