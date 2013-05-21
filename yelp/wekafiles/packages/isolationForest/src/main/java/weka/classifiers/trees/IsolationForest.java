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
 *    IsolationForest.java
 *    Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 *
 */
package weka.classifiers.trees;

import weka.classifiers.RandomizableClassifier;

import java.util.Enumeration;
import java.util.Random;
import java.util.ArrayList;
import java.util.Vector;

import weka.core.Instances;
import weka.core.Instance;
import weka.core.Utils;
import weka.core.Option;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;

import java.io.Serializable;


/**
 <!-- globalinfo-start -->
 <!-- globalinfo-end -->
 * 
 <!-- technical-bibtex-start -->
 <!-- technical-bibtex-end -->
 *
 <!-- options-start -->
 <!-- options-end -->
 *
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version $Revision: 9532 $
 */
public class IsolationForest extends RandomizableClassifier implements TechnicalInformationHandler, Serializable {

  // For serialization
  private static final long serialVersionUID = 5586674623147772788L;

  // The set of trees
  protected Tree[] m_trees = null;

  // The number of trees
  protected int m_numTrees = 100;

  // The subsample size
  protected int m_subsampleSize = 256;
  
  /**
   * Returns a string describing this filter
   */
  public String globalInfo() {

    return "Implements the isolation forest method for anomaly detection. "
      + "The data is expected to have two class values for the class attribute, "
      + "which is ignored at training time. The distributionForInstance() "
      + "method returns the anomaly score as the first element in the distribution, "
      + "the second element is one minus this score.\n\nTo evaluate performance "
      + "of this method for a dataset where anomalies are known, simply "
      + "code the anomalies using the class attribute: normal cases should "
      + "correspond to the second value of the class attribute, anomalies to "
      + "the first one."
      + "\n\nFor more information, see:\n\n"
      + getTechnicalInformation().toString();
  }
  
  /**
   * Returns an instance of a TechnicalInformation object, containing 
   * detailed information about the technical background of this class,
   * e.g., paper reference or book this class is based on.
   * 
   * @return the technical information about this class
   */
  public TechnicalInformation getTechnicalInformation() {
    TechnicalInformation 	result;
        
    result = new TechnicalInformation(Type.INPROCEEDINGS);
    result.setValue(Field.AUTHOR, "Fei Tony Liu and Kai Ming Ting and Zhi-Hua Zhou");
    result.setValue(Field.TITLE, "Isolation Forest");
    result.setValue(Field.BOOKTITLE, "ICDM");
    result.setValue(Field.YEAR, "2008");
    result.setValue(Field.PAGES, "413-422");
    result.setValue(Field.PUBLISHER, "IEEE Computer Society");
    
    return result;
  }

  /** 
   * Returns the Capabilities of this filter.
   */
  public Capabilities getCapabilities() {
    Capabilities result = super.getCapabilities();
    result.disableAll();
    
    // attributes
    result.enable(Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capability.DATE_ATTRIBUTES);

    // class
    result.enable(Capability.BINARY_CLASS);
    result.enable(Capability.MISSING_CLASS_VALUES);

    // instances
    result.setMinimumNumberInstances(0);
    
    return result;
  }

  /**
   * Returns brief description of the classifier.
   */
  public String toString() {

    if (m_trees == null) {
      return "No model built yet.";
    } else {
      return "Isolation forest for anomaly detection (" + 
        m_numTrees + ", " + m_subsampleSize + ")";
    }
  }
  
  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String numTreesTipText() {

    return "The number of trees to use in the forest.";
  }

  /**
   * Get the value of numTrees.
   * 
   * @return Value of numTrees.
   */
  public int getNumTrees() {

    return m_numTrees;
  }

  /**
   * Set the value of numTrees.
   * 
   * @param k value to assign to numTrees.
   */
  public void setNumTrees(int k) {

    m_numTrees = k;
  }
  
  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String subsampleSizeTipText() {

    return "The size of the subsample used to build each tree.";
  }

  /**
   * Get the value of subsampleSize.
   * 
   * @return Value of subsampleSize.
   */
  public int getSubsampleSize() {

    return m_subsampleSize;
  }

  /**
   * Set the value of subsampleSize.
   * 
   * @param n value to assign to subsampleSize.
   */
  public void setSubsampleSize(int n) {

    m_subsampleSize = n;
  }

  /**
   * Lists the command-line options for this classifier.
   * 
   * @return an enumeration over all possible options
   */
  @SuppressWarnings("unchecked")
  public Enumeration listOptions() {

    Vector newVector = new Vector();

    newVector.addElement(new Option(
        "\tThe number of trees in the forest (default 100).", "I", 1,
        "-I <number of trees>"));

    newVector.addElement(new Option(
        "\tThe subsample size for each tree (default 256).", "N", 1,
        "-N <the size of the subsample for each tree>"));

    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }

    return newVector.elements();
  }

  /**
   * Gets options from this classifier.
   * 
   * @return the options for the current setup
   */
  @SuppressWarnings("unchecked")
  public String[] getOptions() {
    Vector result;
    String[] options;
    int i;

    result = new Vector();

    result.add("-I");
    result.add("" + getNumTrees());

    result.add("-N");
    result.add("" + getSubsampleSize());

    options = super.getOptions();
    for (i = 0; i < options.length; i++) {
      result.add(options[i]);
    }

    return (String[]) result.toArray(new String[result.size()]);
  }

  /**
   * Parses a given list of options.
   * <p/>
   * 
   * <!-- options-start -->
   * <!-- options-end -->
   * 
   * @param options
   *            the list of options as an array of strings
   * @throws Exception
   *             if an option is not supported
   */
  public void setOptions(String[] options) throws Exception {
    String tmpStr;

    tmpStr = Utils.getOption('I', options);
    if (tmpStr.length() != 0) {
      m_numTrees = Integer.parseInt(tmpStr);
    } else {
      m_numTrees = 100;
    }

    tmpStr = Utils.getOption('N', options);
    if (tmpStr.length() != 0) {
      m_subsampleSize = Integer.parseInt(tmpStr);
    } else {
      m_subsampleSize = 256;
    }

    super.setOptions(options);

    Utils.checkForRemainingOptions(options);
  }

  /**
   * Builds the forest.
   */
  public void buildClassifier(Instances data) throws Exception {

    // Can classifier handle the data?
    getCapabilities().testWithFail(data);

    // Reduce subsample size if data is too small
    if (data.numInstances() < m_subsampleSize) {
      m_subsampleSize = data.numInstances();
    }

    // Generate trees
    m_trees = new Tree[m_numTrees];
    data = new Instances(data);
    Random r = (data.numInstances() > 0) ? 
      data.getRandomNumberGenerator(m_Seed) : new Random(m_Seed);
    for (int i = 0; i < m_numTrees; i++) {
      data.randomize(r);
      m_trees[i] = new Tree(new Instances(data, 0, m_subsampleSize), r,
                            0, (int) Math.ceil(Utils.log2(data.numInstances())));
    }
  }

  /**
   * Returns the average path length of an unsuccessful search.
   * Returns 0 if argument <= 1
   */
  public static double c(double n) {

    if (n <= 1.0) return 0;
    return 2 * (Math.log(n - 1) + 0.5772156649) - (2 * (n - 1) / n);
  }

  /** 
   * Returns distribution of scores.
   */
  public double[] distributionForInstance(Instance inst) {
    
    double avgPathLength = 0;
    for (int i = 0; i < m_trees.length; i++) {
      avgPathLength += m_trees[i].pathLength(inst);
    }
    avgPathLength /= (double) m_trees.length;

    double[] scores = new double[2];
    scores[0] = Math.pow(2, - avgPathLength / c(m_subsampleSize));
    scores[1] = 1.0 - scores[0];

    return scores;      
  }


  /**
   * Main method for this class.
   */
  public static void main(String[] args) {

    runClassifier(new IsolationForest(), args);
  }

  /**
   * Inner class for building and using an isolation tree.
   */
  protected class Tree implements Serializable {

    // For serialization
    private static final long serialVersionUID = 7786674623147772711L;

    // The size of the node
    protected int m_size;

    // The split attribute
    protected int m_a;

    // The split point
    protected double m_splitPoint;
    
    // The successors
    protected Tree[] m_successors;
    
    /**
     * Constructs a tree from data
     */
    protected Tree(Instances data, Random r, int height, int maxHeight) {

      // Set size of node
      m_size = data.numInstances();

      // Stop splitting if necessary
      if ((m_size <= 1) || (height == maxHeight)) {
        return;
      }

      // Compute mins and maxs and eligible attributes
      ArrayList<Integer> al = new ArrayList<Integer>();
      double[][] minmax = new double[2][data.numAttributes()];
      for (int j = 0; j < data.numAttributes(); j++) {
        minmax[0][j] = data.instance(0).value(j);
        minmax[1][j] = minmax[0][j];
      }
      for (int i = 1; i < data.numInstances(); i++) {
        Instance inst = data.instance(i);
        for (int j = 0; j < data.numAttributes(); j++) {        
          if (inst.value(j) < minmax[0][j]) {
            minmax[0][j] = inst.value(j);
          } 
          if (inst.value(j) > minmax[1][j]) {
            minmax[1][j] = inst.value(j);
          }
        }
      }
      for (int j = 0; j < data.numAttributes(); j++) {        
        if (j != data.classIndex()) {
          if (minmax[0][j] < minmax[1][j]) {
            al.add(j);
          }
        }
      }

      // Check whether any eligible attributes have been found
      if (al.size() == 0) {
        return;
      } else {

        // Randomly pick an attribute and split point
        m_a = al.get(r.nextInt(al.size()));
        m_splitPoint = 
          (r.nextDouble() * (minmax[1][m_a] - minmax[0][m_a])) + minmax[0][m_a];

        // Create sub trees
        m_successors = new Tree[2];
        for (int i = 0; i < 2; i++) {
          Instances tempData = new Instances(data, data.numInstances());
          for (int j = 0; j < data.numInstances(); j++) {
            if ((i == 0) && (data.instance(j).value(m_a) < m_splitPoint)) {
              tempData.add(data.instance(j));
            }
            if ((i == 1) && (data.instance(j).value(m_a) >= m_splitPoint)) {
              tempData.add(data.instance(j));
            }
          }
          tempData.compactify();
          m_successors[i] = new Tree(tempData, r, height + 1, maxHeight);
        }
      }
    }

    /**
     * Returns path length according to algorithm.
     */
    protected double pathLength(Instance inst) {

      if (m_successors == null) {
        return c(m_size);
      } 
      if (inst.value(m_a) < m_splitPoint) {
        return m_successors[0].pathLength(inst) + 1.0;
      } else {
        return m_successors[1].pathLength(inst) + 1.0;
      }
    }
  }
}