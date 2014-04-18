package ru.rinorecognizer;

import java.util.Collection;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;

public class Contact {
	private String alias;
	private String firstName;
	private String lastName;
	private String middleName;	
	private Collection<String> numbers;
	private Collection<String> emails;
	
	private Collection<String> nameList;
	private Collection<String> phoneList;
	private Collection<String> emailList;
	
	public Contact(String alias, Collection<String> numbers) {
		this.alias = alias;
		this.numbers = numbers;
	}
	public Contact(String name, String givenName, String familyName, String middleName, 
			Collection<String> numbers,Collection<String> emails) {
		this.alias = name;
		this.firstName = givenName;
		this.lastName = familyName;
		this.middleName = middleName;
		this.numbers = numbers;
		this.emails = emails;
	}
	public String getName() {
		return alias;
	}
	public String getFirstName() {
		return firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public String getMiddleName() {
		return middleName;
	}
	public void setName(String name) {
		this.alias = name;
	}
	public Collection<String> getNumbers() {
		return numbers;
	}		
	public Collection<String> getEmails() {
		return emails;
	}		
	
	
	public static String getPhoneNumber(String name, Context context) 
	{
		ContentResolver cr = context.getContentResolver();
		
		String numStr = null;
		String selection = Phone.DISPLAY_NAME + " like'%" + name +"%'";
		String[] projection = new String[] {Phone.NUMBER};
		
		Cursor c = cr.query(Phone.CONTENT_URI, 
				projection, selection, null, null);
		
		if (c.moveToFirst()) {
		    numStr = c.getString(0);
		}
		c.close();

		return numStr;
	}    	
	
	public static String getEmail(String name, Context context) 
	{
		ContentResolver cr = context.getContentResolver();
		
		String numStr = null;
		String selection = Email.DISPLAY_NAME + " like'%" + name +"%'";
		String[] projection = new String[] {Email.ADDRESS};
		
		Cursor c = cr.query(Email.CONTENT_URI, 
				projection, selection, null, null);
		
		if (c.moveToFirst()) {
		    numStr = c.getString(0);
		}
		c.close();

		return numStr;
	}    
    
//	public static String getEmail(String name, Context context) 
//	{
//		ContentResolver cr = context.getContentResolver();
//				
//		Cursor cur = cr.query(
//				ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
//				null, 
//				ContactsContract.CommonDataKinds.Email.DISPLAY_NAME + " like'%" + name +"%'", 
//				null, null);
//		
//
//	    if (cur.getCount() > 0) {
//	        while (cur.moveToNext()) {
//
//	            String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
//        	
//                Cursor emailCur = cr.query( 
//	                ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
//	                null,
//	                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", 
//	                new String[]{id}, null); 
//        
//		        while (emailCur.moveToNext()) { 
//		        	String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));                            
//		
//		        	emailList.add(email); // Here you will get list of email    
//		
//		        } 
//		        emailCur.close();  
//	        			
//	        }
//	    }
//		
//
//		String[] projection = new String[] { ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?" };
//		if (cur.moveToFirst()) {
//		    emailStr = cur.getString(0);
//		}
//		cur.close();
//
//		return emailStr;
//	}
	
    
	public static Cursor getContactInfo(String mSearchString, Context context) 
	{	    
		final int CONTACT_ID_INDEX = 0;
		final int LOOKUP_KEY_INDEX = 1;
		final int DISPLAY_NAME_PRIMARY = 2;
		
	    final String[] mProjection =
            {
                Contacts._ID,
                Contacts.LOOKUP_KEY,
                Build.VERSION.SDK_INT
                        >= Build.VERSION_CODES.HONEYCOMB ?
                        Contacts.DISPLAY_NAME_PRIMARY :
                        Contacts.DISPLAY_NAME
            };
	    
	    final String mSelectionClause =
	            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
	            Contacts.DISPLAY_NAME_PRIMARY + " LIKE ?" :
	            Contacts.DISPLAY_NAME + " LIKE ?";

	            String[] mSelectionArgs = { "%" + mSearchString + "%" };
//	    String[] mSelectionArgs = { "%" };
	    
	    String mSortOrder = null;
	    
	    Cursor mCursor = context.getContentResolver().query(
    		Contacts.CONTENT_URI,
	        mProjection,       
	        mSelectionClause,
	        mSelectionArgs,
	        mSortOrder);
		
	    
	    if (mCursor != null) {
	        while (mCursor.moveToNext()) {
	        	String str = mCursor.getString(DISPLAY_NAME_PRIMARY);
	            System.out.print(str);
	        }
	    } else {
            System.out.print("Contact: mCursor was not initialised");	    	
	    }
	    
	    
		return mCursor;
	}
	
	
	
//	public ArrayList<String> ShowContact(String searchName, Context context) {        
//
//	    nameList = new ArrayList<String>();
//        phoneList = new ArrayList<String>();
//        emailList = new ArrayList<String>();
//
//	    ContentResolver cr = context.getContentResolver();
//	    Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
//	            null, null, null);
//	    
//	    if (cur.getCount() > 0) {
//	        while (cur.moveToNext()) {
//	            String id = cur.getString(cur
//	                    .getColumnIndex(ContactsContract.Contacts._ID));
//	            String name = cur
//	                    .getString(cur
//	                            .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//	            if (Integer
//	                    .parseInt(cur.getString(cur
//	                            .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
//	                // Query phone here. Covered next
//
//	                Cursor pCur = cr.query(
//	                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//	                        null,
//	                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
//	                                + " = ?", new String[] { id }, null);
//	                
//	                while (pCur.moveToNext()) {
//	                    // Do something with phones
//	                    String phoneNo = pCur
//	                            .getString(pCur
//	                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//
//	                    nameList.add(name); // Here you can list of contact.
//	                    phoneList.add(phoneNo); // Here you will get list of phone number.                  
//
//
//	                    Cursor emailCur = cr.query( 
//	                            ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
//	                            null,
//	                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", 
//	                            new String[]{id}, null); 
//	                    
//	                    while (emailCur.moveToNext()) { 
//	                    	String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));                            
//
//	                    	emailList.add(email); // Here you will get list of email    
//
//                        } 
//                        emailCur.close();       
//	                }
//	                pCur.close();
//	            }
//	        }
//	    }
//
//	    return nameList; // here you can return whatever you want.
//	}
}
