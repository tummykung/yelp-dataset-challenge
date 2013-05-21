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
import java.util.Random;

public class Projection implements Serializable{
    private static final long serialVersionUID = -7144032544335789283L;
	private Random random;
    private int ranLoc;
    private int mWts;

    public Projection(Random random, int mWts) {
        this.random = random;
        this.mWts = mWts;
    }

    public int[] goodProjection(int[] weightIndices, DataSource ds, int currentClass) {
        int[] bi = null;
        double best = -1;
        for (int p = 1; p < 4; p++) {
            int nWts = Math.max(p * weightIndices.length / 4, 1);
            int[] wi = generateWeights(ds, weightIndices, currentClass, nWts);
            double dist = compositeAbsoluteDifference(ds, wi, currentClass);
            if (dist > best) {
                best = dist;
                bi = wi;
            }
        }
        return bi;
    }

    public int[] selectBestVariables(DataSource ds, int currentClass) {
        if (ds.classMeans == null)
            return null;
        int nVariables = ds.classMeans[0].length;
        int[] varIndices;
        if (nVariables < mWts) {
            varIndices = new int[nVariables];
            for (int i = 0; i < nVariables; i++)
                varIndices[i] = i;
        } else {
            double[] distances = new double[nVariables];
            for (int i = 0; i < nVariables; i++)
                distances[i] = univariateAbsoluteDifference(ds, i, currentClass);
            int[] indices = Sorter.descendingSort(distances);
            varIndices = new int[mWts];
            System.arraycopy(indices, 0, varIndices, 0, mWts);
        }
        return varIndices;
    }

    private double univariateAbsoluteDifference(DataSource ds, int variable, int currentClass) {
        double minDifference = Double.POSITIVE_INFINITY;
        for (int k = 0; k < ds.classMeans.length; k++) {
            if (k != currentClass) {
                double difference = Math.abs(ds.classMeans[currentClass][variable] - ds.classMeans[k][variable]);
                minDifference = Math.min(difference, minDifference);
            }
        }
        return minDifference;
    }

    private double compositeAbsoluteDifference(DataSource ds, int[] wi, int currentClass) {
        double currentCentroid = 0;
        int n = 0;
        for (int j = 0; j < wi.length; j++) {
            double wt = 1.;
            if (wi[j] < 0)
                wt = -1.;
            if (!Double.isNaN(ds.classMeans[currentClass][Math.abs(wi[j])])) {
                currentCentroid += wt * ds.classMeans[currentClass][Math.abs(wi[j])];
                n++;
            }
        }
        currentCentroid /= n;
        double minDist = Double.POSITIVE_INFINITY;
        for (int k = 0; k < ds.classMeans.length; k++) {
            if (k != currentClass) {
                double otherCentroid = 0;
                n = 0;
                for (int j = 0; j < wi.length; j++) {
                    double wt = 1.;
                    if (wi[j] < 0)
                        wt = -1.;
                    if (!Double.isNaN(ds.classMeans[k][Math.abs(wi[j])])) {
                        otherCentroid += wt * ds.classMeans[k][Math.abs(wi[j])];
                        n++;
                    }
                }
                otherCentroid /= n;
                double difference = Math.abs(otherCentroid - currentCentroid);
                minDist = Math.min(difference, minDist);
            }
        }
        return minDist;
    }

    private int[] generateWeights(DataSource ds, int[] weightIndices, int currentClass, int nWts) {
        int[] wi = new int[nWts];
        if (weightIndices.length < mWts)
            shuffle(weightIndices);
        System.arraycopy(weightIndices, 0, wi, 0, nWts);
        setNegatives(ds, wi, currentClass);
        return wi;
    }

    private void setNegatives(DataSource ds, int[] wi, int currentClass) {
        double temperature = .01;
        for (int iter = 0; iter < 5; iter++) {
            for (int attemptMove = 0; attemptMove < wi.length / 2; attemptMove++) {
                double preMove = compositeAbsoluteDifference(ds, wi, currentClass);
                move(wi);
                double postMove = compositeAbsoluteDifference(ds, wi, currentClass);
                if (!acceptMove(preMove - postMove, temperature))
                    reset(wi);
            }
            temperature *= .9;
        }
    }

    private boolean acceptMove(double delta, double t) {
        if (delta < 0)
            return true;
        else
            return random.nextDouble() < Math.exp(-delta / t);
    }

    private void move(int[] wi) {
        ranLoc = random.nextInt(wi.length);
        wi[ranLoc] = -wi[ranLoc];
    }

    private void reset(int[] wi) {
        wi[ranLoc] = -wi[ranLoc];
    }

    private void shuffle(int[] a) {
        for (int i = a.length - 1; i > 0; --i) {
            int j = random.nextInt(i);
            int t = a[i];
            a[i] = a[j];
            a[j] = t;
        }
    }
}