package com.example.rino;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

public class SvmClassifier {

	private svm_model model;
	
	private BufferedReader rangeReader;
	private double feature_min[];
	private double feature_max[];
	private double lower;
	private double upper;
	
	
	public SvmClassifier(String modelFile, String rangeFile) 
	{		
		try {
			model = svm.svm_load_model(modelFile);
			rangeReader = new BufferedReader(new FileReader(rangeFile));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getCommandType(double[] params) 
	{
		feature_min = new double[params.length];
		feature_max = new double[params.length];
		
		restoreRange(rangeReader);
		scaleVector(params);
		
		String type = "";
		svm_node[] nodes = new svm_node[params.length];
		
		for (int i = 0; i < params.length; i++) {
			nodes[i] = new svm_node();
			nodes[i].index = i + 1;
			nodes[i].value = params[i];
		}
		
		int res = (int)svm.svm_predict(model, nodes);
		
		switch (res) {
			case 1: type = "a_call"; break;
			case 2: type = "a_sms" ; break;
			case 3: type = "a_mail"; break;
			case 4: type = "a_look"; break;
			case 5: type = "a_site"; break;
			case 6: type = "a_blnc"; break;
			case 7: type = "a_alrm"; break;
			default:
				System.out.println("Label '" + res + "' is incorrect");
		}
		
		return type;
	}
	
	
	private void restoreRange(BufferedReader fp_restore)
	{		
		int idx;
		double fmin, fmax;
	
		try {	
			if(fp_restore.read() == 'x') {
				fp_restore.readLine();		// pass the '\n' after 'x'
				StringTokenizer st = new StringTokenizer(fp_restore.readLine());
				lower = Double.parseDouble(st.nextToken());
				upper = Double.parseDouble(st.nextToken());
				String restore_line = null;
				
				for (int i = 0; i < feature_min.length; i++)
				{
					restore_line = fp_restore.readLine();
					StringTokenizer st2 = new StringTokenizer(restore_line);
					
					idx = Integer.parseInt(st2.nextToken());
					fmin = Double.parseDouble(st2.nextToken());
					fmax = Double.parseDouble(st2.nextToken());

					feature_min[idx - 1] = fmin;
					feature_max[idx - 1] = fmax;
				}
			}
			fp_restore.close();
			
			
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void scaleVector(double[] v)
	{
		
		for (int i = 0; i < v.length; i++) 
		{
			/* skip single-valued attribute */
			if(feature_max[i] == feature_min[i])
				continue;

			if(v[i] == feature_min[i])
				v[i] = lower;
			else if(v[i] == feature_max[i])
				v[i] = upper;
			else
				v[i] = lower + (upper-lower) * 
					(v[i]-feature_min[i])/
					(feature_max[i]-feature_min[i]);
		}
	}
	
	
}
























