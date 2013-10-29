package ru.rinorecognizer;

import java.util.Collection;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

public class Contact {
	private String name;
	private String firstName;
	private String lastName;
	private String middleName;	
	private Collection<String> numbers;
	
	public Contact(String name, Collection<String> numbers) {
		this.name = name;
		this.numbers = numbers;
	}
	public Contact(String name, String givenName, String familyName,
			String middleName, Collection<String> numbers) {
		this.name = name;
		this.firstName = givenName;
		this.lastName = familyName;
		this.middleName = middleName;
		this.numbers = numbers;
	}
	public String getName() {
		return name;
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
		this.name = name;
	}
	public Collection<String> getNumbers() {
		return numbers;
	}		
	
	
	public static String getPhoneNumber(String name, Context context) 
	{
		String numStr = null;
		String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like'%" + name +"%'";
		String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER };
		
		Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
				projection, selection, null, null);
		
		if (c.moveToFirst()) {
		    numStr = c.getString(0);
		}
		c.close();

		return numStr;
	}    
    
	public static String getEmail(String name, Context context) 
	{
		String emailStr = null;
		String selection = ContactsContract.CommonDataKinds.Email.DISPLAY_NAME + " like'%" + name +"%'";
		String[] projection = new String[] { ContactsContract.CommonDataKinds.Email.ADDRESS };
		
		Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, 
				projection, selection, null, null);
		
		if (c.moveToFirst()) {
		    emailStr = c.getString(0);
		}
		c.close();

		return emailStr;
	}
	
    
	public static Cursor getContactInfo(String name, Context context) 
	{
		String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like'%" + name +"%'";
		String[] projection = new String[] { 
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.Phone.NUMBER,
				ContactsContract.CommonDataKinds.Email.ADDRESS // doesn't work
			};
		
		Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				projection, selection, null, null);
		
		return c;
	}
}
