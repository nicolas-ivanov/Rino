package ru.rinorecognizer;

import java.io.IOException;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

public class SvmClassifier {
	private svm_model model;
	private VectorScaler scaler;
	
	
	public SvmClassifier(String modelFile, String rangeFile) 
	{		
		try {
			model = svm.svm_load_model(modelFile);
			scaler = new VectorScaler(rangeFile);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int classify(float[] params) 
	{		
		double[] scaledParams = scaler.scale(params);
		svm_node[] nodes = new svm_node[scaledParams.length];
		
		for (int i = 0; i < params.length; i++) {
			nodes[i] = new svm_node();
			nodes[i].index = i + 1;
			nodes[i].value = scaledParams[i];
		}
		
		return (int)svm.svm_predict(model, nodes);
	}
	
}
