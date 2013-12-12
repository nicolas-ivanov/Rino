package ru.rinorecognizer.frames;

import java.util.ArrayList;
import java.util.List;

import ru.rinorecognizer.Contact;
import ru.rinorecognizer.Frame;
import ru.rinorecognizer.FramingResult;
import ru.rinorecognizer.MainActivity;
import android.content.Intent;
import android.net.Uri;

import com.example.rino.R;


public class EmailFrame extends Frame {
	private List<Uri> emailUriList;
	private List<String> textList;
	
	public EmailFrame(MainActivity main){
		super(main, ActionType.A_EMAIL);
		emailUriList = new ArrayList<Uri>();
		textList = new ArrayList<String>();
	}
	
	public FramingResult fill(List<String> wgroups, List<ParamsType> labels)
	{		
		Uri newEmailUri = null;
		String newText = null;
		Intent intent = null;
		
		for (int i = 0; i < wgroups.size(); i++)
			switch (labels.get(i)) {
			case P_NAME: 
				newEmailUri = Uri.parse("mailto:" + Contact.getEmail(wgroups.get(i), mainActivity));
				emailUriList.add(newEmailUri);
				break;
				
			case QUOTE: 
				newText = wgroups.get(i);
				textList.add(newText);
				break;
				
			default: break;
			}


		
		// check uri;
		if (emailUriList.size() == 0) {
			response = "Кому нужно отправить email?"; 
		}
		else if (emailUriList.size() == 1) {
			Uri emailUri = emailUriList.get(0);
			intent = new Intent(Intent.ACTION_SENDTO, emailUri);			
			intent.putExtra(Intent.EXTRA_SUBJECT, "Hello from Rino");
			
			response = mainActivity.getStr(R.string.sending_email);
		}
		else { // (listUri.size() >= 2)
			response = "Очень много вариантов... Кому нужно отправить email?"; 
			emailUriList = null;
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
		
		FramingResult framingResult = new FramingResult();
		framingResult.intent = intent;
		framingResult.savedFrame = this;

		return framingResult;
	}	
	
	protected boolean check() {
		return true;
	}
}