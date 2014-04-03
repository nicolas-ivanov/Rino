package ru.rinorecognizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class VectorScaler {
	
	private BufferedReader rangeReader;
	private ArrayList<Double> feature_min;
	private ArrayList<Double> feature_max;
	private ArrayList<Integer> feature_idx;
	
	private double lower;
	private double upper;
	
	
	
	public VectorScaler(String rangeFilePath)
	{
		File rangeFile = new File(rangeFilePath);

		try {
			if (rangeFile.exists()) {
				rangeReader = new BufferedReader(new FileReader(rangeFilePath));
				restoreRange(rangeReader);	
			}
			else {
				System.out.println("File '" + rangeFilePath + "' doesn't exist.");
				System.out.println("Range normalization won't be possible.");
			}
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	
	private double[] scaleRange(float[] v)
	{
		if (rangeReader != null) {
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
		else {
			System.out.println("None normalization is executed.");
			return scaleNone(v); 
		}
	}
	
	
	private double[] scaleSphere(float[] v)
	{		
		// get geometric mean
		double squares_sum = 0;
		
		for (int i = 0; i < v.length; i++)
			squares_sum += v[i] * v[i];
		
		double g_mean = Math.sqrt(squares_sum);

		// get scaled vector
		double[] s = new double[v.length]; 
		
		for (int i = 0; i < s.length; i++)
			s[i] = v[i] / g_mean;
			
		return s;
	}
	
	
	private double[] scaleNone(float[] v)
	{		
		double[] s = new double[v.length]; 
		
		for (int i = 0; i < s.length; i++)
			s[i] = v[i];
			
		return s;
	}	
	
	
	public double[] scale(float[] v)
	{
//		return scaleRange(v);
//		return scaleSphere(v);
		return scaleNone(v);
	}
	
}

