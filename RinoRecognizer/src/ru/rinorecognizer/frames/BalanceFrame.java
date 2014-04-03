package ru.rinorecognizer.frames;

import java.util.List;

import ru.rinorecognizer.Frame;
import ru.rinorecognizer.FramingResult;
import ru.rinorecognizer.IdTranslator;
import ru.rinorecognizer.MainActivity;
import android.content.Intent;
import android.net.Uri;



public class BalanceFrame extends Frame {
	private Uri numUri;
	
	public BalanceFrame(MainActivity main){
		super(main, IdTranslator.ActionType.A_BALANCE);
		String ussd = "*100" + Uri.encode("#");
		numUri = Uri.parse("tel:" + ussd);	
	}
	
	public FramingResult fill(List<String> wgroups, List<IdTranslator.ParamsType> labels)
	{	
		Intent intent = new Intent(android.content.Intent.ACTION_CALL, numUri);	
		
		response = "Запрашиваю баланс";
		expParameter = null;		
		
		FramingResult framingResult = new FramingResult();
		framingResult.intent = intent;
		framingResult.savedFrame = this;

		return framingResult;
	}	
	
	protected boolean check() {
		return true;
	}
}