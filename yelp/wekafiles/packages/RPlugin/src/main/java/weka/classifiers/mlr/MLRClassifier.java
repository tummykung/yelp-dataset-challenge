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
 *    MLRClassifier.java
 *    Copyright (C) 2012 Pentaho Corporation
 *
 */

package weka.classifiers.mlr;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;

import weka.classifiers.AbstractClassifier;
import weka.core.BatchPredictor;
import weka.core.Capabilities;
import weka.core.CapabilitiesHandler;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.JRILoader;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.SelectedTag;
import weka.core.Tag;

/**
 * Wrapper classifier for the MLRClassifier package. This class delegates (via
 * reflection) to MLRClassifierImpl. MLRClassifierImpl uses REngine/JRI classes
 * so has to be injected into the root class loader.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision: 50377 $
 */
public class MLRClassifier extends AbstractClassifier implements OptionHandler,
    CapabilitiesHandler, BatchPredictor, RevisionHandler, Serializable {

  /**
   * For serialization
   */
  private static final long serialVersionUID = -5715911392187197733L;

  static {
    try {
      JRILoader.load();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  // Classification
  public static final int R_CLASSIF_ADA = 0;
  public static final int R_CLASSIF_ADABOOST_M1 = 1;
  public static final int R_CLASSIF_GBM = 2;
  public static final int R_CLASSIF_KKNN = 3;
  public static final int R_CLASSIF_KSVM = 4;
  public static final int R_CLASSIF_LDA = 5;
  public static final int R_CLASSIF_LOCLDA = 6;
  public static final int R_CLASSIF_LOGREG = 7;
  public static final int R_CLASSIF_LSSVM = 8;
  public static final int R_CLASSIF_LVQ1 = 9;
  public static final int R_CLASSIF_MDA = 10;
  public static final int R_CLASSIF_MULTINOM = 11;
  public static final int R_CLASSIF_NAIVE_BAYES = 12;
  public static final int R_CLASSIF_NNET = 13;
  public static final int R_CLASSIF_QDA = 14;
  public static final int R_CLASSIF_RANDOM_FOREST = 15;
  public static final int R_CLASSIF_RDA = 16;
  public static final int R_CLASSIF_RPART = 17;
  public static final int R_CLASSIF_SVM = 18;

  // Regression
  public static final int R_REGR_EARTH = 19;
  public static final int R_REGR_GBM = 20;
  public static final int R_REGR_KKNN = 21;
  public static final int R_REGR_KSVM = 22;
  public static final int R_REGR_LASSO = 23;
  public static final int R_REGR_LM = 24;
  public static final int R_REGR_MARS = 25;
  public static final int R_REGR_NNET = 26;
  public static final int R_REGR_RANDOM_FOREST = 27;
  public static final int R_REGR_RIDGE = 28;
  public static final int R_REGR_RVM = 29;

  /** Tags for the various types of learner */
  public static final Tag[] TAGS_LEARNER = {
      new Tag(R_CLASSIF_ADA, "ada", "classif.ada", false),
      new Tag(R_CLASSIF_ADABOOST_M1, "adabag", "classif.adaboost.M1", false),
      new Tag(R_CLASSIF_GBM, "a.gbm", "classif.gbm", false),
      new Tag(R_CLASSIF_KKNN, "a.kknn", "classif.kknn", false),
      new Tag(R_CLASSIF_KSVM, "a.kernlab", "classif.ksvm", false),
      new Tag(R_CLASSIF_LDA, "a.MASS", "classif.lda", false),
      new Tag(R_CLASSIF_LOCLDA, "a.klaR", "classif.loclda", false),
      new Tag(R_CLASSIF_LOGREG, "a.stats", "classif.logreg", false),
      new Tag(R_CLASSIF_LSSVM, "b.kernlab", "classif.lssvm", false),
      new Tag(R_CLASSIF_LVQ1, "class", "classif.lvq1", false),
      new Tag(R_CLASSIF_MDA, "a.mda", "classif.mda", false),
      new Tag(R_CLASSIF_MULTINOM, "a.nnet", "classif.multinom", false),
      new Tag(R_CLASSIF_NAIVE_BAYES, "a.e1071", "classif.naiveBayes", false),
      new Tag(R_CLASSIF_NNET, "b.nnet", "classif.nnet", false),
      new Tag(R_CLASSIF_QDA, "b.MASS", "classif.qda", false),
      new Tag(R_CLASSIF_RANDOM_FOREST, "a.randomForest",
          "classif.randomForest", false),
      new Tag(R_CLASSIF_RDA, "b.klaR", "classif.rda", false),
      new Tag(R_CLASSIF_RPART, "rpart", "classif.rpart", false),
      new Tag(R_CLASSIF_SVM, "b.e1071", "classif.svm", false),

      new Tag(R_REGR_EARTH, "earth", "regr.earth", false),
      new Tag(R_REGR_GBM, "b.gbm", "regr.gbm", false),
      new Tag(R_REGR_KKNN, "b.kknn", "regr.kknn", false),
      new Tag(R_REGR_KSVM, "c.kernlab", "regr.ksvm", false),
      new Tag(R_REGR_LASSO, "a.penalized", "regr.lasso", false),
      new Tag(R_REGR_LM, "b.stats", "regr.lm", false),
      new Tag(R_REGR_MARS, "b.mda", "regr.mars", false),
      new Tag(R_REGR_NNET, "c.nnet", "regr.nnet", false),
      new Tag(R_REGR_RANDOM_FOREST, "b.randomForest", "regr.randomForest",
          false), new Tag(R_REGR_RIDGE, "b.penalized", "regr.ridge", false),
      new Tag(R_REGR_RVM, "d.kernlab", "regr.rvm", false) };

  protected static final String IMPL = "weka.classifiers.mlr.impl.MLRClassifierImpl";

  protected Object m_delegate;

  protected void init() {
    try {
      m_delegate = Class.forName(IMPL).newInstance();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

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
  @Override
  public Capabilities getCapabilities() {
    if (m_delegate == null) {
      init();
    }
    return ((CapabilitiesHandler) m_delegate).getCapabilities();
  }

  /**
   * Returns an enumeration describing the available options.
   * 
   * @return an enumeration of all the available options.
   */
  @Override
  public Enumeration<Option> listOptions() {
    if (m_delegate == null) {
      init();
    }
    return ((OptionHandler) m_delegate).listOptions();
  }

  /**
   * Parses a given list of options.
   * <p/>
   * 
   * <!-- options-start --> <!-- options-end -->
   */
  @Override
  public void setOptions(String[] options) throws Exception {
    if (m_delegate == null) {
      init();
    }
    ((OptionHandler) m_delegate).setOptions(options);
  }

  /**
   * Gets the current settings of MLRClassifier.
   * 
   * @return an array of strings suitable for passing to setOptions()
   */
  @Override
  public String[] getOptions() {
    if (m_delegate == null) {
      init();
    }

    return ((OptionHandler) m_delegate).getOptions();
  }

  @Override
  public String debugTipText() {
    if (m_delegate == null) {
      init();
    }
    try {
      Method m = m_delegate.getClass().getDeclaredMethod("debugTipText",
          new Class[] {});

      Object result = m.invoke(m_delegate, new Object[] {});
      return result.toString();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return "";
  }

  /**
   * Set whether to output debugging info
   * 
   * @param d true if debugging info is to be output
   */
  @Override
  public void setDebug(boolean d) {
    if (m_delegate == null) {
      init();
    }
    try {
      Method m = m_delegate.getClass().getDeclaredMethod("setDebug",
          new Class[] { Boolean.TYPE });

      m.invoke(m_delegate, new Object[] { new Boolean(d) });
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Get whether to output debugging info
   * 
   * @return true if debugging info is to be output
   */
  @Override
  public boolean getDebug() {
    if (m_delegate == null) {
      init();
    }
    try {
      Method m = m_delegate.getClass().getDeclaredMethod("getDebug",
          new Class[] {});

      Object result = m.invoke(m_delegate, new Object[] {});
      return ((Boolean) result).booleanValue();
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return false;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String batchSizeTipText() {
    if (m_delegate == null) {
      init();
    }
    try {
      Method m = m_delegate.getClass().getDeclaredMethod("batchSizeTipText",
          new Class[] {});

      Object result = m.invoke(m_delegate, new Object[] {});
      return result.toString();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return "";
  }

  /**
   * Get the batch size for prediction (i.e. how many instances to push over
   * into an R data frame at a time).
   * 
   * @return the batch size for prediction
   */
  public String getBatchSize() {
    if (m_delegate == null) {
      init();
    }
    try {
      Method m = m_delegate.getClass().getDeclaredMethod("getBatchSize",
          new Class[] {});

      Object result = m.invoke(m_delegate, new Object[] {});
      return result.toString();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return "";
  }

  /**
   * Set the batch size for prediction (i.e. how many instances to push over
   * into an R data frame at a time).
   * 
   * @return the batch size for prediction
   */
  public void setBatchSize(String size) {
    if (m_delegate == null) {
      init();
    }
    try {
      Method m = m_delegate.getClass().getDeclaredMethod("setBatchSize",
          new Class[] { String.class });

      m.invoke(m_delegate, new Object[] { size });
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String RLearnerTipText() {
    if (m_delegate == null) {
      init();
    }
    try {
      Method m = m_delegate.getClass().getDeclaredMethod("RLearnerTipText",
          new Class[] {});

      Object result = m.invoke(m_delegate, new Object[] {});
      return result.toString();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return "";
  }

  /**
   * Set the base R learner to use
   * 
   * @param learner the learner to use
   */
  public void setRLearner(SelectedTag learner) {
    if (m_delegate == null) {
      init();
    }
    try {
      Method m = m_delegate.getClass().getDeclaredMethod("setRLearner",
          new Class[] { SelectedTag.class });

      m.invoke(m_delegate, new Object[] { learner });
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Get the base R learner to use
   * 
   * @return the learner to use
   */
  public SelectedTag getRLearner() {
    if (m_delegate == null) {
      init();
    }
    try {
      Method m = m_delegate.getClass().getDeclaredMethod("getRLearner",
          new Class[] {});

      Object result = m.invoke(m_delegate, new Object[] {});
      return (SelectedTag) result;
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String learnerParamsTipText() {
    if (m_delegate == null) {
      init();
    }
    try {
      Method m = m_delegate.getClass().getDeclaredMethod(
          "learnerParamsTipText", new Class[] {});

      Object result = m.invoke(m_delegate, new Object[] {});
      return result.toString();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return "";
  }

  /**
   * Set the parameters for the R learner. This should be specified in the same
   * way (i.e. comma separated) as they would be if using the R console.
   * 
   * @param learnerParams the parameters (comma separated) to pass to the R
   *          learner.
   */
  public void setLearnerParams(String learnerParams) {
    if (m_delegate == null) {
      init();
    }
    try {
      Method m = m_delegate.getClass().getDeclaredMethod("setLearnerParams",
          new Class[] { String.class });

      m.invoke(m_delegate, new Object[] { learnerParams });
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Get the parameters for the R learner. This should be specified in the same
   * way (i.e. comma separated) as they would be if using the R console.
   * 
   * @return the parameters (comma separated) to pass to the R learner.
   */
  public String getLearnerParams() {
    if (m_delegate == null) {
      init();
    }
    try {
      Method m = m_delegate.getClass().getDeclaredMethod("getLearnerParams",
          new Class[] {});

      Object result = m.invoke(m_delegate, new Object[] {});
      return result.toString();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return "";
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String dontReplaceMissingValuesTipText() {
    if (m_delegate == null) {
      init();
    }
    try {
      Method m = m_delegate.getClass().getDeclaredMethod(
          "dontReplaceMissingValuesTipText", new Class[] {});

      Object result = m.invoke(m_delegate, new Object[] {});
      return result.toString();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return "";
  }

  /**
   * Set whether to turn off replacement of missing values in the data before it
   * is passed into R.
   * 
   * @param d true if missing values should not be replaced.
   */
  public void setDontReplaceMissingValues(boolean d) {
    if (m_delegate == null) {
      init();
    }
    try {
      Method m = m_delegate.getClass().getDeclaredMethod(
          "setDontReplaceMissingValues", new Class[] { Boolean.TYPE });

      m.invoke(m_delegate, new Object[] { new Boolean(d) });
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Get whether to turn off replacement of missing values in the data before it
   * is passed into R.
   * 
   * @return true if missing values should not be replaced.
   */
  public boolean getDontReplaceMissingValues() {
    if (m_delegate == null) {
      init();
    }
    try {
      Method m = m_delegate.getClass().getDeclaredMethod(
          "getDontReplaceMissingValues", new Class[] {});

      Object result = m.invoke(m_delegate, new Object[] {});
      return ((Boolean) result).booleanValue();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return false;
  }

  /**
   * Returns the tip text for this property
   * 
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String logMessagesFromRTipText() {
    if (m_delegate == null) {
      init();
    }
    try {
      Method m = m_delegate.getClass().getDeclaredMethod(
          "logMessagesFromRTipText", new Class[] {});

      Object result = m.invoke(m_delegate, new Object[] {});
      return result.toString();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return "";
  }

  /**
   * Set whether to log info/warning messages from R to the console.
   * 
   * @param l true if info/warning messages should be logged to the console.
   */
  public void setLogMessagesFromR(boolean l) {
    if (m_delegate == null) {
      init();
    }
    try {
      Method m = m_delegate.getClass().getDeclaredMethod("setLogMessagesFromR",
          new Class[] { Boolean.TYPE });

      m.invoke(m_delegate, new Object[] { new Boolean(l) });
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Get whether to log info/warning messages from R to the console.
   * 
   * @return true if info/warning messages should be logged to the console.
   */
  public boolean getLogMessagesFromR() {
    if (m_delegate == null) {
      init();
    }
    try {
      Method m = m_delegate.getClass().getDeclaredMethod("getLogMessagesFromR",
          new Class[] {});

      Object result = m.invoke(m_delegate, new Object[] {});
      return ((Boolean) result).booleanValue();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return false;
  }

  /**
   * Build the specified R learner on the incoming training data.
   * 
   * @param data the training data to be used for generating the R model.
   * @throws Exception if the classifier could not be built successfully.
   */
  public void buildClassifier(Instances data) throws Exception {
    if (m_delegate == null) {
      init();
    }
    try {
      // m_delegate.buildClassifier(data);
      Method m = m_delegate.getClass().getDeclaredMethod("buildClassifier",
          new Class[] { Instances.class });
      m.invoke(m_delegate, new Object[] { data });
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      throw new Exception(cause);
    }
  }

  /**
   * Batch scoring method
   * 
   * @param insts the instances to push over to R and get predictions for
   * @return an array of probability distributions, one for each instance
   * @throws Exception if a problem occurs
   */
  public double[][] distributionsForInstances(Instances insts) throws Exception {
    if (m_delegate == null) {
      init();
    }

    return ((BatchPredictor) m_delegate).distributionsForInstances(insts);
  }

  /**
   * Calculates the class membership probabilities for the given test instance.
   * 
   * @param instance the instance to be classified
   * @return predicted class probability distribution
   * @throws Exception if distribution can't be computed successfully
   */
  @Override
  public double[] distributionForInstance(Instance inst) throws Exception {
    if (m_delegate == null) {
      init();
    }
    try {
      Method m = m_delegate.getClass().getDeclaredMethod(
          "distributionForInstance", new Class[] { Instance.class });

      Object result = m.invoke(m_delegate, new Object[] { inst });

      return (double[]) result;
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();

      throw new Exception(cause);
    }
  }

  @Override
  public String toString() {
    if (m_delegate == null) {
      return "MLRClassifier: model not built yet!";
    }

    return m_delegate.toString();
  }

  /**
   * Returns the revision string.
   * 
   * @return the revision
   */
  @Override
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 50377 $");
  }

  /**
   * Main method for testing this class.
   * 
   * @param argv the options
   */
  public static void main(String[] args) {
    runClassifier(new MLRClassifier(), args);
  }
}
