package com.example.rino;

import java.util.ArrayList;
import java.util.Collection;

import android.util.Log;

public class ContactsDatabase {
	private Collection<Contact> contacts;
	private static ContactsDatabase instance; 
	
	private ContactsDatabase() {
		contacts = new ArrayList<Contact>();
	}
	
	public synchronized static ContactsDatabase getInstance() {
		if (instance == null) 
			instance = new ContactsDatabase();
		return instance;
	}
	
	public void addContact(String name, Collection<String> numbers) {
		if (name != null) name = name.toLowerCase();
		contacts.add(new Contact(name, numbers));
	}
	
	public void addContact(String name, String givenName, String familyName, String middleName, Collection<String> numbers) {
		if (name != null) name = name.toLowerCase();
		if (givenName != null) givenName = givenName.toLowerCase();
		if (familyName != null) familyName = familyName.toLowerCase();
		if (middleName != null) middleName = middleName.toLowerCase();
		contacts.add(new Contact(name, givenName, familyName, middleName,
				numbers));
	}
	
	//TODO: maybe we should place function for min distance somewhere else
	//Levenstein distance 
	public int findMinDist(String s1, String s2){
		if (s1 == null && s2 == null) 
			return 0;
		if (s1 == null)
			return s2.length() * 3;
		if (s2 == null)
			return s1.length() * 3;
		int n1 = s1.length(), n2 = s2.length();
		s1.charAt(0);
		int[][] d = new int[n1 + 1][n2 + 1];
		d[0][0] = 0;
		for (int j = 1; j <= n2; j++){
			d[0][j] = d[0][j-1] + 2;//price of inserting symbol s2[j]
		}
		for (int i = 1; i <= n1; i++){
			d[i][0] = d[i-1][0] + 2;//price of deleting symbol s1[i]
			for (int j = 1; j <= n2; j++){
				int changePrice = 3; //price of changing symbol s1[i] on s2[j]
				if (s1.charAt(i-1) == s2.charAt(j-1)) 
					changePrice = 0;
				d[i][j] = min(
						d[i-1][j] + 2,//price of deleting symbol s1[i]
						d[i][j-1] + 2,//price of inserting symbol s2[j]
						d[i-1][j-1] + changePrice//price of changing symbol s1[i] on s2[j] 
				);
			}
		}
		return d[n1][n2];
	}
	
	private int min(int i, int j, int k){
		return min(min(i, j), k);
	}
	private int min(int i, int j) {
		if (i < j) 
			return i;
		else 
			return j;
	}

	public Collection<Contact> getContacts(String name) {
		Collection<Contact> possibleContacts = new ArrayList<Contact>();
		String[] names = name.split(" ");
		String firstName = null, lastName = null, middleName = null;
		if (names.length == 1){
			firstName = names[0];
		} else if (names.length == 2){
			lastName = names[0];
			firstName = names[1];
		} else if (names.length == 3){
			lastName = names[0];
			firstName = names[1];
			middleName = names[2];
		} else {
			firstName = names[0];
		}
		int minDistance = Integer.MAX_VALUE;
		for (Contact person : contacts) {
			if (person.getName().equals(name)) {
				possibleContacts.clear();
				possibleContacts.add(person);
				Log.d(MainActivity.TAG, "ContactsDatabase" + ": Result = exact match with " + person.getName());
				return possibleContacts;
			} else {
				Log.d(MainActivity.TAG, "ContactsDatabase" + ": Checking " + person.getName() 
						+ "; FN = " + person.getFirstName() 
						+ "; LN = " + person.getLastName() 
						+ "; MN = " + person.getMiddleName() + " with " + name);
				
				//for order fio
				int distFF = findMinDist(firstName, person.getFirstName());
				int distLL = findMinDist(lastName, person.getLastName());
				int distMM = findMinDist(middleName, person.getMiddleName());
				//TODO: decide if it should really be a sum
				int sumStraight = distFF + distLL + distMM;
				
				Log.d(MainActivity.TAG, "ContactsDatabase"
						+ ": FF = " + distFF + "; LL = " + distLL
						+ "; MM = " + distMM + "; Sum = " + sumStraight);
				
				//for order if
				int distFL = findMinDist(lastName, person.getFirstName());
				int distLF = findMinDist(firstName, person.getLastName());
				//TODO: decide whether to add a penalty for wrong order
				int sumNoMiddle = distFL + distLF;
				
				Log.d(MainActivity.TAG, "ContactsDatabase"
						+ ": FL = " + distFL + "; LF = " + distLF
						+ "; Sum = " + sumNoMiddle);
				
				//for order iof				
				int distFM = findMinDist(middleName, person.getFirstName());				
				int distML = findMinDist(lastName, person.getMiddleName());
				//TODO: decide whether to add a penalty for wrong order
				int sumInverted = distLF + distFM + distML;
				
				Log.d(MainActivity.TAG, "ContactsDatabase"
						+ ": LF = " + distLF + "; FM = " + distFM
						+ "; ML = " + distML + "; Sum = " + sumInverted);
				
				int distSimple = findMinDist(name, person.getName());
				
				Log.d(MainActivity.TAG, "ContactsDatabase"
						+ ": Simple distance = " + distSimple);
				
				int distance = min(min(sumStraight, sumNoMiddle, sumInverted), distSimple);
				
				if (possibleContacts.isEmpty()){
					minDistance = distance;
					possibleContacts.add(person);
				} else if (minDistance > distance){
					minDistance = distance;
					possibleContacts.clear();
					possibleContacts.add(person);
				} else if (minDistance == distance){
					possibleContacts.add(person);
				}
			}
		}
		Log.d(MainActivity.TAG, "ContactsDatabase" + ": Result = found " + possibleContacts.size() + " contacts with minDistance = " + minDistance);
		for (Contact contact : possibleContacts){
			Log.d(MainActivity.TAG, "ContactsDatabase" + ": Result = it may be " + contact.getName());
		}
		//TODO: make a cut off by distance
		return possibleContacts;
	}
	
}



/*private void retrieveContacts() {
Cursor people = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
people.moveToFirst();
int i = 0;
while (people.moveToNext()) {
	i++;
	int nameFieldColumnIndex = people.getColumnIndex(PhoneLookup.DISPLAY_NAME);
	if (nameFieldColumnIndex == -1)	continue;
	String contact = people.getString(nameFieldColumnIndex);
	String contactId = people.getString(people.getColumnIndex(ContactsContract.Contacts._ID)); 
	if (people.getColumnIndex(PhoneLookup.HAS_PHONE_NUMBER) != 0) {
		Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
		phones.moveToFirst();
		Collection<String> numbers = new ArrayList<String>();
		while (phones.moveToNext()) {
			String number = phones.getString(phones.getColumnIndex(
					ContactsContract.CommonDataKinds.Phone.NUMBER));
			Log.d(MainActivity.TAG, this.getLocalClassName()
					+ " " + i 
					+ ": new contact name = " + contact + "; number = "
					+ number);
			numbers.add(number);
		}
		phones.close();
		
		String whereName = ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = ?";
		String[] whereNameParams = new String[] { ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, contactId };
		Cursor nameCur = getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, whereName, whereNameParams, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
		String givenName = null, familyName = null, middleName = null;
		nameCur.moveToFirst();
		//TODO: fix bug
		//Strange: doesn't find all first names and last names which are written and used on phone
		while (nameCur.moveToNext()) {
			givenName = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
			familyName = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
			middleName = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
			Log.d(MainActivity.TAG, this.getLocalClassName()
					+ ": new full contact"
					+ "; FN = " + givenName
					+ "; LN = " + familyName 
					+ "; MN = " + middleName 
					+ "; name = " + contact);
		}
		nameCur.close();
		
		ContactsDatabase.getInstance().addContact(contact, givenName, familyName, middleName, numbers);
	} else {
		Log.d(MainActivity.TAG, this.getLocalClassName()
				+ ": No numbers");
	}
}

people.close();
}*/

