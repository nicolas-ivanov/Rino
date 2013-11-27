package ru.rinorecognizer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

public class SvmClassifier {

	private svm_model model;
	
	private BufferedReader rangeReader;
	private ArrayList<Double> feature_min;
	private ArrayList<Double> feature_max;
	private ArrayList<Integer> feature_idx;
	
	private double lower;
	private double upper;
	
	
	public SvmClassifier(String modelFile, String rangeFile) 
	{		
		try {
			model = svm.svm_load_model(modelFile);
			rangeReader = new BufferedReader(new FileReader(rangeFile));
			restoreRange(rangeReader);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int classify(int[] params) 
	{		
		//TODO: apply scaling only for action type classification
		VectorScaler vs = new VectorScaler();
		double[] scaledParams = vs.scale(params);
//		double[] scaledParams = scaleVector(params);
		svm_node[] nodes = new svm_node[scaledParams.length];
		
		for (int i = 0; i < params.length; i++) {
			nodes[i] = new svm_node();
			nodes[i].index = i + 1;
			nodes[i].value = scaledParams[i];
		}
		
		return (int)svm.svm_predict(model, nodes);
	}
	
	
	private void restoreRange(BufferedReader fp_restore)
	{	
		int fidx;
		double fmin, fmax;
		feature_min = new ArrayList<Double>();
		feature_max = new ArrayList<Double>();
		feature_idx = new ArrayList<Integer>();
	
		try {	
			if(fp_restore.read() == 'x') {
				fp_restore.readLine();		// pass the '\n' after 'x'
				StringTokenizer st = new StringTokenizer(fp_restore.readLine());
				lower = Double.parseDouble(st.nextToken());
				upper = Double.parseDouble(st.nextToken());
				String restore_line = null;
				
				while ((restore_line = fp_restore.readLine()) != null)
				{
					StringTokenizer st2 = new StringTokenizer(restore_line);
					
					fidx = Integer.parseInt(st2.nextToken());
					fmin = Double.parseDouble(st2.nextToken());
					fmax = Double.parseDouble(st2.nextToken());

					feature_min.add(fmin);
					feature_max.add(fmax);
					feature_idx.add(fidx);
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
	
	
	private double[] scaleVector(int[] v)
	{
		double[] s = new double[v.length]; // scaled array
		
		for (int k = 0; k < feature_idx.size(); k++) 
		{
			int i = feature_idx.get(k) - 1;
			
			/* skip single-valued attribute */
			if(feature_max.get(k) == feature_min.get(k))
				continue;

			if(v[i] == feature_min.get(k))
				s[i] = lower;
			else if(v[i] == feature_max.get(k))
				s[i] = upper;
			else
				s[i] = lower + 
					(upper - lower) * (v[k]-feature_min.get(k)) /
					(feature_max.get(k)-feature_min.get(k));
		}
		return s;
	}
	
	
}
