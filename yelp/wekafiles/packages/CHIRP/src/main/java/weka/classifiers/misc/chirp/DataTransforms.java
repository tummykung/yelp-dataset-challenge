/*
 * Linf -- An L-infinity classifier.
 *
 * Copyright 2009 by Leland Wilkinson.
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1 (the "License")
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 */

package weka.classifiers.misc.chirp;

import java.io.Serializable;



public class DataTransforms implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6676919987015597423L;
	private DataSource ds;
	private double[] xmin;
	private double[] xmax;
	private int nCats;
	private int nVars;
	private int nPts;
	private int[] isTransformed;

	public DataTransforms(DataSource ds) {
		this.ds = ds;
		nVars = ds.data.length;
		nPts = ds.data[0].length;
		nCats = 0;
	}

	public DataTransforms(DataSource ds, double[] xmin, double[] xmax, int[] isTransformed) {
		this.ds = ds;
		this.xmin = xmin;
		this.xmax = xmax;
		this.isTransformed = isTransformed;
		nVars = ds.data.length;
		nPts = ds.data[0].length;
		nCats = 0;
	}

	public DataTransforms getTransforms(DataSource ds) {
		return new DataTransforms(ds, xmin, xmax, isTransformed);
	}

	public void transformData() {
		computeDataLimits();
		standardizeData();
		pickTransforms();
		transformValues();
	}

	public void transformTestingInstance(DataSource ds) {
		standardizeTestingInstance(ds);
	  	transformValuesTestingInstance(ds);
   }
	
	private void transformValuesTestingInstance(DataSource ds) {
        for (int j = 0; j < ds.nVars; j++) {
        	ds.data[j][0] = getTransformedValue(ds.data[j][0],j);
        }
    }
	
	private void standardizeTestingInstance(DataSource ds) {
	    	for (int j = 0; j < ds.nVars; j++) {
	    		ds.data[j][0] = (ds.data[j][0] - xmin[j]) / (xmax[j] - xmin[j]);
	        }
	}
	
	private void transformValues() {
		for (int j = 0; j < nVars; j++) {
			for (int i = 0; i < nPts; i++){
				ds.data[j][i] = getTransformedValue(ds.data[j][i], j);
			}
		}
	}

	private double getTransformedValue(double d, int att) {
        if (isTransformed[att]==1 && d<0.999&& d>0.01)
           return  0.5+Math.log(d/(1-d))/10.;
        else  if (isTransformed[att]==2 && d<0.999&& d>0.01){
        	 return 1 / (1 + Math.exp(-6 * d + 3));
        }
        else
            return d;
    }
	
	private void pickTransforms() {
		if (isTransformed == null) {
			isTransformed = new int[nVars];
			double[] x = new double[nPts];
			for (int j = nCats; j < nVars; j++) {
				isTransformed[j] = 0;
				for (int i = 0; i < nPts; i++)
					x[i] = getTransformedValue(ds.data[j][i], j);
				double[] lm = LMoments(x, null, 4);
				double skewness = lm[2];
				double kurtosis = lm[3];
				if (Math.abs(skewness) > .2)
					isTransformed[j] = 1;
				else if ( kurtosis > .2) {
					isTransformed[j] = 2;
				}
			}
		}
	}

	private void standardizeData() {
		for (int j = 0; j < nVars; j++) {
			for (int i = 0; i < nPts; i++) {
				ds.data[j][i] = (ds.data[j][i] - xmin[j]) / (xmax[j] - xmin[j]);
			}
		}
	}

	private void computeDataLimits() {
		if (xmin == null) {
			xmin = new double[nVars];
			xmax = new double[nVars];
			for (int i = 0; i < nCats; i++) {
				xmin[i] = .5;
				xmax[i] = .5;
			}
			for (int j = nCats; j < nVars; j++) {
				xmin[j] = Double.POSITIVE_INFINITY;
				xmax[j] = Double.NEGATIVE_INFINITY;
				for (int i = 0; i < nPts; i++) {
					if (!Double.isNaN(ds.data[j][i])) {
						xmin[j] = Math.min(ds.data[j][i], xmin[j]);
						xmax[j] = Math.max(ds.data[j][i], xmax[j]);
					}
				}
				xmin[j] = xmin[j] - ds.FUZZ0 * (xmax[j] - xmin[j]);
				xmax[j] = xmax[j] + ds.FUZZ0 * (xmax[j] - xmin[j]);
			}
		}
	}

	public static double[] LMoments(double[] x, double[] weights, int nMoments) {
		/* 
		 * Hosking, J.R.M., (1990). 
		 * L-Moments: Analysis and estimation of distributions using linear combinations of order statistics. 
		 * J.Royal Stat. Soc. Ser. B 52, 105Ð124.
		 */
		int n = x.length;
		int[] ind = Sorter.ascendingSort(x);
		double[] moments = new double[nMoments];
		double temp;
		double[][] coef = new double[2][nMoments];
		for (int j = 2; j < nMoments; j++) {
			temp = 1.0 / (j * (n - j));
			coef[0][j] = (j + j - 1) * temp;
			coef[1][j] = (j - 1) * (n + j - 1) * temp;
		}
		temp = -n - 1;
		double c = 1.0 / (n - 1);
		int nhalf = n / 2;
		double s;
		for (int i = 0; i < nhalf; i++) {
			temp += 2.0;
			if (weights != null && weights[i] < 1)
				continue;
			double xi = x[ind[i]];
			double xii = x[ind[n - i - 1]];
			double termp = xi + xii;
			double termn = xi - xii;
			moments[0] += termp;
			double s1 = 1;
			s = temp * c;
			moments[1] += s * termn;
			for (int j = 2; j < nMoments; j += 2) {
				double s2 = s1;
				s1 = s;
				s = coef[0][j] * temp * s1 - coef[1][j] * s2;
				moments[j] += s * termp;
				if (j < nMoments - 1) {
					s2 = s1;
					s1 = s;
					s = coef[0][j + 1] * temp * s1 - coef[1][j + 1] * s2;
					moments[j + 1] += s * termn;
				}
			}
		}
		if (n != nhalf + nhalf) {
			double term = x[nhalf];
			s = 1;
			moments[0] += term;
			for (int j = 2; j < nMoments; j += 2) {
				s = -coef[1][j] * s;
				moments[j] += s * term;
			}
		}
		/* L-moment ratios */
		moments[0] /= n;
		if (moments[1] != 0) {
			for (int j = 2; j < nMoments; j++)
				moments[j] /= moments[1];
			moments[1] /= n;
		}
		return moments;
	}
}