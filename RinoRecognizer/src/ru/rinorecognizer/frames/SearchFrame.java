package ru.rinorecognizer.frames;

import java.util.ArrayList;
import java.util.List;

import ru.rinorecognizer.Frame;
import ru.rinorecognizer.FramingResult;
import ru.rinorecognizer.MainActivity;
import android.content.Intent;
import android.net.Uri;



public class SearchFrame extends Frame {
	private List<Uri> wwwUriList;
	private List<String> textList;
	
	public SearchFrame(MainActivity main) {
		super(main, ActionType.A_SEARCH);
		wwwUriList = new ArrayList<Uri>();
		textList = new ArrayList<String>();
	}
	
	public FramingResult fill(List<String> wgroups, List<ParamsType> labels)
	{			
		Uri newUri = null;
		String newText = null;
		Intent intent = null;
		String resStr = "";
		
		for (int i = 0; i < wgroups.size(); i++)
			switch (labels.get(i)) {
			case P_SITE: 
				newUri = Uri.parse("http://ru.wikipedia.org/wiki/");
				wwwUriList.add(newUri);
				break;
				
			case QUOTE: 
				newText = wgroups.get(i).replace(" ", "_");
				textList.add(newText);
				break;
				
			default: break;
			}
		
		
		// check text;
		if (textList.size() != 1) {
			response = "Что нужно найти?"; 
		}
		else {
			resStr = "http://ru.wikipedia.org/w/index.php?search=" + textList.get(0);
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse(resStr));
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