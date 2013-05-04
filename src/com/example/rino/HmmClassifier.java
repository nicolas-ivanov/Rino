package com.example.rino;

/*
 * Licensed under the New BSD license.  See the LICENSE file.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;
import be.ac.ulg.montefiore.run.jahmm.OpdfMultiGaussian;
import be.ac.ulg.montefiore.run.jahmm.OpdfMultiGaussianFactory;
import be.ac.ulg.montefiore.run.jahmm.io.FileFormatException;
import be.ac.ulg.montefiore.run.jahmm.io.HmmReader;
import be.ac.ulg.montefiore.run.jahmm.io.HmmWriter;
import be.ac.ulg.montefiore.run.jahmm.io.ObservationSequencesReader;
import be.ac.ulg.montefiore.run.jahmm.io.ObservationVectorReader;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfMultiGaussianReader;
import be.ac.ulg.montefiore.run.jahmm.io.OpdfMultiGaussianWriter;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;
import be.ac.ulg.montefiore.run.jahmm.toolbox.KullbackLeiblerDistanceCalculator;


public class HmmClassifier 
{	
	Hmm<ObservationVector> learntHmm;
	
	
	public int[] getStatesSequence(ArrayList<ObservationVector> sequence)
	{			
		return learntHmm.mostLikelyStateSequence(sequence);
	}
	
	
	public void load(File hmmFile) 
	{
		try {
			BufferedReader reader = new BufferedReader(new FileReader(hmmFile));
			OpdfMultiGaussianReader opdfReader = new OpdfMultiGaussianReader();
			learntHmm = HmmReader.read(reader, opdfReader);
			reader.close();

		} catch (FileFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void save(File hmmFile) 
	{
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(hmmFile));
			OpdfMultiGaussianWriter opdfWriter = new OpdfMultiGaussianWriter();
			HmmWriter.write(writer, opdfWriter, learntHmm);
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	
	public void train(InputStream trainDataStream)
	{
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(trainDataStream));
			List<List<ObservationVector>> sequences = ObservationSequencesReader
					.readSequences(new ObservationVectorReader(), reader);
			reader.close();

			BaumWelchLearner bwl = new BaumWelchLearner();
			KullbackLeiblerDistanceCalculator klc = new KullbackLeiblerDistanceCalculator();
			
			Hmm<ObservationVector> prevHmm = learntHmm;
			// Incrementally improve the solution
			for (int i = 0; i < 10; i++) {
				learntHmm = bwl.iterate(learntHmm, sequences);
//				System.out.println("Distance at iteration " + i + ": " + klc.distance(learntHmm, prevHmm));
//				prevHmm = learntHmm;
			}
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	static void saveInit(File hmmFile) 
	{
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(hmmFile));
			OpdfMultiGaussianWriter opdfWriter = new OpdfMultiGaussianWriter();
			Hmm<ObservationVector> initHmm = buildInitHmm();
			HmmWriter.write(writer, opdfWriter, initHmm);
			writer.flush();
			writer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	
	static Hmm<ObservationVector> buildInitHmm()
	{	
		Hmm<ObservationVector> hmm = new Hmm<ObservationVector>(6, new OpdfMultiGaussianFactory(6));

		hmm.setPi(0, 0.20);
		hmm.setPi(1, 0.20);
		hmm.setPi(2, 0.20);
		hmm.setPi(3, 0.20);
		hmm.setPi(4, 0.20);
		hmm.setPi(5, 0.20);
				
		double [][] covariance = new double[6][6];;
		for (int i = 0; i < 6; i++)
			for (int j = 0; j < 6; j++)
				if ( i == j)
					covariance[i][j] = 1;
				else
					covariance[i][j] = 0.9;

		hmm.setOpdf(0, new OpdfMultiGaussian(new double[] {0.9, 0.1, 0.1, 0.1, 0.1, 0.1}, covariance));
		hmm.setOpdf(1, new OpdfMultiGaussian(new double[] {0.1, 0.9, 0.1, 0.1, 0.1, 0.1}, covariance));
		hmm.setOpdf(2, new OpdfMultiGaussian(new double[] {0.1, 0.1, 0.9, 0.1, 0.1, 0.1}, covariance));
		hmm.setOpdf(3, new OpdfMultiGaussian(new double[] {0.1, 0.1, 0.1, 0.9, 0.1, 0.1}, covariance));
		hmm.setOpdf(4, new OpdfMultiGaussian(new double[] {0.1, 0.1, 0.1, 0.1, 0.9, 0.1}, covariance));
		hmm.setOpdf(5, new OpdfMultiGaussian(new double[] {0.1, 0.1, 0.1, 0.1, 0.1, 0.9}, covariance));
		
		
		hmm.setAij(0, 0, 0.1);
		hmm.setAij(0, 1, 0.1);
		hmm.setAij(0, 2, 0.9);
		hmm.setAij(0, 3, 0.1);
		hmm.setAij(0, 4, 0.1);
		hmm.setAij(0, 5, 0.1);
		
		hmm.setAij(1, 0, 0.1);
		hmm.setAij(1, 1, 0.1);
		hmm.setAij(1, 2, 0.1);
		hmm.setAij(1, 3, 0.1);
		hmm.setAij(1, 4, 0.1);
		hmm.setAij(1, 5, 0.1);
		
		hmm.setAij(2, 0, 0.1);
		hmm.setAij(2, 1, 0.1);
		hmm.setAij(2, 2, 0.1);
		hmm.setAij(2, 3, 0.1);
		hmm.setAij(2, 4, 0.1);
		hmm.setAij(2, 5, 0.1);

		hmm.setAij(3, 0, 0.1);
		hmm.setAij(3, 1, 0.1);
		hmm.setAij(3, 2, 0.1);
		hmm.setAij(3, 3, 0.1);
		hmm.setAij(3, 4, 0.1);
		hmm.setAij(3, 5, 0.1);
		
		hmm.setAij(4, 0, 0.1);
		hmm.setAij(4, 1, 0.1);
		hmm.setAij(4, 2, 0.1);
		hmm.setAij(4, 3, 0.1);
		hmm.setAij(4, 4, 0.1);
		hmm.setAij(4, 5, 0.1);
		
		hmm.setAij(5, 0, 0.1);
		hmm.setAij(5, 1, 0.1);
		hmm.setAij(5, 2, 0.1);
		hmm.setAij(5, 3, 0.1);
		hmm.setAij(5, 4, 0.1);
		hmm.setAij(5, 5, 0.1);
		
		return hmm;
	}

}