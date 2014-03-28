package ru.rinorecognizer.parsers;

import android.net.Uri;

public class SearchParser {
	
	public Uri getUri(String wwwStr, String searchStr) {
		
		Uri searchUri = null;
		
		if (wwwStr.equals("http://yandex.ru"))
			searchUri = Uri.parse(wwwStr + "/yandsearch?text=" + searchStr);
		
		else if (wwwStr.equals("http://yandex.com"))
			searchUri = Uri.parse(wwwStr + "/yandsearch?text=" + searchStr);
		
		else if (wwwStr.equals("http://google.ru"))
			searchUri = Uri.parse(wwwStr + "/search?q=" + searchStr);
		
		else if (wwwStr.equals("http://google.com"))
			searchUri = Uri.parse(wwwStr + "/#q=" + searchStr);
		
		else if (wwwStr.equals("http://ru.wikipedia.org"))
			searchUri = Uri.parse(wwwStr + "/w/index.php?search=" + searchStr);
		
		else if (wwwStr.equals("http://nova.rambler.ru"))
			searchUri = Uri.parse(wwwStr + "/search?query=" + searchStr);
		
		
		return searchUri;
	}

}
