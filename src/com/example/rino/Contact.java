package com.example.rino;

import java.util.Collection;

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
}
