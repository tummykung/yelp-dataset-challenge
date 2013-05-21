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
 *    MLRClassifierImpl.java
 *    Copyright (C) 2012 Pentaho Corporation
 *
 */

package weka.classifiers.mlr.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPGenericVector;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.REXPVector;
import org.rosuda.REngine.RList;

import weka.classifiers.mlr.MLRClassifier;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.BatchPredictor;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.CapabilitiesHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RLoggerAPI;
import weka.core.RSession;
import weka.core.RUtils;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

import com.thoughtworks.xstream.XStream;

/**
 * Implementation class for a wrapper classifier for the MLR R library:<br>
 * <br>
 * 
 * http://mlr.r-forge.r-project.org/
 * 
 * <p>
 * The class will attempt to install and load the MLR library if it is not
 * already present in the user's R environment. Similarly, it will attempt to
 * install and load specific base learners as required.
 * <p>
 * 
 * The classifier supports serialization by serializing to binary inside of R,
 * retrieving the serialized classifier via the JRI native interface to R and
 * then finally serializing the JRI REXP object to XML using XStream. The last
 * step is required because JRI REXP objects do not implement Serializable.
 * 
 * <!-- options-start --> <!-- options-end -->
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 51265 $
 */
public class MLRClassifierImpl implements BatchPredictor, OptionHandler,
    CapabilitiesHandler, RevisionHandler, Serializable {

  /** For serialization */
  private static final long serialVersionUID = 3554353477500239283L;

  // availability and initialization
  protected transient boolean m_mlrAvailable = false;
  protected transient boolean m_initialized = false;
  protected transient boolean m_baseLearnerLibraryAvailable = false;

  /** Don't use Weka's replace missing values filter? */
  protected boolean m_dontReplaceMissingValues = false;

  /** Replace missing values */
  protected ReplaceMissingValues m_missingFilter;

  /** The R learner to use */
  protected int m_rLearner = MLRClassifier.R_CLASSIF_RPART;

  /** Hyperparameters (comma separated) for the learner */
  protected String m_schemeOptions = "";

  /**
   * Batch prediction size (default: 100 instances)
   */
  protected String m_batchPredictSize = "100";

  /** Whether to output info/warning messages from R to the console */
  protected boolean m_logMessagesFromR = false;

  /** Holds the textual representation of the R model */
  protected StringBuffer m_modelText;

  /**
   * Holds the serialized R model (R binary serialization followed by XStream
   * because Rserve/JRI REXP objects do not implement Serializable)
   */
  protected StringBuffer m_serializedModel;

  /** Used to make this model unique in the R environment */
  protected String m_modelHash;

  protected Instances m_testHeader;

  /**
   * A small thread that will remove the model from R after 5 seconds have
   * elapsed. This gets launched the first time distributionForInstance() is
   * called. Subsequent calls to distributionForInstance() will reset the
   * countdown timer back to 5
   */
  protected transient Thread m_modelCleaner;

  /** Thread safe integer used by the model cleaner thread */
  protected transient AtomicInteger m_counter;

  /** Simple console logger to capture info/warning messages from R */
  protected transient RLoggerAPI m_logger;

  /** Debugging output */
  protected boolean m_Debug;

  /**
   * Fall back to Zero R if there are no instances with non-missing class or
   * only the class is present in the data
   */
  protected ZeroR m_zeroR;

  /**
   * Global info for this wrapper classifier.
   * 
   * @return the global info suitable for displaying in the GUI.
   */
  public String globalInfo() {
    return "Classifier that wraps the MLR R library for building "
        + "and making predictions with various R classifiers";
  }

  /**
   * Returns default capabilities of the classifier.
   * 
   * @return the capabilities of this classifier
   */
  public Capabilities getCapabilities() {
    Capabilities result = new Capabilities(this);
    result.disableAll();

    // attributes
    result.enable(Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capability.MISSING_VALUES);

    String mlrIdentifier = MLRClassifier.TAGS_LEARNER[m_rLearner].getReadable();
    if (mlrIdentifier.startsWith("classif")) {
      result.enable(Capability.NOMINAL_CLASS);
    } else {
      result.enable(Capability.NUMERIC_CLASS);
    }

    // class
    result.enable(Capability.MISSING_CLASS_VALUES);

    // instances
    result.setMinimumNumberInstances(0);

    return result;
  }

  /**
   * Attempt to initialize the R environment. Sees if R is available, followed
   * by the MLR package. If MLR is not available it attempts to install and load
   * it for the user.
   * 
   * @throws Exception if a problem occurs
   */
  protected void init() throws Exception {

    if (!m_initialized) {
      m_logger = new RLoggerAPI() {

        public void logMessage(String message) {
          if (m_logMessagesFromR) {
            System.err.println(message);
          }
        }

        public void statusMessage(String message) {
          // not needed.
        }
      };
    }

    m_initialized = true;
    RSession eng = null;

    eng = RSession.acquireSession(this);
    eng.setLog(this, m_logger);
    if (!eng.loadLibrary(this, "mlr")) {
      System.err.println("MLR can't be loaded - trying to install....");
      eng.installLibrary(this, "mlr", "http://R-Forge.R-project.org");
      /*
       * if (!eng.installLibrary(this, "mlr", "http://R-Forge.R-project.org")) {
       * System.err.println("Failed to install MLR package!"); return; //
       * nothing we can do }
       */

      // try loading again
      if (!eng.loadLibrary(this, "mlr")) {
        return;
      }
    }

    m_mlrAvailable = true;
    RSession.releaseSession(this);
  }

  /**
   * Attempts to load a base learner package in R. If this fails then it will
   * try to install the required package and then load again.
   * 
   * @throws Exception if a problem occurs
   */
  protected void loadBaseLearnerLibrary() throws Exception {
    String lib = MLRClassifier.TAGS_LEARNER[m_rLearner].getIDStr();
    if (lib.indexOf('.') > 0) {
      lib = lib.substring(lib.indexOf('.') + 1, lib.length());
    }

    RSession eng = null;
    eng = RSession.acquireSession(this);
    eng.setLog(this, m_logger);
    if (!eng.loadLibrary(this, lib)) {
      System.err.println("Attempting to install learner library: " + lib);

      eng.installLibrary(this, lib);
      /*
       * if (!eng.installLibrary(this, lib)) {
       * System.err.println("Unable to continue - " +
       * "failed to install learner library: " + lib);
       * m_baseLearnerLibraryAvailable = false; return; }
       */

      // try loading again
      if (!eng.loadLibrary(this, lib)) {
        return;
      }
    }

    m_baseLearnerLibraryAvailable = true;
    RSession.releaseSession(this);
  }

  /**
   * Returns an enumeration describing the available options.
   * 
   * @return an enumeration of all the available options.
   */
  public Enumeration<Option> listOptions() {
    Vector<Option> newVector = new Vector<Option>();

    StringBuffer learners = new StringBuffer();
    boolean first = true;
    for (Tag t : MLRClassifier.TAGS_LEARNER) {
      String l = t.getReadable();
      learners.append((first ? "" : " ") + l);
      if (first) {
        first = false;
      }
    }

    newVector.addElement(new Option("\tR learner to use ("
        + learners.toString() + ")\n\t(default = rpart)", "learner", 1,
        "-learner"));
    newVector.addElement(new Option(
        "\tLearner hyperparameters (comma separated)", "params", 1, "-params"));
    newVector.addElement(new Option("\tDon't replace missing values", "M", 0,
        "-M"));
    newVector.addElement(new Option("\tBatch size for batch prediction"
        + "\n\t(default = 100)", "batch", 1, "-batch"));
    newVector.addElement(new Option("\tLog messages from R", "L", 0, "-L"));
    newVector.addElement(new Option(
        "\tIf set, classifier is run in debug mode and\n"
            + "\tmay output additional info to the console", "D", 0, "-D"));

    return newVector.elements();
  }

  /**
   * Parses a given list of options.
   * <p/>
   * 
   * <!-- options-start --> <!-- options-end -->
   */
  public void setOptions(String[] options) throws Exception {
    String learnerS = Utils.getOption("learner", options);
    if (learnerS.length() > 0) {
      for (Tag t : MLRClassifier.TAGS_LEARNER) {
        if (t.getReadable().startsWith(learnerS)) {
          setRLearner(new SelectedTag(t.getID(), MLRClassifier.TAGS_LEARNER));
          break;
        }
      }
    }

    String paramsS = Utils.getOption("params", options);
    if (paramsS.length() > 0) {
      setLearnerParams(paramsS);
    }

    String batchSize = Utils.getOption("batch", options);
    if (batchSize.length() > 0) {
      setBatchSize(batchSize);
    }

    setDontReplaceMissingValues(Utils.getFlag('M', options));

    setDebug(Utils.getFlag('D', options));

    setLogMessagesFromR(Utils.getFlag('L', options));

    Utils.checkForRemainingOptions(options);
  }

  /**
   * Gets the current settings of MLRClassifier.
   * 
   * @return an array of strings suitable for passing to setOptions()
   */
  public String[] getOptions() {
    ArrayList<String> options = new ArrayList<String>();

    options.add("-learner");
    options.add(MLRClassifier.TAGS_LEARNER[m_rLearner].getReadable());
    if (m_schemeOptions != null && m_schemeOptions.length() > 0) {
      options.add("-params");
      options.add(m_schemeOptions);
    }

    if (getDontReplaceMissingValues()) {
      options.add("-M");
    }

    options.add("-batch");
    options.add(getBatchSize());

    if (getLogMessagesFromR()) {
      options.add("-L");
    }

    if (getDebug()) {
      options.add("-D");
    }

    return options.toArray(new String[1]);
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String debugTipText() {
    return "Output debugging information";
  }

  /**
   * Set whether to output debugging info
   * 
   * @param d true if debugging info is to be output
   */
  public void setDebug(boolean d) {
    m_Debug = d;
  }

  /**
   * Get whether to output debugging info
   * 
   * @return true if debugging info is to be output
   */
  public boolean getDebug() {
    return m_Debug;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String batchSizeTipText() {
    return "The preferred number of instances to push over to R at a time for "
        + "prediction (if operating in batch prediction mode). "
        + "More or fewer instances than this will be accepted.";
  }

  /**
   * Get the batch size for prediction (i.e. how many instances to push over
   * into an R data frame at a time).
   * 
   * @return the batch size for prediction
   */
  public String getBatchSize() {
    return m_batchPredictSize;
  }

  /**
   * Set the batch size for prediction (i.e. how many instances to push over
   * into an R data frame at a time).
   * 
   * @return the batch size for prediction
   */
  public void setBatchSize(String size) {
    m_batchPredictSize = size;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String RLearnerTipText() {
    return "R learner to use";
  }

  /**
   * Set the base R learner to use
   * 
   * @param learner the learner to use
   */
  public void setRLearner(SelectedTag learner) {
    if (learner.getTags() == MLRClassifier.TAGS_LEARNER) {
      m_rLearner = learner.getSelectedTag().getID();
    }
  }

  /**
   * Get the base R learner to use
   * 
   * @return the learner to use
   */
  public SelectedTag getRLearner() {
    return new SelectedTag(m_rLearner, MLRClassifier.TAGS_LEARNER);
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String learnerParamsTipText() {
    return "Parameters for the R learner";
  }

  /**
   * Set the parameters for the R learner. This should be specified in the same
   * way (i.e. comma separated) as they would be if using the R console.
   * 
   * @param learnerParams the parameters (comma separated) to pass to the R
   *          learner.
   */
  public void setLearnerParams(String learnerParams) {
    m_schemeOptions = learnerParams;
  }

  /**
   * Get the parameters for the R learner. This should be specified in the same
   * way (i.e. comma separated) as they would be if using the R console.
   * 
   * @return the parameters (comma separated) to pass to the R learner.
   */
  public String getLearnerParams() {
    return m_schemeOptions;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String dontReplaceMissingValuesTipText() {
    return "Don't replace missing values";
  }

  /**
   * Set whether to turn off replacement of missing values in the data before it
   * is passed into R.
   * 
   * @param d true if missing values should not be replaced.
   */
  public void setDontReplaceMissingValues(boolean d) {
    m_dontReplaceMissingValues = d;
  }

  /**
   * Get whether to turn off replacement of missing values in the data before it
   * is passed into R.
   * 
   * @return true if missing values should not be replaced.
   */
  public boolean getDontReplaceMissingValues() {
    return m_dontReplaceMissingValues;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String logMessagesFromRTipText() {
    return "Whether to output info/warning messages from R";
  }

  /**
   * Set whether to log info/warning messages from R to the console.
   * 
   * @param l true if info/warning messages should be logged to the console.
   */
  public void setLogMessagesFromR(boolean l) {
    m_logMessagesFromR = l;
  }

  /**
   * Get whether to log info/warning messages from R to the console.
   * 
   * @return true if info/warning messages should be logged to the console.
   */
  public boolean getLogMessagesFromR() {
    return m_logMessagesFromR;
  }

  /**
   * Build the specified R learner on the incoming training data.
   * 
   * @param data the training data to be used for generating the R model.
   * @throws Exception if the classifier could not be built successfully.
   */
  public void buildClassifier(Instances data) throws Exception {
    // can classifier handle the data?
    getCapabilities().testWithFail(data);

    if (m_modelHash == null) {
      m_modelHash = "" + hashCode();
    }

    data = new Instances(data);
    data.deleteWithMissingClass();

    if (data.numInstances() == 0 || data.numAttributes() == 1) {
      if (data.numInstances() == 0) {
        System.err
            .println("No instances with non-missing class - using ZeroR model");
      } else {
        System.err.println("Only the class attribute is present in "
            + "the data - using ZeroR model");
      }
      m_zeroR = new ZeroR();
      m_zeroR.buildClassifier(data);
      return;
    }

    if (!m_dontReplaceMissingValues) {
      m_missingFilter = new ReplaceMissingValues();
      m_missingFilter.setInputFormat(data);
      data = Filter.useFilter(data, m_missingFilter);
    }

    m_testHeader = new Instances(data, 0);

    m_serializedModel = null;

    if (!m_initialized) {
      init();
    }

    if (!m_mlrAvailable) {
      throw new Exception(
          "MLR is not available for some reason - can't continue!");
    }

    loadBaseLearnerLibrary();
    if (!m_baseLearnerLibraryAvailable) {
      throw new Exception("Library "
          + MLRClassifier.TAGS_LEARNER[m_rLearner].getIDStr() + " for learner "
          + MLRClassifier.TAGS_LEARNER[m_rLearner].getReadable()
          + " is not available for some reason - can't continue!");
    }

    RSession eng = null;
    eng = RSession.acquireSession(this);
    eng.setLog(this, m_logger);
    eng.clearConsoleBuffer(this);

    // clean up any previous model
    // TODO suffix model identifier with hashcode of this object
    eng.parseAndEval(this, "remove(weka_r_model" + m_modelHash + ")");

    // transfer training data into a data frame in R
    RUtils.instancesToDataFrame(eng, this, data, "mlr_data");

    try {
      String mlrIdentifier = MLRClassifier.TAGS_LEARNER[m_rLearner]
          .getReadable();

      /*
       * String type = (mlrIdentifier.indexOf("classification") > 0) ?
       * "classif." : "regr.";
       */

      if (data.classAttribute().isNumeric()
          && mlrIdentifier.startsWith("classif")) {
        throw new Exception("Training instances has a numeric class but "
            + "selected R learner is a classifier!");
      } else if (data.classAttribute().isNominal()
          && mlrIdentifier.startsWith("regr")) {
        throw new Exception("Training instances has a nominal class but "
            + "selected R learner is a regressor!");
      }

      /*
       * mlrIdentifier = mlrIdentifier.substring(0, mlrIdentifier.indexOf('(') -
       * 1); mlrIdentifier = type + mlrIdentifier;
       */

      // make classification/regression task
      String taskType = "make.task";
      String taskString = "task <- " + taskType + "(data = mlr_data, "
          + "target = \"" + RUtils.cleanse(data.classAttribute().name())
          + "\")";
      if (m_Debug) {
        System.err.println("Prediction task: " + taskString);
      }
      eng.parseAndEval(this, taskString);
      eng.parseAndEval(this, "print(task)");

      // make learner object
      String probs = (data.classAttribute().isNominal()) ? ", predict.type = \"prob\""
          : "";
      String learnString = "";
      if (m_schemeOptions != null && m_schemeOptions.length() > 0) {
        learnString = "l <- make.learner(\"" + mlrIdentifier + "\"" + probs
            + ", " + m_schemeOptions + ")";
      } else {
        learnString = "l <- make.learner(\"" + mlrIdentifier + "\"" + probs
            + ")";
      }

      if (m_Debug) {
        System.err.println("Make a learner object: " + learnString);
      }

      eng.parseAndEval(this, learnString);

      eng.parseAndEval(this, "print(l)");

      // train model
      eng.parseAndEval(this, "weka_r_model" + m_modelHash
          + " <- train(l, task)");

      // get the model for serialization
      REXP serializedRModel = eng.parseAndEval(this, "serialize(weka_r_model"
          + m_modelHash + ", NULL)");

      // REXP model = eng.get(this, "base_model");

      m_modelText = new StringBuffer();

      // get the textual representation
      eng.parseAndEval(this, "print(weka_r_model" + m_modelHash
          + "@learner.model)");
      m_modelText.append(eng.getConsoleBuffer(this));

      // now try and serialize the model
      XStream xs = new XStream();
      String xml = xs.toXML(serializedRModel);
      if (xml != null && xml.length() > 0) {
        m_serializedModel = new StringBuffer();
        m_serializedModel.append(xml);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      // remove R training data frame after completion
      eng.parseAndEval(this, "remove(mlr_data)");
      RSession.releaseSession(this);

      throw new Exception(ex.getMessage());
    }

    eng.parseAndEval(this, "remove(mlr_data)");
    RSession.releaseSession(this);
  }

  protected void pushModelToR(RSession eng) throws Exception {
    XStream xs = new XStream();
    REXP model = (REXP) xs.fromXML(m_serializedModel.toString());

    eng.assign(this, "weka_r_model" + m_modelHash, model);
    eng.parseAndEval(this, "weka_r_model" + m_modelHash
        + " <- unserialize(weka_r_model" + m_modelHash + ")");

    if (m_Debug) {
      eng.clearConsoleBuffer(this);
      eng.parseAndEval(this, "print(weka_r_model" + m_modelHash
          + "@learner.model)");
      System.err.println("Printing pushed model....");
      System.err.println(eng.getConsoleBuffer(this));
    }
  }

  protected double[][] frameToPreds(RSession eng, REXP r, Attribute classAtt,
      int numRows) throws Exception {
    RList frame = r.asList();

    double[][] result = classAtt.isNumeric() ? new double[numRows][1]
        : new double[numRows][classAtt.numValues()];

    String attributeNames[] = null;

    attributeNames = ((REXPString) ((REXPGenericVector) r)._attr().asList()
        .get("names")).asStrings();

    if (classAtt.isNominal()) {
      for (int i = 0; i < classAtt.numValues(); i++) {
        String classL = RUtils.cleanse(classAtt.value(i));

        int index = -1;
        for (int j = 0; j < attributeNames.length; j++) {
          // if (attributeNames[j].indexOf(classL) > 0) {
          if (attributeNames[j].equals("prob." + classL)) {
            index = j;
            break;
          }
        }

        if (index == -1) {
          // it appears that the prediction frame will not contain a column for
          // empty classes
          /*
           * throw new Exception("Unable to find class " + classL +
           * " in the prediction " + "frame returned from R!!");
           */
          continue;
        }

        Object columnObject = frame.get(index);
        REXPVector colVector = (REXPVector) columnObject;
        double[] colD = colVector.asDoubles();
        if (colD.length != numRows) {
          throw new Exception("Was expecting " + numRows + " predictions "
              + "but got " + colD.length + "!");
        }
        for (int j = 0; j < numRows; j++) {
          result[j][i] = colD[j];
        }

        // result[i] = colVector.asDoubles()[0];
      }
    } else {
      Object columnObject = frame.get(1);
      REXPVector colVector = (REXPVector) columnObject;
      double[] colD = colVector.asDoubles();
      if (colD.length != numRows) {
        throw new Exception("Was expecting " + numRows + " predictions "
            + "but got " + colD.length + "!");
      }
      for (int j = 0; j < numRows; j++) {
        result[j][0] = colD[j];
      }

      // result[0] = colVector.asDoubles()[0];
    }

    /*
     * if (classAtt.isNominal()) { for (int i = 0; i < classAtt.numValues();
     * i++) { Object columnObject = frame.get(i); REXPVector colVector =
     * (REXPVector) columnObject; result[i] = colVector.asDoubles()[0]; } } else
     * { Object columnObject = frame.get(0); REXPVector colVector = (REXPVector)
     * columnObject; result[0] = colVector.asDoubles()[0]; }
     */

    return result;
  }

  private double[][] batchScoreWithZeroR(Instances insts) throws Exception {
    double[][] result = new double[insts.numInstances()][];

    for (int i = 0; i < insts.numInstances(); i++) {
      Instance current = insts.instance(i);
      result[i] = m_zeroR.distributionForInstance(current);
    }

    return result;
  }

  /**
   * Batch scoring method
   * 
   * @param insts the instances to push over to R and get predictions for
   * @return an array of probability distributions, one for each instance
   * @throws Exception if a problem occurs
   */
  public double[][] distributionsForInstances(Instances insts) throws Exception {
    double[][] probs = null;

    if ((m_serializedModel == null || m_serializedModel.length() == 0)
        && m_zeroR == null) {
      throw new Exception("No model has been built yet!");
    }

    if (m_zeroR != null) {
      return batchScoreWithZeroR(insts);
    }

    if (!m_initialized) {
      init();
    }

    if (!m_mlrAvailable) {
      throw new Exception(
          "MLR is not available for some reason - can't continue!");
    }

    m_testHeader.delete();

    if (!m_dontReplaceMissingValues) {
      for (int i = 0; i < insts.numInstances(); i++) {
        Instance inst = insts.instance(i);
        m_missingFilter.input(inst);
        inst = m_missingFilter.output();
        m_testHeader.add(inst);
      }
    }

    RSession eng = null;
    eng = RSession.acquireSession(this);
    eng.setLog(this, m_logger);

    try {
      // we need to check whether the model has been pushed into R. There
      // is some overhead in having to check every time but its probably less
      // than naively pushing the model over every time distForInst() is called
      if (eng.isVariableSet(this, "weka_r_model" + m_modelHash)) {
        if (m_Debug) {
          System.err
              .println("No need to push serialized model to R - it's already there.");
        }
      } else {
        if (m_Debug) {
          System.err.println("Pushing serialized model to R...");
        }
        // we need to push it over
        pushModelToR(eng);
      }

      Instances toPush = m_dontReplaceMissingValues ? insts : m_testHeader;
      RUtils.instancesToDataFrame(eng, this, toPush, "weka_r_test");

      String testB = "p <- predict(weka_r_model" + m_modelHash
          + ", newdata = weka_r_test)";

      if (m_Debug) {
        System.err.println("Excuting prediction: ");
        System.err.println(testB);
      }

      eng.parseAndEval(this, testB);

      REXP result = eng.parseAndEval(this, "p@df");

      probs = frameToPreds(eng, result, insts.classAttribute(),
          insts.numInstances());

      eng.parseAndEval(this, "remove(p)");
    } catch (Exception ex) {
      ex.printStackTrace();

      RSession.releaseSession(this);
      throw new Exception(ex.getMessage());
    }

    RSession.releaseSession(this);

    return probs;
  }

  /**
   * Calculates the class membership probabilities for the given test instance.
   * 
   * @param instance the instance to be classified
   * @return predicted class probability distribution
   * @throws Exception if distribution can't be computed successfully
   */
  public double[] distributionForInstance(Instance inst) throws Exception {
    if ((m_serializedModel == null || m_serializedModel.length() == 0)
        && m_zeroR == null) {
      throw new Exception("No model has been built yet!");
    }

    if (m_zeroR != null) {
      return m_zeroR.distributionForInstance(inst);
    }

    if (!m_initialized) {
      init();
    }

    if (!m_mlrAvailable) {
      throw new Exception(
          "MLR is not available for some reason - can't continue!");
    }

    if (!m_dontReplaceMissingValues) {
      m_missingFilter.input(inst);
      inst = m_missingFilter.output();
    }

    if (m_testHeader.numInstances() > 0) {
      m_testHeader.delete();
    }
    m_testHeader.add(inst);
    // System.out.println(m_testHeader);

    if (m_Debug) {
      System.err.println("Instance to predict: " + inst.toString());
    }

    double[] pred = null;

    /*
     * if (inst.classAttribute().isNumeric()) { pred = new double[1]; } else {
     * pred = new double[inst.classAttribute().numValues()]; }
     */

    RSession eng = null;
    eng = RSession.acquireSession(this);
    eng.setLog(this, m_logger);

    try {
      // we need to check whether the model has been pushed into R. There
      // is some overhead in having to check every time but its probably less
      // than naively pushing the model over every time distForInst() is called
      if (eng.isVariableSet(this, "weka_r_model" + m_modelHash)) {
        if (m_Debug) {
          System.err
              .println("No need to push serialized model to R - it's already there.");
        }
      } else {
        if (m_Debug) {
          System.err.println("Pushing serialized model to R...");
        }
        // we need to push it over
        pushModelToR(eng);
      }

      if (m_modelCleaner == null) {
        m_counter = new AtomicInteger(5);
        m_modelCleaner = new Thread() {
          @Override
          public void run() {
            while (m_counter.get() > 0) {
              try {
                Thread.sleep(1000);
              } catch (InterruptedException ex) {
              }
              m_counter.decrementAndGet();
            }

            // cleanup the model in R
            try {
              if (m_Debug) {
                System.err.println("Cleaning up model in R...");
              }
              RSession teng = RSession.acquireSession(this);
              teng.setLog(this, m_logger);
              teng.parseAndEval(this, "remove(weka_r_model" + m_modelHash + ")");
              RSession.releaseSession(this);
            } catch (Exception ex) {
              System.err.println("A problem occurred whilst trying "
                  + "to clean up the model in R: " + ex.getMessage());
            }
            m_modelCleaner = null;
          }
        };

        m_modelCleaner.setPriority(Thread.MIN_PRIORITY);
        m_modelCleaner.start();
      } else {
        m_counter.set(5);
      }

      // StringBuffer testB = new StringBuffer();
      RUtils.instancesToDataFrame(eng, this, m_testHeader, "weka_r_test");

      String testB = "p <- predict(weka_r_model" + m_modelHash
          + ", newdata = weka_r_test)";

      /*
       * testB.append("p <- predict(weka_r_model" + m_modelHash +
       * ", newdata = data.frame(");
       * 
       * boolean first = true; for (int i = 0; i < inst.numAttributes(); i++) {
       * 
       * if (i != inst.classIndex()) { if (!first) { testB.append(","); } else {
       * first = false; }
       * 
       * testB.append(RSession.cleanse(inst.attribute(i).name())).append("=");
       * if (inst.isMissing(i)) { testB.append("NA"); } else if
       * (inst.attribute(i).isNumeric()) { testB.append(inst.value(i)); } else
       * if (inst.attribute(i).isNominal()) { testB.append("\"" +
       * RSession.cleanse(inst.stringValue(i)) + "\""); } else { throw new
       * Exception("Can't handle attribute " + inst.attribute(i).name()); } } }
       * testB.append("))");
       */

      // eng.clearConsoleBuffer(this);
      if (m_Debug) {
        System.err.println("Excuting prediction: ");
        System.err.println(testB);
      }

      eng.parseAndEval(this, testB);
      REXP result = eng.parseAndEval(this, "p@df");
      // REXP result = eng.parseAndEval(this, testB.toString());
      // eng.parseAndEval(this, "print(p@df)");
      // System.out.println(eng.getConsoleBuffer(this));

      /*
       * if (inst.classAttribute().isNumeric()) { pred[0] = doubleToPred(eng,
       * result); } else { pred = matrixToPreds(eng, result); }
       */

      pred = frameToPreds(eng, result, inst.classAttribute(), 1)[0];

      /*
       * Instances predInst = eng.dataFrameToInstances(this, result);
       * 
       * if (inst.classAttribute().isNominal()) { // + 1 for the "repsonse"
       * column if (predInst.numAttributes() !=
       * inst.classAttribute().numValues() + 1) { throw new
       * Exception("Prediction generated from R does not contain the " +
       * "correct number of probabilities for class " +
       * inst.classAttribute().name()); } Instance p = predInst.instance(0); for
       * (int i = 0; i < inst.classAttribute().numValues(); i++) { pred[i] =
       * p.value(i); } } else { if (predInst.numAttributes() != 1) { throw new
       * Exception("There should be just a single value predicted " +
       * "for a numeric class!"); } pred[0] = predInst.instance(0).value(0); }
       */

      eng.parseAndEval(this, "remove(p)");
    } catch (Exception ex) {
      ex.printStackTrace();

      RSession.releaseSession(this);
      throw new Exception(ex.getMessage());
    }

    m_counter.set(5);
    RSession.releaseSession(this);

    return pred;
  }

  /**
   * Returns the textual description of the R classifier.
   * 
   * @return description of the R classifier as a string.
   */
  @Override
  public String toString() {
    if (m_modelText != null && m_modelText.length() > 0) {
      return m_modelText.toString();
    }

    return "MLRClassifier: model not built yet!";
  }

  /**
   * Returns the revision string.
   * 
   * @return the revision
   */
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 51265 $");
  }
}
