package ru.rinorecognizer.frames;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ru.rinorecognizer.Contact;
import ru.rinorecognizer.ContactsDBHelper;
import ru.rinorecognizer.Frame;
import ru.rinorecognizer.IdTranslator.ActionType;
import ru.rinorecognizer.IdTranslator.LabelsType;
import ru.rinorecognizer.IdTranslator.ParamsType;
import ru.rinorecognizer.MainActivity;
import ru.rinorecognizer.R;
import android.content.Intent;
import android.net.Uri;


public class CallFrame extends Frame {
	private List<Contact> contactList;
	// optional
	
	public CallFrame(MainActivity main){
		super(main, ActionType.A_CALL);
		contactList = new ArrayList<Contact>();
	}
	
	public Frame fill(List<String> wgroups, List<LabelsType> labels)
	{				
		for (int i = 0; i < wgroups.size(); i++) {
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
				
			default: break;
			}
		}
		

		if (contactList.size() == 0) {
			response = "Кому нужно позвонить?"; 
			expParameter = ParamsType.P_NAME;
		}
		else if (contactList.size() == 1) {
			Contact c = contactList.get(0);
			Uri newUri = Uri.parse("tel:" + c.number);
			intent = new Intent(Intent.ACTION_CALL, newUri);
			response = mainActivity.getStr(R.string.calling_number) + " " + newUri.getSchemeSpecificPart();
			expParameter = null;
			isComplete = true;
		}
		else {
			response = "Слишком много вариантов:\n";
			Iterator<Contact> iter = contactList.iterator();
			
	        while (iter.hasNext()) {
	        	Contact c = iter.next();
	        	response += "\t " + c.name + " (" + c.number + ")\n";
	        }
	        response += "Кому позвонить?";
	        
			expParameter = ParamsType.P_NAME;
			contactList = null;
		}

		return this;
	}
	
	protected boolean check() {
		return true;
	}
}