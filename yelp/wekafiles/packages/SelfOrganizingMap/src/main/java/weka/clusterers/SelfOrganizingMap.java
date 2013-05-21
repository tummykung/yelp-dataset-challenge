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
 *    SelfOrganizingMap.java
 *    Copyright (C) 2000-2010 University of Waikato, Hamilton, New Zealand
 *
 */
package weka.clusterers;

import java.util.Enumeration;
import java.util.Vector;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.EuclideanDistance;

/**
<!-- globalinfo-start -->
 * A Clusterer that implements Kohonen's Self-Organizing Map algorithm for
 * unsupervised clustering. <br/>
 * See T. Kohonen, Self-Organization and Associative Memory, 3rd Edition, Springer, 1989
 * <p/>
<!-- globalinfo-end -->
 *
<!-- options-start -->
 * Valid options are: <p/>
 *
 * <pre> -L &lt; initial learning rate&gt;
 *  The initial learning rate for the training algorithm.
 *  (Value should be greater than 0.01 and less or equal to 1, Default = 1).</pre>
 *
 * <pre> -O  &lt;number of epochs in ordering phase&gt;
 *  Number of epochs in ordering phase.
 *  (Value should be greater than or equal to 2000, Default = 2000).</pre>
 *
 * <pre> -C  &lt;number of epochs in convergence phase&gt;
 *  Number of epochs to train through.
 *  (Value should be greater than or equal to 1000, Default = 1000).</pre>
 *
 * <pre> -H &lt;height of lattice&gt;
 *  The height of lattice.
 *  (Value should be &gt; 0, Default = 1).</pre>
 *
 * <pre> -W &lt;width of lattice&gt;
 *  The width of lattice.
 *  (Value should be &gt; 0, Default = 1).</pre>
 *
 * <pre> -I
 *  Normalizing the attributes will NOT be done.
 *  (Set this to not normalize the attributes).</pre>
 *
 * <pre> -S
 *  Statistics will NOT be calculated after training.
 *  (Set this to not calculate statistics).</pre>
 *
<!-- options-end -->
 *
 * @author John Salatas (jsalatas at gmail.com)
 * @version $Revision: 4 $
 */
public class SelfOrganizingMap extends AbstractClusterer
        implements OptionHandler {

    /** for serialization */
    static final long serialVersionUID = -3028490959617832916L;
    /** the distance function used. */
    private EuclideanDistance m_euclideanDistance = new EuclideanDistance();
    /** The width of lattice */
    private int m_width;
    /** The height of lattice */
    private int m_height;
    /** The number of epochs in convergence phase  */
    private int m_convergenceEpochs;
    /** The number of epochs in ordering phase */
    private int m_orderingEpochs;
    /** The initial learning rate for the network */
    private double m_learningRate;
    /** The training instances.  */
    private Instances m_instances;
    /** The weights for each unit in the lattice. */
    private Instances m_clusters;
    // It is used in EuclideanDistance.closestPoint
    private int[] m_clusterList;
    /** This flag states that the user wants the input values normalized. */
    private boolean m_normalizeAttributes;
    /** The maximum value for all the attributes. */
    private double[] m_attributeMax;
    /** The minimum value for all the attributes. */
    private double[] m_attributeMin;
    /**  This flag states that the user wants to calculate statistics after training. */
    private boolean m_calcStats;
    /** holds the training instances to clusters assignments */
    private Instances[] m_clusterInstances;
    /** holds the cluster statistics */
    private double[][][] m_clusterStats;

    /**
     * @return The width of lattice.
     */
    public int getWidth() {
        return m_width;
    }

    /**
     * Sets the width of lattice.
     * @param width The width of lattice.
     */
    public void setWidth(int width) {
        if (width > 0) {
            this.m_width = width;
        }
    }

    /**
     * @return The height of lattice.
     */
    public int getHeight() {
        return m_height;
    }

    /**
     * Sets the height of lattice.
     * Must be greater than 0.
     * @param height The height.
     */
    public void setHeight(int height) {
        if (height > 0) {
            this.m_height = height;
        }
    }

    /**
     * @return The number of epochs in convergence phase.
     */
    public int getConvergenceEpochs() {
        return m_convergenceEpochs;
    }

    /**
     * Set the number of epochs in convergence phase.
     * Must be greater than or equal to 1000.
     * @param n The number of epochs.
     */
    public void setConvergenceEpochs(int n) {
        if (n >= 1000) {
            m_convergenceEpochs = n;
        }
    }

    /**
     * @return The number of epochs in ordering phase.
     */
    public int getOrderingEpochs() {
        return m_orderingEpochs;
    }

    /**
     * Set the number of epochs in ordering phase.
     * Must be greater than or equal to 2000.
     * @param n The number of epochs.
     */
    public void setOrderingEpochs(int n) {
        if (n >= 2000) {
            m_orderingEpochs = n;
        }
    }

    /**
     * @return The initial learning rate for the nodes.
     */
    public double getLearningRate() {
        return m_learningRate;
    }

    /**
     * The initial learning rate can be set using this command.
     * Must be greater than 0 and no more than 1.
     * @param l The initial learning rate.
     */
    public void setLearningRate(double l) {
        if (l > 0.01 && l <= 1) {
            m_learningRate = l;
        }
    }

    /**
     * @return The flag for normalizing attributes.
     */
    public boolean getNormalizeAttributes() {
        return m_normalizeAttributes;
    }

    /**
     * @param a True if the attributes should be normalized (even nominal
     * attributes will get normalized here) (range goes between -1 - 1).
     */
    public void setNormalizeAttributes(boolean a) {
        m_normalizeAttributes = a;
    }

    /**
     * @return The flag for calculating statistics after training.
     */
    public boolean getCalcStats() {
        return m_calcStats;
    }

    /**
     *
     * @param c True if statistics should be calculated.
     */
    public void setCalcStats(boolean c) {
        this.m_calcStats = c;
    }

    /**
     * @return a string to describe the height option.
     */
    public String heightTipText() {
        return "The height of lattice.";
    }

    /**
     * @return a string to describe the caclulate statistics option.
     */
    public String calcStatsTipText() {
        return "This should calculate statistics for each cluster after training.";
    }

    /**
     * @return a string to describe the width option.
     */
    public String widthTipText() {
        return "The width of lattice.";
    }

    /**
     * @return a string to describe the learning rate option.
     */
    public String learningRateTipText() {
        return "The initial amount the weights are updated.";
    }

    /**
     * @return a string to describe the number of epochs in convergence option.
     */
    public String convergenceEpochsTipText() {
        return "The number of epochs in convergence phase.";
    }

    /**
     * @return a string to describe the number of epochs in ordering phase option.
     */
    public String orderingEpochsTipText() {
        return "The number of epochs in ordering phase.";
    }

    /**
     * @return a string to describe the normalize attributes option.
     */
    public String normalizeAttributesTipText() {
        return "This will normalize the attributes.";
    }

    /**
     * This will return a string describing the clusterer.
     * @return The string.
     */
    public String globalInfo() {
        return "A Clusterer that implements Kohonen's Self Organizing Map\n"
                + "algorithm for unsupervised clustering.";
    }

    /**
     * Returns the revision string.
     *
     * @return		the revision
     */
    public String getRevision() {
        return RevisionUtils.extract("$Revision: 4 $");
    }

    /**
     * The constructor.
     */
    public SelfOrganizingMap() {
        m_clusters = null;
        m_width = 2;
        m_height = 2;
        m_convergenceEpochs = 1000;
        m_orderingEpochs = 2000;
        m_learningRate = 1.0;
        m_normalizeAttributes = true;
        m_calcStats = true;
    }

    /**
     * Returns default capabilities of the classifier.
     *
     * @return      the capabilities of this classifier
     */
    public Capabilities getCapabilities() {
        Capabilities result = super.getCapabilities();
        result.disableAll();
        result.enable(Capability.NO_CLASS);

        // attributes
        result.enable(Capability.NUMERIC_ATTRIBUTES);
        result.enable(Capability.NOMINAL_ATTRIBUTES);
        result.enable(Capability.MISSING_VALUES);
        
        return result;
    }

    /**
     * Parses a given list of options. <p/>
     *
    <!-- options-start -->
     * Valid options are: <p/>
     *
     * <pre> -L &lt;initial learning rate&gt;
     *  The initial learning rate for the training algorithm.
     *  (Value should be greater than 0.01 and less or equal to 1, Default = 1).</pre>
     *
     * <pre> -O  &lt;number of epochs in ordering phase&gt;
     *  Number of epochs in ordering phase.
     *  (Value should be greater than or equal to 2000, Default = 2000).</pre>
     *
     * <pre> -C &lt;number of epochs in convergence phase&gt;
     *  Number of epochs in convergence phase.
     *  (Value should be greater than or equal to 1000, Default = 1000).</pre>
     *
     * <pre> -I
     *  Normalizing the attributes will NOT be done.
     *  (Set this to not normalize the attributes).</pre>
     *
     * <pre> -H &lt;height of lattice&gt;
     *  The height of lattice.
     *  (Value should be &gt; 0, Default = 2).</pre>
     *
     * <pre> -W &lt;width of lattice&gt;
     *  The width of lattice.
     *  (Value should be &gt; 0, Default = 2).</pre>
     *
     * <pre> -S
     *  Statistics will NOT be calculated after training.
     *  (Set this to not calculate statistics).</pre>
     *
    <!-- options-end -->
     *
     * @param options the list of options as an array of strings
     * @throws Exception if an option is not supported
     */
    public void setOptions(String[] options) throws Exception {
        //the defaults can be found here!!!!
        String learningString = Utils.getOption('L', options);
        if (learningString.length() != 0) {
            setLearningRate((new Double(learningString)).doubleValue());
        } else {
            setLearningRate(1);
        }
        String orderingEpochsString = Utils.getOption('O', options);
        if (orderingEpochsString.length() != 0) {
            setOrderingEpochs(Integer.parseInt(orderingEpochsString));
        } else {
            setOrderingEpochs(2000);
        }
        String convergenceEpochsString = Utils.getOption('C', options);
        if (convergenceEpochsString.length() != 0) {
            setConvergenceEpochs(Integer.parseInt(convergenceEpochsString));
        } else {
            setConvergenceEpochs(1000);
        }
        String heightString = Utils.getOption('H', options);
        if (heightString.length() != 0) {
            setHeight(Integer.parseInt(heightString));
        } else {
            setHeight(2);
        }
        String widthString = Utils.getOption('W', options);
        if (widthString.length() != 0) {
            setWidth(Integer.parseInt(widthString));
        } else {
            setWidth(2);
        }
        if (Utils.getFlag('I', options)) {
            setNormalizeAttributes(false);
        } else {
            setNormalizeAttributes(true);
        }
        if (Utils.getFlag('S', options)) {
            setCalcStats(false);
        } else {
            setCalcStats(true);
        }
        Utils.checkForRemainingOptions(options);
    }

    /**
     * Gets the current settings of NeuralNet.
     *
     * @return an array of strings suitable for passing to setOptions()
     */
    public String[] getOptions() {
        
        String[] options = new String[12];
        int current = 0;
        options[current++] = "-L";
        options[current++] = "" + getLearningRate();
        options[current++] = "-O";
        options[current++] = "" + getOrderingEpochs();
        options[current++] = "-C";
        options[current++] = "" + getConvergenceEpochs();
        options[current++] = "-H";
        options[current++] = "" + getHeight();
        options[current++] = "-W";
        options[current++] = "" + getWidth();
        if (!getNormalizeAttributes()) {
            options[current++] = "-I";
        }
        if (!getCalcStats()) {
            options[current++] = "-S";
        }
        while (current < options.length) {
            options[current++] = "";
        }
        return options;
    }

    /**
     * Classifies a given instance.
     *
     * @param i the instance to be assigned to a cluster
     * @return the number of the assigned cluster as an interger
     * if the class is enumerated, otherwise the predicted value
     * @throws Exception if instance could not be classified
     * successfully
     */
    public int clusterInstance(Instance i) throws Exception {
        if ((m_clusters == null) || (m_instances == null)) {
            return 0;
        }
        
        Instance instance = new DenseInstance(i);
        
        if (m_normalizeAttributes) {
            instance = normalizeInstance(instance);
        }
        
        int a = m_euclideanDistance.closestPoint(instance, m_clusters, m_clusterList);
        
        return a;
    }

    /**
     * Returns an enumeration describing the available options.
     *
     * @return an enumeration of all the available options.
     */
    public Enumeration listOptions() {
        Vector result = new Vector();
        
        result.addElement(new Option(
                "\tLearning Rate for the training algorithm.\n"
                + "\t(default 1)",
                "L", 1, "-L <num>"));
        
        result.addElement(new Option(
                "\tNumber of epochs in ordering phase.\n"
                + "\t(default 2000)",
                "O", 1, "-O <num>"));
        
        result.addElement(new Option(
                "\tNumber of epochs in convergence phase.\n"
                + "\t(default 1000)",
                "C", 1, "-C <num>"));
        
        result.addElement(new Option(
                "\tThe height of lattice.\n"
                + "\t(default 1)",
                "H", 1, "-H <num>"));
        
        result.addElement(new Option(
                "\tThe width of lattice.\n"
                + "\t(default 1)",
                "W", 1, "-W <num>"));
        
        result.addElement(new Option(
                "\tNormalizing the attributes will NOT be done.\n"
                + "\t(Set this to not normalize the attributes).",
                "I", 0, "-I"));
        
        return result.elements();
    }
    
    private String pad(String source, String padChar,
            int length, boolean leftPad) {
        StringBuffer temp = new StringBuffer();
        
        if (leftPad) {
            for (int i = 0; i < length; i++) {
                temp.append(padChar);
            }
            temp.append(source);
        } else {
            temp.append(source);
            for (int i = 0; i < length; i++) {
                temp.append(padChar);
            }
        }
        return temp.toString();
    }

    /**
     * return a string describing this clusterer.
     *
     * @return a description of the clusterer as a string
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nSelf Organized Map\n==================\n");
        if ((m_clusters == null) || (m_instances == null)) {
            sb.append("No clusterer built yet!\n");
            sb.append("==================\n\n");
            return sb.toString();
        }
        
        sb.append("\nNumber of clusters: " + (m_width * m_height) + "\n");
        
        int maxWidth = 0;
        int maxAttWidth = 0;
        
        if (m_calcStats) {
            // set up max widths
            // attributes
            for (int i = 0; i < m_clusters.numAttributes(); i++) {
                Attribute a = m_clusters.attribute(i);
                if (a.name().length() > maxAttWidth) {
                    maxAttWidth = m_clusters.attribute(i).name().length();
                }
            }
            for (int i = 0; i < m_clusters.numInstances(); i++) {
                for (int j = 0; j < m_clusters.numAttributes(); j++) {
                    // check mean and std. dev. against maxWidth
                    double mean = Math.log(Math.abs(m_clusterStats[j][i][2])) / Math.log(10.0);
                    double stdD = Math.log(Math.abs(m_clusterStats[j][i][3])) / Math.log(10.0);
                    double width = (mean > stdD)
                            ? mean
                            : stdD;
                    if (width < 0) {
                        width = 1;
                    }
                    // decimal + # decimal places + 1
                    width += 6.0;
                    if ((int) width > maxWidth) {
                        maxWidth = (int) width;
                    }
                }
            }
            
            if (maxAttWidth < "Attribute".length()) {
                maxAttWidth = "Attribute".length();
            }
            
            maxAttWidth += 2;
            
            sb.append("\n\n");
            sb.append(pad("Cluster", " ",
                    (maxAttWidth + maxWidth + 1) - "Cluster".length(),
                    true));
            
            sb.append("\n");
            sb.append(pad("Attribute", " ", maxAttWidth - "Attribute".length(), false));

            // cluster #'s
            for (int i = 0; i < m_clusters.numInstances(); i++) {
                String classL = "" + i;
                sb.append(pad(classL, " ", maxWidth + 1 - classL.length(), true));
            }
            sb.append("\n");
            
            sb.append(pad("", " ", maxAttWidth, true));
            for (int i = 0; i < m_clusters.numInstances(); i++) {
                String numInst = Utils.doubleToString(m_clusterInstances[i].numInstances(), maxWidth, 2).trim();
                numInst = "(" + numInst + ")";
                sb.append(pad(numInst, " ", maxWidth + 1 - numInst.length(), true));
            }
            
            sb.append("\n");
            sb.append(pad("", "=", maxAttWidth
                    + (maxWidth * m_clusters.numInstances())
                    + m_clusters.numInstances() + 1, true));
            sb.append("\n");
            
            for (int i = 0; i < m_clusters.numAttributes(); i++) {
                String attName = m_clusters.attribute(i).name();
                sb.append(attName + "\n");
                
                String valueL = "  value";
                sb.append(pad(valueL, " ", maxAttWidth + 1 - valueL.length(), false));
                for (int j = 0; j < m_clusters.numInstances(); j++) {
                    // values
                    String value =
                            Utils.doubleToString(denormalizeInstance(m_clusters.get(j)).value(i), maxWidth, 4).trim();
                    sb.append(pad(value, " ", maxWidth + 1 - value.length(), true));
                }
                sb.append("\n");
                String minL = "  min";
                sb.append(pad(minL, " ", maxAttWidth + 1 - minL.length(), false));
                for (int j = 0; j < m_clusters.numInstances(); j++) {
                    // means
                    String min =
                            Utils.doubleToString(m_clusterStats[i][j][0], maxWidth, 4).trim();
                    sb.append(pad(min, " ", maxWidth + 1 - min.length(), true));
                }
                sb.append("\n");
                String maxL = "  max";
                sb.append(pad(maxL, " ", maxAttWidth + 1 - maxL.length(), false));
                for (int j = 0; j < m_clusters.numInstances(); j++) {
                    // means
                    String max =
                            Utils.doubleToString(m_clusterStats[i][j][1], maxWidth, 4).trim();
                    sb.append(pad(max, " ", maxWidth + 1 - max.length(), true));
                }
                sb.append("\n");
                String meanL = "  mean";
                sb.append(pad(meanL, " ", maxAttWidth + 1 - meanL.length(), false));
                for (int j = 0; j < m_clusters.numInstances(); j++) {
                    // means
                    String mean =
                            Utils.doubleToString(m_clusterStats[i][j][2], maxWidth, 4).trim();
                    sb.append(pad(mean, " ", maxWidth + 1 - mean.length(), true));
                }
                sb.append("\n");
                // now do std deviations
                String stdDevL = "  std. dev.";
                sb.append(pad(stdDevL, " ", maxAttWidth + 1 - stdDevL.length(), false));
                for (int j = 0; j < m_clusters.numInstances(); j++) {
                    String stdDev =
                            Utils.doubleToString(m_clusterStats[i][j][3], maxWidth, 4).trim();
                    sb.append(pad(stdDev, " ", maxWidth + 1 - stdDev.length(), true));
                }
                sb.append("\n\n");
            }
        }
        
        return sb.toString();
    }

    /**
     * Generates a clusterer. Has to initialize all fields of the clusterer
     * that are not being set via options.
     *
     * @param data set of instances serving as training data
     * @throws Exception if the clusterer has not been
     * generated successfully
     */
    public void buildClusterer(Instances data) throws Exception {
        // can clusterer handle the data?
        getCapabilities().testWithFail(data);

        // copy the original instances
        m_instances = new Instances(data);

        // normalize instances
        m_instances = normalize(m_instances);

        // init clusters
        m_clusters = initClusters();

        // init the pointList (used in EuclideanDistance.closestPoint)
        m_clusterList = new int[m_clusters.numInstances()];
        for (int i = 0; i < m_clusterList.length; i++) {
            m_clusterList[i] = i;
        }

        // the actual learning rate
        double learningRate;
        // the actual effective neighborhood
        double effectiveWidth;
        // init euclidean distance
        m_euclideanDistance.setDontNormalize(true);
        m_euclideanDistance.setInstances(m_clusters);
        // the winner neuron
        int winningNeuron;
        
        for (int epoch = 1; epoch <= m_convergenceEpochs + m_orderingEpochs; epoch++) {
            learningRate = calcLearningRate(epoch);
            effectiveWidth = calcEffectiveWidth(epoch);
            for (int instance = 0; instance < m_instances.numInstances(); instance++) {
                winningNeuron = m_euclideanDistance.closestPoint(m_instances.get(instance), m_clusters, m_clusterList);

                // update the weights
                for (int neuron = 0; neuron < m_clusters.numInstances(); neuron++) {
                    updateWeights(neuron, winningNeuron, instance, learningRate, effectiveWidth);
                }
            }
        }
        
        if (m_calcStats) {
            calcStatistics();
        }
    }

    /**
     * This function calculates the clusterer's statistics
     */
    private void calcStatistics() {
        // init cluster statistics
        m_clusterStats = new double[m_instances.numAttributes()][m_width * m_height][4];

        // init clusters assignements
        m_clusterInstances = new Instances[m_width * m_height];

        // keep cluster's attributes
        for (int i = 0; i < m_clusters.numInstances(); i++) {
            m_clusterInstances[i] = new Instances(m_instances);
            m_clusterInstances[i].clear();
            
            try {
                Instances clusters = getClusters();
                for (int j = 0; j < clusters.numAttributes(); j++) {
                    m_clusterStats[j][i][0] = clusters.get(i).value(j);
                }
            } catch (Exception ex) {
            }
        }

        // get instances in each class
        for (int instance = 0; instance < m_instances.numInstances(); instance++) {
            Instance inst = m_instances.get(instance);
            int cluster = -1;
            try {
                cluster = clusterInstance(denormalizeInstance(inst));
                
            } catch (Exception ex) {
            }
            if (cluster != -1) {
                m_clusterInstances[cluster].add(denormalizeInstance(inst));
            }
        }

        //calc min, max, mean, stdev for each cluster
        for (int cluster = 0; cluster < m_clusters.numInstances(); cluster++) {
            for (int attr = 0; attr < m_clusters.numAttributes(); attr++) {
                int unknownValues = 0;
                double min = Double.POSITIVE_INFINITY;
                double max = Double.NEGATIVE_INFINITY;
                double mean = 0;
                double stdev = 0;
                for (int instance = 0; instance < m_clusterInstances[cluster].numInstances(); instance++) {
                    Instance inst = m_clusterInstances[cluster].get(instance);
                    if (!Double.isNaN(inst.value(attr))) {
                        mean += inst.value(attr);
                        if (inst.value(attr) < min) {
                            min = inst.value(attr);
                        }
                        if (inst.value(attr) > max) {
                            max = inst.value(attr);
                        }
                    } else {
                        unknownValues++;
                    }
                }
                mean /= (m_clusterInstances[cluster].numInstances() - unknownValues);
                for (int instance = 0; instance < m_clusterInstances[cluster].numInstances(); instance++) {
                    Instance inst = m_clusterInstances[cluster].get(instance);
                    if (!Double.isNaN(inst.value(attr))) {
                        stdev += (inst.value(attr) - mean) * (inst.value(attr) - mean);
                    }
                }
                stdev /= (m_clusterInstances[cluster].numInstances() - 1 - unknownValues);
                stdev = Math.sqrt(stdev);
                if (min == Double.POSITIVE_INFINITY) {
                    min = 0;
                }
                if (max == Double.NEGATIVE_INFINITY) {
                    max = 0;
                }
                if((m_clusterInstances[cluster].numInstances() - unknownValues)==0) {
                    min = Double.NaN;
                    max = Double.NaN;
                    stdev = Double.NaN;
                }
                
                m_clusterStats[attr][cluster][0] = min;
                m_clusterStats[attr][cluster][1] = max;
                m_clusterStats[attr][cluster][2] = mean;
                m_clusterStats[attr][cluster][3] = stdev;
            }
        }
    }

    /**
     * This function updates the weights of a cluster
     * @param updateCluster the cluster to update
     * @param winningCluster the winning cluster for the current training instance
     * @param instance the index of current training instance
     * @param learningRate the current learning rate
     * @param effWidth the current effective width
     */
    private void updateWeights(int updateCluster, int winningCluster, int instance, double learningRate, double effWidth) {
        double diff;
        int winnerWidth = winningCluster % m_width;
        int winnerHeight = winningCluster / m_width;
        int width = updateCluster % m_width;
        int height = updateCluster / m_width;
        
        double distance = Math.pow((winnerWidth - width), 2) + Math.pow((winnerHeight - height), 2);
        double h = Math.exp(-distance / (2 * Math.pow(effWidth, 2)));
        for (int j = 0; j < m_clusters.numAttributes(); j++) {
            diff = learningRate * h * (m_instances.get(instance).value(j) - m_clusters.get(updateCluster).value(j));
            if (!Double.isNaN(diff)) {
                m_clusters.get(updateCluster).setValue(j, m_clusters.get(updateCluster).value(j) + diff);
            }
        }
    }

    /**
     * This function calculates the effective width of the topological
     * netowork for the current iteration
     * @param iteration the current iteration.
     * @return the effective width .
     */
    private double calcEffectiveWidth(int iteration) {
        double effectiveWidth;
        
        if (iteration <= m_orderingEpochs) {
            // Ordering phase
            double latticeRadius = Math.sqrt(m_width * m_width + m_height * m_height) / 2;
            effectiveWidth = latticeRadius * Math.exp(-iteration * Math.log(latticeRadius) / m_orderingEpochs);
        } else {
            // Convergence phase
            effectiveWidth = 0.0001;
        }
        
        return effectiveWidth;
    }

    /**
     * This function calculates the learning rate for the current iteration
     * @param iteration the current iteration.
     * @return the learning rate.
     */
    private double calcLearningRate(int iteration) {
        double learningRate;
        
        if (iteration <= m_orderingEpochs) {
            // Ordering phase
            learningRate = m_learningRate * Math.exp(-(double) iteration * Math.log(100. * m_learningRate) / m_orderingEpochs);
        } else {
            // Convergence phase
            learningRate = 0.01;
        }
        
        return learningRate;
    }

    /**
     * This function performs the denormalization of the attributes of an instance.
     *
     * @param inst the instance.
     * @return The modified instance. This needs to be done as it deep copies
     * the instance which will need to be passed back out.
     */
    protected Instance denormalizeInstance(Instance inst) {
        inst = new DenseInstance(inst);
        if (m_normalizeAttributes) {
            for (int noa = 0; noa < inst.numAttributes(); noa++) {
                inst.setValue(noa, (inst.value(noa) * (m_attributeMax[noa] - m_attributeMin[noa]) + (m_attributeMax[noa] + m_attributeMin[noa])) / 2);
            }
        }
        return inst;
    }

    /**
     * This function performs the normalization of the attributes of an instance.
     *
     * @param inst the instance.
     * @return The modified instance. This needs to be done as it deep copies
     * the instance which will need to be passed back out.
     */
    protected Instance normalizeInstance(Instance inst) {
        inst = new DenseInstance(inst);
        double min;
        double max;
        for (int noa = 0; noa < m_instances.numAttributes(); noa++) {
            if (inst.value(noa) > m_attributeMax[noa]) {
                max = inst.value(noa);
            } else {
                max = m_attributeMax[noa];
            }
            if (inst.value(noa) < m_attributeMin[noa]) {
                min = inst.value(noa);
            } else {
                min = m_attributeMin[noa];
            }
            if ((max - min) != 0) {
                inst.setValue(noa, -1 + 2 * (inst.value(noa) - min) / (max - min));
            } else {
                inst.setValue(noa, inst.value(noa));
            }
        }
        return inst;
    }

    /**
     * This function performs the normalization of the attributes if applicable.
     * (note that regardless of the options it will fill an array with the range
     * and base, set to normalize all attributes and the class to be between -1
     * and 1)
     * @param inst the instances.
     * @return The modified instances. This needs to be done. If the attributes
     * are normalized then deep copies will be made of all the instances which
     * will need to be passed back out.
     */
    private Instances normalize(Instances inst) throws Exception {
        if (inst != null) {
            inst = new Instances(inst);
            // x bounds
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            double value;
            m_attributeMax = new double[inst.numAttributes()];
            m_attributeMin = new double[inst.numAttributes()];
            
            for (int noa = 0; noa < inst.numAttributes(); noa++) {
                min = Double.POSITIVE_INFINITY;
                max = Double.NEGATIVE_INFINITY;
                for (int i = 0; i < inst.numInstances(); i++) {
                    if (!inst.instance(i).isMissing(noa)) {
                        value = inst.instance(i).value(noa);
                        if (value < min) {
                            min = value;
                        }
                        if (value > max) {
                            max = value;
                        }
                    }
                }
                
                m_attributeMax[noa] = max;
                m_attributeMin[noa] = min;
            }
        }
        
        if (m_normalizeAttributes) {
            for (int i = 0; i < inst.numInstances(); i++) {
                inst.set(i, normalizeInstance(inst.instance(i)));
            }
        }
        
        return inst;
    }

    /**
     * This function initializes the clusters' weights.
     *
     * @return  The initialized clusters
     */
    protected Instances initClusters() {
        Instances weights = new Instances(m_instances, m_width * m_height);
        for (int i = 0; i < m_width * m_height; i++) {
            double[] instValues = new double[m_instances.numAttributes()];
            for (int j = 0; j < m_instances.numAttributes(); j++) {
                if (m_normalizeAttributes) {
                    instValues[j] = 0;
                } else {
                    instValues[j] = (m_attributeMax[j] + m_attributeMin[j]) / 2;
                }
            }
            Instance inst = new DenseInstance(1, instValues);
            weights.add(i, inst);
        }
        
        return weights;
    }

    /**
     * Returns the number of clusters.
     *
     * @return the number of clusters generated for a training dataset.
     * @exception Exception if number of clusters could not be returned
     * successfully
     */
    public int numberOfClusters() throws Exception {
        return m_height * m_width;
    }

    /**
     * This function returns the clusters if the clusterer is build
     * or an exception if the clusterer is not build.
     * 
     * The clusters are returned dernomalized even if normalizeAttributes
     * option is set.
     *
     * @return The clusters
     * @throws Exception
     */
    public Instances getClusters() throws Exception {
        if (m_clusters == null) {
            throw new Exception("No clusterer built yet!");
        }
        Instances inst = new Instances(m_clusters);
        if (m_normalizeAttributes) {
            for (int i = 0; i < inst.numInstances(); i++) {
                inst.set(i, denormalizeInstance(inst.instance(i)));
            }
        }
        
        return inst;
    }

    /**
     * This function returns the training statistics in a 3-dimension array
     * as follows:<br/>
     * First dimension: the attribute index<br/>
     * Second dimension: the cluster index<br/>
     * Third dimension: the static index (Valid values are 0: min, 1: max, 2: mean, 3: st. dev.)
     *
     * @return the statistics array
     * @throws Exception
     */
    public double[][][] getStatistics() throws Exception {
        if (m_calcStats) {
            if (m_clusterStats == null) {
                throw new Exception("No clusterer built yet!");
            }
        } else {
            throw new Exception("Statistics are not calculated");
        }
        return m_clusterStats;
    }

    /**
     * This function returns the cluster assignment for each of the
     * training instances. The array's index indicates the corresponding
     * cluster.
     *
     * @return the cluster assignments array
     * @throws Exception
     */
    public Instances[] getClusterInstances() throws Exception {
        if (m_calcStats) {
            if (m_clusterInstances == null) {
                throw new Exception("No clusterer built yet!");
            }
        } else {
            throw new Exception("Statistics are not calculated");
        }
        return m_clusterInstances;
    }
}
