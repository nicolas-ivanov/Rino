package ru.rinorecognizer.frames;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ru.rinorecognizer.Contact;
import ru.rinorecognizer.ContactsDBHelper;
import ru.rinorecognizer.Frame;
import ru.rinorecognizer.FramingResult;
import ru.rinorecognizer.IdTranslator;
import ru.rinorecognizer.MainActivity;
import ru.rinorecognizer.R;
import android.content.Intent;
import android.net.Uri;


public class EmailFrame extends Frame {
	private List<Contact> contactList;
	private List<String> textList;
	
	public EmailFrame(MainActivity main){
		super(main, IdTranslator.ActionType.A_EMAIL);
		contactList = new ArrayList<Contact>();
		textList = new ArrayList<String>();
	}
	
	public FramingResult fill(List<String> wgroups, List<IdTranslator.ParamsType> labels)
	{		
		String newText = null;
		Intent intent = null;
		
		for (int i = 0; i < wgroups.size(); i++)
			switch (labels.get(i)) {
			case P_NAME: 
				contactList = ContactsDBHelper.getEmail(wgroups.get(i), mainActivity);
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
				response = "Кому нужно отправить email?";
				expParameter = IdTranslator.ParamsType.P_NAME;
			}
			else if (contactList.size() == 1) {
				Contact c = contactList.get(0);
				Uri emailUri = Uri.parse("mailto:" + c.email);
				intent = new Intent(Intent.ACTION_SENDTO, emailUri);			
				intent.putExtra(Intent.EXTRA_SUBJECT, "Hello from Rino");
				
				response = mainActivity.getStr(R.string.sending_email);
				expParameter = null;
			}
			else {
				response = "Слишком много вариантов:\n";
				Iterator<Contact> iter = contactList.iterator();
				
		        while (iter.hasNext()) {
		        	Contact c = iter.next();
		        	response += "\t " + c.name + " (" + c.email + ")\n";
		        }
		        response += "Кому отправить письмо?";
		        
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
					intent.putExtra(Intent.EXTRA_TEXT, text + "\n\nBest regards,\nRinoRecognizer");
			}	
		}
		
		FramingResult framingResult = new FramingResult();
		framingResult.intent = intent;
		framingResult.savedFrame = this;

		return framingResult;
	}	
	
	protected boolean check() {
		return true;
	}
}