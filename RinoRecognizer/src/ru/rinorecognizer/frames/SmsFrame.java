package ru.rinorecognizer.frames;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ru.rinorecognizer.Contact;
import ru.rinorecognizer.ContactsDBHelper;
import ru.rinorecognizer.Frame;
import ru.rinorecognizer.IdTranslator;
import ru.rinorecognizer.MainActivity;
import android.content.Intent;
import android.net.Uri;


public class SmsFrame extends Frame {
	private List<Contact> contactList;
	private List<String> textList;
	
	public SmsFrame(MainActivity main){
		super(main, IdTranslator.ActionType.A_SMS);
		contactList = new ArrayList<Contact>();
		textList = new ArrayList<String>();
	}
	
	public Frame fill(List<String> wgroups, List<IdTranslator.ParamsType> labels)
	{
		String newText = null;
		
		for (int i = 0; i < wgroups.size(); i++)
			switch (labels.get(i)) {
			
			case P_NAME: 
				contactList = ContactsDBHelper.getPhoneNumber(wgroups.get(i), mainActivity);
				break;
				
			case P_NUMBER: 
				Contact c = new Contact();
				c.name = "Телефон";
				c.number = wgroups.get(i);
				contactList.add(c);
				break;
				
			case QUOTE: 
				newText = wgroups.get(i);
				textList.add(newText);
				break;
				
			default: break;
			}

		
		
		if (textList.size() == 0) {
			response = "Что нужно написать?";
			expParameter = IdTranslator.ParamsType.QUOTE;
		}
		else {
			if (contactList.size() == 0) {
				response = "Кому отправить смс?"; 
				expParameter = IdTranslator.ParamsType.P_NAME;
				isComplete = false;
			}
			else if (contactList.size() == 1) {
				Contact c = contactList.get(0);
				Uri newUri = Uri.parse("smsto:" + c.number);
				intent = new Intent(Intent.ACTION_SENDTO, newUri);
				response = "Отправляю смс абоненту «" + c.name + "»";
				expParameter = null;
			}
			else { 
				response = "Слишком много вариантов:\n";
				Iterator<Contact> iter = contactList.iterator();
				
		        while (iter.hasNext()) {
		        	Contact c = iter.next();
		        	response += "\t " + c.name + " (" + c.number + ")\n";
		        }
		        response += "Кому отправить смс?";
		        
				expParameter = IdTranslator.ParamsType.P_NAME;
				contactList = null;
			}
			
			
			// check text;
			if (textList.size() == 0) {
				textList.add(""); 
			}
			else if (textList.size() >= 1) {
				String text = "";
				for (String t: textList)
					text += " " + t;
				
				if (intent != null)
					intent.putExtra("sms_body", text);
			}		
		}

		return this;
	}
	
	protected boolean check() {
		return true;
	}
}