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
 *    JsonFieldExtractor.java
 *    Copyright (C) 2012 Pentaho Corporation
 */

package weka.gui.beans;

import java.awt.BorderLayout;
import java.beans.EventSetDescriptor;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Environment;
import weka.core.EnvironmentHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Range;
import weka.core.SerializedObject;
import weka.core.Utils;
import weka.gui.Logger;

/**
 * A Knowledge Flow component that extracts fields from blocks of text in JSON 
 * format contained in the values of string attributes. The value of each 
 * string attribute in each instance is assumed to contain JSON blocks of the 
 * same structure. The user can specify a "path" to use for finding the field 
 * to extract. Each field extracted becomes a new attribute in the outgoing instances
 * structure. The type of each attribute and its legal values (if nominal) can 
 * also be specified. Given the following "Alias" JSON structure: <br><br>
 * 
 *  <code>
 *  {
 *    alias : [
 *      {
 *        firstname : "bob",
 *        lastname : "smith"
 *      },
 *      {
 *        firstname : "fred",
 *        lastname : "jones",
 *      }]
 *  }
 *  </code><br><br>
 *  
 *  one would extract the lastname of the second element of the array with 
 *  a path like:<br><br>
 *  
 *  <code>$.alias[1].lastname</code><br><br>
 *  
 *  where "$" indicates the root of the JSON structure.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 49032 $
 */
@KFStep(category = "Tools", toolTipText = "Extract fields from JSON structures contained in string attributes")
public class JsonFieldExtractor extends JPanel implements BeanCommon,
    EventConstraints, DataSourceListener, InstanceListener, Visible,
    Serializable, EnvironmentHandler, DataSource {
  
  /** For serialization */
  private static final long serialVersionUID = -435909896042524927L;

  protected static class JsonFieldPath {
    protected String m_path = "";
    
    /** The attributes to apply this path to */
    protected String m_attsToApplyTo = "";
    
    protected String m_outputAttName = "";
    protected String m_outputAttType = "";
    
    /** Holds either number/date formatting string or legal nominal values */
    protected String m_formatOrLabels = "";
    
    protected String m_pathS;
    protected String m_outputAttNameS;
    
    protected List<String> m_pathParts;
    
    protected int[] m_selectedAtts;
    
    protected String m_statusMessagePrefix;
    protected Logger m_logger;
    
    protected SimpleDateFormat m_dateFormat;
    protected List<String> m_nominalLabels;
    
    public JsonFieldPath() {      
    }
    
    public JsonFieldPath(String setup) {
      parseFromInternal(setup);
    }
    
    public JsonFieldPath(String path, String selectedAtts, String attName, 
        String attType, String format) {
      m_path = path;      
      m_attsToApplyTo = selectedAtts;
      m_outputAttName = attName;
      m_outputAttType = attType;
      m_formatOrLabels = format;
    }
    
    public void parseFromInternal(String setup) {
      String[] parts = setup.split("@@JSP@@");
      if (parts.length < 4 || parts.length > 5) {
        throw new IllegalArgumentException("Malformed Json path specification: " 
            + setup);
      }
      m_attsToApplyTo = parts[0].trim();
      m_path = parts[1].trim();
      m_outputAttName = parts[2].trim();
      m_outputAttType = parts[3].trim();
      
      if (parts.length == 5) {
        m_formatOrLabels = parts[4].trim();        
      }
      
      if (m_outputAttType.equals("date") && m_dateFormat == null) {
        m_dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      }      
    }
    
    /**
     * Initialize this json field path by substituting any 
     * environment variables in the attributes, path and
     * date format/nominal labels strings. Sets up the attribute 
     * indices to apply to and validates that the selected 
     * attributes are all String attributes.
     * 
     * @param env the environment variables
     * @param structure the structure of the incoming instances
     * @throws Exception if there is a problem with the path 
     * specification
     */
    public void init(Environment env, Instances structure) 
      throws Exception {
      m_pathS = m_path;
      String attsToApplyToS = m_attsToApplyTo;
      m_outputAttNameS = m_outputAttName;
      
      try {
        m_pathS = env.substitute(m_pathS);
        attsToApplyToS = env.substitute(attsToApplyToS);
        m_outputAttNameS = env.substitute(m_outputAttNameS);
      } catch (Exception ex) {}
      
      if (!m_pathS.startsWith("$")) {
        throw new Exception("Path specification must start with " +
        		"a $");
      }
      
      // split out the parts of the path
      m_pathParts = new ArrayList<String>();
      String[] pathParts = m_pathS.split("\\.");
      for (String p : pathParts) {
        m_pathParts.add(p);
      }
      
      // Try a range first for the attributes
      String tempRangeS = attsToApplyToS;
      tempRangeS = tempRangeS.replace("/first", "first").replace("/last", "last");
      Range tempR = new Range();
      tempR.setRanges(attsToApplyToS);
      try {
        tempR.setUpper(structure.numAttributes() - 1);
        m_selectedAtts = tempR.getSelection();
      } catch (IllegalArgumentException ex) {
        // probably contains attribute names then
        m_selectedAtts = null;
      }
      
      if (m_selectedAtts == null) {
        // parse the comma separated list of attribute names
        Set<Integer> indexes = new HashSet<Integer>();
        String[] attParts = m_attsToApplyTo.split(",");
        for (String att : attParts) {
          att = att.trim();
          if (att.toLowerCase().equals("/first")) {
            indexes.add(0);
          } else if (att.toLowerCase().equals("/last")) {
            indexes.add((structure.numAttributes() - 1));
          } else {
            // try and find attribute
            if (structure.attribute(att) != null) {
              indexes.add(new Integer(structure.attribute(att).index()));
            } else {
              if (m_logger != null) {
                String msg = m_statusMessagePrefix + "Can't find attribute '" +
                                att + "in the incoming instances - ignoring";
                m_logger.logMessage(msg);
              }
            }
          }
        }        
        
        m_selectedAtts = new int[indexes.size()];
        int c = 0;
        for (Integer i : indexes) {
          m_selectedAtts[c++] = i.intValue();
        }
      }
      
      // validate the types of the selected atts
      Set<Integer> indexes = new HashSet<Integer>();
      for (int i = 0; i < m_selectedAtts.length; i++) {
        if (structure.attribute(m_selectedAtts[i]).isString()) {
          indexes.add(m_selectedAtts[i]);
        } else {
          if (m_logger != null) {
            String msg = m_statusMessagePrefix + "Attribute '" +
            structure.attribute(m_selectedAtts[i]).name() + "is not a string attribute - " +
                        "ignoring";
            m_logger.logMessage(msg);
          }
        }
      }
      
      // final array
      m_selectedAtts = new int[indexes.size()];
      int c = 0;
      for (Integer i : indexes) {
        m_selectedAtts[c++] = i.intValue();
      }
      
      
      String formatOrLabelsS = m_formatOrLabels;
      try {
        formatOrLabelsS = env.substitute(formatOrLabelsS);
      } catch (Exception ex) { }
      
      if (m_outputAttType.equals("date") && formatOrLabelsS != null 
          && formatOrLabelsS.length() > 0) {
        m_dateFormat = new SimpleDateFormat(formatOrLabelsS);
      } else if (m_outputAttType.equals("nominal")) {
        formatOrLabelsS = formatOrLabelsS.replace("{", "").replace("}", "").trim();
        String[] labels = formatOrLabelsS.split(",");
        m_nominalLabels = new ArrayList<String>();
        for (String label : labels) {
          if (label.length() > 0) {
            m_nominalLabels.add(label);
          }
        }
        
        if (m_nominalLabels.size() == 0) {
          throw new Exception("No labels defined for JSON field extracted to the " +
          		"nominal attribute '" + m_outputAttName + "'");
        }
      }
      
      if (m_outputAttType.equals("date") && m_dateFormat == null) {
        m_dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      }
    }
    
    /**
     * 
     * @param inst instance in input format
     * @throws Exception if there is a parsing error for the json object/array
     */
    public Object apply(Instance inst) throws Exception {
      for (int i = 0; i < m_selectedAtts.length; i++) {
        String value = inst.stringValue(m_selectedAtts[i]);
        Object jsonRoot = JSONValue.parseWithException(value);
        Object result = apply(jsonRoot);
        
        // first attribute/json object that has a match for our path we
        // return the value for
        if (result != null) {
          return result;
        }
      }
      
      return null; // missing value
    }
    
    public Object apply(Object jsonRoot) {
      Object jsonPart = null;
      int pathElementIndex = 0;
      
      if (jsonRoot instanceof JSONObject &&
          m_pathParts.get(0).indexOf("]") > 0) {
        return null;
      }
      if (jsonRoot instanceof JSONArray && 
          m_pathParts.get(0).indexOf("]") < 0) {
        return null;
      }
      
      if (jsonRoot instanceof JSONArray) {
        // index we are looking for
        String part = m_pathParts.get(0);
        String index = part.replace("$[", "");
          //replace("]", "").trim();
        index = index.substring(0, index.indexOf(']'));
        int indexI = Integer.parseInt(index);
        JSONArray array = (JSONArray)jsonRoot;
        if (indexI >= array.size()) {
          return null;
        }
        jsonPart = array.get(indexI);
        part = part.substring(part.indexOf(']') + 1, part.length());
        // check for further dimensions of the array
        while (part.length() > 0 && part.charAt(0) == '[') {
          if (!(jsonPart instanceof JSONArray)) {
            return null;
          }
          index = part.substring(1, part.indexOf(']'));
          indexI = Integer.parseInt(index);
          if (indexI >= ((JSONArray)jsonPart).size()) {
            return null;
          }
          jsonPart = ((JSONArray)jsonPart).get(indexI);
          part = part.substring(part.indexOf(']') + 1, part.length());
        }
        
        pathElementIndex = 1; // move to the second element in our path
      } else {
        JSONObject obj = (JSONObject)jsonRoot;
        if (m_pathParts.size() == 1) {
          return null;
        }
        String toMatch = m_pathParts.get(1);
        if (toMatch.indexOf('[') > 0) {
          toMatch = toMatch.substring(0, toMatch.indexOf('[')).trim();
        }
        /*String toMatch = m_pathParts.get(1).replace("[", "").
          replace("]", "").trim(); */
        jsonPart = obj.get(toMatch);
        
        if (m_pathParts.get(1).indexOf("[") > 0) {
          if (!(jsonPart instanceof JSONArray)) { 
            return null;
          }
          String pp = m_pathParts.get(1);
          String index = pp.substring(pp.indexOf('[') + 1, pp.indexOf(']')).trim();
          /*String index = m_pathParts.get(1).replace("[", "").
            replace("]", "").trim(); */
          int indexI = Integer.parseInt(index);
          if (indexI >= ((JSONArray)jsonPart).size()) {
            return null;
          }
          jsonPart = ((JSONArray)jsonPart).get(indexI);
          
          pp = pp.substring(pp.indexOf(']') + 1, pp.length());
          // check for further dimensions of the array
          while (pp.length() > 0 && pp.charAt(0) == '[') {
            if (!(jsonPart instanceof JSONArray)) {
              return null;
            }
            index = pp.substring(1, pp.indexOf(']'));
            indexI = Integer.parseInt(index);
            if (indexI >= ((JSONArray)jsonPart).size()) {
              return null;
            }
            jsonPart = ((JSONArray)jsonPart).get(indexI);
            pp = pp.substring(pp.indexOf(']') + 1, pp.length());
          }
        }        
        
        pathElementIndex = 2;
      }
            
      return doNextPart(jsonPart, pathElementIndex);
    }
    
    protected Object doNextPart(Object jsonPart, int pathElementIndex) {
      
      while (true) {
        if (pathElementIndex > m_pathParts.size() - 1) {
          // this is the leaf of our path - is the json object a
          // primitive?
          if (jsonPart instanceof JSONArray || 
              jsonPart instanceof JSONObject) {
            // return null;
            jsonPart = null;
            break;
          } else {
            //          return jsonPart;
            break;
          }
        }

        if (!(jsonPart instanceof JSONArray) &&
            !(jsonPart instanceof JSONObject)) {
          // json structure has hit a primative but we're not at a leaf
          // in our path
          return null;
        }

        // continue processing
/*        if (jsonPart instanceof JSONObject &&
            m_pathParts.get(pathElementIndex).indexOf("]") > 0) {
          System.out.println(jsonPart);
          System.err.println("Here.........");
//          return null;
        } */
        if (jsonPart instanceof JSONArray && 
            m_pathParts.get(pathElementIndex).indexOf("]") < 0) {
          return null;
        }

        if (jsonPart instanceof JSONArray) {
          // index we are looking for
          String pp = m_pathParts.get(pathElementIndex);
          String index = pp.substring(pp.indexOf('[') + 1, pp.indexOf(']')).trim();
          /*String index = m_pathParts.get(pathElementIndex).replace("[", "").
          replace("]", "").trim(); */
          int indexI = Integer.parseInt(index);
          JSONArray array = (JSONArray)jsonPart;
          if (indexI >= array.size()) {
            return null;
          }
          jsonPart = array.get(indexI);
          pp = pp.substring(pp.indexOf(']') + 1, pp.length());
          // check for further dimensions of the array
          while (pp.length() > 0 && pp.charAt(0) == '[') {
            if (!(jsonPart instanceof JSONArray)) {
              return null;
            }
            index = pp.substring(1, pp.indexOf(']'));
            indexI = Integer.parseInt(index);
            if (indexI > ((JSONArray)jsonPart).size()) {
              return null;
            }
            jsonPart = ((JSONArray)jsonPart).get(indexI);
            pp = pp.substring(pp.indexOf(']') + 1, pp.length());
          }
          
          pathElementIndex++; // move to the next element in our path
        } else {
          JSONObject obj = (JSONObject)jsonPart;

          String toMatch = m_pathParts.get(pathElementIndex);
          if (toMatch.indexOf('[') > 0) {
            toMatch = toMatch.substring(0, toMatch.indexOf('[')).trim();
          }          
          
          /*String toMatch = m_pathParts.get(pathElementIndex).replace("[", "").
          replace("]", "").trim(); */
          jsonPart = obj.get(toMatch);

          if (m_pathParts.get(pathElementIndex).indexOf("]") > 0) {
            if (!(jsonPart instanceof JSONArray)) { 
              return null;
            }
            String pp = m_pathParts.get(pathElementIndex);
            String index = pp.substring(pp.indexOf('[') + 1, pp.indexOf(']')).trim();
            /*String index = m_pathParts.get(pathElementIndex).replace("[", "").
            replace("]", "").trim(); */
            int indexI = Integer.parseInt(index);
            if (indexI >= ((JSONArray)jsonPart).size()) {
              return null;
            }
            jsonPart = ((JSONArray)jsonPart).get(indexI);
            
            pp = pp.substring(pp.indexOf(']') + 1, pp.length());
            // check for further dimensions of the array
            while (pp.length() > 0 && pp.charAt(0) == '[') {
              if (!(jsonPart instanceof JSONArray)) {
                return null;
              }
              index = pp.substring(1, pp.indexOf(']'));
              indexI = Integer.parseInt(index);
              if (indexI >= ((JSONArray)jsonPart).size()) {
                return null;
              }
              jsonPart = ((JSONArray)jsonPart).get(indexI);
              pp = pp.substring(pp.indexOf(']') + 1, pp.length());
            }
            
          }
          pathElementIndex++;
        }
      }
                  
      return jsonPart;
    }
    
    /**
     * Set the path for this field
     * 
     * @param path the path for this field
     */
    public void setPath(String path) {
      m_path = path;
    }
    
    /**
     * Get the path for this field
     * 
     * @return path the path for this field
     */
    public String getPath() {
      return m_path;
    }
    
    /**
     * Set the attributes to apply the rule to
     * 
     * @param a the attributes to apply the rule to.
     */
    public void setAttsToApplyTo(String a) {
      m_attsToApplyTo = a;
    }
        
    /**
     * Get the attributes to apply the rule to
     * 
     * @return the attributes to apply the rule to.
     */
    public String getAttsToApplyTo() {
      return m_attsToApplyTo;
    }
    
    /**
     * Set the name of the attribute to create for this field
     * 
     * @param name the name of the attribute to create
     */
    public void setOutputAttName(String name) {
      m_outputAttName = name;
    }
    
    /**
     * Get the name of the attribute to create for this field
     * 
     * @return the name the name of the attribute to create
     */
    public String getOutputAttName() {
      return m_outputAttName;
    }
    
    /**
     * Get the name of the attribute to create for this field after
     * any environment variables have been substituted. Must only be
     * called after init() has been called.
     * 
     * @return the name of the attribute to create after environment variable 
     * substitution
     */
    public String getOutputAttNameResolved() {
      return m_outputAttNameS;
    }
    
    /**
     * Return the list of nominal labels for the output attribute. 
     * This will be null if the type of the output attribute is not 
     * nominal.
     * 
     * @return the list of nominal labels
     */
    public List<String> getNominalLabels() {
      return m_nominalLabels;
    }
    
    /**
     * Set output attribute type
     * 
     * @param attType the type of the output attribute to create
     */
    public void setOutputAttType(String attType) {
      m_outputAttType = attType;
    }
    
    /**
     * Get output attribute type
     * 
     * @return the type of the output attribute to create.
     */
    public String getOutputAttType() {
      return m_outputAttType;
    }
    
    /**
     * Depending on the output attribute type, 
     * set either the date formatting string to use or set of
     * legal nominal values
     * 
     * @param l date formatting string or legal attribute values
     */
    public void setDateFormatOrLabels(String l) {
      m_formatOrLabels = l;
    }
    
    /**
     * Depending on the output attribute type, 
     * get either the date formatting string to use or set of
     * legal nominal values
     * 
     * @return date formatting string or legal attribute values
     */
    public String getDateFormatOrLabels() {
      return m_formatOrLabels;
    }
    
    public String toString() {
      // return a nicely formatted string for display
      // that shows all the details
      
      StringBuffer buff = new StringBuffer();
      buff.append("[Input atts: " + m_attsToApplyTo + "]  ");
      buff.append("JSON Path: ").append(getPath()).append(" --> ");
      buff.append("[Output att: ").append(this.getOutputAttName()).
        append(" : ").append(this.getOutputAttType());
      if (getOutputAttType().toLowerCase().equals("date")) {
        buff.append(" (" + getDateFormatOrLabels() + ")");
      }
      if (getOutputAttType().toLowerCase().equals("nominal")) {
        String temp = this.getDateFormatOrLabels().replace("{", "").replace("}", "").trim();
        buff.append(" {" + getDateFormatOrLabels() + "}");
      }
      buff.append("]");
      
      return buff.toString();
    }
    
    protected String toStringInternal() {
      
      // return a string in internal format that is 
      // easy to parse all the data out of
      StringBuffer buff = new StringBuffer();
      buff.append(m_attsToApplyTo).append("@@JSP@@");
      buff.append(m_path).append("@@JSP@@");
      buff.append(m_outputAttName).append("@@JSP@@");
      buff.append(m_outputAttType).append("@@JSP@@");
      buff.append(m_formatOrLabels);
      
      return buff.toString();
    }
  }
  
  /** Internally encoded list of field paths */
  protected String m_fieldPathDetails = "";
  
  /** Temporary list of field paths */
  protected transient List<JsonFieldPath> m_fieldPaths;
  
  /** Environment variables */
  protected transient Environment m_env;
  
  /** Downstream steps listening to instance events */
  protected ArrayList<InstanceListener> m_instanceListeners = 
    new ArrayList<InstanceListener>();

  /** Downstream steps listening to data set events */
  protected ArrayList<DataSourceListener> m_dataListeners = 
    new ArrayList<DataSourceListener>();
  
  /** Logging */
  protected transient Logger m_log;
  
  /** Busy indicator */
  protected transient boolean m_busy;
  
  /** Component talking to us */
  protected Object m_listenee;
  
  /** The output structure */
  protected Instances m_outputStructure;
  
  /** Instance event to use */
  protected InstanceEvent m_ie = new InstanceEvent(this);
  
  /**
   * Default visual filters
   */
  protected BeanVisual m_visual = 
    new BeanVisual("JsonFieldExtractor", 
                   BeanVisual.ICON_PATH+"DefaultFilter.gif",
                   BeanVisual.ICON_PATH+"DefaultFilter_animated.gif");
  
  public String globalInfo() {
    return "A Knowledge Flow component that extracts fields from blocks of text " +
    		"in JSON format contained in the values of string attributes. " +
    		"The value of each string attribute in each instance is assumed to contain " +
    		"JSON blocks of the same structure. The user can specify a 'path' to use " +
    		"for finding the field to extract. Each field extracted becomes a " +
    		"new attribute in the outgoing instances structure. The type of each " +
    		"attribute and its legal values (if nominal) can also be specified. Given " +
    		"the following 'Alias' JSON structure: \n\n" +
    		"{\n\talias : [\n\t\t{\n\t\t\tfirstname : \"bob\",\n\t\t\tlastname : \"smith\"" +
    		"\n\t\t},\n\t\t{\n\t\t\tfirstname : \"fred\",\n\t\t\tlastname : \"jones\"," +
    		"\n\t\t}]\n}\n\none would extract the lastname of the second element of " +
    		"the array with a path like:\n\n\t$.alias[1].lastname\n\nwhere \"$\" " +
    		"indicates the root of the JSON structure.";
  }
  
  /**
   * Constructor
   */
  public JsonFieldExtractor() {
    useDefaultVisual();
    setLayout(new BorderLayout());
    add(m_visual, BorderLayout.CENTER);
    
    m_env = Environment.getSystemWide();
  }

  public void useDefaultVisual() {
    m_visual.loadIcons(BeanVisual.ICON_PATH+"DefaultFilter.gif",
        BeanVisual.ICON_PATH+"DefaultFilter_animated.gif");
    m_visual.setText("JsonFieldExtractor");
  }
  
  /**
   * Set the encoded list of JSON path details to extract
   * 
   * @param pathDetails the list of path details
   */
  public void setPathDetails(String pathDetails) {
    m_fieldPathDetails = pathDetails;
  }
  
  /**
   * Get the encoded list of JSON path details to extract
   * 
   * @return the lsit of path details
   */
  public String getPathDetails() {
    return m_fieldPathDetails;
  }

  /**
   * Set a new visual representation
   *
   * @param newVisual a <code>BeanVisual</code> value
   */
  public void setVisual(BeanVisual newVisual) {
    m_visual = newVisual;
  }

  /**
   * Get the visual representation
   *
   * @return a <code>BeanVisual</code> value
   */
  public BeanVisual getVisual() {
    return m_visual;
  }
  
  protected void makeOutputStructure(Instances inputStructure) 
    throws Exception {
    m_fieldPaths = new ArrayList<JsonFieldPath>();
    
    if (m_fieldPathDetails != null && m_fieldPathDetails.length() > 0) {
      String fieldParts[] = m_fieldPathDetails.split("@@field-path@@");
      for (String p : fieldParts) {
        JsonFieldPath fp = new JsonFieldPath(p.trim());
        fp.m_statusMessagePrefix = statusMessagePrefix();
        fp.m_logger = m_log;
        fp.init(m_env, inputStructure);
        m_fieldPaths.add(fp);
      }
      
      m_outputStructure = (Instances)(new SerializedObject(inputStructure).getObject());
      for (JsonFieldPath fp : m_fieldPaths) {
        String attName = fp.getOutputAttNameResolved();
        String attType = fp.getOutputAttType().trim().toLowerCase();
        String dateFormat = fp.getDateFormatOrLabels();
        if (dateFormat == null || dateFormat.length() == 0) {
          dateFormat = null;
        }
        List<String> nominalLabels = fp.getNominalLabels();
        
        Attribute newA = null;
        if (attType.equals("string")) {
          newA = new Attribute(attName, (List<String>)null);
        } else if (attType.equals("nominal")) {
          newA = new Attribute(attName, nominalLabels);
        } else if (attType.equals("numeric")) {
          newA = new Attribute(attName);
        } else if (attType.equals("date")) {
          newA = new Attribute(attName, dateFormat);
        } else {
          throw new Exception("Unknown attribute type '" + attType + "'");
        }
        m_outputStructure.insertAttributeAt(newA, 
            m_outputStructure.numAttributes());
      }
      return;
    }
    
    m_outputStructure = new Instances(inputStructure);
  }
  
  protected Instance makeOutputInstance(Instance inputI, boolean batch) 
    throws Exception {
    int numNewAtts = m_fieldPaths.size();
    double[] newVals = new double[inputI.numAttributes() + numNewAtts];
    
    // original att values first
    for (int i = 0; i < inputI.numAttributes(); i++) {
      Attribute current = inputI.attribute(i);
      if (current.isString()) {
        if (batch) {
          newVals[i] = inputI.value(i);
        } else {          
          // incremental - one string value only in memory per string attribute
          newVals[i] = 0;
          m_outputStructure.attribute(i).setStringValue(inputI.stringValue(i));
        }
      } else {
        newVals[i] = inputI.value(i);
      }
    }
    
    // now the new att values
    for (int i = 0; i < m_fieldPaths.size(); i++) {
      JsonFieldPath fp = m_fieldPaths.get(i);
      Object result = fp.apply(inputI);
      Attribute currentA = m_outputStructure.attribute(fp.getOutputAttNameResolved());
      if (result == null) {
        newVals[inputI.numAttributes() + i] = Utils.missingValue();
      } else {
        String attType = fp.getOutputAttType().trim().toLowerCase();
        String format = fp.getDateFormatOrLabels();
        if (attType.equals("string")) {
          if (batch) {
            newVals[inputI.numAttributes() + i] = 
              currentA.addStringValue(result.toString());
          } else {
            newVals[inputI.numAttributes() + i] = 0;
            currentA.setStringValue(result.toString());
          }
        } else if (attType.equals("nominal")) {
          int index = currentA.indexOfValue(result.toString());
          if (index >= 0) {
            newVals[inputI.numAttributes() + i] = index;
          } else {
            newVals[inputI.numAttributes() + i] = Utils.missingValue(); 
          }          
        } else if (attType.equals("numeric")) {
          if (!(result instanceof Number)) {
            /*throw new Exception ("Extracted value does not seem to " +
            		"be a number for new attribute '" + currentA.name()
            		+ "'!"); */
            if (m_log != null) {
              m_log.logMessage(statusMessagePrefix() + "WARNING: " +
              		"Extracted value does not seem to " +
                        "be a number for new attribute '" + fp.getOutputAttNameResolved());
              newVals[inputI.numAttributes() + i] = Utils.missingValue();
            }            
          } else {          
            newVals[inputI.numAttributes() + i] = ((Number)result).doubleValue();
          }
        } else if (attType.equals("date")) {
          String dateValueString = result.toString();
          try {
            Date d = fp.m_dateFormat.parse(dateValueString);
            newVals[inputI.numAttributes() + i] = d.getTime();
          } catch (ParseException e) {
            if (m_log != null) {
              m_log.logMessage(statusMessagePrefix() + "WARNING: unable to parse date value '" 
                  + dateValueString +"' for new attribute " + fp.getOutputAttNameResolved());
              newVals[inputI.numAttributes() + i] = Utils.missingValue();
            }
          }
        }
      }
    }
    
    Instance result = new DenseInstance(1.0, newVals);
    result.setDataset(m_outputStructure);
    
    return result;
  }

  public void acceptInstance(InstanceEvent e) {
    m_busy =  true;
    
    if (e.getStatus() == InstanceEvent.FORMAT_AVAILABLE) {
      Instances structure = e.getStructure();
      try {
        makeOutputStructure(structure);
      } catch (Exception ex) {
        String msg = statusMessagePrefix() + "ERROR: unable to create output instances structure.";
        if (m_log != null) {
          m_log.statusMessage(msg);
          m_log.logMessage("[JsonFieldExtractor] " + ex.getMessage());
        }
        stop();
        
        ex.printStackTrace();
        m_busy = false;
        return;
      }
      
      if (m_log != null) {
        m_log.statusMessage(statusMessagePrefix() + "Processing stream...");
      }
      
      m_ie.setStructure(m_outputStructure);
      notifyInstanceListeners(m_ie);
    } else {
      Instance inst = e.getInstance();
      Instance out = null;
      if (inst != null) {
        try {
          out = makeOutputInstance(inst, false);
        } catch (Exception ex) {
          String msg = statusMessagePrefix() + "ERROR: unable to create output instance.";
          if (m_log != null) {
            m_log.statusMessage(msg);
            m_log.logMessage("[JsonFieldExtractor] " + ex.getMessage());
          }
          stop();
          
          ex.printStackTrace();
          m_busy = false;
          return;
        }
      }
      
      if (inst == null || out != null || e.getStatus() == InstanceEvent.BATCH_FINISHED) {
        // notify listeners
        m_ie.setInstance(out);
        m_ie.setStatus(e.getStatus());
        notifyInstanceListeners(m_ie);
      }
      
      if (e.getStatus() == InstanceEvent.BATCH_FINISHED ||
          inst == null) {
        // we're done
        if (m_log != null) {
          m_log.statusMessage(statusMessagePrefix() + "Finished");
        }
      }      
    }
    
    m_busy = false;    
  }

  /**
   * Accept and process a data set event
   * 
   * @param e the data set event to process
   */
  public void acceptDataSet(DataSetEvent e) {
    m_busy = true;
    if (m_log != null) {
      m_log.statusMessage(statusMessagePrefix() + "Processing batch...");
    }
    
    try {
      makeOutputStructure(new Instances(e.getDataSet(), 0));
    } catch (Exception ex) {
      String msg = statusMessagePrefix() + "ERROR: unable to create output instances structure.";
      if (m_log != null) {
        m_log.statusMessage(msg);
        m_log.logMessage("[JsonFieldExtractor] " + ex.getMessage());
      }
      stop();
      
      ex.printStackTrace();
      m_busy = false;
      return;
    }
    
    Instances outputBatch = new Instances(m_outputStructure, 0);
    Instances toProcess = e.getDataSet();
    try {
      for (int i = 0; i < toProcess.numInstances(); i++) {
        Instance result = makeOutputInstance(toProcess.instance(i), true);
        outputBatch.add(result);
      }
    } catch (Exception ex) {
      String msg = statusMessagePrefix() + "ERROR: unable to create output instance.";
      if (m_log != null) {
        m_log.statusMessage(msg);
        m_log.logMessage("[JsonFieldExtractor] " + ex.getMessage());
      }
      stop();
      
      ex.printStackTrace();
      m_busy = false;
      return;
    }
    
    if (m_log != null) {
      m_log.statusMessage(statusMessagePrefix() + "Finished.");
    }
    
    // notify listeners
    DataSetEvent d = new DataSetEvent(this, outputBatch);
    notifyDataListeners(d);
    
    m_busy = false;
  }

  public boolean eventGeneratable(String eventName) {

    if (m_listenee == null) {
      return false;
    }
    
    if (!eventName.equals("instance") && !eventName.equals("dataSet")) {
      return false;
    }
    
    if (m_listenee instanceof DataSource) {
      if (m_listenee instanceof EventConstraints) {
        EventConstraints ec = (EventConstraints)m_listenee;
        return ec.eventGeneratable(eventName);
      }
    }
        
    return true;
  }

  /**
   * Set a custom (descriptive) name for this bean
   * 
   * @param name the name to use
   */
  public void setCustomName(String name) {
    m_visual.setText(name);
  }

  /**
   * Get the custom (descriptive) name for this bean (if one has been set)
   * 
   * @return the custom name (or the default name)
   */
  public String getCustomName() {
    return m_visual.getText();
  }

  /**
   * Stop any processing that the bean might be doing.
   */
  public void stop() {
    if (m_listenee != null) {
      if (m_listenee instanceof BeanCommon) {
        ((BeanCommon)m_listenee).stop();
      }
    }
    
    if (m_log != null) {
      m_log.statusMessage(statusMessagePrefix() + "Stopped");
    }
    
    m_busy = false;
  }

  /**
   * Returns true if. at this time, the bean is busy with some
   * (i.e. perhaps a worker thread is performing some calculation).
   * 
   * @return true if the bean is busy.
   */
  public boolean isBusy() {
    return m_busy;
  }

  /**
   * Set a logger
   *
   * @param logger a <code>weka.gui.Logger</code> value
   */
  public void setLog(Logger logger) {
    m_log = logger;
  }

  /**
   * Returns true if, at this time, 
   * the object will accept a connection via the named event
   *
   * @param esd the EventSetDescriptor for the event in question
   * @return true if the object will accept a connection
   */
  public boolean connectionAllowed(EventSetDescriptor esd) {
    return connectionAllowed(esd.getName());
  }

  /**
   * Returns true if, at this time, 
   * the object will accept a connection via the named event
   *
   * @param eventName the name of the event
   * @return true if the object will accept a connection
   */
  public boolean connectionAllowed(String eventName) {
    if (!eventName.equals("instance") && !eventName.equals("dataSet")) {
      return false;
    }
    
    if (m_listenee != null) {
      return false;
    }
    
    return true;
  }

  /**
   * Notify this object that it has been registered as a listener with
   * a source for receiving events described by the named event
   * This object is responsible for recording this fact.
   *
   * @param eventName the event
   * @param source the source with which this object has been registered as
   * a listener
   */
  public void connectionNotification(String eventName, Object source) {
    if (connectionAllowed(eventName)) {
      m_listenee = source;
    }
  }

  /**
   * Notify this object that it has been deregistered as a listener with
   * a source for named event. This object is responsible
   * for recording this fact.
   *
   * @param eventName the event
   * @param source the source with which this object has been registered as
   * a listener
   */
  public void disconnectionNotification(String eventName, Object source) {
    if (source == m_listenee) {
      m_listenee = null;
    }
  }

  /**
   * Add a datasource listener
   * 
   * @param dsl the datasource listener to add
   */
  public void addDataSourceListener(DataSourceListener dsl) {
    m_dataListeners.add(dsl);
  }

  /**
   * Remove a datasource listener
   * 
   * @param dsl the datasource listener to remove
   */
  public void removeDataSourceListener(DataSourceListener dsl) {
    m_dataListeners.remove(dsl);    
  }

  /**
   * Add an instance listener
   * 
   * @param dsl the instance listener to add
   */
  public void addInstanceListener(InstanceListener dsl) {
    m_instanceListeners.add(dsl);    
  }

  /**
   * Remove an instance listener
   * 
   * @param dsl the instance listener to remove
   */
  public void removeInstanceListener(InstanceListener dsl) {
    m_instanceListeners.remove(dsl);
  }

  /**
   * Set environment variables to use
   */
  public void setEnvironment(Environment env) {
    m_env = env;
  }
  
  protected String statusMessagePrefix() {
    return getCustomName() + "$" + hashCode() + "|";
  }
  
  @SuppressWarnings("unchecked")
  private void notifyDataListeners(DataSetEvent e) {
    List<DataSourceListener> l;
    synchronized (this) {
      l = (List<DataSourceListener>) m_dataListeners.clone();
    }
    if (l.size() > 0) {
      for (DataSourceListener ds : l) {
        ds.acceptDataSet(e);
      }
    }
  }
  
  @SuppressWarnings("unchecked")
  private void notifyInstanceListeners(InstanceEvent e) {
    List<InstanceListener> l;
    synchronized (this) {
      l = (List<InstanceListener>) m_instanceListeners.clone();
    }
    if (l.size() > 0) {
      for (InstanceListener il : l) {
        il.acceptInstance(e);
      }
    }
  }

  public static void main(String[] args) {
    try {
        String json = "{\"first\": {\"array\": [[{\"firstfield\": \"Zaphod\"}]]}}";
//      String json = "{\"first\": {\"array\": [{\"firstfield\": \"Zaphod\"}]}}";
      //String json = "{\"person\": [{\"firstname\": 1.23, \"lastname\": \"Smith\"},{\"firstfield\": \"Zaphod\"}]}";
      // String pathConfig = "1@@JSP@@$.person[0].firstname@@JSP@@FIRSTNAME@@JSP@@string";
      String pathConfig = "1@@JSP@@$.first.array[0][0].firstfield@@JSP@@FIRSTNAME@@JSP@@string";
      /*String json = "{\"created_at\": \"Wed, 19 Jan 2011 21:16:37 +0000\", " +
      		"\"profile_image_url\": \"http://a2.twimg.com/sticky/default_profile_images/default_profile_1_normal.png\", " +
      		"\"from_user_id_str\": \"191709163\", " +
      		"\"id_str\": \"27836852555751424\", " +
      		"\"from_user\": \"DanLabTesting\", " +
      		"\"text\": \"Twitter api: 1234455\", " +
      		"\"to_user_id\": null, " +
      		"\"metadata\": {\"result_type\": \"recent\"},"  +
      		"\"id\": 27836852555751424," +
      		"\"geo\": null," +
      		"\"from_user_id\": 191709163," +
      		"\"iso_language_code\": \"en\"," +
      		"\"source\": \"&lt;a href=&quot;http://www.danlabgames.com/index.php?computer=ipad&quot; rel=&quot;nofollow&quot;&gt;Wacka Monsta&lt;/a&gt;\",\"to_user_id_str\": null}";
      String pathConfig = "1@@JSP@@$.metadata.result_type@@JSP@@Tweet@@JSP@@string"; */
      Attribute dummy = new Attribute("Dummy", (ArrayList<String>) null);
      ArrayList<Attribute> dummyAtts = new ArrayList<Attribute>();
      dummyAtts.add(dummy);
      Instances dummyInsts = new Instances("Dummy relation", dummyAtts, 0);            
      
      JsonFieldExtractor.JsonFieldPath fp = 
        new JsonFieldExtractor.JsonFieldPath(pathConfig);
      Environment env = Environment.getSystemWide();
      fp.init(env, dummyInsts);
      
      Object jsonO = JSONValue.parseWithException(json);
      System.out.println(jsonO);
      Object result = fp.apply(jsonO);
      
      if (result != null) {
        System.out.println("Got value :" + result.toString());
        if (result instanceof Number) {
          System.out.println("Is a Number.");
        }
      } else {
        System.out.println("Result was null!");
      }
      
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
