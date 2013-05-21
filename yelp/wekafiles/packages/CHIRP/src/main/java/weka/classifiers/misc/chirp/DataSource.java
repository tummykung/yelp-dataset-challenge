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
import java.util.List;

public class DataSource implements Serializable{
   private static final long serialVersionUID = -2025572128257846091L;
	/* container class for data; all fields are public to keep accessors simple */
    public double[][] data;
    public double[][] classMeans;
    public int[] classCounts;
    public double[][] categoryScores;
    public int[] classValues;
    public String[] classNames;
    public int[] predictedValues;
    public DataTransforms transforms;
    public int nCons;
    public int nVars;
    public int nPts;
    public int nCats;
    public List<Integer> nCatValues;
    public int nClasses;
    public static final double FUZZ0 = .00001;
    public static final double FUZZ1 = .99999;

    public DataSource(){
    	
    }
    public DataSource(double[][] data, int[] classValues, String[] classNames, List<Integer> nCatValues,
            int nCats, int nVars, int nRows) {
     	this.data = data;
     	this.classValues = classValues;
         this.classNames = classNames;
         this.nCatValues = nCatValues;
         this.nVars = nVars;
         this.nCats = nCats;
         this.nPts = nRows;
         this.nClasses = classNames.length;
         predictedValues = new int[nPts];
         for (int i = 0; i < nPts; i++)
             predictedValues[i] = -1;
         transforms = new DataTransforms(this);
     }


    public void setTransforms(DataTransforms transforms) {
        this.transforms = transforms;
    }

    public void transformData() {
        transforms.transformData();
    }
    public void transformTestingInstance() {
        transforms.transformTestingInstance(this);
    }
    
    public void updateClassStatistics(int classIndex) {
        scaleCategories(classIndex);
        computeClassStatistics();
    }

    private void computeClassStatistics() {
    	classMeans = new double[nClasses][nVars];
        classCounts = new int[nClasses];
        int[][] counts = new int[nClasses][nVars];
        for (int i = 0; i < nPts; i++) {
        	//System.out.println("44: "+i+" "+predictedValues[i]+" "+classValues[i]);
            if (predictedValues[i] < 0 && classValues[i] >= 0) {
                update(classMeans[classValues[i]], counts[classValues[i]], i);
                classCounts[classValues[i]]++;
            }
        }
           
    }

    private void update(double[] means, int[] counts, int rowIndex) {
        for (int j = 0; j < nVars; j++) {
            double xi = data[j][rowIndex];
            if (!Double.isNaN(xi)) {
                counts[j]++;
                double xd = xi - means[j];
                means[j] += xd / counts[j];
            }
        }
    }
    
    private void scaleCategories(int classIndex) {
        categoryScores = new double[nCats][];
        for (int j = 0; j < nCats; j++) {
            int nCategories = nCatValues.get(j);
            int[][] cellCounts = new int[2][nCategories];
            for (int i = 0; i < nPts; i++) {
                if (predictedValues[i] < 0) {
                    int k = (int) (data[j][i] * nCategories);
                    if (classValues[i] == classIndex)
                        cellCounts[0][k]++;
                    else
                        cellCounts[1][k]++;
                }
            }
            categoryScores[j] = new double[nCategories];
            for (int k = 0; k < nCategories; k++)
                categoryScores[j][k] = (double) cellCounts[0][k] / (cellCounts[0][k] + cellCounts[1][k]);
        }
    }
}