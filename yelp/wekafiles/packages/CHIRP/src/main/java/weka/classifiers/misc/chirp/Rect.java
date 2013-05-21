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

public class Rect implements Serializable{
	private static final long serialVersionUID = 6371397318498983183L;
	public double x;
	public double y;
	public double width;
	public double height;

	public Rect(double x, double y, double w, double h) {
	    this.x = x;
	    this.y = y;
	    this.width = w;
	    this.height = h;
	}
	
	public double getX() {
		return (double) x;
	}

	public double getY() {
		return (double) y;
	}

	public double getWidth() {
		return (double) width;
	}

	public double getHeight() {
		return (double) height;
	}

	public double getCenterX() {
		return getX() + getWidth() / 2.0;
	}

	public double getCenterY() {
		return getY() + getHeight() / 2.0;
	}

	public double getMinX() {
		return getX();
	}

	public double getMinY() {
		return getY();
	}

	public double getMaxX() {
		return getX() + getWidth();
	}

	public double getMaxY() {
		return getY() + getHeight();
	}

	public boolean contains(double x, double y) {
		double x0 = getX();
		double y0 = getY();
		return (x >= x0 && y >= y0 && x < x0 + getWidth() && y < y0
				+ getHeight());
	}
}
