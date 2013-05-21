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

/* Hyper-rectangular Description Region */
public class HDR implements Serializable{
	private static final long serialVersionUID = 7873256174117330538L;
	private Rect rect;

    public HDR(Rect rect) {
        this.rect = rect;
    }

    public boolean containsPoint(double x, double y) {
        return rect.contains(x, y);
    }

    public double centroidInfinityDistance(double x, double y) {
        return Math.max(Math.abs(x - rect.getCenterX()),
                Math.abs(y - rect.getCenterY()));
    }

    public double rectInfinityDistance(double x, double y) {
    	double Xmin, Ymin;
    	
    	if (x < rect.getX())  		/* left section */
            Xmin = rect.getX() - x; 
    	else if (x > rect.getMaxX()) 	/* right section */
            Xmin = x - rect.getMaxX();
    	else 				/* mid section */   
    	    Xmin = 0;
    	
    	if (y < rect.getY())  		/* top section */
            Ymin = rect.getY() - y; 
    	else if (y > rect.getMaxY()) 	/* bottom section */
            Ymin = y - rect.getMaxY();
    	else  				/* mid section */   
    	    Ymin = 0;
    	
    	return Math.max(Xmin, Ymin);
    }   
}
