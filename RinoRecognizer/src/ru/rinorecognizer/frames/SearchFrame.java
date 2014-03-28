package ru.rinorecognizer.frames;

import java.util.ArrayList;
import java.util.List;

import ru.rinorecognizer.Frame;
import ru.rinorecognizer.FramingResult;
import ru.rinorecognizer.MainActivity;
import ru.rinorecognizer.parsers.SearchParser;
import ru.rinorecognizer.parsers.SiteParser;
import android.content.Intent;
import android.net.Uri;



public class SearchFrame extends Frame {
	private List<Uri> wwwUriList;
	private List<String> searchList;
	
	public SearchFrame(MainActivity main) {
		super(main, ActionType.A_SEARCH);
		wwwUriList = new ArrayList<Uri>();
		searchList = new ArrayList<String>();
	}
	
	public FramingResult fill(List<String> wgroups, List<ParamsType> labels)
	{			
		Uri siteUri = null;		
		String newText = null;
		Intent intent = null;
		String searchStr = "";
		
		for (int i = 0; i < wgroups.size(); i++)
			switch (labels.get(i)) {
			case P_SITE: 

				SiteParser sp = new SiteParser();
				siteUri = sp.getUri(wgroups.get(i));
				wwwUriList.add(siteUri);
				break;
				
			case QUOTE: 
				newText = wgroups.get(i);
				searchList.add(newText);
				break;
				
			default: break;
			}
		
		
		// check site
		if (wwwUriList.size() == 0) {
			response = "Какой сайт нужно открыть?"; 
		}
		else if (wwwUriList.size() == 1) 
		{
			Uri wwwUri = wwwUriList.get(0);
			
			// check text;
			if (searchList.size() == 0) {
				response += "Что нужно найти?";			
			}
			else if (searchList.size() == 1) {
				searchStr = searchList.get(0);
				response = "Ищу «" + searchStr + "»";
				
				SearchParser searchParser = new SearchParser();
				Uri searchUri = searchParser.getUri(wwwUri.toString(), searchStr);
				
				if (searchUri != null)
					intent = new Intent(Intent.ACTION_VIEW, searchUri);
				else
					response += "Не удалось осуществить поиск с помощью " + wwwUri.toString() + "...";
			}
			else { // textList.size() >= 2
				response = "Слишком много вариантов...\n";
				
				int i = 0;
				for (String str: searchList)
					response += "\t" + (i++) + ". " + str + "\n";
				
				response += "Что нужно найти?";
				searchList = null;
			}
		}
		else 
		{ // (wwwUriList.size() >= 2)
			response = "Слишком много вариантов...\n";
			
			int i = 0;
			for (Uri uri: wwwUriList)
				response += "\t" + (i++) + ". " + uri.toString() + "\n";
			
			response += "Какой сайт нужно открыть?";
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