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

public class Binner implements Serializable{
    private static final long serialVersionUID = 1057395471919718409L;
	private Bin[][] bins;
    private int nBins;
    private CHDR chdr;

    public Binner(CHDR chdr) {
        this.chdr = chdr;
        this.nBins = chdr.nBins;
        bins = new Bin[nBins][nBins];
    }

    public int pureCount(int row, int col) {
        Bin bin = bins[row][col];
        if (bin.isPure(chdr.classIndex, 1.0))
            return bin.classCounts[chdr.classIndex];
        else
            return 0;
    }

    public Bin[][] getBins() {
        return bins;
    }

    public int getNumBins() {
        return nBins;
    }

    public CHDR getCHDR() {
        return chdr;
    }

    public void compute(DataSource data, double[] x, double[] y) {
        int[] classValues = data.classValues;
        for (int j = 0; j < nBins; j++) {
            for (int k = 0; k < nBins; k++)
                bins[j][k] = new Bin(chdr.nClasses);
        }
        for (int i = 0; i < x.length; i++) {
            if (Double.isNaN(x[i]) || Double.isNaN(y[i]) || classValues[i] < 0)
                continue;
            if (data.predictedValues[i] < 0) {
                int ix = (int) (DataSource.FUZZ1 * x[i] * nBins);
                int iy = (int) (DataSource.FUZZ1 * y[i] * nBins);
                ix = Math.max(Math.min(ix, nBins - 1), 0);
                iy = Math.max(Math.min(iy, nBins - 1), 0);
                Bin bin = bins[ix][iy];
                bin.classCounts[classValues[i]]++;
                bin.count++;
                bin.centroid[0] += (x[i] - bin.centroid[0]) / bin.count;
                bin.centroid[1] += (y[i] - bin.centroid[1]) / bin.count;
            }
        }
    }
}
