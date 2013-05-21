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

import weka.classifiers.RandomizableClassifier;
import weka.classifiers.misc.chirp.Classifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

import java.util.Enumeration;
import java.util.Vector;


/**
<!-- globalinfo-start -->
CHIRP classifies with a set cover on iterated random projections. For more information, see: CHIRP: A New Classifier Based on Composite Hypercubes on Iterated Random Projections. Proceedings of the ACM KDD 2011. 
<p/>
<!-- globalinfo-end -->

<!-- technical-bibtex-start -->
 * BibTeX:
 * <pre>
 * &#64;inproceedings{Wilkinson:2011:CNC:2020408.2020418,
 * author = {Wilkinson, Leland and Anand, Anushka and Tuan, Dang Nhon},
 * title = {CHIRP: a new classifier based on composite hypercubes on iterated random projections},
 * booktitle = {Proceedings of the 17th ACM SIGKDD international conference on Knowledge discovery and data mining},
 * series = {KDD '11},
 * year = {2011},
 * isbn = {978-1-4503-0813-7},
 * location = {San Diego, California, USA},
 * pages = {6--14},
 * numpages = {9},
 * url = {http://doi.acm.org/10.1145/2020408.2020418},
 * doi = {http://doi.acm.org/10.1145/2020408.2020418},
 * acmid = {2020418},
 * publisher = {ACM},
 * address = {New York, NY, USA},
 * keywords = {random projections, supervised classification},
 * }
 * </pre>
 * <p/>
 <!-- technical-bibtex-end -->

<!-- options-start -->
 * Valid options are: <p/>
 * 
 * <pre> -V 
 *  Specifies number of voters.</pre>
 * 
 * <pre> -S 
 *  Specifies random seed.</pre>
 *  
 * <pre> -D
 *  If set, classifier is run in debug mode and
 *  may output additional info to the console</pre>
 * 
 <!-- options-end -->
 * 
 * @author Leland Wilkinson
 * @author Anushka Anand
 * @author Tuan Dang
 * @version $Revision: 1.0 $
 */


public class CHIRP 
  extends RandomizableClassifier 
  implements TechnicalInformationHandler{
	
	private static final long serialVersionUID = -7695705059259069157L;
	private Classifier linf;
	protected int defaultVoters = 7;
	protected int m_numVoters = defaultVoters;
	protected int numInstance = 0;
  
  public String globalInfo() {
    return "CHIRP is an iterative sequence of three stages " 
    + "(projecting, binning, and covering) that are designed "
    + "to deal with the curse of dimensionality, computational "
    + "complexity, and nonlinear separability. \n"
    + "CHIRP classifies with a set cover on iterated random projections."
    + "For more information, see: CHIRP: A New Classifier Based on Composite " 
    + "Hypercubes on Iterated Random Projections. Proceedings of the ACM KDD 2011." ;
  }
   
  public Enumeration listOptions() {
    Vector result = new Vector();
    result.addElement(new Option("\tSpecifies the number of voter", "V", 1, "-V <numVoters>"));
    
    // TODO - Add more options here
    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      result.addElement(enu.nextElement());
    }
  
    return result.elements();
  }
  
  /**
   * @return      the capabilities of this classifier
   */
  public Capabilities getCapabilities() {
    Capabilities result = super.getCapabilities(); // returns the object
    result.disableAll();
    result.disableAllClasses();               // disable all class types
    result.disableAllClassDependencies();     // no dependencies!
    // from weka.classifiers.Classifier attributes
    result.enable(Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capability.BINARY_ATTRIBUTES);
    result.enable(Capability.STRING_ATTRIBUTES);
    result.enable(Capability.MISSING_VALUES);   
    // class
    result.enable(Capability.NOMINAL_CLASS);
    //result.enable(Capability.NUMERIC_CLASS);
    result.enable(Capability.BINARY_CLASS);
    //result.enable(Capability.STRING_CLASS);
    result.enable(Capability.MISSING_CLASS_VALUES);
    // instances
    result.setMinimumNumberInstances(1);
   
    return result;
  }
  
  public void setOptions(String[] options) throws Exception {
    String	tmpStr;
    tmpStr = Utils.getOption('V', options);
    if (tmpStr.length() != 0) {
      setNumVoters(Integer.parseInt(tmpStr));
    } else {
      setNumVoters(defaultVoters);
    }
    super.setOptions(options);
    
  }
  
  /**
   * Set debugging mode.
   *
   * @param debug true if debug output should be printed
   */
  public void setDebug(boolean debug) {
    m_Debug = debug;
  }

  /**
   * Get whether debugging is turned on.
   *
   * @return true if debugging output is on
   */
  public boolean getDebug() {
    return m_Debug;
  }

  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String debugTipText() {
    return "If set to true, classifier may output additional info to " +
      "the console.";
  }
  
  /**
   * Gets the current settings of the Classifier.
   * 
   * @return an array of strings suitable for passing to setOptions
   */
  public String[] getOptions() {
    Vector        result;
    String[]      options;
    int           i;
    result  = new Vector();
    result.add("-V");
    result.add("" + getNumVoters());
    
    options = super.getOptions();
    for (i = 0; i < options.length; i++)
      result.add(options[i]);
    return (String[]) result.toArray(new String[result.size()]);
  }
   
  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String numVotersTipText() {
    return "The number of voters.";
  }
  
  /**
   * Gets numVoters.
   * 
   * @return numVoters
   */
  public int getNumVoters() {
    return m_numVoters;
  }
  
  /**
   * Sets numVoters.
   * 
   * @param n
   *            the new value for numVoters
   * @throws Exception
   *                if parameter illegal
   */
  public void setNumVoters(int n) throws Exception {
    if (n <= 0) 
      throw new IllegalArgumentException("CHIRP: Number of voters must be positive.");
    m_numVoters = n;
  }
 
  public void buildClassifier(Instances trainData) throws Exception {
	  // can classifier handle the data?
	  getCapabilities().testWithFail(trainData);
	  
	  int nVoters =getNumVoters();
      int randomSeed =getSeed();
      linf = new Classifier(nVoters, trainData, randomSeed, m_Debug);
      
      if (m_Debug) {
    		System.out.println("");
    		System.out.println("CHIRP began with the random seed:"+randomSeed);
      }
      linf.buildClassifier();
      
      numInstance = 0;
  }
  public double classifyInstance(Instance instance) throws Exception {
	  numInstance++;
	  int decision = linf.classifyInstance(instance);
	  if (m_Debug) 
			System.out.println("Finished classification instance "+numInstance+"  Decision:"+decision);
	  return decision;
  }
 
  
  /**
   * Output a representation of this classifier
   * 
   * @return	a string representation of the classifier
   */
  
  public String toString() {
    String result = new String();
    result = "CHIRP: number of classifiers:"+getNumVoters()
    			+" Random seed:"+ getSeed();
    return result;
  }
  
  /**
   * Return the technical information. 
   * TODO: Cite Technical report when published
   * 
   * @return the technical information about this class
   */
  public TechnicalInformation getTechnicalInformation() {  
    TechnicalInformation result;
    result = new TechnicalInformation(Type.INPROCEEDINGS);
    result.setValue(Field.AUTHOR, "Leland Wilkinson, Anushka Anand, and Tuan Dang ");
    result.setValue(Field.TITLE, "CHIRP: A new classiÞer based on " +
    		"Composite Hypercubes on Iterated Random Projection");
    result.setValue(Field.BOOKTITLE, "Proceedings of the 17th ACM KDD");
    result.setValue(Field.YEAR, "2011");
    return result;
  }
  
  /**
   * Returns the revision string.
   * 
   * @return        the revision
   */
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 1.0 $");
  }
  
  /**
   * Executes the classifier from command line.
   * 
   * @param argv
   *            should contain the following arguments: -t training file [-T
   *            test file] [-c class index]
   */
  public static void main(String [] argv) {
	    runClassifier(new CHIRP(), argv);
	}
}
