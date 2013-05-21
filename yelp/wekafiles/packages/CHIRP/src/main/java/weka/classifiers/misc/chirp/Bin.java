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

public class Bin implements Serializable{
    private static final long serialVersionUID = 8828994626082375823L;
	/* simple container class with public fields to keep accessors simple */
    public double[] centroid;
    public int[] classCounts;
    public int count;
    public boolean isCovered;

    public Bin(int nClasses) {
        classCounts = new int[nClasses];
        centroid = new double[2];
    }

    public boolean isPure(int classIndex, double purityThreshold) {
        if (count == 0)
            return false;
        return (double) classCounts[classIndex] / count >= purityThreshold;
    }
}