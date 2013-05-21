/*
 * CHIRP: A new classifier based on Composite Hypercubes on Iterated Random Projections.
 *
 * Copyright 2010 by Leland Wilkinson.
 *
 * The contents of this file are subject to the Mozilla Public License Version 2.0 (the "License")
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 */

package weka.classifiers.misc.chirp;

import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.Random;

public class Classifier implements Serializable  {
    private static final long serialVersionUID = 2006380018850120970L;
	private int nReps;
    private Instances trainingData;
    private int seed;
    private Trainer[] trainer;
    public static boolean debug;
     
    public Classifier(int nReps, Instances trainData, int seed, boolean m_debug) {
        this.nReps = nReps;
        this.trainingData = trainData;
        this.seed = seed;
        debug = m_debug;
    }
    
    public void buildClassifier()  {
        Random random = new Random(seed);
        trainer = new Trainer[nReps];
        for (int rep = 0; rep < nReps; rep++) { 
        	if (debug){
        		System.out.println("");     
        		System.out.println("Voter:"+(rep+1));
        	}
        	//Only class attribute 
        	if (trainingData.numAttributes()<=1) 	
        		return ;
        	
			trainer[rep] = new Trainer(trainingData, random, nReps);  
        	trainer[rep].classify();
        }
    }

    public int classifyInstance(Instance instance)  {    	
    	 Scorer.voter=0;
    	 int[] result = null;
    	 int nClasses = Trainer.nClasses;
         int predicted ;
         int weight;
         int[] counts = new int[nClasses];      
         Trainer.tranformTesting(instance);
         for (int rep = 0; rep < nReps; rep++) {   
    		Scorer.voter++;
    		
    		//This helps to pass Only Class Attribute test
    		if (trainer[rep]==null) return 0;
            result = trainer[rep].score(); 
    	
        	//This helps to pass Zero instance test
        	if (result==null) return 0;
            predicted = result[0];
            weight = result[1];
            counts[predicted] +=weight;   
    	}

        int decision = -1;  // Decision
        double maxCount = -1;
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] > maxCount) {
                maxCount = counts[i];
                decision = i;
            }
        }   
        return decision;
    }
}
