package com.example.rino;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

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
		contacts.add(new Contact(name.toLowerCase(Locale.getDefault()), numbers));
	}
	
	public Collection<String> getContact(String name) {
		for (Contact person : contacts) {
			if (person.getName().equals(name)) {
				return person.getNumbers(); 
			}
		}
		return null;
	}
}
