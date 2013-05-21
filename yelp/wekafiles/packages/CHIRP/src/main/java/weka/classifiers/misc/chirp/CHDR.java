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
import java.util.List;
/* Composite Hyper-rectangular Description Region */

public class CHDR implements Serializable{
    private static final long serialVersionUID = -8978983465350072555L;
	public int classIndex;
    public String className;
    public int nClasses;
    public int nCats;
    public int[] xwt, ywt;
    public int nBins;
    private double[] xBounds, yBounds;
    public double[][] catScores;
    @SuppressWarnings("rawtypes")
	public List hdrs;

    public CHDR(int classIndex, String className, int nClasses, int nCats,  int nBins,
                int[] xwt, int[] ywt, double[] xBounds, double[] yBounds, double[][] catScores_) {
        this.classIndex = classIndex;
        this.className = className;
        this.nClasses = nClasses;
        this.nCats = nCats;
        this.nBins = nBins;
        this.xwt = xwt;
        this.ywt = ywt;
        this.xBounds = xBounds;
        this.yBounds = yBounds;
        this.catScores = catScores_;
        hdrs = new ArrayList();
    }

    public void add(HDR hdr) {
        hdrs.add(hdr);
    }

    public int score(double[] row) {
        double xval = getX(row);
        double yval = getY(row);
        for (int i = 0; i < hdrs.size(); i++) {
            HDR hdr = (HDR) hdrs.get(i);
            if (hdr.containsPoint(xval, yval)){
            	return classIndex;
            }    
        }
        return -1;
    }

    public double findClosestRect(double[] row) {
        double xval = getX(row);
        double yval = getY(row);
        double dist = Double.POSITIVE_INFINITY;
        for (int i = 0; i < hdrs.size(); i++) {
            HDR hdr = (HDR) hdrs.get(i);
            dist = Math.min(hdr.rectInfinityDistance(xval, yval), dist);
        }
        return dist;
    }

    public double getX(double[] row) {
        return (computeProjection(row, xwt) - xBounds[0]) / (xBounds[1] - xBounds[0]);
    }

    public double getY(double[] row) {
        return (computeProjection(row, ywt) - yBounds[0]) / (yBounds[1] - yBounds[0]);
    }

    private double computeProjection(double[] row, int[] wi) {
        double nwt = 0;
        double x = 0;
        for (int j = 0; j < wi.length; j++) {
            double wt = 1.;
            if (wi[j] < 0)
                wt = -1.;
            int wij = Math.abs(wi[j]);
            double xi = row[wij];
            if (wij < nCats) {
            	int k = (int) (xi*catScores[wij].length);
            	//System.out.println("       "+k+" "+xi);
                if (k>=catScores[wij].length){
            		k=catScores[wij].length-1;
            	}
            	//System.out.println("   "+wij+" "+k);
                xi = catScores[wij][k];
            }
            if (!Double.isNaN(xi)) {
                x += wt * xi;
                nwt++;
            }
        }
        if (nwt < wi.length)
            x = x * wi.length / nwt;
        return x;
    }
}