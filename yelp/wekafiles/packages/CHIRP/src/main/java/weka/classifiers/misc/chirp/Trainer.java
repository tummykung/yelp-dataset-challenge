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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class Trainer implements Serializable {
    private static final long serialVersionUID = 1L;
	private static DataSource trainingData;
    private int bestBin2D;
    private int bestCover;
    private List chdrs;
    private Bin2D bin2D;
    private List bin2DList;
    private Projection projection;
    private Scorer scorer;
    public int nPts;                   // number of points
    public static int nVars;                  // number of variables (features)
    private static int nCats;                  // number of categorical variables
    private int mPairs;                 // number of pairwise plots generated
    private int nPairs;                 // number of best pairwise plots examined
    private int mWts;                   // maximum number of weighted variables in projections
    public static int nClasses;               // number of classes
    private int currentClass;           // current class being evaluated
    private int currentPair;            // current pairwise plot being evaluated
    private int[] purityIndices;        // indices of sorted purities (one for each 2D Bin)
    private int nFailures;              // number of sucessive classes tried without finding nonempty rectangle
    private int minCoveredPoints;       // minimum number of instances a rectangle may score
    private boolean[] isClassified;     // records whether a class has been classified at least once
    private double purityThreshold;     // smallest purity for considering a bin cell pure
    private static boolean[] isCatCol;
    private static  DataSource testingData;
    
    public Trainer(Instances trainingInstances,  Random random, int nReps) {
        mPairs = 25;
        nPairs = 5;
        mWts = 50;
        chdrs = new ArrayList();
        
    	//Read column lables
    	nVars = trainingInstances.numAttributes()-1;          //last column is class label so ignore it  
    	String[] columnLabels = new String[nVars];
        int k =0;
        for (Enumeration e = trainingInstances.enumerateAttributes(); e.hasMoreElements() ;) {
      	  	Attribute a = (Attribute) e.nextElement();
      	  	columnLabels[k] = a.name();
      	  	k++;
        }       
       
       nCats = 0;
       isCatCol = new boolean[nVars];
       List<Integer> nCatValues = new ArrayList<Integer>();
       for (int kk=0; kk < nVars; kk++){
       		if (trainingInstances.attribute(kk).isNominal()){
        		nCats++;
        		isCatCol[kk] = true;
        		nCatValues.add(trainingInstances.attribute(kk).numValues());
       		}
        }
        
        //store data[][] array
        nPts = trainingInstances.numInstances();
        double[][] data = new double[nVars][nPts];
        for (int i=0;i<nPts;i++){
            int cat = 0, con = nCats;
        	 for (int j=0;j<nVars;j++){
        		 if (isCatCol[j]) {
            		 data[cat][i]=trainingInstances.get(i).value(j);
                	 cat++;
                 }
                 else{
                	 data[con][i]=trainingInstances.get(i).value(j);
                	 con++;
                 }
        	 }	       
      	}           
        //store class labels
        String[] classNames = new String[trainingInstances.classAttribute().numValues()];
        int i =0;
        for (Enumeration e = trainingInstances.classAttribute().enumerateValues(); e.hasMoreElements() ;) {
        	String s = (String) e.nextElement();
      	  	classNames[i] = s;
      	    i++;
        }
        int[] classValues = new int[nPts];
        for (int j = 0; j < nPts; j++){
        	classValues[j] = (int) trainingInstances.instance(j).classValue() ;	
        } 
        
        trainingData = new DataSource(data, classValues, classNames,nCatValues,nCats, nVars,nPts);
        trainingData.transformData();
        nClasses = trainingData.nClasses;
        isClassified = new boolean[nClasses];
        projection = new Projection(random, mWts);
        scorer = new Scorer(nPts);
        trainingData.updateClassStatistics(0);
        
        currentClass = nClasses;
        minCoveredPoints = Math.min(Math.max(nPts / 10, 1), 10);
        purityThreshold = 1;
    }
    
    public void classify() {
    	nFailures = 0;
    	nextClass();
        while (nFailures < 2*nClasses){
            processClass();
        }    
         while (thereIsAnUnclassifiedClass()) {
            processRemainingUnclassifiedClasses();
        }
    }

    private void processClass() {
        int nCovered = coverBins();
        if (nCovered > bestCover) {
         	bestBin2D = purityIndices[currentPair];
            bestCover = nCovered;
        }
        if (currentPair < nPairs) {
            nextPair();
        } else {
            if (bestCover > minCoveredPoints) {
                Bin2D bin2D = (Bin2D) bin2DList.get(bestBin2D);
                chdrs.add(bin2D.chdr);
                double p = scorer.scoreTrainingData(trainingData, chdrs, nVars);
                if (p < .01)
                    purityThreshold = .75;
                nFailures = 0;
                isClassified[currentClass] = true;
            }
            nextClass();
        }
    }

    // Process remaining unclassified classes.
    private void processRemainingUnclassifiedClasses() {
        nFailures = 0;
        while (nFailures < 2 * nClasses)
            processClass();
    }

    private boolean thereIsAnUnclassifiedClass() {
        minCoveredPoints --;
        if (minCoveredPoints == 0)
            return false;
        for (int k = 0; k < nClasses; k++) {
            if (!isClassified[k])
                return true;
        }
        return false;
    }

    private int coverBins() {
        if (trainingData.classCounts[currentClass] == 0)
            return 0;
        RectangularCover r = new RectangularCover(bin2D, minCoveredPoints, purityThreshold);
        return r.compute();
    }

    private void nextPair() {
        currentPair++;
        getNext2DBin();
    }

    private void nextClass() {
        currentPair = 0;
        bestCover = -1;
        nFailures++;
        incrementClass();
        trainingData.updateClassStatistics(currentClass);
        if (trainingData.classCounts[currentClass] > 0) {
            build2DBinList();
            getNext2DBin();
            
        }
    }

    private void incrementClass() {
        currentClass++;
        if (currentClass >= nClasses)
            currentClass = 0;
    }

    private void getNext2DBin() {
        int currentIndex = purityIndices[currentPair];
        bin2D = (Bin2D) bin2DList.get(currentIndex);
    }

    private void build2DBinList() {
        double[] pureCounts = new double[mPairs];
        bin2DList = new ArrayList();

        int[] bestVars = projection.selectBestVariables(trainingData, currentClass);
        int nBins = (int) Math.max(2 * Math.log(scorer.remainingPoints) / Math.log(2), 10);
        for (int i = 0; i < mPairs; i++) {
            Bin2D b2D = build2DBin(bestVars, nBins);
            bin2DList.add(b2D);
            pureCounts[i] = b2D.pureCount;
        }
        purityIndices = Sorter.descendingSort(pureCounts);
    }

    private Bin2D build2DBin(int[] bestVars, int nBins) {
        String className = trainingData.classNames[currentClass];
        double[] xBounds;
        double[] yBounds;
        int[] xwt = projection.goodProjection(bestVars, trainingData, currentClass);
        int[] ywt = projection.goodProjection(bestVars, trainingData, currentClass);
        xBounds = computeBounds(trainingData, xwt, nPts);
        yBounds = computeBounds(trainingData, ywt, nPts);
        double[][] categoryScores = copyCategoryScores(xwt, ywt);

        CHDR chdr = new CHDR(currentClass, className, nClasses, nCats, nBins,
                xwt, ywt, xBounds, yBounds, categoryScores);

        Binner binner = new Binner(chdr);
        double[] x = fillArray(xwt, xBounds);
        double[] y = fillArray(ywt, yBounds);
        binner.compute(trainingData, x, y);
        return new Bin2D(binner);
    }

    //--------------------------------------------------
    private double[][] copyCategoryScores(int[] xind, int[] yind) {
        double[][] cs = new double[trainingData.categoryScores.length][];
        for (int j = 0; j < xind.length; j++) {
            int index = Math.abs(xind[j]);
            if (index < nCats) {
                int nc = trainingData.categoryScores[index].length;
                cs[index] = new double[nc];
                System.arraycopy(trainingData.categoryScores[index], 0, cs[index], 0, nc);
            }
        }
        for (int j = 0; j < yind.length; j++) {
            int index = Math.abs(yind[j]);
            if (index < nCats) {
                int nc = trainingData.categoryScores[index].length;
                cs[index] = new double[nc];
                System.arraycopy(trainingData.categoryScores[index], 0, cs[index], 0, nc);
            }
        }
        return cs;
    }
    
    private double[] fillArray(int[] wi, double[] bounds) {
        double[] result = new double[nPts];
        double[] wt = new double[wi.length];
        for (int j = 0; j < wi.length; j++) {
            wt[j] = 1.;
            if (wi[j] < 0)
                wt[j] = -1.;
        }
        double nwt;
        for (int i = 0; i < nPts; i++) {
            nwt = 0;
            for (int j = 0; j < wi.length; j++) {
                int wij = Math.abs(wi[j]);
                double xi = trainingData.data[wij][i];
                if (wij < nCats) {
                    int k = (int) (xi * trainingData.categoryScores[wij].length);
                    xi = trainingData.categoryScores[wij][k];
                }
                if (!Double.isNaN(xi)) {
                    result[i] += wt[j] * xi;
                    nwt++;
                }
            }
            if (nwt < wi.length)
                result[i] = result[i] * wi.length / nwt;
            result[i] = (result[i] - bounds[0]) / (bounds[1] - bounds[0]);
        }
        return result;
    }

    private double[] computeBounds(DataSource dataSource, int[] wi, int nPts) {
        double[] bounds = new double[2];
        bounds[0] = Double.POSITIVE_INFINITY;
        bounds[1] = Double.NEGATIVE_INFINITY;
        double nwt, data;
        for (int i = 0; i < nPts; i++) {
            nwt = 0;
            data = 0;
            for (int j = 0; j < wi.length; j++) {
                double wt = 1.;
                if (wi[j] < 0)
                    wt = -1.;
                int wij = Math.abs(wi[j]);
                double xi = dataSource.data[wij][i];
                if (wij < nCats) {
                    int k = (int) (xi * dataSource.categoryScores[wij].length);
                    xi = dataSource.categoryScores[wij][k];
                }
                if (!Double.isNaN(xi)) {
                    data += wt * xi;
                    nwt++;
                }
            }
            if (nwt < wi.length)
                data = data * wi.length / nwt;
            if (!Double.isNaN(data)) {
                bounds[0] = Math.min(bounds[0], data);
                bounds[1] = Math.max(bounds[1], data);
            }
        }
        bounds[0] = bounds[0] - DataSource.FUZZ0 * (bounds[1] - bounds[0]);
        bounds[1] = bounds[1] + DataSource.FUZZ0 * (bounds[1] - bounds[0]);
        return bounds;
    }

    public static void tranformTesting(Instance instance) {
        testingData= new DataSource();
        int cat = 0, con = nCats;
        double[][] data = new double[nVars][1];
    	for (int j=0;j<nVars;j++){
    		 if (isCatCol[j]) {
        		 data[cat][0]=instance.value(j);
            	 cat++;
             }
             else{
            	 data[con][0]=instance.value(j);
            	 con++;
             }
    	}	       
    	testingData.data = data;
    	testingData.nVars = instance.numAttributes()-1;
    	testingData.setTransforms(trainingData.transforms);
    	testingData.transformTestingInstance();
    }	
    
    public int[] score() {
        return scorer.scoreTestingData(testingData, chdrs, nVars);
     }
}