package ru.rinorecognizer.frames;

import java.util.ArrayList;
import java.util.List;

import ru.rinorecognizer.Frame;
import ru.rinorecognizer.FramingResult;
import ru.rinorecognizer.MainActivity;
import android.content.Intent;
import android.net.Uri;

import com.example.rino.R;


public class SiteFrame extends Frame {
	private List<Uri> wwwUriList;
	
	public SiteFrame(MainActivity main){
		super(main, ActionType.A_SITE);
		wwwUriList = new ArrayList<Uri>();
	}
	
	public FramingResult fill(List<String> wgroups, List<ParamsType> labels)
	{			
		Uri newUri = null;
		Intent intent = null;
		
		for (int i = 0; i < wgroups.size(); i++)
			switch (labels.get(i)) {
			case P_SITE: 
				newUri = Uri.parse("http://www." + wgroups.get(i));
				wwwUriList.add(newUri);
				break;
				
			default: break;
			}
		
		
		
		// check uri;
		if (wwwUriList.size() == 0) {
			response = "Какой сайт нужно открыть?"; 
		}
		else if (wwwUriList.size() == 1) {
			Uri wwwUri = wwwUriList.get(0);
			intent = new Intent(Intent.ACTION_VIEW, wwwUri);
		}
		else { // (wwwUriList.size() >= 2)
			response = "Очень много вариантов... Какой сайт нужно открыть?"; 
			wwwUriList = null;
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