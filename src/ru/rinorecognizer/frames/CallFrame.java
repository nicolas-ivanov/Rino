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


public class CallFrame extends Frame {
	private List<Uri> listUri;
	// optional
	
	public CallFrame(MainActivity main){
		super(main, ActionType.A_CALL);
		listUri = new ArrayList<Uri>();
	}
	
	public FramingResult fill(List<String> wgroups, List<ParamsType> labels)
	{		
		Uri newUri = null;
		Intent intent = null;
		
		for (int i = 0; i < wgroups.size(); i++) {
			switch (labels.get(i)) {
			case P_NAME: 
				newUri = Uri.parse("tel:" + Contact.getPhoneNumber(wgroups.get(i), mainActivity));
				listUri.add(newUri);
				break;
				
			case P_NUMBER:
				newUri = Uri.parse("tel:" + wgroups.get(i));
				listUri.add(newUri);
				break;
				
			default: break;
			}
		}
		
		// check();
		if (listUri.size() == 0) {
			response = "Кому нужно позвонить?"; 
		}
		else if (listUri.size() == 1) {
			Uri numUri = listUri.get(0);
			intent = new Intent(Intent.ACTION_CALL, numUri);
			response = mainActivity.getStr(R.string.calling_number) + " " + numUri.getSchemeSpecificPart();
		}
		else { // (listUri.size() >= 2)
			response = "Слишком много вариантов... Кому позвонить?"; 
			listUri = null;
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