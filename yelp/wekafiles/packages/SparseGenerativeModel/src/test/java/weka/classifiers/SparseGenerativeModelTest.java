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
 * Copyright 2002 University of Waikato
 */

package weka.classifiers.bayes;

import weka.classifiers.AbstractClassifierTest;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.SparseGenerativeModel;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests SparseGenerativeModel. Run from the command line with:<p>
 * java weka.classifiers.bayes.SparseGenerativeModel
 *
 * @author <a href="mailto:asp12@cms.waikato.ac.nz">Antti Puurula</a>
 * @version $Revision: 8034 $
 */
public class SparseGenerativeModelTest extends AbstractClassifierTest {

  public SparseGenerativeModelTest(String name) { super(name);  }

  public Classifier getClassifier() {
    return new SparseGenerativeModel();
  }

  public static Test suite() {
    return new TestSuite(SparseGenerativeModelTest.class);
  }

  public static void main(String[] args){
    junit.textui.TestRunner.run(suite());
  }

}
