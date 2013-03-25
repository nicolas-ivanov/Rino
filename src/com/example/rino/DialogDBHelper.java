package com.example.rino;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DialogDBHelper extends SQLiteOpenHelper {
	private SQLiteDatabase db;
	
	
    public DialogDBHelper(Context context) {
      super(context, "dialogDB_v1", null, 1);
      db = getWritableDatabase();

//      int clearCount = db.delete("dialogDB", null, null);
//      Log.d(MainActivity.TAG, "DBHelper: deleted rows count = " + clearCount);
    }

    
    @Override
    public void onCreate(SQLiteDatabase db) {
    	Log.d(MainActivity.TAG, "DBHelper: created");
    	db.execSQL("create table dialogDB ("
    			+ "id integer primary key autoincrement," 
    			+ "phrase text" + ");");
    }

    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    
    public ArrayList<String> getDialogHistory() {
		Log.d(MainActivity.TAG, "DBHelper: get dialog history");
		
		Cursor c = db.query("dialogDB", null, null, null, null, null, null);
		ArrayList<String> dialog = new ArrayList<String>();

		if (c.moveToFirst()) {
			int phraseIndex = c.getColumnIndex("phrase");

			do {
				dialog.add(c.getString(phraseIndex));
				Log.d(MainActivity.TAG, "DBHelper: added to dialog list \"" + c.getString(phraseIndex));
			} while (c.moveToNext());
		} else {
			Log.d(MainActivity.TAG, "DBHelper: dialog history is empty");
			dialog.add("История пуста");
		}

		c.close();
		return dialog;
    }
    
    
    public void saveDialogHistory(ArrayList<String> dialog) {
		Log.d(MainActivity.TAG, "DBHelper: save dialog history");
        String str;
//		String str = dialog.get(0);
		Iterator<String> dialogIter = dialog.iterator();
        
	    while (dialogIter.hasNext()) {	        
	    	str = dialogIter.next();
	    	
		    ContentValues cv = new ContentValues();
	        cv.put("phrase", str);

	        long rowID = db.insert("dialogDB", null, cv);
	        Log.d(MainActivity.TAG, "DBHelper: row inserted, ID = " + rowID 
	        		+ " phrase = \"" + str + "\"");
	    }       
    }
    
}

