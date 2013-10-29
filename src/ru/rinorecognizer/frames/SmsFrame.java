package ru.rinorecognizer.frames;

import java.util.ArrayList;
import java.util.List;

import ru.rinorecognizer.Contact;
import ru.rinorecognizer.Frame;
import ru.rinorecognizer.FramingResult;
import ru.rinorecognizer.MainActivity;
import ru.rinorecognizer.FramingTask.ActionType;
import ru.rinorecognizer.FramingTask.ParamsType;

import android.content.Intent;
import android.net.Uri;

import com.example.rino.R;


public class SmsFrame extends Frame {
	private List<Uri> uriList;
	private List<String> textList;
	
	public SmsFrame(MainActivity main){
		super(main, ActionType.A_SMS);
		uriList = new ArrayList<Uri>();
		textList = new ArrayList<String>();
	}
	
	public FramingResult fill(List<String> wgroups, List<ParamsType> labels)
	{
		Uri newUri = null;
		String newText = null;
		Intent intent = null;
		
		for (int i = 0; i < wgroups.size(); i++)
			switch (labels.get(i)) {
			case P_NAME: 
				newUri = Uri.parse("smsto:" + Contact.getPhoneNumber(wgroups.get(i), mainActivity));
				uriList.add(newUri);
				break;
				
			case P_NUMBER: 
				newUri = Uri.parse("smsto:" + wgroups.get(i));
				uriList.add(newUri);
				break;
				
			case QUOTE: 
				newText = wgroups.get(i);
				textList.add(newText);
				break;
				
			default: break;
			}

		
		
		// check uri;
		if (uriList.size() == 0) {
			response = "Кому отправить смс?"; 
		}
		else if (uriList.size() == 1) {
			Uri numUri = uriList.get(0);
			intent = new Intent(Intent.ACTION_SENDTO, newUri);
			response = mainActivity.getStr(R.string.calling_number) + " " + numUri.getSchemeSpecificPart();
		}
		else { // (listUri.size() >= 2)
			response = "Слишком много вариантов... Кому отправить смс?"; 
			uriList = null;
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
		
		
		FramingResult framingResult = new FramingResult();
		framingResult.intent = intent;
		framingResult.savedFrame = this;

		return framingResult;
	}
	
	protected boolean check() {
		return true;
	}
}