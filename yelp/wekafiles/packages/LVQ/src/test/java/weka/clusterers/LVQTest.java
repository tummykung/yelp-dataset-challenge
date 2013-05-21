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
 * Copyright (C) 2005 University of Waikato, Hamilton, New Zealand
 */
package weka.clusterers;


import weka.clusterers.AbstractClustererTest;
import weka.clusterers.CheckClusterer;
import weka.clusterers.Clusterer;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests LVQ. Run from the command line with:<p/>
 * java weka.clusterers.LVQTest
 *
 * @author John Salatas (jsalatas at gmail.com)
 * @version $Revision: 1 $
 */
 
 
 public class LVQTest 
  extends AbstractClustererTest {

  public LVQTest(String name) { 
    super(name);  
  }

  /** Creates a default LVQ */
  public Clusterer getClusterer() {
    return new LVQ();
  }

  public static Test suite() {
    return new TestSuite(LVQTest.class);
  }

  public static void main(String[] args){
    junit.textui.TestRunner.run(suite());
  }
}
