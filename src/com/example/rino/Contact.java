package com.example.rino;

import java.util.Collection;

public class Contact {
	private String name;
	private Collection<String> numbers;
	
	public Contact(String name, Collection<String> numbers) {
		this.name = name;
		this.numbers = numbers;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Collection<String> getNumbers() {
		return numbers;
	}		
}
