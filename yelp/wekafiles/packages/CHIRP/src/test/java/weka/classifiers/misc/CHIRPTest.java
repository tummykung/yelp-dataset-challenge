/*
 * CHIRP: A new classifier based on Composite Hypercubes on Iterated Random Projections.
 *
 * Copyright 2011 by Tuan Dang.
 *
 * The contents of this file are subject to the Mozilla Public License Version 2.0 (the "License")
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 */

package weka.classifiers.misc;

import weka.classifiers.AbstractClassifierTest;
import weka.classifiers.Classifier;
import weka.classifiers.misc.CHIRP;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests CHIRP. Run from the command line with:<p>
 * java weka.classifiers.misc.CHIRP
 *
 * @author <a href="mailto:tdang@cs.uic.edu">Tuan Dang</a>
 * @version $Revision: 1.0 $
 */
public class CHIRPTest extends AbstractClassifierTest {

  public CHIRPTest(String name) { super(name);  }

  /** Creates a default Classifier */
  public Classifier getClassifier() {
    return new CHIRP();
  }

  public static Test suite() {
    return new TestSuite(CHIRPTest.class);
  }

  public static void main(String[] args){
    junit.textui.TestRunner.run(suite());
  }

}
