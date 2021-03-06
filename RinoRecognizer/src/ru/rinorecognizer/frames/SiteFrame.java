package ru.rinorecognizer.frames;

import java.util.ArrayList;
import java.util.List;

import ru.rinorecognizer.Frame;
import ru.rinorecognizer.IdTranslator.ActionType;
import ru.rinorecognizer.IdTranslator.LabelsType;
import ru.rinorecognizer.IdTranslator.ParamsType;
import ru.rinorecognizer.MainActivity;
import ru.rinorecognizer.parsers.SiteParser;
import android.content.Intent;
import android.net.Uri;


public class SiteFrame extends Frame {
	private List<Uri> wwwUriList;
	
	public SiteFrame(MainActivity main){
		super(main, ActionType.A_SITE);
		wwwUriList = new ArrayList<Uri>();
	}
	
	public Frame fill(List<String> wgroups, List<LabelsType> labels)
	{			
		Uri siteUri = null;
		
		for (int i = 0; i < wgroups.size(); i++) 
		{
			switch (labels.get(i)) {
			case P_SITE: 
				SiteParser sp = new SiteParser();
				siteUri = sp.getUri(wgroups.get(i));
				wwwUriList.add(siteUri);
				break;
				
			default: break;
			}
		}
		
		// check
		if (wwwUriList.size() == 0) {
			response = "Какой сайт нужно открыть?"; 
			expParameter = ParamsType.P_SITE;
		}
		else if (wwwUriList.size() == 1) 
		{
			Uri wwwUri = wwwUriList.get(0);
			response = "Открываю " + wwwUri.toString();
			expParameter = null;
			intent = new Intent(Intent.ACTION_VIEW, wwwUri);
		}
		else 
		{ // (wwwUriList.size() >= 2)
			response = "Слишком много вариантов...\n";
			
			int i = 0;
			for (Uri uri: wwwUriList)
				response += "\t" + (i++) + ". " + uri.toString() + "\n";
			
			response += "Какой сайт нужно открыть?";
			expParameter = ParamsType.P_SITE;
			wwwUriList = null;
		}

		return this;
	}	
	
	protected boolean check() {
		return true;
	}
}