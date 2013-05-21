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
 * Tests SelfOrganizingMap. Run from the command line with:<p/>
 * java weka.clusterers.SelfOrganizingMapTest
 *
 * @author John Salatas (jsalatas at gmail.com)
 * @version $Revision: 1 $
 */
 
 
 public class SelfOrganizingMapTest 
  extends AbstractClustererTest {

  public SelfOrganizingMapTest(String name) { 
    super(name);  
  }

  /** Creates a default SelfOrganizingMap */
  public Clusterer getClusterer() {
    return new SelfOrganizingMap();
  }

  public static Test suite() {
    return new TestSuite(SelfOrganizingMapTest.class);
  }

  public static void main(String[] args){
    junit.textui.TestRunner.run(suite());
  }
}
