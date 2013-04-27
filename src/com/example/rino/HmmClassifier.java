package com.example.rino;

/*
 * Licensed under the New BSD license.  See the LICENSE file.
 */

import java.util.ArrayList;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;
import be.ac.ulg.montefiore.run.jahmm.OpdfMultiGaussianFactory;
import be.ac.ulg.montefiore.run.jahmm.ViterbiCalculator;
import be.ac.ulg.montefiore.run.jahmm.draw.GenericHmmDrawerDot;


public class HmmClassifier 
{	
	public int[] getStatesSequence(ArrayList<ObservationVector> sequence) 
	throws java.io.IOException
	{			
		Hmm<ObservationVector> learntHmm = buildInitHmm();		
		
		/* Write the final result to a 'dot' (graphviz) file. */
//		(new GenericHmmDrawerDot()).write(learntHmm, "learntHmm.dot");
		
		System.out.println("Sequence probability: " + learntHmm.probability(sequence));
		
		/* Use Viterbi algorith to find out the most likely hidden sequence */
		ViterbiCalculator vc = new ViterbiCalculator(sequence, learntHmm);
		int[] statesSeq = vc.stateSequence();

		for (int i: statesSeq) {
			System.out.println("State = " + i );			
		}
		return statesSeq;

	}
	

	
	static Hmm<ObservationVector> buildInitHmm()
	{	
		Hmm<ObservationVector> hmm = new Hmm<ObservationVector>(6, new OpdfMultiGaussianFactory(6));

		hmm.setPi(0, 0.20);
		hmm.setPi(1, 0.20);
		hmm.setPi(2, 0.20);
		hmm.setPi(3, 0.25);
		hmm.setPi(4, 0.25);
		hmm.setPi(5, 0.25);

		
//		double [] mean = {2. , 4.};
//		double [][] covariance = { {3. , 2} , {2. , 4.} };
//		
//		OpdfMultiGaussian omg = new OpdfMultiGaussian ( mean, covariance );
//		ObservationReal [] obs = new ObservationReal [10000];
//		
//		for ( int i = 0; i < obs . length ; i ++)
//			obs [i] = omg.generate() ;
//		
//		omg.fit(obs);
//
//		
//		hmm.setOpdf(0, omg);
//		hmm.setOpdf(0, new Opdf(new double[] { 0.9, 0, 0, 0, 0, 0, 0.1 }));
//		hmm.setOpdf(1, new OpdfInteger(new double[] { 0, 0.9, 0, 0, 0, 0, 0.1 }));
//		hmm.setOpdf(2, new OpdfInteger(new double[] { 0, 0, 0.9, 0, 0, 0, 0.1 }));
//		hmm.setOpdf(3, new OpdfInteger(new double[] { 0, 0, 0, 0.9, 0, 0, 0.1 }));
//		hmm.setOpdf(4, new OpdfInteger(new double[] { 0, 0, 0, 0, 0.9, 0, 0.1 }));
//		hmm.setOpdf(5, new OpdfInteger(new double[] { 0, 0, 0, 0, 0, 0.9, 0.1 }));
//		hmm.setOpdf(6, new OpdfInteger(new double[] { 0.1, 0, 0, 0, 0, 0, 0.9 }));

		hmm.setAij(0, 0, 0.1);
		hmm.setAij(0, 1, 0.1);
		hmm.setAij(0, 2, 0.1);
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