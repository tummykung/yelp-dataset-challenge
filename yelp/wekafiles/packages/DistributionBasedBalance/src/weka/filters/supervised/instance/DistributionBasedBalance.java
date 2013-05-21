
package weka.filters.supervised.instance;

import weka.core.Instances;
import weka.core.DenseInstance;
import weka.core.Range;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Tag;
import weka.core.SelectedTag;
import weka.core.Utils;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.filters.unsupervised.instance.RemoveWithValues;
import weka.filters.Filter;
import weka.filters.SupervisedFilter;
import weka.core.Instance;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Random;

/**
 * <!-- globalinfo-start --> Re-samples a dataset for each class label selected
 * with option -L. Instances are re-sampled using a selected distribution which
 * is learned for each pair <attribute,instance label>. For more information,
 * see <br/>
 * <br/>
 * Pablo Bermejo et. al. Improving the performance of Naive Bayes
 * Multinomial in e-mail foldering by introducing distribution-based balance of
 * datasets. Expert Systems With Applications. Volume 38 Issue 3, pages 2072-2080. March 2011.
 * <p/>
 * <!-- globalinfo-end -->
 * 
 * <!-- technical-bibtex-start --> BibTeX:
 * 
 * <pre>
 * &#64;article{BermejoDBB,
 *    author = {Pablo Bermejo and Jose A. Gamez and Jose M. Puerta},
 *    journal = {Expert Systems With Applications},
 *    pages = {2072--2080},
 *    title = {Improving the performance of Naive Bayes Multinomial in e-mail foldering by introducing distribution-based balance of datasets},
 *    volume = {38-3},
 *    year = {2011}
 * }
 * </pre>
 * <p/>
 * <!-- technical-bibtex-end -->
 * 
 * <!-- options-start --> Valid options are:
 * <p/>
 * 
 * <pre>
 * -D <num>
 * Specifies the distribution to learn from training set .
 * (default 0: GAUSSIAN_BALANCE)
 * </pre>
 * 
 * 
 * <pre>
 * -L &lt;col1,col2-col4,...&gt
 * Specifies the indexes of class label to balance. first, last and all are allowed
 * (default all)
 * </pre>
 * 
 * <pre>
 * -I (true|false)
 *  Specifies if class labels indexes are to be inverted."
 *  (default false)
 * </pre>
 * 
 * <pre>
 * -N (true|false)
 *  Specifies if sampled values for attributes are allowed to be negative
 *  (default false)
 * </pre>
 * 
 * <pre>
 * -P <num>
 *  Specifies the number of instances to sample per class label.
 *  (default 30)
 * </pre>
 * 
 * <pre>
 * -S <num>
 *   Seed for values generation from learned distributions.
 * </pre>
 * 
 * <pre>
 * -X <num>
 *   Specifies if fast aproximated sampling of poission values is allowed
 *  (default true)
 * </pre>
 * 
 * <!-- options-end -->
 * 
 * @author Pablo Bermejo (Pablo.Bermejo@uclm.es)
 * @version $Revision: 1.0 $
 */
public class DistributionBasedBalance extends Filter implements
		SupervisedFilter, OptionHandler, TechnicalInformationHandler {

	/** distribution to learn in order to re-sample new instances */
	private int m_balanceType = GAUSSIAN_BALANCE;
	/** number P of instances to re-sample per class label */
	private int m_P = 30;
	/**
	 * indicate if negative values are to be sampled. if false, sampled values
	 * are 0 as minimum
	 */
	private boolean m_allowNegativeValues = false;
	/** seed use for numbers generation */
	private int m_seed = (new Random()).nextInt();
	/**
	 * indicates if sampling from a poisson distribution can be using an
	 * approximate way which reduces the sampling time and gets similar sampled
	 * values
	 */
	private boolean m_allowPoissonApproximation = true;
	/** range of selected label indexes to balance */
	private Range m_labelsRange = new Range("first-last");
	/** stores the time spent (milliseconds) in learning the distribution */
	private double m_statisticsTime_ms;
	/** stores the time spent (milliseconds) in sammpling the new instances */
	private double m_samplingTime_ms;
	/** used when MULTINOMIAL_BALANCE is selected */
	private int[] m_totalInstancesPerClass;
	/** learn and sample Uniform Distribution */
	public static final int UNIFORM_BALANCE = 0;
	/** learn and sample Gaussian Distribution */
	public static final int GAUSSIAN_BALANCE = 1;
	/** learn and sample Poisson Distribution */
	public static final int POISSON_BALANCE = 2;
	/** learn and sample Multinomial Distribution */
	public static final int MULTINOMIAL_BALANCE = 3;
	public static final Tag[] TAGS_BALANCE = {
			new Tag(UNIFORM_BALANCE, "Learn Uniform Distribution."),
			new Tag(GAUSSIAN_BALANCE, "Learn Gaussian Distribution."),
			new Tag(POISSON_BALANCE, "Learn Poisson Distribution."),
			new Tag(MULTINOMIAL_BALANCE, "Learn Multinomial Distribution."), };

	static final long serialVersionUID = -1542684266954712587L;

	/**
	 * The constructor.
	 */
	public DistributionBasedBalance() {
		resetOptions();
	}

	/**
	 * 
	 * @return a description of the filter suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String globalInfo() {
		return "Re-samples with replacement instances tagged with class labels specified"
				+ "by the user. Sampling of instances is performed following a distribution learned"
				+ "for each pair <attribute,class label>. For more information, see \n\n"
				+ getTechnicalInformation().toString();
	}

	/**
	 * @return Capabilities of this filter
	 */
	@Override
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
		result.disableAllAttributes();
		result.disableAllAttributeDependencies();
		result.enable(Capability.NUMERIC_ATTRIBUTES);
		result.enable(Capability.MISSING_VALUES);

		result.disable(Capability.MISSING_CLASS_VALUES);
		result.disableAllClasses();
		result.disableAllClassDependencies(); 
		result.enable(Capability.NOMINAL_CLASS);
		return result;
	}

	/**
	 * Returns an instance of a TechnicalInformation object, containing detailed
	 * information about the technical background of this class, e.g., paper
	 * reference or book this class is based on.
	 * 
	 * @return the technical information about this class
	 */
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result = new TechnicalInformation(Type.ARTICLE);

		result.setValue(Field.AUTHOR, "Pablo Bermejo et al.");
		result.setValue(
				Field.TITLE,
				"Improving the performance of Naive Bayes Multinomial"
						+ " in e-mail  foldering by introducing distribution-based balance of datasets");
		result.setValue(Field.JOURNAL, "Expert Systems With Applications");
		result.setValue(Field.YEAR, "2011");
		result.setValue(Field.VOLUME, "38-3");
		result.setValue(Field.PAGES, " 2072-2080");

		return result;
	}

	/**
	 * pushes new instances on the filter output stack sampled from the selected
	 * distribution
	 * 
	 * @param values
	 *            results from calling computeStats(). its meaning depends on
	 *            m_balanceType
	 * @throws Exception
	 *             if sth goes wrong
	 */
	private void resampleFromDistribution(float[][][] values) throws Exception {
		Random random = new Random(m_seed);
		Instances format = getInputFormat();
		int labelPointer = 0;
		int[] classLabels = m_labelsRange.getSelection();
		for (int l : classLabels) {
			double[][] instancesValues = new double[m_P][format.numAttributes()];
			for (int a = 0; a < format.numAttributes() - 1; a++) {
				for (int p = 0; p < m_P; p++) {
					double x = -1;
					if (m_balanceType == UNIFORM_BALANCE) { // values[0] is max
															// and values[1] is
															// min
						x = (random.nextFloat() * (values[0][labelPointer][a] - values[1][labelPointer][a]))
								+ values[1][labelPointer][a];

					}
					if (m_balanceType == GAUSSIAN_BALANCE) { // values[0] is
																// mean and
																// values[1] is
																// stdDev
						x = random.nextGaussian() * values[1][labelPointer][a]
								+ values[0][labelPointer][a];

					}
					if (m_balanceType == POISSON_BALANCE) { // values[0] is mean
						float lambda = values[0][labelPointer][a];
						x = (lambda >= 30 && m_allowPoissonApproximation) ? nextPoissonApproximated(
								values[0][labelPointer][a], random)
								: nextPoisson(values[0][labelPointer][a],
										random);
					}

					if (Double.isInfinite(x))
						x = Double.NaN; // this happens when all att values are
										// ?
					if (x < 0 && !m_allowNegativeValues) {
						x = 0;
					} else {
						instancesValues[p][a] = x;
					}

				}
			}

			values[0][labelPointer] = null; // free unncessary data in memory
			if (m_balanceType != POISSON_BALANCE) {
				values[1][labelPointer] = null;// free unncessary data in memory

			}
			for (int p = 0; p < m_P; p++) {
				instancesValues[p][format.classIndex()] = l;
				push(new DenseInstance(1, instancesValues[p]));
			}
			labelPointer++;

		}

	}

	/**
	 * Pushes new instances on the filter output stack sampled from a
	 * multinomial distribution Appropiate for textual databases
	 * 
	 * @param values
	 *            results from calling computeStats(). values[0] is
	 *            probOfAttGivenClass[][] values[1] is counts[][1] of atts
	 *            values along each class
	 * @throws Exception
	 *             if sth goes wrong
	 */
	private void resampleFromMultinomialDistribution(float[][][] values)
			throws Exception {
		Random random = new Random(m_seed);
		Instances format = getInputFormat();
		float lambda = 0;
		int labelPointer = 0;
		int[] classLabels = m_labelsRange.getSelection();

		for (int l : classLabels) {
			double[][] instancesValues = new double[m_P][format.numAttributes()];
			if (m_totalInstancesPerClass[labelPointer] == 0) {
				lambda = 0;
			} else {
				lambda = (values[1][labelPointer][0])
						/ m_totalInstancesPerClass[labelPointer];
			}
			for (int p = 0; p < m_P; p++) {
				float[] probs = values[0][labelPointer];
				// get total counts of atts values(length of document)
				int trials = (lambda >= 30 && m_allowPoissonApproximation) ? nextPoissonApproximated(
						lambda, random) : nextPoisson(lambda, random);

				for (int t = 0; t < trials; t++) {
					int drawnAtt = drawAttribute(probs, random);
					instancesValues[p][drawnAtt]++;
				}
			}

			values[0][labelPointer] = null; // free unncessary data in memory
			values[1][labelPointer] = null;// free unncessary data in memory

			for (int p = 0; p < m_P; p++) {
				instancesValues[p][format.classIndex()] = l;
				push(new DenseInstance(1, instancesValues[p]));
			}
			labelPointer++;

		}

	}

	/**
	 * search for the first index i such that probs[i]>= r.nextFloat() a binary
	 * search is used since this is the bottleneck in multinomial resampling
	 * 
	 * @param probs
	 *            [] array of ordered values from 0 to 1
	 * @param r
	 *            Random object
	 * @return min index in probs[] such that probs[i]>= r.nextFloat()
	 */
	private int drawAttribute(float[] probs, Random r) {

		float v = r.nextFloat();
		int index = java.util.Arrays.binarySearch(probs, v);
		if (index < 0) {
			return -(index + 1); // place where v would fit in the ordered array
		} else {
			return index; // exact match found
		}
	}

	/**
	 * 
	 * @param lambda
	 *            mean of distribution
	 * @param random
	 *            Random object
	 * @return int generated from a poisson distribution with mean==lambda
	 */
	private int nextPoisson(float lambda, Random random) {

		float limit = -lambda;
		float product = (float) Math.log(random.nextFloat());
		int count;

		for (count = 0; product > limit; count++) {
			product += Math.log(random.nextFloat());
		}
		return count;
	}

	/**
	 * Approximation of poisson distribution so that sampling is faster See
	 * 'method PA' in The Computer Generation of Poisson Random Variables by A.
	 * C. Atkinson, Journal of the Royal Statistical Society Series C (Applied
	 * Statistics) Vol. 28, No. 1. (1979), pages 29-35.
	 * 
	 * @param lambda
	 *            mean of distribution
	 * @param random
	 *            Random object
	 * @return int generated from a poisson distribution with mean==lambda
	 */
	private int nextPoissonApproximated(float lambda, Random random) {
		float c = (float) (0.767 - 3.36 / lambda);
		float beta = ((float) (Math.PI / Math.sqrt(3.0 * lambda)));
		float alpha = beta * lambda;
		float k = (float) (Math.log(c) - lambda - Math.log(beta));

		while (true) {
			float u = random.nextFloat();
			float x = (float) (alpha - Math.log((1.0 - u) / u)) / beta;
			int n = (int) Math.floor(x + 0.5);
			if (n < 0) {
				continue;
			}
			float v = random.nextFloat();
			float y = alpha - beta * x;
			float lhs = (float) (y + Math.log(v
					/ Math.pow((1.0 + Math.exp(y)), 2)));
			float rhs = (float) (k + n * Math.log(lambda) - lnFactorial(n));
			if (lhs <= rhs) {
				return n;
			}
		}

	}

	/**
	 * Fast computation of ln(n!) for non-negative ints
	 * 
	 * negative ints are passed on to the general gamma-function based version
	 * in weka.core.SpecialFunctions
	 * 
	 * if the current n value is higher than any previous one, the cache is
	 * extended and filled to cover it
	 * 
	 * the common case is reduced to a simple array lookup
	 * 
	 * @param n
	 *            the integer
	 * @return ln(n!)
	 */
	protected double lnFactorial(int n) {
		double[] m_lnFactorialCache = new double[] { 0.0, 0.0 };

		if (n < 0) {
			return weka.core.SpecialFunctions.lnFactorial(n);
		}

		if (m_lnFactorialCache.length <= n) {
			double[] tmp = new double[n + 1];
			System.arraycopy(m_lnFactorialCache, 0, tmp, 0,
					m_lnFactorialCache.length);
			for (int i = m_lnFactorialCache.length; i < tmp.length; i++) {
				tmp[i] = tmp[i - 1] + Math.log(i);
			}
			m_lnFactorialCache = tmp;
		}

		return m_lnFactorialCache[n];
	}

	/**
	 * If m_balanceType==UNIFORM_BALANCE then computes the max (values[0]) and
	 * min (values[1]) values for each selected label and all atts if
	 * m_balanceType==GAUSSIAN_BALANCE then computes the mean (values[0]) and
	 * variance (values[1]) for each selected label and all atts if
	 * m_balanceType==POISSON_BALANCE then computes the mean (values[0]) for
	 * each selected label and all atts
	 * 
	 * @return double[][][] with stats for each selected label and all atts
	 */
	private float[][][] computeStats() throws Exception {
		Instances data = getInputFormat();
		int[] classLabels = m_labelsRange.getSelection();

		float[][][] values = new float[2][classLabels.length][data
				.numAttributes() - 1];
		float[][] countsPerClass = null;// for MULTINOMIAL_BALANCE
		float[][] probOfAttGivenClass = null;// for MULTINOMIAL_BALANCE

		if (m_balanceType == POISSON_BALANCE) {
			values[1] = null;
		}
		if (m_balanceType == MULTINOMIAL_BALANCE) {
			m_totalInstancesPerClass = data.attributeStats(data.classIndex()).nominalCounts;
			countsPerClass = new float[classLabels.length][1]; // java
																// internally
																// inits it with
																// 0s
			probOfAttGivenClass = new float[classLabels.length][data
					.numAttributes() - 1];
		}

		for (int l = 0; l < classLabels.length; l++) {

			// remove all instance without current class label
			RemoveWithValues remove = new RemoveWithValues();
			remove.setInvertSelection(true);
			remove.setAttributeIndex("" + (data.classIndex() + 1));// indexes
																	// start by
																	// 1
			remove.setNominalIndices("" + (classLabels[l] + 1));
			remove.setInputFormat(data);
			Instances tempData = Filter.useFilter(data, remove);// indexes start
																// by 1

			// get statistics for each attribute given current class label
			for (int a = 0; a < data.numAttributes() - 1; a++) {
				if (m_balanceType == UNIFORM_BALANCE) {
					values[0][l][a] = (float) tempData.attributeStats(a).numericStats.max;
					values[1][l][a] = (float) tempData.attributeStats(a).numericStats.min;
				}

				if (m_balanceType == GAUSSIAN_BALANCE) {
					values[0][l][a] = (float) tempData.attributeStats(a).numericStats.mean;
					values[1][l][a] = (float) tempData.attributeStats(a).numericStats.stdDev;

				}
				if (m_balanceType == POISSON_BALANCE) {
					values[0][l][a] = (float) tempData.attributeStats(a).numericStats.mean;
					values[1] = null;
				}

				if (m_balanceType == MULTINOMIAL_BALANCE) {
					for (int i = 0; i < tempData.numInstances(); i++) {
						float value = (float) (tempData.instance(i).value(a) * tempData
								.instance(i).weight());
						countsPerClass[l][0] += value;
						probOfAttGivenClass[l][a] += value;
					}
				}

			}
			// all instances tagged with labels selected for balancing will be
			// re-sampled with replacement so they are removed
			remove.setInvertSelection(false);
			data = Filter.useFilter(data, remove);

		}

		if (m_balanceType == MULTINOMIAL_BALANCE) {
			/* normalising probOfAttsGivenClass values */
			for (int l = 0; l < classLabels.length; l++) {
				for (int a = 0; a < data.numAttributes() - 1; a++) {
					probOfAttGivenClass[l][a] = probOfAttGivenClass[l][a]
							/ countsPerClass[l][0];
				}
				/* computing cumulative probs */
				for (int a = 0; a < data.numAttributes() - 2; a++) {
					probOfAttGivenClass[l][a + 1] = probOfAttGivenClass[l][a]
							+ probOfAttGivenClass[l][a + 1];
				}
			}

			values[0] = probOfAttGivenClass;
			values[1] = countsPerClass;
		}

		// instances tagged with non-selected class labels will remain the same
		Enumeration<Instance> untouchedClassInstances = data
				.enumerateInstances();
		while (untouchedClassInstances.hasMoreElements()) {
			push(untouchedClassInstances.nextElement());
		}

	/*	for (int l = 0; l < data.numClasses(); l++) {
			for (int a = 0; a < data.numAttributes() - 1; a++) {
				System.out.print("[" + values[0][l][a] + "][" + values[1][l][a]
						+ "]\t");
			}
			System.out.println();
		}*/
		return values;
	}

	/**
	 * Parses a given list of options. Should be called after setInputFormat()
	 * <p/>
	 * 
	 * <!-- options-start --> Valid options are:
	 * <p/>
	 * 
	 * <pre>
	 * -D <num>
	 * Specifies the distribution to learn from training set .
	 * (default 0: GAUSSIAN_BALANCE)
	 * </pre>
	 * 
	 * <pre>
	 * -L &lt;col1,col2-col4,...&gt
	 * Specifies the indexes of class label to balance.
	 * (default all)
	 * </pre>
	 * 
	 * <pre>
	 * -I (true|false)
	 *  Specifies if class labels indexes are to be inverted."
	 *  (default false)
	 * </pre>
	 * 
	 * <pre>
	 * -N (true|false)
	 *  Specifies if sampled values for attributes are allowed to be negative
	 *  (default false)
	 * </pre>
	 * 
	 * <pre>
	 * -P <num>
	 *  Specifies the number of instances to sample per class label.
	 *  (default 30)
	 * </pre>
	 * 
	 * <pre>
	 * -S <num>
	 *   Seed for values generation from learned distributions.
	 *  )
	 * </pre>
	 * 
	 * <pre>
	 * -X <num>
	 *   Specifies if fast aproximated sampling of poission values is allowed
	 *  (default true)
	 * </pre>
	 * 
	 * <!-- options-end -->
	 * 
	 * @param options
	 *            the list of options as an array of strings
	 * @throws Exception
	 *             if an option is not supported
	 */
	@Override
	public void setOptions(String[] options) throws Exception {

		String selectionString = Utils.getOption('D', options);
		if (selectionString.length() != 0) {
			setBalanceType(new SelectedTag(Integer.parseInt(selectionString),
					TAGS_BALANCE));
		}

		selectionString = Utils.getOption('L', options);
		if (selectionString.length() != 0) {
			setLabelsRange(selectionString);
		}

		selectionString = Utils.getOption('I', options);
		if (selectionString.length() != 0) {
			setInvertSelection(Boolean.parseBoolean(selectionString));
		}

		selectionString = Utils.getOption('N', options);
		if (selectionString.length() != 0) {
			setAllowNegativeValues(Boolean.parseBoolean(selectionString));
		}

		selectionString = Utils.getOption('P', options);
		if (selectionString.length() != 0) {
			setP(Integer.parseInt(selectionString));
		}

		selectionString = Utils.getOption('S', options);
		if (selectionString.length() != 0) {
			setSeed(Integer.parseInt(selectionString));
		}

		selectionString = Utils.getOption('X', options);
		if (selectionString.length() != 0) {
			setAllowPoissonApproximation(Boolean.parseBoolean(selectionString));
		}

		super.setOutputFormat(getInputFormat());// reset @relation name
	}

	/**
	 * @return true if indexes of class labels are selected in an inverted
	 *         manner. Otherwise,false.
	 */
	public boolean getInvertSelection() {
		return m_labelsRange.getInvert();
	}

	/**
	 * Specifies if class labels are selected in an inverted manner.
	 * 
	 * @param m_invertSelection
	 * @throws Exception
	 */
	public void setInvertSelection(boolean m_invertSelection) throws Exception {
		m_labelsRange.setInvert(m_invertSelection);
	}

	/**
	 * 
	 * @return int number of instances to sample per each class label selected
	 */
	public int getP() {
		return m_P;
	}

	/**
	 * Sets the number of instances to sample per each class label selected
	 * 
	 * @param m_P
	 */
	public void setP(int m_P) {
		this.m_P = m_P;
	}

	/**
	 * 
	 * @return int seed for random number generators
	 */
	public int getSeed() {
		return m_seed;
	}

	/**
	 * Sets the seed to use for random number generators
	 * 
	 * @param seed
	 */
	public void setSeed(int seed) {
		this.m_seed = seed;
	}

	/**
	 * 
	 * @return true if it is desired to allow negative values when sampling from
	 *         a Gaussian or Uniform distribution. Otherwise,false.
	 */
	public boolean getAllowNegativeValues() {
		return m_allowNegativeValues;
	}

	/**
	 * set if it is desired to allow negative values when sampling from a
	 * Gaussian or Uniform distribution.
	 * 
	 * @param m_allowNegativeValues
	 */
	public void setAllowNegativeValues(boolean m_allowNegativeValues) {
		this.m_allowNegativeValues = m_allowNegativeValues;
	}

	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String balanceTypeTipText() {
		return new String("Distribution to learn in order to re-sample new"
				+ "instances: Uniform, Gaussian), Poisson or Multinomial.");
	}

	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String allowNegativeValuesTipText() {
		return new String("Indicate if negative values are to be sampled."
				+ " If false, sampled values are 0 as minimum ");
	}

	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String seedTipText() {
		return new String("Seed use for numbers generation.");
	}

	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String allowPoissonApproximationTipText() {
		return new String(
				"Indicate if sampling from a poisson distribution can be using an "
						+ " approximate way which reduces the sampling time and gets similar sampled values.");
	}

	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String pTipText() {
		return new String("Number of instances to re-sample per class label");
	}

	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String labelsRangeTipText() {
		return new String(
				"Range of selected label indexes to balance. Indexes start "
						+ "from 1. 'first', 'last', and 'all' are valid values.");
	}

	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String invertSelectionTipText() {
		return new String("Invert selection of class labels");
	}

	/**
	 * reset all options to the same state than when this filter is created
	 */
	public void resetOptions() {

		m_balanceType = GAUSSIAN_BALANCE;
		m_labelsRange.setRanges("first-last");
		m_labelsRange.setInvert(false);
		m_allowNegativeValues = false;
		m_allowPoissonApproximation = true;
		m_P = 30;
		m_seed = (new Random()).nextInt();
	}

	/**
	 * 
	 * @return String[] describing the options
	 */
	@Override
	public String[] getOptions() {
		String[] options = new String[14];
		int current = 0;

		options[current++] = "-D";
		options[current++] = "" + getBalanceType().getSelectedTag().getID();
		options[current++] = "-L";
		options[current++] = "" + m_labelsRange.getRanges();
		options[current++] = "-I";
		options[current++] = "" + getInvertSelection();
		options[current++] = "-N";
		options[current++] = "" + getAllowNegativeValues();
		options[current++] = "-P";
		options[current++] = "" + getP();
		options[current++] = "-S";
		options[current++] = "" + getSeed();
		options[current++] = "-X";
		options[current++] = "" + getAllowPoissonApproximation();

		return options;

	}

	/**
	 * Sets the format of the input instances.
	 * 
	 * @param instanceInfo
	 *            an Instances object containing the input instance structure
	 *            (any instances contained in the object are ignored - only the
	 *            structure is required).
	 * @return true if the outputFormat may be collected immediately
	 * @throws Exception
	 *             if the input format can't be set successfully
	 */
	@Override
	public boolean setInputFormat(Instances instanceInfo) throws Exception {
		super.setInputFormat(instanceInfo);
		m_labelsRange.setUpper(instanceInfo.numClasses() - 1);
		super.setOutputFormat(instanceInfo);
		return true;
	}

	/**
	 * Input an instance for filtering. Filter requires all training instances
	 * be read before producing output.
	 * 
	 * @param instance
	 *            the input instance
	 * @return true if the filtered instance may now be collected with output().
	 * @throws IllegalStateException
	 *             if no input structure has been defined
	 */
	@Override
	public boolean input(Instance instance) {
		if (getInputFormat() == null) {
			throw new IllegalStateException("No input instance format defined");
		}
		if (m_NewBatch) {
			resetQueue();
			m_NewBatch = false;
		}
		if (m_FirstBatchDone) {
			push(instance);
			return true;
		} else {
			bufferInput(instance);
			return false;
		}
	}

	/**
	 * Signify that this batch of input to the filter is finished. If the filter
	 * requires all instances prior to filtering, output() may now be called to
	 * retrieve the filtered instances.
	 * 
	 * @return true if there are instances pending output
	 * @throws IllegalStateException
	 *             if no input structure has been defined
	 * @throws Exception
	 *             if provided options cannot be executed on input instances
	 */
	@Override
	public boolean batchFinished() throws Exception {
		if (getInputFormat() == null) {
			throw new IllegalStateException("No input instance format defined");
		}

		if (!m_FirstBatchDone) {
			dbBalance();
		}
		flushInput();

		m_NewBatch = true;
		m_FirstBatchDone = true;
		return (numPendingOutput() != 0);
	}

	/**
	 * Returns the revision string.
	 * 
	 * @return the revision
	 */
	@Override
	public String getRevision() {
		return weka.core.RevisionUtils.extract("$Revision: 1.0 $");
	}

	/**
	 * Returns an enumeration describing the available options.
	 * 
	 * @return an enumeration of all the available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		Vector<Option> newVector = new Vector<Option>();

		newVector
				.addElement(new Option(
						"\tSpecifies the distribution to learn from training set\n"
								+ "\t(default 0: GAUSSIAN_BALANCE)", "D", 1,
						"-D <num>"));

		newVector.addElement(new Option(
				"\tSpecifies the indexes of class label to balance\n"
						+ "\t(default all)", "L", 1, "-L <index>,<index>..."));
		newVector.addElement(new Option(
				"\tSpecifies if class labels indexes are to be inverted\n"
						+ "\t(default false)", "I", 1, "-I (true|false)"));

		newVector.addElement(new Option(
				"\tSpecifies if sampled values for attributes are allowed to be negative\n"
						+ "\t(default false )", "N", 1, "-N (true|false)"));
		newVector.addElement(new Option(
				"\tSpecifies the number of instances to sample per class label\n"
						+ "\t(default 30)", "P", 1, "-P <num>"));
		newVector.addElement(new Option(
				"\tSeed for values generation from learned distributions.\n",
				"S", 1, "-S <num>"));
		newVector
				.addElement(new Option(
						"\tSpecifies if fast approximated poisson sampling is allowed when"
								+ "m_balanceType==POISSON_DISTRIBUTION or MULTINOMIAL_DISTRIBUTION\n"
								+ "\t(default true)", "X", 1, "-X (true|false)"));

		return newVector.elements();
	}

	/**
	 * 
	 * @return true if fast approximate poission sampling is to be done
	 */
	public boolean getAllowPoissonApproximation() {
		return m_allowPoissonApproximation;
	}

	/**
	 * Specify if fast approximate poisson sampling is to be done. Sampled
	 * values are similar to those obtained by exact poisson sampling. This
	 * affects when m_balanceType is POISSON_BALANCE or MULTINOMIAL_BALANCE
	 * 
	 * @param m_allowPoissonApproximation
	 *            true or false
	 */
	public void setAllowPoissonApproximation(boolean m_allowPoissonApproximation) {
		this.m_allowPoissonApproximation = m_allowPoissonApproximation;
	}

	/**
	 * 
	 * @return double indicating the time spent (milliseconds) in the whole
	 *         filtring (balancing) process
	 */
	public double getFilteringTime_ms() {
		return getSamplingTime_ms() + getStatisticsTime_ms();
	}

	/**
	 * 
	 * @return double indicating the time spent (milliseconds) when sampling new
	 *         instances from a given distribution
	 */
	public double getSamplingTime_ms() {
		return m_samplingTime_ms;
	}

	/**
	 * 
	 * @return double indicating the time spent (milliseconds) when learning the
	 *         necessary statistics for a given distribution
	 */
	public double getStatisticsTime_ms() {
		return m_statisticsTime_ms;
	}

	/**
	 * 
	 * @return SelectedTag indicating the type of balance set
	 */
	public SelectedTag getBalanceType() {
		return new SelectedTag(m_balanceType, TAGS_BALANCE);
	}

	/**
	 * 
	 * @param newType
	 *            the type of distribution-based balancing desired
	 * @throws Exception
	 */
	public void setBalanceType(SelectedTag newType) throws Exception {
		if (newType.getTags() == TAGS_BALANCE) {
			m_balanceType = newType.getSelectedTag().getID();
		} else {
			throw new Exception("Wrong SelectedTag: "
					+ newType.getSelectedTag().getID());
		}
	}

	/**
	 * 
	 * @return int[] class labels which will be balanced
	 */
	public int[] getSelectedClassLabels() {
		return m_labelsRange.getSelection();
	}

	/**
	 * Set which class labels are to be balanced first, last and all are allowed
	 * 
	 * @param rangeList
	 *            a string representing the list of label indexes. Labels are
	 *            indexed from 1 for users. eg: first-2,4,6-last
	 */
	public void setLabelsRange(String rangeList) throws Exception {

		if (rangeList.indexOf("all") != -1) {
			m_labelsRange.setRanges("first-last");
		} else {
			m_labelsRange.setRanges(rangeList);
		}
	}

	public String getLabelsRange() {
		return m_labelsRange.getRanges();
	}

	/**
	 * method to call the corresponding method to perform a distribution-based
	 * balance of training data
	 * 
	 * @throws Exception
	 *             is sth goes wrong
	 */
	private void dbBalance() throws Exception {

		long start = System.currentTimeMillis();
		float[][][] stats = computeStats();
		m_statisticsTime_ms = System.currentTimeMillis() - start;
		start = System.currentTimeMillis();

		switch (m_balanceType) {
		case UNIFORM_BALANCE:
			resampleFromDistribution(stats);
			break;
		case GAUSSIAN_BALANCE:
			resampleFromDistribution(stats);
			break;
		case POISSON_BALANCE:
			resampleFromDistribution(stats);
			break;
		case MULTINOMIAL_BALANCE:
			// resample method is different from the others in order to make
			// code-reading easier
			resampleFromMultinomialDistribution(stats);
			break;
		default:
			break; // will never reach here
		}

		m_samplingTime_ms = System.currentTimeMillis() - start;

	}

	/**
	 * Main method for running this filter.
	 * 
	 * @param args
	 *            should contain arguments to the filter:
	 * 
	 */
	public static void main(String[] args) {
		runFilter(new DistributionBasedBalance(), args);
	}

}
