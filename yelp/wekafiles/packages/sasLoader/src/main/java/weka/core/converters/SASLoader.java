/*
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Time Series 
 * Forecasting.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */

/*
 *    SASLoader.java
 *    Copyright (C) 2011 Pentaho Corporation
 */

package weka.core.converters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eobjects.sassy.SasColumnType;
import org.eobjects.sassy.SasReader;
import org.eobjects.sassy.SasReaderCallback;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Environment;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * Reads a source that is in SAS's "sas7bdat" format. Uses the eobjects.org
 * "SassyReader" project: http://sassyreader.eobjects.org.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 46648 $
 *
 */
public class SASLoader extends AbstractFileLoader implements BatchConverter {
  
  /**
   * for serialization
   */
  private static final long serialVersionUID = -8509692511061017173L;

  /** the file extension */
  public static String FILE_EXTENSION = ".sas7bdat";
  
  /** the structure of the data */
  protected Instances m_structure = null;

  /**
   * Returns a description of the file type.
   *
   * @return a short file description
   */
  public String getFileDescription() {
    return "Sas sas7bdat files";
  }

  /**
   * Get the file extension used for sas7bdat files.
   *
   * @return the file extension
   */
  public String getFileExtension() {
    return FILE_EXTENSION;
  }

  /**
   * Gets all the file extensions used for this type of file.
   *
   * @return the file extensions
   */
  public String[] getFileExtensions() {
    return new String[]{FILE_EXTENSION};
  }
  
  /**
   * Returns a string describing this attribute evaluator.
   * 
   * @return a description of the evaluator suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return 
        "Reads a source that is in SAS's binary \"sas7bdat\" format. Uses the " +
        "\"SassyReader\" eobjects library: http://sassyreader.eobjects.org.";
  }

  /**
   * Return the full data set. If the structure hasn't yet been determined
   * by a call to getStructure then method should do so before processing
   * the rest of the data set.
   *
   * @return the structure of the data set as an empty set of Instances
   * @exception IOException if there is no source or parsing fails
   */
  public Instances getDataSet() throws IOException {
    // TODO Auto-generated method stub
    setRetrieval(BATCH);
    if (m_structure == null) {
      getStructure();
    }
    
    final List<Object[]> data = new ArrayList<Object[]>();
    
    // any nominal labels?
    Map<Integer, HashSet<String>> nominalLabels = null;
    for (int i = 0; i < m_structure.numAttributes(); i++) {
      if (m_structure.attribute(i).isString()) {
        nominalLabels = new HashMap<Integer, HashSet<String>>();
        break;
      }
    }
    
    String file = m_File;
    try {
      if (m_env == null) {
        m_env = Environment.getSystemWide();
      }
      file = m_env.substitute(file);
    } catch (Exception ex) {}
    
    File theFile = new File(file);
    
    SasReader dataReader = new SasReader(theFile);
    dataReader.read(new SasReaderCallback() {
      
      public boolean readData() {
        return true;
      }
      
      public void column(int columnIndex, String columnName, String columnLabel, 
          SasColumnType columnType, int columnLength) {
        // nothing to do here
      }
      
      public boolean row(int row, Object[] rowData) {
        data.add(rowData);
        return true;
      }
    });

    Instances structure = new Instances(m_structure, 0);
    
    // sort out the real structure if we have nominal attributes
    if (nominalLabels != null && data.size() > 0) {
      for (int i = 0; i < m_structure.numAttributes(); i++) {
        if (m_structure.attribute(i).isString()) {
          nominalLabels.put(new Integer(i), new HashSet<String>());
        }
      }
      
      for (int i = 0; i < data.size(); i++) {
        Object[] currentRow = data.get(i);
        for (Integer index : nominalLabels.keySet()) {
          HashSet<String> labels = nominalLabels.get(index);
          String label = currentRow[index.intValue()].toString();
          labels.add(label);
        }
      }
      
      // now rebuild the structure
      ArrayList<Attribute> attribNames = new ArrayList<Attribute>();
      for (int i = 0; i < m_structure.numAttributes(); i++) {
        if (m_structure.attribute(i).isNumeric()) {
          attribNames.add(new Attribute(m_structure.attribute(i).name()));
        } else {
          HashSet<String> labels = nominalLabels.get(i);
          List<String> attLabels = new ArrayList<String>();
          for (String label : labels) {
            attLabels.add(label);
          }
          attribNames.add(new Attribute(m_structure.attribute(i).name(), attLabels));
        }
      }
      structure = new Instances(m_structure.relationName(), attribNames, 0);
    }
    
    Instances result = new Instances(structure, 0);
    if (data.size() > 0) {
      for (Object[] row : data) {
        Instance converted = convertInstance(row, result);
        result.add(converted);
      }
    }
    
    return result;
  }
  
  protected Instance convertInstance(Object[] row, Instances structure) 
    throws IOException {
    
    double[] vals = new double[structure.numAttributes()];
    for (int i = 0; i < structure.numAttributes(); i++) {
      Attribute current = structure.attribute(i);
      if (row[i] == null) {
        vals[i] = Utils.missingValue();
      } else {
        if (current.isNumeric()) {
          vals[i] = ((Number)row[i]).doubleValue();
        } else if (current.isNominal()) {
          String raw = row[i].toString();
          int index = current.indexOfValue(raw);
          if (index < 0) {
            throw new IOException("Unknown nominal value \"" + raw + "\"");
          }
          vals[i] = (double)index;
        } else {
          // String
          // TODO
        }
      }
    }
    
    Instance inst = new DenseInstance(1.0, vals);    
    
    return inst;
  }

  @Override
  public Instance getNextInstance(Instances arg0) throws IOException {
    // TODO Auto-generated method stub
    throw new IOException("Incremental reading is not supported!");
    //return null;
  }

  /**
   * Assumes that non-numeric attributes are String. getDataSet() will create
   * true nominal attributes from the String ones.
   * 
   * @return the structure of the data set as an empty set of Instances
   * @throws IOException if an error occurs
   */
  public Instances getStructure() throws IOException {
    String file = m_File;
    try {
      if (m_env == null) {
        m_env = Environment.getSystemWide();
      }
      file = m_env.substitute(file);
    } catch (Exception ex) {}
    
    File theFile = new File(file);
    
    if (!theFile.exists() || theFile.isDirectory()) {
      throw new IOException("Unable to read from file \"" + file + "\"");
    }
    
    final Map<Integer, String> names = new HashMap<Integer, String>();
    final Map<Integer, SasColumnType> types = new HashMap<Integer, SasColumnType>();
      
    SasReader headerReader = new SasReader(theFile);
    
    headerReader.read(new SasReaderCallback() {
      public boolean readData() {
        return false; // just want the column info
      }
      
      public void column(int columnIndex, String columnName, String columnLabel,
          SasColumnType columnType, int columnLength) {

        names.put(new Integer(columnIndex), columnName);
        types.put(new Integer(columnIndex), columnType);
      }
      
      public boolean row(int row, Object[] rowData) {
        return false; // we don't want the data at this point;
      }
    });
    
    Map<String, Integer> nameCount = new HashMap<String, Integer>();
    ArrayList<Attribute> attribNames = new ArrayList<Attribute>();
    for (Integer i : names.keySet()) {
      String name = names.get(i);
      SasColumnType type = types.get(i);
      
      // make sure names are unique
      if (nameCount.get(name) == null) {
        nameCount.put(name, new Integer(1));
      } else {
        int c = nameCount.get(name);
        nameCount.put(name, new Integer(c + 1));
        name += "_" + c;
      }
      
      if (type == SasColumnType.NUMERIC) {
        attribNames.add(new Attribute(name));
      } else {
        // String attribute
        attribNames.add(new Attribute(name, (List<String>)null));
      }
    }
    
    String relationName = file.replaceAll("\\.[cC][sS][vV]$","");
    m_structure = new Instances(relationName, attribNames, 0);
    
    return m_structure;
  }
  
  /**
   * Resets the Loader object and sets the source of the data set to be 
   * the supplied File object.
   *
   * @param file                the source file.
   * @throws IOException        if an error occurs
   */
  public void setSource(File file) throws IOException {
    File original = file;
    m_structure = null;
    
    setRetrieval(NONE);

    if (file == null)
      throw new IOException("Source file object is null!");

    if (m_useRelativePath) {
      try {
        m_sourceFile = Utils.convertToRelativePath(original);
        m_File = m_sourceFile.getPath();
      } catch (Exception ex) {
        //        System.err.println("[AbstractFileLoader] can't convert path to relative path.");
        m_sourceFile = original;
        m_File       = m_sourceFile.getPath();
      }
    } else {
      m_sourceFile = original;
      m_File       = m_sourceFile.getPath();
    }
  }
  
  /**
   * Resets the Loader object and sets the source of the data set to be 
   * the supplied url.
   *
   * @param url the source url.
   * @throws IOException if an error occurs
   */
  public void reset() throws IOException {
    m_structure = null;
    setRetrieval(NONE);
    
    if (m_File != null && !(new File(m_File).isDirectory())) {
      setFile(new File(m_File));
    }
  }
  
  /**
   * Returns the revision string.
   * 
   * @return            the revision
   */
  public String getRevision() {
    return "$Revision: 46648 $";
  }
  
  /**
   * Main method.
   *
   * @param args should contain the name of an input file.
   */
  public static void main(String[] args) {
    runFileLoader(new SASLoader(), args);
  }
}
