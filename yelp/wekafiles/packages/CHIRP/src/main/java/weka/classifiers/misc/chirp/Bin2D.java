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

public class Bin2D implements Serializable{
    private static final long serialVersionUID = -4786986092285827979L;
	/* simple container class with public fields to keep accessors simple */
    public Binner binner;
    public CHDR chdr;
    public int pureCount;

    public Bin2D(Binner binner) {
        this.binner = binner;
        this.chdr = binner.getCHDR();
        computePureCount();
    }

    private void computePureCount() {
        pureCount = 0;
        int nBins = binner.getNumBins();
        for (int i = 0; i < nBins; i++) {
            for (int j = 0; j < nBins; j++) {
                pureCount += binner.pureCount(i, j);
            }
        }
    }
}
