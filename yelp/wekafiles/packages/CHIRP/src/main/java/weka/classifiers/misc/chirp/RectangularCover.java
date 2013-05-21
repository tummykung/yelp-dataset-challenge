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

public class RectangularCover {
    private Bin2D bin2D;
    private Bin[][] bins;
    private int nBins;
    private Binner binner;
    private int classIndex;
    private int i1, i2, j1, j2;
    private int minCoveredPoints;
    private double bestOdds;
    private int classCount, otherCount;
    private double purityThreshold;
  
    public RectangularCover(Bin2D bin2D, int minCoveredPoints, double purityThreshold) {
        this.bin2D = bin2D;
        this.minCoveredPoints = minCoveredPoints;
        this.binner = bin2D.binner;
        this.purityThreshold = purityThreshold;
        bins = binner.getBins();
        nBins = binner.getNumBins();
        classIndex = bin2D.chdr.classIndex;
    }

    public int compute() {
        int totalCovered = 0;
        for (int rct = 0; rct < 10; rct++) {   // number of rectangles allowed
        	Rect bestCover = null;
            double bestCount = 0;
            for (int i = 0; i < nBins; i++) {
                for (int j = 0; j < nBins; j++) {
                    if (!bins[i][j].isCovered && bins[i][j].isPure(classIndex, purityThreshold)) {
                        cover(i, j);
                        if (classCount > bestCount) {
                            bestCover = makeRectangle();
                            bestCount = classCount;
                        }
                    }
                }
            }
            if (bestCount < minCoveredPoints) {
                break;
            } else {
                bin2D.chdr.add(new HDR(bestCover));
                tagCoveredBins(bestCover);
                totalCovered += bestCount;
            }
        }
        return totalCovered;
    }

    private Rect makeRectangle() {
        double x = (double) i1 / nBins;
        double y = (double) j1 / nBins;
        double w = (double) (i2 - i1 + 1) / nBins;
        double h = (double) (j2 - j1 + 1) / nBins;
        return new Rect(x, y, w, h);
    }

    private void tagCoveredBins(Rect rect) {
        int i1 = (int) (rect.x * nBins);
        int j1 = (int) (rect.y * nBins);
        int i2 = i1 + (int) (rect.width * nBins);
        int j2 = j1 + (int) (rect.height * nBins);
        for (int i = i1; i < i2; i++) {
            for (int j = j1; j < j2; j++) {
                bins[i][j].isCovered = true;
            }
        }
    }

    private void cover(int ix, int iy) {
        i1 = ix;
        i2 = i1;
        j1 = iy;
        j2 = j1;
        classCount = bins[ix][iy].classCounts[classIndex];
        otherCount = bins[ix][iy].count - classCount;
        bestOdds = (classCount + 1) / (otherCount + 1);
        int borderCount;
        do {
            borderCount = 0;
            borderCount += lookUp();
            borderCount += lookRight();
            borderCount += lookDown();
            borderCount += lookLeft();
        } while (borderCount > 0);
        if (i1 == i2 && j1 == j2)
            classCount = 0;
    }

    private int lookUp() {
        if (j2 > nBins - 2)
            return 0;
        int cCount = classCount;
        int oCount = otherCount;
        for (int i = i1; i <= i2; i++) {
            Bin bin = bins[i][j2 + 1];
            cCount += bin.classCounts[classIndex];
            oCount += bin.count - bin.classCounts[classIndex];
            if (cCount <= bin.count|| oCount>10)
                return 0;
        }
        double odds = (double) (cCount + 1) / (oCount + 1);
        if (odds < bestOdds)
            return 0;
        bestOdds = odds;
        classCount = cCount;
        otherCount = oCount;
        j2++;
        return 1;
    }

    private int lookDown() {
        if (j1 < 1)
            return 0;
        int cCount = classCount;
        int oCount = otherCount;
        for (int i = i1; i <= i2; i++) {
            Bin bin = bins[i][j1 - 1];
            cCount += bin.classCounts[classIndex];
            oCount += bin.count - bin.classCounts[classIndex];
            if (cCount <= bin.count|| oCount>10)
                return 0;
        }
        double odds = (double) (cCount + 1) / (oCount + 1);
        if (odds < bestOdds)
            return 0;
        bestOdds = odds;
        classCount = cCount;
        otherCount = oCount;
        j1--;
        return 1;
    }

    private int lookLeft() {
        if (i1 < 1)
            return 0;
        int cCount = classCount;
        int oCount = otherCount;
        for (int j = j1; j <= j2; j++) {
            Bin bin = bins[i1 - 1][j];
            cCount += bin.classCounts[classIndex];
            oCount += bin.count - bin.classCounts[classIndex];
            if (cCount <= bin.count || oCount>10)
                return 0;
        }
        double odds = (double) (cCount + 1) / (oCount + 1);
        if (odds < bestOdds)
            return 0;
        bestOdds = odds;
        classCount = cCount;
        otherCount = oCount;
        i1--; 
        return 1;
    }

    private int lookRight() {
        if (i2 > nBins - 2)
            return 0;
        int cCount = classCount;
        int oCount = otherCount;
        for (int j = j1; j <= j2; j++) {
            Bin bin = bins[i2 + 1][j];
            cCount += bin.classCounts[classIndex];
            oCount += bin.count - bin.classCounts[classIndex];
            if (cCount <= bin.count|| oCount>10)
                return 0;
        }
        double odds = (double) (cCount + 1) / (oCount + 1);
        if (odds < bestOdds)
            return 0;
        bestOdds = odds;
        classCount = cCount;
        otherCount = oCount;
        i2++;
        return 1;
    }
}