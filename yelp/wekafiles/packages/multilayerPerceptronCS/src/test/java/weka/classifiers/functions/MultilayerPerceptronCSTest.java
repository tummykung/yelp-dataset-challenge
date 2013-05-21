/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright 2009 University of Waikato
 */

package weka.classifiers.functions;

import weka.classifiers.AbstractClassifierTest;
import weka.classifiers.Classifier;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests NeuralNetworkCS. Run from the command line with:<p>
 * java weka.classifiers.functions.MultilayerPerceptronTest
 *
 * @author Mark Hall
 * @version $Revision: 8108 $
 */
public class MultilayerPerceptronCSTest extends AbstractClassifierTest {


  public MultilayerPerceptronCSTest(String name) { super(name);  }

  /** Creates a default ThresholdSelector */
  public Classifier getClassifier() {
    return new MultilayerPerceptronCS();
  }

  public static Test suite() {
    return new TestSuite(MultilayerPerceptronCSTest.class);
  }

  public static void main(String[] args){
    junit.textui.TestRunner.run(suite());
  }

}
