/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 * Copyright 2002 University of Waikato
 */

package weka.classifiers.bayes;

import weka.classifiers.AbstractClassifierTest;
import weka.classifiers.Classifier;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests NaiveBayes. Run from the command line with:<p>
 * java weka.classifiers.bayes.NaiveBayesTest
 *
 * @author <a href="mailto:eibe@cs.waikato.ac.nz">Eibe Frank</a>
 * @version $Revision: 1.2 $
 */
public class A1DETest extends AbstractClassifierTest {

  public A1DETest(String name) { super(name);  }

  /** Creates a default NaiveBayes */
  @Override
public Classifier getClassifier() {
    return new A1DE();
  }

  public static Test suite() {
    return new TestSuite(A1DETest.class);
  }

  public static void main(String[] args){
    junit.textui.TestRunner.run(suite());
  }

}
