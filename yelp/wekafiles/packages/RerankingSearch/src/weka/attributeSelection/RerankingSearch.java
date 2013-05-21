package weka.attributeSelection;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Tag;
import weka.core.Utils;
import weka.core.SelectedTag;
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;
import weka.filters.unsupervised.attribute.Remove;
import weka.attributeSelection.Ranker;

import java.util.Enumeration;
import java.util.Vector;

/**
 * <!-- globalinfo-start --> Meta-Search algorithm. It first creates an univariate ranking of all 
 * attributes in decreasing order given an information-theory-based
 * AttributeEvaluator; then, the ranking is split in blocks of size B, and a
 * ASSearch is run for the first block. Given the selected attributes, the rest
 * of the ranking is re-ranked based on conditional IG of each attribute given the selected 
 * attributes so far. Then ASSearch is run again on the first current
 * block, and so on. Search stops when no attribute is selected in current block. For more
 * information, see <br/>
 * <br/>
 * Pablo Bermejo et. al. Fast wrapper feature subset selection in
 * high-dimensional datasets by means of filter re-ranking. Knowledge-Based
 * Systems. Volume 25 Issue 1. February 2012.
 * <p/>
 * <!-- globalinfo-end -->
 * 
 * <!-- technical-bibtex-start --> BibTeX:
 * 
 * <pre>
 *  &#64;article{BermejoRerank,
 * author = "Pablo Bermejo and Luis de la Ossa and Jose A. Gamez and Jose M. Puerta",   
 * title = "Fast wrapper feature subset selection in high-dimensional datasets by means of filter re-ranking",
 * journal = "Knowledge-Based Systems",
 * number = "1",
 * pages = " - ",
 * year = "2012",
 * doi = "DOI: 10.1016/j.knosys.2011.01.015",
 * }
 * 
 * </pre>
 * <p/>
 * <!-- technical-bibtex-end -->
 * 
 * <!-- options-start --> Valid options are:
 * 
 * 
 * <pre>
 *  -method <num>
 *  Specifies the method used to re-ranking attributes
 *  (default 0: CMIM)
 * </pre>
 * 
 * <pre>
 *  -blockSize <num>
 *  Specifies the size of blocks over which search is performed
 *  (default 20)
 * </pre>
 * 
 * 
 * <pre>
 * -rankingMeasure <num>
 *  information-theory-based univariate attribute evaluator to create first ranking
 *  (default 0: Information Gain)
 * </pre>
 * 
 * <pre>
 * -search <ASSearch algorithm>
 * Class name of ASSearch search algorithm to be used over blocks.
 * Place any options of the search algorithm LAST on the command line
 * following a "--". eg.:
 * -search weka.attributeSelection.GreedyStepwise ... -- -C
 * </pre>
 * <p/>
 * 
 * 
 * 
 * <!-- options-end -->
 * 
 * @author Pablo Bermejo (Pablo.Bermejo@uclm.es)
 * @version $Revision: 1.0 $
 */

public class RerankingSearch extends ASSearch implements OptionHandler {

	/** for serialisation */
	static final long serialVersionUID = -6538648431457629831L;

	/** Re-rank method */
	protected int m_rerankMethod = CMIM;

	/** Block size */
	protected int m_B = 20;

	/**
	 * Uni-varaite Attribute evaluator respect to the class. This is used to
	 * create the first ranking of attributes. It should be an information-based
	 * evaluation
	 */
	protected int m_informationBasedEvaluator = IG;

	/** search algorithm applied over block to select attributes */
	protected ASSearch m_searchAlgorithm = new weka.attributeSelection.GreedyStepwise();

	/** attribute set evaluator applied in search */
	protected ASEvaluation m_ASEval;

	/** Total time (ms) spent in search */
	protected double m_searchTime_ms;

	/** Time (ms) spent in re-ranking */
	protected double m_rerankingTime_ms;

	/** Total number of blocks over which search was performed */
	protected int m_blocksSearched;

	/** selected attributes */
	protected int[] m_selected;

	/** Fleuret's Conditional Mutual Information Maximization */
	protected static final int CMIM = 0;
	/** Battiti's Mutual Information-Based Feature Selection */
	protected static final int MIFS = 1;
	/** Peng's Max-Relevance and Min-Redundancy */
	protected static final int MRMR = 2;

	protected static final int IG = 0;
	protected static final int SU = 1;

	/** ranking of attributes in decreasing order given m_univariateEvaluator */
	protected int[] m_ranking;

	/**
	 * merit of each attribute evaluated by m_univariateEvaluator.
	 * Position i refers to attribute i in training data
	 */
	protected double[] m_attributes_merits_globalIndexes;

	public static final Tag[] TAGS_RERANK = {
			new Tag(CMIM,
					"Fleuret's Conditional Mutual Information Maximization."),
			new Tag(MIFS,
					"Battiti's Mutual Information-Based Feature Selection."),
			new Tag(MRMR, "Peng's Max-Relevance and Min-Redundancy") };

	public static final Tag[] TAGS_INFORMATION_BASED_EVAL = {
			new Tag(IG, "Information Gain."),
			new Tag(SU, "Symmetrical Uncertainty."), };

	public RerankingSearch() {
		resetOptions();
	}

	/**
	 * Performs search
	 * 
	 * @param ASEval
	 *            the attribute evaluator to guide the search
	 * @param data
	 *            the training instances.
	 * @return an array (not necessarily ordered) of selected attribute indexes
	 * @throws Exception
	 *             if the search can't be completed
	 */
	@Override
	public int[] search(ASEvaluation ASEval, Instances data) throws Exception {

		if (!(ASEval instanceof SubsetEvaluator)) {
			throw new Exception(m_ASEval.getClass().getName() + " is not a "
					+ "Subset evaluator!");
		}

		m_selected = new int[0];

		long start = System.currentTimeMillis();
		String startSet_relativeIndexes = "";
		int[] lastGlobalSelected = null;
		boolean anySelected = true;

		// discretize data for ranking and re-ranking computations
		Discretize myfilter = new Discretize();
		myfilter.setInputFormat(data);
		Instances discretized_data = Filter.useFilter(data, myfilter);

		createUnivariateRanking(discretized_data);
		while (anySelected) {
			m_blocksSearched++;
			Instances block = projectBlock(data);
			((StartSetHandler) m_searchAlgorithm)
					.setStartSet(startSet_relativeIndexes);
			m_ASEval = (ASEvaluation.makeCopies(ASEval, 1))[0];
			m_ASEval.buildEvaluator(block);
			int[] atts = m_searchAlgorithm.search(m_ASEval, block);
			startSet_relativeIndexes = getStartSetString(atts.length);
			m_selected = getGlobalIndexes(atts, block, data);
			if (different(m_selected, lastGlobalSelected)
					&& m_ranking.length != 0) {
				lastGlobalSelected = m_selected;
				rerank(discretized_data);
			} else
				anySelected = false;

		}

		m_searchTime_ms = System.currentTimeMillis() - start;
		return m_selected;
	}

	/**
	 * Parses a given list of options.
	 * <p/>
	 * 
	 * <!-- options-start --> Valid options are:
	 * <p/>
	 * 
	 * 
	 * <pre>
	 *  -method <num>
	 *  Specifies the method used to re-ranking attributes
	 *  (default 0: CMIM)
	 * </pre>
	 * 
	 * <pre>
	 *  -blockSize <num>
	 *  Specifies the size of blocks over which search is performed
	 *  (default 20)
	 * </pre>
	 * 
	 * 
	 * <pre>
	 * -rankingMeasure <num>
	 *  information-theory-based univariate attribute evaluator to create first ranking
	 *  (default 0: Information Gain)
	 * </pre>
	 * 
	 * <pre>
	 * -search <ASSearch algorithm>
	 * Class name of ASSearch search algorithm to be used over blocks.
	 * Place any options of the search algorithm LAST on the command line
	 * following a "--". eg.:
	 * -search weka.attributeSelection.GreedyStepwise ... -- -C
	 * </pre>
	 * 
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
		resetOptions();

		String selectionString = Utils.getOption("method", options);
		if (selectionString.length() != 0) {
			setRerankMethod(new SelectedTag(Integer.parseInt(selectionString),
					TAGS_RERANK));
		}

		selectionString = Utils.getOption("blockSize", options);
		if (selectionString.length() != 0) {
			setB(Integer.parseInt(selectionString));
		}

		selectionString = Utils.getOption("rankingMeasure", options);
		if (selectionString.length() != 0)
			setInformationBasedEvaluator(new SelectedTag(
					Integer.parseInt(selectionString),
					TAGS_INFORMATION_BASED_EVAL));

		selectionString = Utils.getOption("search", options);
		if (selectionString.length() != 0)
			setSearchAlgorithm((ASSearch) Utils.forName(ASSearch.class,
					selectionString, Utils.partitionOptions(options)));

	}

	/**
	 * get a String[] describing the value set for all options
	 * 
	 * @return String[] describing the options
	 */
	@Override
	public String[] getOptions() {
		String[] options = new String[8];
		int current = 0;

		options[current++] = "-method";
		options[current++] = "" + getRerankMethod().getSelectedTag().getID();
		options[current++] = "-blockSize";
		options[current++] = "" + getB();
		options[current++] = "-rankingMeasure";
		options[current++] = ""
				+ getInformationBasedEvaluator().getSelectedTag().getID();
		try {
			options[current++] = "-search";

			options[current++] = "" + getSearchAlgorithm().getClass().getName();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		return options;

	}

	/**
	 * It creates a ranking of attributes in decreasing order of merit, given
	 * the chosen attribute evaluator. It initiates m_ranking[] with the indexes
	 * of attributes ranked in decreasing order of merit.
	 * Furthermore, it initiates m_attributes_merits_globalIndexes[] with 
	 * the merit of attributes (index i refers to the merit of attribute i in data).
	 * 
	 * @param data
	 *            from which to build evaluator
	 * @throws Exception
	 */
	protected void createUnivariateRanking(Instances data) throws Exception {
		AttributeEvaluator eval = null;

		switch (m_informationBasedEvaluator) {
		case IG:
			eval = new weka.attributeSelection.InfoGainAttributeEval();
			break;
		case SU:
			eval = new weka.attributeSelection.SymmetricalUncertAttributeEval();
			break;

		default:
			throw new Exception("Unknown attribute evaluator: "
					+ m_informationBasedEvaluator);

		}

		((ASEvaluation) eval).buildEvaluator(data);
		Ranker ranker = new Ranker();
		ranker.search((ASEvaluation) eval, data);
		double[][] r = ranker.rankedAttributes();
		m_ranking = new int[data.numAttributes() - 1];
		m_attributes_merits_globalIndexes = new double[data.numAttributes() - 1];

		for (int i = 0; i < m_ranking.length; i++) {
			m_ranking[i] = (int) r[i][0];
			m_attributes_merits_globalIndexes[m_ranking[i]] = r[i][1];

		}

	}

	/**
	 * 
	 * It projects data over attributes selected up to know + first m_B
	 * attributes in ranking + the class attribute. Then, it removes first
	 * m_B attributes from m_ranking.
	 * 
	 * @param data
	 *            from which to project attributes to create a block
	 * 
	 * @return Intances object with block over which to perform search
	 * @throws Exception
	 */
	protected Instances projectBlock(Instances data) throws Exception {
		Instances block;

		int numberToCopy = (m_B <= m_ranking.length) ? m_B : m_ranking.length;

		int[] atts = new int[m_selected.length + numberToCopy + 1];
		System.arraycopy(m_selected, 0, atts, 0, m_selected.length);
		System.arraycopy(m_ranking, 0, atts, m_selected.length, numberToCopy);
		atts[atts.length - 1] = data.numAttributes() - 1;
		Remove myfilter = new Remove();
		myfilter.setAttributeIndicesArray(atts);
		myfilter.setInvertSelection(true);
		myfilter.setInputFormat(data);
		block = Filter.useFilter(data, myfilter);
		int classIndex = block.numAttributes() - 1;
		block.setClassIndex(classIndex);

		// remove block's attributes from ranking
		int[] newRanking = new int[m_ranking.length - numberToCopy];
		System.arraycopy(m_ranking, numberToCopy, newRanking, 0,
				newRanking.length);
		m_ranking = newRanking;

		return block;

	}

	/**
	 * It converts the indexes of attributes in relative[] referring to
	 * relativeData, to the indexes of the referred attributes in globalInstances
	 * data.
	 * 
	 * @param relative
	 *            attributes indexes selected in previous block
	 * @param relativeData
	 *            previous block
	 * @param globalData
	 *            original data with all the attributes
	 * @return the global index of attributes selected in previous block
	 */
	protected int[] getGlobalIndexes(int[] relative, Instances relativeData,
			Instances globalData) {
		int[] global = new int[relative.length];

		for (int i = 0; i < relative.length; i++) {
			String name = relativeData.attribute(relative[i]).name();
			global[i] = globalData.attribute(name).index();

		}

		return global;

	}

	/**
	 * Returns an enumeration describing the available options.
	 * 
	 * @return an enumeration of all the available options.
	 **/
	@Override
	public Enumeration<Option> listOptions() {
		Vector<Option> newVector = new Vector<Option>();

		newVector.addElement(new Option(
				"\tSpecifies the method used to re-ranking attributes\n"
						+ "\t(default 0: CMIM)", "method", 1, "-method <num>"));

		newVector
				.addElement(new Option(
						"\tSpecifies the size of blocks over which search is performed\n"
								+ "\t(default 20)", "blockSize", 1,
						"-blockSize <num>"));

		newVector
				.addElement(new Option(
						"\tInformation-theory-based univariate attribute evaluator to create first ranking\n"
								+ "\t(default 0: Information Gain)",
						"rankingMeasure", 1, "-rankingMeasure <num>"));

		newVector
				.addElement(new Option(
						"\tClass name of ASSearch search algorithm to be used over blocks.\n"
								+ "\tPlace any options of the search algorithm LAST on the command line following a '--'. eg.:\n"
								+ "\t -search weka.attributeSelection.GreedyStepwise ... -- -C\n"
								+ "\t(default: weka.attributeSelection.GreeyStepwise)",
						"search", 1, "-search <search algorithm>"));

		return newVector.elements();
	}

	/**
	 * re-rank remaining attributes in m_ranking[] given m_selected[]
	 * 
	 * @param data
	 *            original training data
	 */
	protected void rerank(Instances data) throws Exception {
		long start = System.currentTimeMillis();

		switch (m_rerankMethod) {
		case CMIM:
			rerankCMIM(data);
			break;
		case MIFS:
			rerankMIFS_MRMR(data, 0.5);
			break;
		case MRMR:
			rerankMIFS_MRMR(data, 1 / m_selected.length);
			break;
		default:
			throw new Exception("Unknown rerank method: " + m_rerankMethod);

		}

		m_rerankingTime_ms += System.currentTimeMillis() - start;

	}

	/**
	 * Creates a string of attributes indexes separated by commas. Since
	 * attributes selected in the preivous block are appended at the beginning
	 * of the new block, we know the relative indexes start by 0.
	 * 
	 * @param numberSelected
	 *            cardinaility of attributes selected in previous block
	 * @return String with start set indexes for next block
	 */
	protected String getStartSetString(int numberSelected) {
		String ss = "";
		if (numberSelected == 0)
			return ss;

		for (int i = 0; i < numberSelected - 1; i++)
			ss += (i + 1) + ","; // indexes in a startset start by 1
		ss += numberSelected;
		return ss;

	}

	/**
	 * check if two arrays contain the same values, in any order
	 * 
	 * @param a
	 *            []
	 * @param b
	 *            []
	 * @return true if a[] and b[] do not have the same integer values
	 */
	protected boolean different(int a[], int b[]) {

		if (a == null || b == null)
			return true;
		if (a.length != b.length)
			return true;

		for (int n : a) {
			if (!isIn(n, b))
				return true;
		}

		return false;
	}

	/**
	 * Check if value n is in array
	 * 
	 * @param n
	 * @param array
	 * @return true if n is in array in any position
	 */
	protected boolean isIn(int n, int array[]) {
		for (int m : array)
			if (n == m)
				return true;
		return false;
	}

	/**
	 * Get a deep copy of the search algorithm used over blocks
	 * 
	 * @return deep copy of current m_searchAlgorithm
	 */
	public ASSearch getSearchAlgorithm() throws Exception {
		return (ASSearch.makeCopies(m_searchAlgorithm, 1))[0];
	}

	/**
	 * set the search algorithm to use over blocks in ranking
	 * 
	 * @param m_searchAlgorithm
	 *            new search algorithm to be used over blocks
	 */
	public void setSearchAlgorithm(ASSearch m_searchAlgorithm) {
		this.m_searchAlgorithm = m_searchAlgorithm;
	}

	/**
	 * Get method used for re-ranking
	 * 
	 * @return SelectedTag indicating the type of re-ranking
	 */
	public SelectedTag getRerankMethod() {
		return new SelectedTag(m_rerankMethod, TAGS_RERANK);

	}

	/**
	 * Set method to use for re-ranking
	 * 
	 * @param newType
	 *            the type of re-rerank method desired
	 */
	public void setRerankMethod(SelectedTag newType) throws Exception {

		if (newType.getTags() == TAGS_RERANK) {
			m_rerankMethod = newType.getSelectedTag().getID();
		} else {
			throw new Exception("Wrong SelectedTag: "
					+ newType.getSelectedTag().getID());
		}

	}

	/**
	 * total time in milliseconds spent during the search
	 * 
	 * @return double search time in milliseconds
	 */
	public double getSearchTime_ms() {
		return m_searchTime_ms;
	}

	/**
	 * time in milliseconds spent during the search in re-ranking computations
	 * 
	 * @return double time spent in re-ranking
	 */
	public double getRerankingTime_ms() {
		return m_rerankingTime_ms;
	}

	/**
	 * get number of blocks attributes in ranking over which search has been
	 * performed. This is the number of re-rankings performed + 1
	 * 
	 * @return int number of blocks searched
	 */
	public int getBlocksSearched() {
		return m_blocksSearched;
	}

	/**
	 * get size of blocks over which search is performed
	 * 
	 * @return m_B size (cardinality) of blocks over which search is performed
	 */
	public int getB() {
		return m_B;
	}

	/**
	 * set size of blocks (cardinality) over which search is performed
	 * 
	 * @param B
	 */
	public void setB(int B) {
		m_B = B;
	}

	/**
	 * * Get method used to crate first univariate ranking
	 * 
	 * @return SelectedTag evaluator used to generate the original ranking
	 */
	public SelectedTag getInformationBasedEvaluator() {
		return new SelectedTag(m_informationBasedEvaluator,
				TAGS_INFORMATION_BASED_EVAL);
	}

	/**
	 * set evaluator to create univariate ranking
	 * 
	 * @param newType
	 *            the information-based univariate evaluation to create first
	 *            ranking
	 */
	public void setInformationBasedEvaluator(SelectedTag newType)
			throws Exception {

		if (newType.getTags() == TAGS_INFORMATION_BASED_EVAL) {
			m_informationBasedEvaluator = newType.getSelectedTag().getID();
		} else {
			throw new Exception("Wrong SelectedTag: "
					+ newType.getSelectedTag().getID());
		}

	}

	/**
	 * reset all options to their default values
	 */
	public void resetOptions() {
		m_rerankMethod = CMIM;
		m_B = 20;
		m_informationBasedEvaluator = IG;
		m_searchAlgorithm = new weka.attributeSelection.GreedyStepwise();

	}

	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String rerankMethodTipText() {
		return new String(
				"Type of IG(X;C|S) approximation for re-ranking remaining attributes in ranking after"
						+ " the current block has ben processed and S is the current subset of selected attributes.");
	}

	/**
	 * get list of attributes selected in search
	 * 
	 * @return int[] of attributes selected
	 */
	public int[] getSelected() {
		int[] copy = new int[m_selected.length];
		System.arraycopy(m_selected, 0, copy, 0, m_selected.length);

		return copy;
	}

	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String bTipText() {
		return new String("Size of each block to split ranking.");
	}

	/**
	 * Returns the tip text for m_univariteEvaluator
	 * 
	 * @return tip text for displaying in the explorer/experimenter gui
	 */
	public String informationBasedEvaluatorTipText() {
		return new String(
				"Evaluator used to create former ranking of attributes respect to "
						+ "the class.");
	}

	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String searchAlgorithmTipText() {
		return new String("ASSearch algorithm to run over blocks.");
	}

	/**
	 * Returns a string describing this search method
	 * 
	 * @return a description of the search suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String globalInfo() {
		return "Meta-Search algorithm. It first creates an univariate ranking of all " 
 +"attributes in decreasing order given an information-theory-based "
 +"AttributeEvaluator; then, the ranking is split in blocks of size B, and a "
 +"ASSearch is run for the first block. Given the selected attributes, the rest "
 +"of the ranking is re-ranked based on conditional IG of each attribute given the selected " 
 +"attributes so far. Then ASSearch is run again on the first current "
 +"block, and so on. Search stops when no attribute is selected in current block. For more "
 +"information, see  "
 +"\"Pablo Bermejo et. al. Fast wrapper feature subset selection in  "
 +"high-dimensional datasets by means of filter re-ranking. Knowledge-Based "
 +"Systems. Volume 25 Issue 1. February 2012\".";
	}

	/**
	 * Description of the search
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		String results = "Selected attributes: ";
		for (int i : m_selected)
			results += i + " ";
		results += "\nTotal Search Time in milliseconds: " + getSearchTime_ms();
		results += "\nReranking Time in milliseconds: " + getRerankingTime_ms();
		results += "\nBlocks searched: " + getBlocksSearched();
		results += "\nAttribute Evaluator during search: "
				+ m_ASEval.toString();
		results += "\nOptions of RerankingSearch:\n";
		String[] options = getOptions();
		for (String s : options)
			results += s + " ";

		return results;

	}

	/**
	 * Modifies m_ranking ordering attributes in decreasing order of Fleuret's
	 * CMIM approximation of I(Xi;C|m_selected) for all Xi in m_ranking. CMIM
	 * approximates this value with formula: max_Xi min_Xj I(Xi;C|m_selected)
	 * for all Xi in m_ranking and all Xj in m_selected
	 * 
	 * @param data from which to compute conditional mutual informations
	 * 
	 */
	protected void rerankCMIM(Instances data) throws Exception {

		double[][] I_XiC_givenS = new double[m_ranking.length][m_selected.length];
		for (int i = 0; i < m_ranking.length; i++) {
			for (int j = 0; j < m_selected.length; j++) {
				I_XiC_givenS[i][j] = getConditionalMutualInformation(
						m_ranking[i], data.numAttributes() - 1, m_selected[j],
						data);
			}
		}

		int[] maxToMin = minMaxOrder(I_XiC_givenS);
		I_XiC_givenS = null;
		int[] auxRanking = new int[m_ranking.length];
		for (int i = 0; i < auxRanking.length; i++) {
			auxRanking[i] = m_ranking[maxToMin[i]];
		}
		m_ranking = auxRanking;
	}

	/**
	 * Modifies m_ranking ordering attributes in decreasing order of approximation 
	 * of I(Xi;C|m_selected) for all Xi in m_ranking, and all
	 * Xj in m_selected.
	 * MIFS approximates this value with Battiti's formula:
	 * I(Xi,C) - (0.5* sum I(Xi,Xj)). 
	 * MRMR approximates this value with Peng's formula:
	 * I(Xi,C) - (1/|S| * sum I(Xi,Xj)).
	 * Thus, the only difference between both methods is
	 * the multiplicaton factor
	 * 
	 * @param data to compute mutual information values
	 * @param factor double value used to multiply by mutual informations
	 */
	protected void rerankMIFS_MRMR(Instances data, double factor)
			throws Exception {

		
		double[] sumI_XiXj = new double[m_ranking.length];

		for (int i = 0; i < m_ranking.length; i++) {
			for (int j = 0; j < m_selected.length; j++) {
				sumI_XiXj[i] += getMutualInformation(m_ranking[i],
						m_selected[j], data);
			}
		}
		double[] values = new double[m_ranking.length];
		for (int i = 0; i < m_ranking.length; i++) {
			values[i] = getMutualInformation(m_ranking[i],
					data.numAttributes() - 1, data) - (factor * sumI_XiXj[i]);
		}
		sumI_XiXj = null;

		int[] minToMax = weka.core.Utils.stableSort(values);
		int[] maxToMin = new int[minToMax.length];
		for (int i = 0; i < maxToMin.length; i++) {
			maxToMin[i] = minToMax[minToMax.length - 1 - i];
		}
		minToMax = null;
		int[] auxRanking = new int[m_ranking.length];
		for (int i = 0; i < auxRanking.length; i++) {
			auxRanking[i] = m_ranking[maxToMin[i]];
		}

		m_ranking = auxRanking;
	}

	/**
	 * 
	 * 
	 * @param I_XiC_givenXj
	 *            conditional information I(X;C|m_selected) for all Xi in
	 *            training data
	 * @return int[] indexes in first dimension of I_XiC_givenXj , sorted in
	 *         decreasing order of max_Xi min_Xj I(Xi;C|m_selected)
	 * 
	 */
	protected int[] minMaxOrder(double[][] I_XiC_givenXj) {

		int[][] minToMax = new int[I_XiC_givenXj.length][I_XiC_givenXj[0].length];
		for (int i = 0; i < minToMax.length; i++) {
			minToMax[i] = weka.core.Utils.stableSort(I_XiC_givenXj[i]);
		}

		double[] values = new double[I_XiC_givenXj.length];
		for (int i = 0; i < I_XiC_givenXj.length; i++) {

			values[i] = I_XiC_givenXj[i][minToMax[i][0]];
		}
		int[] minToMax2 = weka.core.Utils.stableSort(values);

		int[] maxToMin = new int[minToMax2.length];

		for (int i = 0; i < maxToMin.length; i++) {
			maxToMin[i] = minToMax2[minToMax2.length - 1 - i];
		}
		return maxToMin;

	}

	/**
	 * Computes I(X;Y|Z)
	 * 
	 * @param posX
	 *            index of att X
	 * @param posY
	 *            index of att Y
	 * @param posZ
	 *            index of conditioning attribute Z
	 * @param data
	 *            training data
	 * @return double conditional mutual information I(X;Y|Z)
	 * @throws Exception
	 */
	protected double getConditionalMutualInformation(int posX, int posY,
			int posZ, Instances data) throws Exception {

		// get number of states per attribute
		int nx = data.attribute(posX).numValues();
		int ny = data.attribute(posY).numValues();
		int nz = data.attribute(posZ).numValues();

		// compute necessary distributions
		double[] pz = getMarginalProb(posZ, data);
		double[][] pxz = getJointXY(posX, posZ, data);
		double[][] pyz = getJointXY(posY, posZ, data);
		double[][][] pxyz = getJointXYZ(posX, posY, posZ, data);

		// compute conditional mutual information
		double cmi = 0.0;
		for (int z = 0; z < nz; z++)
			for (int y = 0; y < ny; y++)
				for (int x = 0; x < nx; x++)
					if (pxyz[x][y][z] == 0.0)
						cmi += 0.0;
					else
						cmi += (pxyz[x][y][z] * Utils
								.log2((pz[z] * pxyz[x][y][z])
										/ (pxz[x][z] * pyz[y][z])));

		return cmi;
	}

	/**
	 * Computes I(X;Y)
	 * 
	 * @param posX
	 *            index of att X
	 * @param posY
	 *            index of att Y
	 * @param data
	 *            training data
	 * @return double mutual information I(X;Y)
	 * @throws Exception
	 */
	protected double getMutualInformation(int posX, int posY, Instances data) {
		// get number of states per attribute
		int nx = data.attribute(posX).numValues();
		int ny = data.attribute(posY).numValues();

		// compute necessary distributions
		double[] px = getMarginalProb(posX, data);
		double[] py = getMarginalProb(posY, data);
		double[][] pxy = getJointXY(posX, posY, data);

		// compute mutual information
		double mi = 0.0;
		for (int y = 0; y < ny; y++)
			for (int x = 0; x < nx; x++)
				if (pxy[x][y] == 0.0)
					mi += 0.0;
				else
					mi += (pxy[x][y] * Utils.log2(pxy[x][y] / (px[x] * py[y])));

		return mi;
	}

	/**
	 * Compute joint probability por attribute pX and pY in data
	 * 
	 * @return double[][] joint probabilities
	 */
	protected double[][] getJointXY(int pX, int pY, Instances data) {
		int nvX = data.attribute(pX).numValues();
		int nvY = data.attribute(pY).numValues();
		double[][] prob = new double[nvX][nvY];
		double tot = data.numInstances();

		for (int i = 0; i < nvX; i++)
			for (int j = 0; j < nvY; j++)
				prob[i][j] = 0.0;

		Instance row = null;

		for (int j = 0; j < data.numInstances(); j++) {
			row = data.instance(j);
			prob[(int) row.value(pX)][(int) row.value(pY)]++;
		}

		// normalize
		for (int i = 0; i < nvX; i++)
			for (int j = 0; j < nvY; j++) {
				prob[i][j] /= tot;
			}

		return prob;
	}

	/**
	 * Compute joint probability por attribute pX, pY and pZ in data
	 * 
	 * @return double[][] joint probabilities
	 */
	protected double[][][] getJointXYZ(int pX, int pY, int pZ, Instances data) {
		int nvX = data.attribute(pX).numValues();
		int nvY = data.attribute(pY).numValues();
		int nvZ = data.attribute(pZ).numValues();
		double[][][] prob = new double[nvX][nvY][nvZ];
		double tot = data.numInstances();

		for (int i = 0; i < nvX; i++)
			for (int j = 0; j < nvY; j++)
				for (int k = 0; k < nvZ; k++)
					prob[i][j][k] = 0.0;

		Instance row = null;
		for (int j = 0; j < data.numInstances(); j++) {
			row = data.instance(j);
			prob[(int) row.value(pX)][(int) row.value(pY)][(int) row.value(pZ)]++;
		}

		// normalize
		for (int i = 0; i < nvX; i++)
			for (int j = 0; j < nvY; j++)
				for (int k = 0; k < nvZ; k++) {
					prob[i][j][k] /= tot;
				}

		return prob;
	}

	/**
	 * Computes marginal probability for attribute with index pos, in data
	 * 
	 * @return double[] marginal probabilities of index pos
	 */
	protected double[] getMarginalProb(int pos, Instances data) {
		int nv = data.attribute(pos).numValues();
		double[] prob = new double[nv];
		double tot = data.numInstances();

		Instance row = null;
		for (int j = 0; j < data.numInstances(); j++) {
			row = data.instance(j);
			prob[(int) row.value(pos)]++;
		}

		// normalize
		for (int i = 0; i < nv; i++)
			prob[i] /= tot;

		return prob;
	}

	
}
