package ru.rinorecognizer.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.net.Uri;


public class SiteParser {
	
	private static final Pattern p_twoParts = Pattern.compile("(\\w+) (\\w+)");
	
	public Uri getUri(String sitePatch) {
		
		Uri siteUri = null;
		String siteAddress = null;
		sitePatch.replaceAll(" \\.", "");
		
		Matcher twoPartsMatcher = p_twoParts.matcher(sitePatch);
		
		if (twoPartsMatcher.matches()) // example: "yandex.ru ru"
		{
			String siteName = twoPartsMatcher.group(1);
			String siteDomain = twoPartsMatcher.group(2);
			
			siteAddress = "http://" + siteName.replaceAll("\\.\\w+", "." + siteDomain);
		}	
		else {	// example: "yandex.ru"
			siteAddress = "http://" + sitePatch;
		}
		
		siteUri = Uri.parse(siteAddress);
		
		return siteUri;
	}
}
