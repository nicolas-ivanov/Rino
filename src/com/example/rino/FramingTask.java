package com.example.rino;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;


public class FramingTask extends AsyncTask<String, String, Intent> {

	private MainActivity mainActivity;
	private MainActivity.SvmBunch svm_bunch;

	
	FramingTask(MainActivity main, MainActivity.SvmBunch bunch){
		mainActivity = main;
		svm_bunch = bunch; 
	}

    
    @Override
    protected Intent doInBackground(String... commands) 
    {    	
    	// step 1: detect command type
    	
    	CommandFeaturesGetter cfGetter = new CommandFeaturesGetter(mainActivity); 
    	double[] cFeatures = cfGetter.getParams(commands[0]);
		int c_id = svm_bunch.svm_A.classify(cFeatures);
		MainActivity.ActionType a_type = MainActivity.ActionType.a_call;
		
		switch (c_id) {
		case 1: a_type = MainActivity.ActionType.a_call; break;
		case 2: a_type = MainActivity.ActionType.a_sms; break;
		case 3: a_type = MainActivity.ActionType.a_email; break;
		case 4: a_type = MainActivity.ActionType.a_look; break;
		case 5: a_type = MainActivity.ActionType.a_site; break;
		case 6: a_type = MainActivity.ActionType.a_balance; break;
		case 7: a_type = MainActivity.ActionType.a_alarm; break;
		default:
				System.out.println("Command ID '" + c_id + "' is incorrect");
		}
		publishProgress("Command type: " + a_type.toString());
		
    	
    	// step 2: map each word of a command with a label to get parameters

		MainActivity.ParamsType[] p_type = new MainActivity.ParamsType[commands.length];
		int[] p_type_id = new int[commands.length];
    	WordFeaturesGetter wfGetter = new WordFeaturesGetter(mainActivity); 
    	double[][] wFeatures = wfGetter.getParams(commands[0]);
    	Intent resIntent = null;
		
		switch (a_type) {
		case a_call:
			for (int i = 0; i < p_type.length; i++)
				p_type_id[i] = svm_bunch.svm_call.classify(wFeatures[i]);
			
			p_type = convertToEnum(p_type_id);
			publishProgress("Features :/n" + p_type.toString());
			
			// step 3: form Intent
			resIntent = new Intent(android.content.Intent.ACTION_CALL);
			
			break;
			
		case a_sms: break;
		case a_email: break;
		case a_look: break;
		case a_site: break;
		case a_balance: break;
		case a_alarm: break;
		}
    	
    	return resIntent;
	}
    
    
    @Override
    protected void onProgressUpdate(String... answer) {
    	super.onProgressUpdate(answer);
  		mainActivity.addAnswer(answer[0]);
    }
    
    
    @Override
    protected void onPostExecute(Intent res) {
    	Log.d(MainActivity.TAG, "AsyncTask: finished");
	    super.onPostExecute(res);
	    mainActivity.endFramingTask();
	}
    
    
    private MainActivity.ParamsType[] convertToEnum(int[] p_type_id)
    {
		MainActivity.ParamsType[] p_type = new MainActivity.ParamsType[p_type_id.length];
		
    	for (int i = 0; i < p_type_id.length; i++)
    		switch (p_type_id[i]) {
    		case 1: p_type[i] = MainActivity.ParamsType.action; break;
    		case 2: p_type[i] = MainActivity.ParamsType.p_name; break;
    		case 3: p_type[i] = MainActivity.ParamsType.p_number; break;
    		case 4: p_type[i] = MainActivity.ParamsType.p_email; break;
    		case 5: p_type[i] = MainActivity.ParamsType.p_site; break;
    		case 6: p_type[i] = MainActivity.ParamsType.p_time; break;
    		case 7: p_type[i] = MainActivity.ParamsType.other; break;
    		default:
    				System.out.println("Parameter's ID '" + p_type[i] + "' is incorrect");
    		}
    	
    	return p_type;
    }
	
}