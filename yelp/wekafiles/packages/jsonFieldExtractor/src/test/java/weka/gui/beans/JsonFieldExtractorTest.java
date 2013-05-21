/* Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */

/*
 *    JsonFieldExtractorTest.java
 *    Copyright (C) 2011 Pentaho Corporation
 */

package weka.gui.beans;

import java.util.ArrayList;

import org.json.simple.JSONValue;

import weka.core.Attribute;
import weka.core.Environment;
import weka.core.Instances;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for the JsonFieldExtractor
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 49031 $
 */
public class JsonFieldExtractorTest extends TestCase {
  
  public JsonFieldExtractorTest(String name) {
    super(name);
  }
  
  protected Instances getDummyInstances() {
    Attribute dummy = new Attribute("Dummy", (ArrayList<String>) null);
    ArrayList<Attribute> dummyAtts = new ArrayList<Attribute>();
    dummyAtts.add(dummy);
    Instances dummyInsts = new Instances("Dummy relation", dummyAtts, 0);
    
    return dummyInsts;
  }
  
  public void testGetFieldFromRootObject() throws Exception {
    String json = "{\"person\": \"Fred\"}";
    String pathConfig = "1@@JSP@@$.person@@JSP@@FIRSTNAME@@JSP@@string";
    
    Instances dummyInsts = getDummyInstances();
    
    JsonFieldExtractor.JsonFieldPath fp = 
      new JsonFieldExtractor.JsonFieldPath(pathConfig);
    Environment env = Environment.getSystemWide();
    fp.init(env, dummyInsts);
    
    Object jsonO = JSONValue.parseWithException(json);
    Object result = fp.apply(jsonO);
    
    assertNotNull(result);
    assertEquals("Fred", result);
  }
  
  public void testGetElementFromRootArray() throws Exception {
    String json = "[{\"firstname\": \"Bob\", \"lastname\": \"Smith\"},{\"firstfield\": \"Zaphod\"}]";
    String pathConfig = "1@@JSP@@$[1].firstfield@@JSP@@FIRSTNAME@@JSP@@string";
    
    Instances dummyInsts = getDummyInstances();
    
    JsonFieldExtractor.JsonFieldPath fp = 
      new JsonFieldExtractor.JsonFieldPath(pathConfig);
    Environment env = Environment.getSystemWide();
    fp.init(env, dummyInsts);
    
    Object jsonO = JSONValue.parseWithException(json);
    Object result = fp.apply(jsonO);

    assertNotNull(result);
    assertEquals("Zaphod", result);
  }
  
  public void testGetElementFromRootMultiDimensionalArray() 
    throws Exception {
    String json = "[[{\"firstfield\": \"Zaphod\"}]]";
    String pathConfig = "1@@JSP@@$[0][0].firstfield@@JSP@@FIRSTNAME@@JSP@@string";
    
    Instances dummyInsts = getDummyInstances();
    
    JsonFieldExtractor.JsonFieldPath fp = 
      new JsonFieldExtractor.JsonFieldPath(pathConfig);
    Environment env = Environment.getSystemWide();
    fp.init(env, dummyInsts);
    
    Object jsonO = JSONValue.parseWithException(json);
    Object result = fp.apply(jsonO);

    assertNotNull(result);
    assertEquals("Zaphod", result);
  }
  
  public void testGetElementFromMultiDimensionalArray() 
    throws Exception {
    String json = "{\"first\": {\"array\": [[{\"firstfield\": \"Zaphod\"}]]}}";
    String pathConfig = "1@@JSP@@$.first.array[0][0].firstfield@@JSP@@FIRSTNAME@@JSP@@string";
    
    Instances dummyInsts = getDummyInstances();
    
    JsonFieldExtractor.JsonFieldPath fp = 
      new JsonFieldExtractor.JsonFieldPath(pathConfig);
    Environment env = Environment.getSystemWide();
    fp.init(env, dummyInsts);
    
    Object jsonO = JSONValue.parseWithException(json);
    Object result = fp.apply(jsonO);

    assertNotNull(result);
    assertEquals("Zaphod", result);
  }
  
  public void testGetFieldFromObjectArray() throws Exception {

    String json = "{\"person\": [{\"firstname\": \"Bob\", \"lastname\": \"Smith\"},{\"firstfield\": \"Zaphod\"}]}";
    String pathConfig = "1@@JSP@@$.person[0].firstname@@JSP@@FIRSTNAME@@JSP@@string";
    
    Instances dummyInsts = getDummyInstances();
    
    JsonFieldExtractor.JsonFieldPath fp = 
      new JsonFieldExtractor.JsonFieldPath(pathConfig);
    Environment env = Environment.getSystemWide();
    fp.init(env, dummyInsts);
    
    Object jsonO = JSONValue.parseWithException(json);
    Object result = fp.apply(jsonO);

    assertNotNull(result);
    assertEquals("Bob", result);
  }    
  
  public void testGetNonExistentField() throws Exception {
    String json = "{\"person\": [{\"firstname\": \"Bob\", \"lastname\": \"Smith\"},{\"firstfield\": \"Zaphod\"}]}";
    String pathConfig = "1@@JSP@@$.person[0].doesntExist@@JSP@@FIRSTNAME@@JSP@@string";
    
    Instances dummyInsts = getDummyInstances();
    
    JsonFieldExtractor.JsonFieldPath fp = 
      new JsonFieldExtractor.JsonFieldPath(pathConfig);
    Environment env = Environment.getSystemWide();
    fp.init(env, dummyInsts);
    
    Object jsonO = JSONValue.parseWithException(json);
    Object result = fp.apply(jsonO);

    assertNull(result);
  }
  
  public void testArrayElementOutOfBounds() throws Exception {
    String json = "{\"person\": [{\"firstname\": \"Bob\", \"lastname\": \"Smith\"},{\"firstfield\": \"Zaphod\"}]}";
    String pathConfig = "1@@JSP@@$.person[5].firstname@@JSP@@FIRSTNAME@@JSP@@string";
    
    Instances dummyInsts = getDummyInstances();
    
    JsonFieldExtractor.JsonFieldPath fp = 
      new JsonFieldExtractor.JsonFieldPath(pathConfig);
    Environment env = Environment.getSystemWide();
    fp.init(env, dummyInsts);
    
    Object jsonO = JSONValue.parseWithException(json);
    Object result = fp.apply(jsonO);
    
    // element out of bound should result in a null field value

    assertNull(result);
  }
  
  public void testGetANumber() throws Exception {
    String json = "[{\"firstname\": \"Bob\", \"lastname\": \"Smith\"},{\"firstfield\": 1.23}]";
    String pathConfig = "1@@JSP@@$[1].firstfield@@JSP@@FIRSTNAME@@JSP@@string";
    
    Instances dummyInsts = getDummyInstances();
    
    JsonFieldExtractor.JsonFieldPath fp = 
      new JsonFieldExtractor.JsonFieldPath(pathConfig);
    Environment env = Environment.getSystemWide();
    fp.init(env, dummyInsts);
    
    Object jsonO = JSONValue.parseWithException(json);
    Object result = fp.apply(jsonO);

    assertNotNull(result);
    assert(result instanceof Number);
    assertEquals(((Number)result).equals(new Double(1.23)), true);
  }
  
  public static Test suite() {
    return new TestSuite(weka.gui.beans.JsonFieldExtractorTest.class);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
}