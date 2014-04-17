package ru.rinorecognizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;

public class DataWriter {
	
	BufferedWriter output;
	
	public DataWriter(){
		
	     try {
	 		// Set output to a file on SD card
	 		File outDataFile = new File(Environment.getExternalStorageDirectory().toString(), "recognizedCommands.txt");
	 		if(!outDataFile.exists()) {	
	 			outDataFile.createNewFile();
	 		}

			output = new BufferedWriter(new FileWriter(outDataFile));
	 		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void write(String str) {
		try {
			output.write(str + "\n");
			output.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
