package ru.rinorecognizer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.telephony.PhoneNumberUtils;

public class ContactsDBHelper {
    
	
	public static List<Contact> getPhoneNumber(String nameString, Context context) 
	{
	    String[] mProjection =
        {
	    		Phone.DISPLAY_NAME_PRIMARY,
	    		Phone.NUMBER
        };

		String[] mSelectionArgs = nameString.split(" ");
		String mSelectionClause = Phone.IS_PRIMARY;

		for (int i = 0; i < mSelectionArgs.length; i++) {
			mSelectionClause += " AND " + Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?";
			mSelectionArgs[i] = "%" + mSelectionArgs[i] + "%";
		}
	    
		String mSortOrder = Phone.DISPLAY_NAME_PRIMARY + " ASC";
	    
	    Cursor mCursor = context.getContentResolver().query(
    		Phone.CONTENT_URI,
	        mProjection,       
	        mSelectionClause,
	        mSelectionArgs,
	        mSortOrder);
		
	    
	    List<Contact> contactList = new ArrayList<Contact>();
	    
		if (mCursor == null)
			contactList = null;
		else 
	        while (mCursor.moveToNext()) {
				Contact c = new Contact();
				c.name = mCursor.getString(mCursor.getColumnIndex(Phone.DISPLAY_NAME_PRIMARY));
				c.number = formatNumber(mCursor.getString(mCursor.getColumnIndex(Phone.NUMBER)));
				contactList.add(c);
	        }
	    
		return contactList;
	}	
	
	
	
	
	public static List<Contact> getEmail(String nameString, Context context) 
	{
	    String[] mProjection =
        {
	    		Email.DISPLAY_NAME_PRIMARY,
	    		Email.ADDRESS
        };

		String[] mSelectionArgs = nameString.split(" ");
		String mSelectionClause = Email.IS_PRIMARY;

		for (int i = 0; i < mSelectionArgs.length; i++) {
			mSelectionClause += " AND " + Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?";
			mSelectionArgs[i] = "%" + mSelectionArgs[i] + "%";
		}
	    
		String mSortOrder = Email.DISPLAY_NAME + " ASC";
	    
	    Cursor mCursor = context.getContentResolver().query(
    		Email.CONTENT_URI,
	        mProjection,       
	        mSelectionClause,
	        mSelectionArgs,
	        mSortOrder);
		
	    
	    List<Contact> contactList = new ArrayList<Contact>();
	    
		if (mCursor == null)
			contactList = null;
		else 
	        while (mCursor.moveToNext()) {
				Contact c = new Contact();
				c.name = mCursor.getString(mCursor.getColumnIndex(Email.DISPLAY_NAME_PRIMARY));
				c.email = mCursor.getString(mCursor.getColumnIndex(Email.ADDRESS));
				contactList.add(c);
	        }
	    
		return contactList;
	}
	
	
	private static String formatNumber(String number) {
		
		number = number.replaceAll(" ", "");
		
		if (number.matches("8\\d{10}"))
			number = "+7" + number.substring(1);
		
		PhoneNumberUtils.formatNumber(number);
		
		return number;
	}
	
	
	
}