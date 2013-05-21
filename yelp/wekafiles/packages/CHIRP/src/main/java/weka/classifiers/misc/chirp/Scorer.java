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

public class Scorer implements Serializable {
	private static final long serialVersionUID = 6113097928404937082L;
	public int remainingPoints;
	public static double[] voterError = new double[30];
	public static int voter = 0;
	private static int numNN = 0;
	public static int count = 0;

	public Scorer(int nPts) {
		remainingPoints = nPts;
	}

	public double scoreTrainingData(DataSource trainingData, List chdrs,
			int nVars) {
		int numScored = 0;
		double errorRate = 0;
		double startingPoints = remainingPoints;
		double[] row = new double[nVars];
		for (int i = 0; i < trainingData.predictedValues.length; i++) {
			if (trainingData.predictedValues[i] < 0) {
				for (int j = 0; j < nVars; j++)
					row[j] = trainingData.data[j][i];
				int predicted = -1;
				for (int k = 0; k < chdrs.size(); k++) {
					CHDR ch = (CHDR) chdrs.get(k);
					predicted = ch.score(row);
					if (predicted >= 0) {
						remainingPoints--;
						numScored++;
						break;
					}
				}
				trainingData.predictedValues[i] = predicted;
				int observed = trainingData.classValues[i];
				if (observed != predicted)
					errorRate++;
			}
		}
		errorRate /= trainingData.predictedValues.length;
		if (numScored > 0 && Classifier.debug)
			System.out.println("Error = " + errorRate + ", scored = "
					+ numScored + ", remaining = " + remainingPoints);
		return numScored / startingPoints;
	}

	public int[] scoreTestingData( DataSource testingData,
			List chdrs, int nVars) {
		if (chdrs.size() < 1)
			return null;
		double[] row = new double[nVars];
		
		for (int j = 0; j < nVars; j++) {
			row[j] = testingData.data[j][0];
		}
		
		int predicted = -1;
		double weight = 10;
		for (int k = 0; k < chdrs.size(); k++) {
			CHDR chdr = (CHDR) chdrs.get(k);
			predicted = chdr.score(row);
			if (predicted >= 0) {
				break;
			}
		}
		if (predicted < 0) {
			predicted = scoreUsingNearestRectangle(row, chdrs);
			numNN++;
			weight = 3;
		}
		count++;
		int[] result = new int[2];
		result[0] = predicted;
		result[1] = (int) weight;
		return result;
	}

	private int scoreUsingNearestRectangle(double[] row, List chdrs) {
		double minDist = Double.POSITIVE_INFINITY;
		int minK = -1;
		for (int k = 0; k < chdrs.size(); k++) {
			CHDR chdr = (CHDR) chdrs.get(k);
			double dist = chdr.findClosestRect(row);
			if (dist < minDist) {
				minDist = dist;
				minK = k;
			}
		}
		CHDR chdr = (CHDR) chdrs.get(minK);
		return chdr.classIndex;
	}

	public void deletePoints(DataSource dataSource, CHDR chdr,
			int nVars) {
		double[] row = new double[nVars];
		for (int i = 0; i < dataSource.nPts; i++) {
			if (dataSource.predictedValues[i] < 0) {
				for (int j = 0; j < nVars; j++)
					row[j] = dataSource.data[j][i];
				dataSource.predictedValues[i] = chdr.score(row);
			}
		}
	}
}
