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

package weka.attributeSelection;

import java.io.File;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;
import weka.core.Debug;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

/**
 <!-- globalinfo-start -->
 * PSOSearch explores the attribute space using the Particle Swarm Optimization (PSO) algorithm.<br/>
 * <br/>
 * For more information see:<br/>
 * <br/>
 * Moraglio, A., Di Chio, C., and Poli, R. <br/>
 * Geometric Particle Swarm Optimisation. <br/>
 * EuroGP 2007, LNCS 445, pp. 125-135. <br/>
 * <br/>
 * For an application to gene expression data classification, see:<br/>
 * <br/>
 * García-Nieto, J.M., Alba, E., Jourdan, L., and Talbi, E.-G.<br/>  
 * Sensitivity and specificity based multiobjective approach for feature selection: Application to cancer diagnosis. <br/>
 * Information Processing Letters, 109(16):887-896, July 2009. <br/>
 * <br/>
 * IMPORTANT: To ensure the correct behavior of PSOSearch it is mandatory for the class attribute to be the last one in the ARFF file.
 * <p/> 
 <!-- globalinfo-end -->
 * 
 <!-- technical-bibtex-start -->
 * BibTeX:
 * <pre>
 * &#64;INPROCEEDINGS{moraglio2007,
 *	author       = {Moraglio, A. and Di Chio, C. and Poli, R.},
 *	title        = {Geometric particle swarm optimisation},
 *	booktitle    = {Proceedings of the 10th European conference on Genetic programming},
 *	series       = {EuroGP'07},
 *	year         = {2007},
 *	pages        = {125-136},
 *	publisher    = {Springer-Verlag},
 *	address      = {Berlin, Heidelberg}
 *  }
 * </pre>
 * <pre>
 * &#64;ARTICLE{nieto2009,
 *	author       = {García-Nieto, J.M. and Alba, E. and Jourdan, L. and Talbi, E.-G.},
 *	title        = {Sensitivity and specificity based multiobjective approach for feature selection: Application to cancer diagnosis},
 *	journal      = {Information Processing Letters},
 *	year         = {2009},
 *	volume       = {109},
 *	number       = {16},
 *	pages        = {887-896},
 *	month        = {July},
 *  }
 * </pre>
 * <p/>
 <!-- technical-bibtex-end -->
 * 
 <!-- options-start -->
 * Valid options are: <p/>
 * 
 * <pre> -N <swarm size>
 * The number of particles in the swarm.
 * (default = 20)</pre>
 * 
 * <pre> -I <iterations>
 * The number of iterations to perform.
 * (default = 20)</pre>
 * 
 * <pre> -T <mutation type>
 * The type of mutation to be applied: 
 * 0 = bit-flip (default), 1 = bit-off</pre>
 *  
 * <pre> -M <mutation probability>
 * The probability of mutation.
 * (default = 0.01)</pre>
 * 
 * <pre> -A <intertia weight>
 * The inertia weight in 3PBMCX.
 * (default = 0.33)</pre>
 * 
 * <pre> -B <social weight>
 * The social weight in 3PBMCX.
 * (default = 0.33)</pre>
 * 
 * <pre> -C <individual weight>
 * The individual weight in 3PBMCX.
 * (default = 0.34)</pre>
 *  
 * <pre> -P <start set>
 *  Specify a starting set of attributes.
 *  Eg. 1,3,5-7.If supplied, the starting set becomes
 *  one member of the initial random population.</pre>
 * 
 * <pre> -R <report frequency>
 *  Set frequency of reports.
 *  e.g, setting the value to 5 will 
 *  report every 5 iterations
 *  (default = number of generations)</pre>
 * 
 * <pre> -S <seed>
 *  Set the random number seed.
 *  (default = 1)</pre>
 *  
 * <pre> -L <log file name>
 *  Set the log file location.
 *  Log file just keeps track of the best fitness
 *  found through all the iterations.
 *  (default = null)</pre>
 * 
 <!-- options-end -->
 * 
 * @author Sebastian Luna Valero
 *
 */
public class PSOSearch extends ASSearch 
	implements StartSetHandler,OptionHandler, TechnicalInformationHandler {

	/** for serialization */
	private static final long serialVersionUID = -925254202661284182L;

	/** 
	 * holds a starting set as an array of attributes. Becomes one member of the
	 * initial random population
	 */
	private int[] m_starting;

	/** holds the start set for the search as a Range */
	private Range m_startRange;

	/** does the data have a class */
	private boolean m_hasClass;

	/** holds the class index */
	private int m_classIndex;

	/** number of attributes in the data */
	private int m_numAttribs;

	/** the current population */
	private PSOBitSet [] m_population;
	
	/** the best historical positions in all iterations */
	private PSOBitSet [] m_bestPopulation;

	/** the number of individual solutions */
	private int m_popSize;

	/** the best particle found during the whole search */
	private PSOBitSet m_totalBest;

	/** the number of selected genes in the the total best */
	private int m_totalBestFeatureCount;

	/** the best particle found in each iteration */
	private PSOBitSet m_partialBest;
	
	/** the number of features in the partial best */
	private int m_partialBestFeatureCount;

	/** the number of entries to cache for lookup */
	private int m_lookupTableSize;

	/** the lookup table */
	private Hashtable m_lookupTable;

	/** random number generation */
	private Random m_random;

	/** seed for random number generation */
	private int m_seed;

	/** the weight of the current particle position */
	private double m_currentW;
	
	/** the weight of the best individual particle position */
	private double m_bestPW;
	
	/** the weight of the best global particle position */
	private double m_bestGW;

	/** the mutation type: 0 = bit-flip (default), 1 = bit-off */
	private int m_mutationType;
	
	/** mutation type: bit-flip (default) */
	protected static final int BIT_FLIP = 0;
	
	/** mutation type: bif-off */
	protected static final int BIT_OFF = 1;
	
	/** mutation types */
	public static final Tag [] TAGS_SELECTION = {
		new Tag(BIT_FLIP, "bit-flip"),
		new Tag(BIT_OFF, "bit-off")
	};
	
	/** the probability of mutation occuring */
	private double m_pMutation;

	/** sum of the current population fitness */
	private double m_sumFitness;

	private double m_maxFitness;
	private double m_minFitness;
	private double m_avgFitness;

	/** the maximum number of generations to evaluate */
	private int m_maxGenerations;
	
	/** how often reports are generated */
	private int m_reportFrequency;

	/** holds the generation reports */
	private StringBuffer m_generationReports;
	
	/** log file to save important traces */
	private File m_logFile;
	
	/** list to keep track of the fitness */
	private LinkedList m_bestFitnessList;
	
	
	/**
	 * Constructs a new PSOSearch object.
	 * 
	 */
	public PSOSearch() {
		resetOptions();
	}
	

	/**
	 * Searches the attribute subset space using the PSO algorithm.
	 *
	 * @param ASEval the attribute evaluator to guide the search
	 * @param data the training instances.
	 * @return an array (not necessarily ordered) of selected attribute indexes
	 * @throws Exception if the search can't be completed
	 */
	@Override
	public int[] search(ASEvaluation ASEval, Instances data)
	throws Exception {
		m_totalBest = null;
		m_generationReports = new StringBuffer();

		if (!(ASEval instanceof SubsetEvaluator)) {
			throw  new Exception(ASEval.getClass().getName() 
					+ " is not a " 
					+ "Subset evaluator!");
		}

		if (ASEval instanceof UnsupervisedSubsetEvaluator) {
			m_hasClass = false;
		}
		else {
			m_hasClass = true;
			m_classIndex = data.classIndex();
		}

		SubsetEvaluator ASEvaluator = (SubsetEvaluator)ASEval;
		m_numAttribs = data.numAttributes();

		m_startRange.setUpper(m_numAttribs-1);
		if (!(getStartSet().equals(""))) {
			m_starting = m_startRange.getSelection();
		}
		
		// check if the configuration parameters are correctly set!
		checkOptions();

		// initial random population
		m_lookupTable = new Hashtable(m_lookupTableSize);
		m_random = new Random(m_seed);
		m_population = new PSOBitSet[m_popSize];
		m_bestPopulation = new PSOBitSet[m_popSize];
		
		// set up random initial population
		initPopulation();
		evaluatePopulation(ASEvaluator);
		populationStatistics();
		scalePopulation();
		checkBest();
		m_generationReports.append(populationReport(0));

		int i = 1;

		while (i <= m_maxGenerations) {			
			generation();
			evaluatePopulation(ASEvaluator);
			populationStatistics();
			scalePopulation();
			// update what is the best particle in the swarm (that with higher fitness)
			checkBest();
			
			if ((i == m_maxGenerations) || 
				(i % m_reportFrequency == 0)) {
				
				m_generationReports.append(populationReport(i));
				
			} // if 
			
			i++;
			
		} // while
		
		// save the best fitness track to a file??
		if (!getLogFile().isDirectory()) {
			StringBuffer message = new StringBuffer();
			int bestI = 0;
			boolean found = false;
			
			// look for the first iteration where the best fitness was found
			while (!found) {
				if (Double.compare(((Double)m_bestFitnessList.get(bestI)).doubleValue(),m_totalBest.getObjective()) == 0) {
					found = true;
				} else {
					bestI++;
				}
			}

			message.append("\nBest fitness found: " + m_totalBest.getObjective() 
					+ ", with " + countFeatures(m_totalBest.getChromosome()) + " features selected.\n");
			message.append("Best iteration found: " + bestI + "\n");
			message.append("Selected features are: " + printPopMember(m_totalBest.getChromosome()) + "\n");
						
		    // write 
		    Debug.writeToFile(getLogFile().getAbsolutePath(), message.toString(), true);
		    
		} // if m_logFile		
		
		return attributeList(m_totalBest.getChromosome());
		
	} // search

	
	/**
	 * converts a BitSet into a list of attribute indexes 
	 * @param group the BitSet to convert
	 * @return an array of attribute indexes
	 **/
	private int[] attributeList (BitSet group) {
		int count = 0;

		// count how many were selected
		for (int i = 0; i < m_numAttribs; i++) {
			if (group.get(i)) {
				count++;
			}
		}

		int[] list = new int[count];
		count = 0;

		for (int i = 0; i < m_numAttribs; i++) {
			if (group.get(i)) {
				list[count] = i;
				count++;
			}
		}

		return list;
		
	} // attributeList
	
	/**
	 * creates random population members for the initial population. Also
	 * sets the first population member to be a start set (if any) 
	 * provided by the user
	 * @throws Exception if the population can't be created
	 */
	private void initPopulation () throws Exception {
		int i,j,bit;
		int num_bits;
		boolean ok;
		int start = 0;

		// add the start set as the first population member (if specified)
		if (m_starting != null) {
			m_population[0] = new PSOBitSet(m_numAttribs);
			m_bestPopulation[0] = new PSOBitSet(m_numAttribs);
			for (i=0;i<m_starting.length;i++) {
				if ((m_starting[i]) != m_classIndex) {
					m_population[0].set(m_starting[i]);
					m_bestPopulation[0].set(m_starting[i]);
				}
			}
			start = 1;
		}

		for (i=start;i<m_popSize;i++) {
			m_population[i] = new PSOBitSet(m_numAttribs);
			m_bestPopulation[i] = new PSOBitSet(m_numAttribs);

			num_bits = m_random.nextInt();
			num_bits = num_bits % m_numAttribs-1;
			if (num_bits < 0) {
				num_bits *= -1;
			}
			if (num_bits == 0) {
				num_bits = 1;
			}

			for (j=0;j<num_bits;j++) {
				ok = false;
				do {
					bit = m_random.nextInt();
					if (bit < 0) {
						bit *= -1;
					}
					bit = bit % m_numAttribs;
					if (m_hasClass) {
						if (bit != m_classIndex) {
							ok = true;
						}
					}
					else {
						ok = true;
					}
				} while (!ok);

				if (bit > m_numAttribs) {
					throw new Exception("Problem in population init");
				}
				m_population[i].set(bit);
				m_bestPopulation[i].set(bit);
			} // for num_bits
		} // for popSize
	} // initPopulation
	
	
	/**
	 * evaluates an entire population. Population members are looked up in
	 * a hash table and if they are not found then they are evaluated using
	 * ASEvaluator.
	 * @param ASEvaluator the subset evaluator to use for evaluating population
	 * members
	 * @throws Exception if something goes wrong during evaluation
	 */
	private void evaluatePopulation(SubsetEvaluator ASEvaluator)
	throws Exception {
		int i;
		double merit;
		BitSet auxBitSet;
		PSOBitSet auxPSOBitSet;

		for (i=0;i<m_popSize;i++) {
			// if its not in the lookup table then evaluate and insert
			if (m_lookupTable.containsKey(m_population[i].getChromosome()) == false) {
				merit = ASEvaluator.evaluateSubset(m_population[i].getChromosome());
				m_population[i].setObjective(merit);
				auxPSOBitSet = (PSOBitSet)m_population[i].clone();
				auxBitSet = (BitSet)m_population[i].getChromosome().clone();
				m_lookupTable.put(auxBitSet, auxPSOBitSet);
			} else {
				auxPSOBitSet = (PSOBitSet)m_lookupTable.get(m_population[i].getChromosome());
				m_population[i].setObjective(auxPSOBitSet.getObjective());				
			} // if lookupTable
			// update m_bestPopulation
			updateBestPopulation(i, m_population[i].getObjective(), m_population[i].getChromosome());
		} // for popSize
	} // evaluatePopulation

	
	/**
	 * keeps updated the best position and merit of each particle through all iterations
	 * 
	 * @param index of the particle to be updated
	 * @param merit of the new particle
	 * @param chromosome (position) of the new particle
	 */
	private void updateBestPopulation(int index, double merit, BitSet chromosome)
	throws Exception {
		
		if (merit > m_bestPopulation[index].getObjective()) {
			m_bestPopulation[index] = (PSOBitSet)m_population[index].clone();			
		} else if (Utils.eq(merit, m_bestPopulation[index].getObjective())) {
			if (countFeatures(chromosome) < countFeatures(m_bestPopulation[index].getChromosome())) {
				m_bestPopulation[index] = (PSOBitSet)m_population[index].clone();			
			}
		}
	} // updateBestPopulation

	
	/**
	 * calculates summary statistics for the current population
	 */
	private void populationStatistics() {
		int i;

		m_sumFitness = m_minFitness = m_maxFitness = 
			m_population[0].getObjective();

		for (i=1;i<m_popSize;i++) {
			m_sumFitness += m_population[i].getObjective();
			if (m_population[i].getObjective() > m_maxFitness) {
				m_maxFitness = m_population[i].getObjective();
			}
			else if (m_population[i].getObjective() < m_minFitness) {
				m_minFitness = m_population[i].getObjective();
			}
		}
		m_avgFitness = (m_sumFitness / m_popSize);
	} // populationStatistics

	
	/**
	 * scales the raw (objective) merit of the population members
	 */
	private void scalePopulation() {
		int j;
		double a = 0;
		double b = 0;
		double fmultiple = 2.0;
		double delta;

		// prescale
		if (m_minFitness > ((fmultiple * m_avgFitness - m_maxFitness) / 
				(fmultiple - 1.0))) {
			delta = m_maxFitness - m_avgFitness;
			a = ((fmultiple - 1.0) * m_avgFitness / delta);
			b = m_avgFitness * (m_maxFitness - fmultiple * m_avgFitness) / delta;
		}
		else {
			delta = m_avgFitness - m_minFitness;
			a = m_avgFitness / delta;
			b = -m_minFitness * m_avgFitness / delta;
		}

		// scalepop
		m_sumFitness = 0;
		for (j=0;j<m_popSize;j++) {
			if (a == Double.POSITIVE_INFINITY || a == Double.NEGATIVE_INFINITY ||
					b == Double.POSITIVE_INFINITY || b == Double.NEGATIVE_INFINITY) {
				m_population[j].setFitness(m_population[j].getObjective());
			} else {
				m_population[j].setFitness(Math.abs((a * m_population[j].getObjective() + b)));
			}
			m_sumFitness += m_population[j].getFitness();
		}
	} // scalePopulation
	
	
	/**
	 * checks if any population members in the current population
	 * are better than the best found so far.
	 * 
	 * @throws Exception if something goes wrong
	 */
	private void checkBest() throws Exception {
		// Convergence: This method has changed respect to the version 1.0.0.
		// Convergence is not reached when all particles in the swarm have the same fitness. (version 1.0.0 did it)
		// Convergence is reached when all particles remains in the same position during several iterations.
		// Checking convergence add extra computational cost, and we are not interested in that so far.
		int i,count,lowestCount = m_numAttribs;
		boolean sameFitness = false;
		PSOBitSet currentBest = m_population[0];
		double currentBestFitness = currentBest.getObjective();
		int currentBestCount = countFeatures(currentBest.getChromosome()); 

		if (m_maxFitness - m_minFitness > 0) {
			// find the best in this population
			for (i=1;i<m_popSize;i++) {
				if (m_population[i].getObjective() > currentBestFitness) {
					currentBestFitness = m_population[i].getObjective();
					currentBest = m_population[i];
					currentBestCount = countFeatures(currentBest.getChromosome());
				} else if (Utils.eq(m_population[i].getObjective(), currentBestFitness)) {
					// see if it contains fewer features
					count = countFeatures(m_population[i].getChromosome());
					if (count < m_partialBestFeatureCount) {
						currentBestFitness = m_population[i].getObjective();
						currentBest = m_population[i];
						currentBestCount = count;
					}
				}
			}
		} else {
			// look for the smallest subset
			for (i=1;i<m_popSize;i++) {
				count = countFeatures(m_population[i].getChromosome());	
				if (count < lowestCount) {
					lowestCount = count;
					currentBest = m_population[i];
					currentBestCount = count;
				} // if-count
			} // for-pop
			sameFitness = true;
		} // if-fitness
		
		m_partialBest = (PSOBitSet)currentBest.clone();
		m_partialBestFeatureCount = currentBestCount;

		// compare the best particle in the the swarm in this iteration (partial best) 
		// to the best found so far (total best)
		if (m_totalBest == null) {
			m_totalBest = (PSOBitSet)currentBest.clone();
			m_totalBestFeatureCount = currentBestCount;
		} else if (currentBestFitness > m_totalBest.getObjective()) {
			m_totalBest = (PSOBitSet)currentBest.clone();
			m_totalBestFeatureCount = currentBestCount;
		} else if (Utils.eq(m_totalBest.getObjective(), currentBestFitness)) {
			// see if the localbest has fewer features than the best so far
			if (currentBestCount < m_totalBestFeatureCount) {
				m_totalBest = (PSOBitSet)currentBest.clone();
				m_totalBestFeatureCount = currentBestCount;
			}
		}
		
		// keep track of the total best during all the iterations
		m_bestFitnessList.add(m_totalBest.getObjective());
		
		// save the best fitness track to a file??
		if (!getLogFile().isDirectory()) {
			StringBuffer message = new StringBuffer();
			if (sameFitness) {
				// "bfe" in the log file denotes that all particles have the same fitness
				message.append("bfe: " + ((Double)m_totalBest.getObjective()).doubleValue());
			} else {
				// on the contrary, "bf" just denotes the best fitness reached so far
				message.append("bf: " + ((Double)m_totalBest.getObjective()).doubleValue());
			}
		    // write 
		    Debug.writeToFile(getLogFile().getAbsolutePath(), message.toString(), true);
		} // if m_logFile		
		
	}

	
	/**
	 * counts the number of features in a subset
	 * @param featureSet the feature set for which to count the features
	 * @return the number of features in the subset
	 */
	private int countFeatures(BitSet featureSet) {
		int count = 0;
		for (int i=0;i<m_numAttribs;i++) {
			if (featureSet.get(i)) {
				count++;
			}
		}
		return count;
	}
	
	
	/**
	 * generates a report on the current population
	 * @return a report as a String
	 */
	private String populationReport (int genNum) {
		int i;
		StringBuffer temp = new StringBuffer();

		if (genNum == 0) {
			temp.append("\nInitial population\n");
		}
		else {
			temp.append("\nGeneration: "+genNum+"\n");
		}
		temp.append("merit   \tscaled  \tsubset\n");

		for (i=0;i<m_popSize;i++) {
			// Sebas
//			temp.append(Utils.doubleToString(Math.abs(m_population[i].getObjective()),8,5)
			temp.append(Utils.doubleToString(m_population[i].getObjective(),8,5)
					+"\t"
					+Utils.doubleToString(m_population[i].getFitness(),8,5)
					+"\t");
			temp.append(printPopMember(m_population[i].getChromosome())+"\n");
		}
		return temp.toString();
	} // populationReport

	
	/**
	 * prints a population member as a series of attribute numbers
	 * @param temp the chromosome of a population member
	 * @return a population member as a String of attribute numbers
	 */
	private String printPopMember(BitSet temp) {
		StringBuffer text = new StringBuffer();

		for (int j=0;j<m_numAttribs;j++) {
			if (temp.get(j)) {
				text.append((j+1)+" ");
			}
		}
		return text.toString();
	} // printPopMember

	
	/**
	 * prints a population member's chromosome
	 * @param temp the chromosome of a population member
	 * @return a population member's chromosome as a String
	 */
	private String printPopChrom(BitSet temp) {
		StringBuffer text = new StringBuffer();

		for (int j=0;j<m_numAttribs;j++) {
			if (temp.get(j)) {
				text.append("1");
			} else {
				text.append("0");
			}
		}
		return text.toString();
	}
	

	/**
	 * performs a single generation: apply 3PBMCX and mutation to all particles in the swarm
	 * @throws Exception if an error occurs
	 */
	private void generation() throws Exception {
		int i;
		
		for (i = 0; i < m_popSize; i++) {
			// first apply 3PBMCX to particle i
			m_population[i].threePBMCX(m_random, 
					m_currentW, m_bestGW, m_bestPW, 
					m_partialBest.getChromosome(),m_bestPopulation[i].getChromosome());
			
			// second apply mutation to particle i
			m_population[i].mutation(m_mutationType, m_random, m_pMutation);
		} // for
	} // generation
	
	
	/**
	 * return a description of the search
	 * @return a description of the search method
	 */
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append("\tPSO Search.\n\tStart set: ");
		
		if (m_starting == null) {
			result.append("no attributes");			
		} else {
			result.append(startSetToString());
		}
		
		result.append("\n\tPopulation size: " + m_popSize);
		result.append("\n\tNumber of iterations: " + m_maxGenerations);
		
		if (m_mutationType == BIT_FLIP) {
			result.append("\n\tMutation type: bit-flip");
		} else {
			result.append("\n\tMutation type: bit-off");
		}
		
		result.append("\n\tMutation probability: " + m_pMutation);
		result.append("\n\tInertia weight: " + m_currentW);
		result.append("\n\tSocial weight: " + m_bestGW);
		result.append("\n\tIndividual weight: " + m_bestPW);
		result.append("\n\tReport frequency: " + m_reportFrequency);
		result.append("\n\tSeed: " + m_seed);
		result.append("\n\tLog file: " + getLogFile() + "\n");
		
		result.append(m_generationReports.toString());
		
		return result.toString();
	}

	
	/**
	 * Returns an enumeration describing the available options.
	 * @return an enumeration of all the available options.
	 **/
	public Enumeration listOptions() {
		Vector newVector = new Vector(11);
		
		newVector.addElement(new Option("\tNumber of particles in the swarm (default = 20)", 
				                        "N",1,"-N <swarm size>"));
		
		newVector.addElement(new Option("\tNumber of iterations to perform (default = 20)",
				                        "I",1,"-I <iterations>"));
		
		newVector.addElement(new Option("\tType of mutation (default = bit-flip)",
				                        "T",1,"-T <0 = bit-flip | 1 = bit-off>"));
		
		newVector.addElement(new Option("\tSet the mutation probability (default = 0.01)",
				                        "M",1,"-M <mutation probability>"));
		
		newVector.addElement(new Option("\tInertia weight in 3PBMCX (default = 0.33)",
				                        "A",1,"-A <inertia weight>")); 
		
		newVector.addElement(new Option("\tSocial weight in 3PBMCX (default = 0.33)",
				                        "B",1,"-B <social weight>"));
		
		newVector.addElement(new Option("\tIndividual weight in 3PBMCX (default = 0.34)" +
				                        "\nIMPORTANT CONSTRAINT: " +
				                        "inertia weight + social weight + individual weight = 1 " +
				                        "and all these weights should be greater than or equal to zero!",
				                        "C",1,"-C <individual weight>"));
		
	    newVector.addElement(new Option("\tSpecify a starting set of attributes." 
                                        + "\n\tEg. 1,3,5-7."
                                        +"If supplied, the starting set becomes"
                                        +"\n\tone member of the initial random"
                                        +"\n\tpopulation."
                                        ,"P",1, "-P <start set>"));

	    newVector.addElement(new Option("\tSet frequency of reports."
                                        +"\n\te.g, setting the value to 5 will "
                                        +"\n\treport every 5 iterations"
                                        +"\n\t(default = number of iterations)" 
                                        ,"R",1,"-R <report frequency>"));
	    
	    newVector.addElement(new Option("\tSet the random number seed."
                                        +"\n\t(default = 1)" 
                                        ,"S",1,"-S <seed>"));
	    
	    newVector.addElement(new Option("\tSet the log file location."
	    		                        +"\n\tLog file just keeps track of the best fitness"
                                        +"\n\tfound through all the iterations."
	    		                        +"\n\t(default = null)"
	    		                        ,"L",1,"-L <log file name>"));
	    
		return newVector.elements();
		
	} // listOptions
	
	
	/**
	 * Parses a given list of options. <p/>
	 *
     <!-- options-start -->
	 * Valid options are: <p/>
	 * 
	 * <pre> -N &lt;swarm size&gt
	 * The number of particles in the swarm.
	 * (default = 20)</pre>
	 * 
	 * <pre> -I &lt;iterations&gt
	 * The number of iterations to perform.
	 * (default = 20)</pre>
	 * 
	 * <pre> -T &lt;mutation type&gt
	 * The type of mutation to be applied:
	 * 0 = bit-flip (default), 1 = bit-off.</pre>
	 * 
	 * <pre> -M &lt;mutation probability&gt
	 * The probability of mutation.
	 * (default = 0.01)</pre>
	 * 
	 * <pre> -A &lt;intertia weight&gt
	 * The inertia weight in 3PBMCX.
	 * (default = 0.33)</pre>
	 * 
	 * <pre> -B &lt;social weight&gt
	 * The social weight in 3PBMCX.
	 * (default = 0.33)</pre>
	 * 
	 * <pre> -C &lt;individual weight&gt
	 * The individual weight in 3PBMCX.
	 * (default = 0.34)
	 * IMPORTANT CONSTRAINT: &lt;inertia weight&gt + &lt;social weight&gt + &lt;individual weight&gt = 1
	 * and all these weights should be greater than or equal to zero!</pre>
	 *  
	 * <pre> -P &lt;start set&gt
	 *  Specify a starting set of attributes.
	 *  Eg. 1,3,5-7.If supplied, the starting set becomes
	 *  one member of the initial random population.</pre>
	 * 
	 * <pre> -R &lt;report frequency&gt
	 *  Set frequency of reports.
	 *  e.g, setting the value to 5 will 
	 *  report every 5 iterations
	 *  (default = number of generations)</pre>
	 * 
	 * <pre> -S &lt;seed&gt
	 *  Set the random number seed.
	 *  (default = 1)</pre>
	 *  
	 * <pre> -L &lt;log file name&gt
	 *  Set the log file location.
	 *  Log file just keeps track of the best fitness
     *  found through all the iterations.
	 *  (default = null)</pre>
	 * 
	 <!-- options-end -->
	 *
	 * @param options the list of options as an array of strings
	 * @throws Exception if an option is not supported
	 *
	 **/	
	public void setOptions(String[] options) throws Exception {
		String optionString;
		
		resetOptions();
		
		optionString = Utils.getOption('N', options);
		if (optionString.length() != 0) {
			setPopulationSize(Integer.parseInt(optionString));
		}
		
		optionString = Utils.getOption('I', options);
		if (optionString.length() != 0) {
			setIterations(Integer.parseInt(optionString));
		}
		
		optionString = Utils.getOption('T', options);
		if (optionString.length() != 0) {
			setMutationType(new SelectedTag(Integer.parseInt(optionString), TAGS_SELECTION));
		} else {
			setMutationType(new SelectedTag(BIT_FLIP, TAGS_SELECTION));
		}
		
	    optionString = Utils.getOption('M', options);
	    if (optionString.length() != 0) {
	    	setMutationProb(Double.parseDouble(optionString));
	    }
	    
	    optionString = Utils.getOption('A', options);
	    if (optionString.length() != 0) {
	    	setInertiaWeight(Double.parseDouble(optionString));
	    }
	    
	    optionString = Utils.getOption('B', options);
	    if (optionString.length() != 0) {
	    	setSocialWeight(Double.parseDouble(optionString));
	    }
	    
	    optionString = Utils.getOption('C', options);
	    if (optionString.length() != 0) {
	    	setIndividualWeight(Double.parseDouble(optionString));
	    }
	    
	    optionString = Utils.getOption('P', options);
	    if (optionString.length() != 0) {
	      setStartSet(optionString);
	    }
	    
	    optionString = Utils.getOption('R', options);
	    if (optionString.length() != 0) {
	      setReportFrequency(Integer.parseInt(optionString));
	    }
	    
	    optionString = Utils.getOption('S', options);
	    if (optionString.length() != 0) {
	      setSeed(Integer.parseInt(optionString));
	    }
	    
	    optionString = Utils.getOption('L', options);
	    if (optionString.length() != 0) {
	    	setLogFile(new File(optionString));
	    } else {
	    	setLogFile(new File(System.getProperty("user.dir")));
	    }
	    

	} // setOptions

	public String[] getOptions() {
		String[] options = new String[22];
		int current = 0;

		options[current++] = "-N";
		options[current++] = "" + getPopulationSize();
		
		options[current++] = "-I";
		options[current++] = "" + getIterations();
		
		options[current++] = "-T";
		options[current++] = "" + getMutationType();
		
		options[current++] = "-M";
		options[current++] = "" + getMutationProb();
		
		options[current++] = "-A";
		options[current++] = "" + getInertiaWeight();
		
		options[current++] = "-B";
		options[current++] = "" + getSocialWeight();
		
		options[current++] = "-C";
		options[current++] = "" + getIndividualWeight();
		
		if (!(getStartSet().equals(""))) {
			options[current++] = "-P";
			options[current++] = "" + startSetToString();
		}
		
		options[current++] = "-R";
		options[current++] = "" + getReportFrequency();
		
		options[current++] = "-S";
		options[current++] = "" + getSeed();
		
		options[current++] = "-L";
		options[current++] = getLogFile().toString();		

		while (current < options.length) {
			options[current++] = "";
		}

		return options;
	}

	
	/**
	 * reset to default values for options
	 */
	private void resetOptions () {
		m_population = null;
		m_bestPopulation = null;
		m_popSize = 20;
		m_lookupTableSize = 1001;
		m_currentW = 0.33;
		m_bestGW = 0.33;
		m_bestPW = 0.34;
		m_pMutation = 0.01;
		m_mutationType = BIT_FLIP;
		m_maxGenerations = 20;
		m_reportFrequency = m_maxGenerations;
		m_starting = null;
		m_startRange = new Range();
		m_seed = 1;
		m_logFile = new File(System.getProperty("user.dir"));
		m_bestFitnessList = new LinkedList();
	}
	
	
	/**
	 * Check if the configuration parameters are correctly set. If not, throws an exception.
	 * 
	 * @throws Exception
	 */
	private void checkOptions() throws Exception{
		
		if (m_popSize < 1) {	
			throw new Exception("Population size set to: " + m_popSize + ", cannot be less than 1!");
		} else if ((m_currentW < 0.0)||(m_bestGW < 0.0)||(m_bestPW < 0.0)||((m_currentW + m_bestGW + m_bestPW) != 1.0)) {
			throw new Exception("Inertia weight: " + m_currentW
					            + ", social weight: " + m_bestGW
					            + ", individual weight: " + m_bestPW
					            + " -> all these weights should be greater than or equal to zero and the sum must be equal to 1!");
		} else if ((m_pMutation < 0.0)||(m_pMutation > 1.0)) {
			throw new Exception("Mutation probability set to: " + m_pMutation + ", it must be a real number in the range [0.0, 1.0]!");
		} else if (m_maxGenerations < 1) {
			throw new Exception("Iterations set to: " + m_maxGenerations + ", cannot be less than 1!");
		}
		
	}


	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String populationSizeTipText() {
		return "Set the population size (particles in the swarm)";
	}

	/**
	 * set the population size
	 * @param p the size of the population
	 */
	public void setPopulationSize(int p) {
		m_popSize = p;
	}

	/**
	 * get the size of the population
	 * @return the population size
	 */
	public int getPopulationSize() {
		return m_popSize;
	}


	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */	
	public String iterationsTipText() {
		return "Set the number of iterations to perform.";
	}
	
	
	/**
	 * Set the number of iterations.
	 * 
	 * @param iterations the number of iterations to perform.
	 */
	public void setIterations(int iterations) {
		m_maxGenerations = iterations;
	}

	
	/**
	 * Get the number of iterations.
	 * 
	 * @return the number of iterations.
	 */
	public int getIterations() {
		return m_maxGenerations;
	}


	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String mutationProbTipText() {
		return "Set the probability of mutation.";
	}

	
	/**
	 * Set the probability of mutation
	 * 
	 * @param m the probability for mutation occuring
	 */
	public void setMutationProb(double m) {
		m_pMutation = m;
	}

	
	/**
	 * Get the probability of mutation
	 * 
	 * @return the probability of mutation occuring
	 */
	public double getMutationProb() {
		return m_pMutation;
	}
	
	
	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */	
	public String mutationTypeTipText() {
		return "Set the mutation type";
	}
	
	
	/**
	 * Set the mutation type
	 * 
	 * @param t type of mutation: 0 = bit-flip (default), 1 = bit-off
	 */
	public void setMutationType(SelectedTag t) {
		if (t.getTags() == TAGS_SELECTION) {
			m_mutationType = t.getSelectedTag().getID();
		}
	}

	
	/**
	 * Get the mutation type
	 * 
	 * @return the mutation type
	 */
	public SelectedTag getMutationType() {
		return new SelectedTag(m_mutationType,TAGS_SELECTION);
	}
	
	
	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String inertiaWeightTipText() {
		return "Set the inertia weight in 3PBMCX.";
	}
	
	
	/**
	 * Set the inertia weight in 3PBMCX.
	 * 
	 * @param w1 the inertia weight.
	 */
	public void setInertiaWeight(double w1) {
		m_currentW = w1;
	}
	
	
	/**
	 * Get the inertia weight in 3PBMCX.
	 * 
	 * @return the inertia weight.
	 */
	public double getInertiaWeight() {
		return m_currentW;
	}
	
	
	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String socialWeightTipText() {
		return "Set the social weight in 3PBMCX.";
	}
	
	
	/**
	 * Set the social weight in 3PBMCX.
	 * 
	 * @param w2 the social weight.
	 */
	public void setSocialWeight(double w2) {
		m_bestGW = w2;
	}
	
	
	/**
	 * Get the social weight in 3PBMCX.
	 * 
	 * @return the social weight.
	 */
	public double getSocialWeight() {
		return m_bestGW;
	}
	
	
	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String individualWeightTipText() {
		return "Set the individual weight in 3PBMCX."
		       + "\n\nIMPORTANT CONSTRAINT: inertia weight + social weight + individual weight = 1 "
		       + "and all these weights should be greater than or equal to zero!";
	}
	
	
	/**
	 * Set the individual weight in 3PBMCX.
	 * 
	 * @param w3 the individual weight.
	 */
	public void setIndividualWeight(double w3) {
		m_bestPW = w3;
	}
	
	
	/**
	 * Get the individual weight in 3PBMCX.
	 * 
	 * @return the individual weight.
	 */
	public double getIndividualWeight() {
		return m_bestPW;
	}


	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String startSetTipText() {
		return "Set a start point for the search. This is specified as a comma "
		+"seperated list off attribute indexes starting at 1. It can include "
		+"ranges. Eg. 1,2,5-9,17. The start set becomes one of the population "
		+"members of the initial population.";
	}

	
	/**
	 * Sets a starting set of attributes for the search. It is the
	 * search method's responsibility to report this start set (if any)
	 * in its toString() method.
	 * @param startSet a string containing a list of attributes (and or ranges),
	 * eg. 1,2,6,10-15.
	 * @throws Exception if start set can't be set.
	 */
	public void setStartSet (String startSet) throws Exception {
		m_startRange.setRanges(startSet);
	}

	
	/**
	 * Returns a list of attributes (and or attribute ranges) as a String
	 * @return a list of attributes (and or attribute ranges)
	 */
	public String getStartSet () {
		return m_startRange.getRanges();
	}
	
	
	/**
	 * converts the array of starting attributes to a string. This is
	 * used by getOptions to return the actual attributes specified
	 * as the starting set. This is better than using m_startRanges.getRanges()
	 * as the same start set can be specified in different ways from the
	 * command line---eg 1,2,3 == 1-3. This is to ensure that stuff that
	 * is stored in a database is comparable.
	 * @return a comma seperated list of individual attribute numbers as a String
	 */
	private String startSetToString() {
		StringBuffer FString = new StringBuffer();
		boolean didPrint;

		if (m_starting == null) {
			return getStartSet();
		}

		for (int i = 0; i < m_starting.length; i++) {
			didPrint = false;

			if ((m_hasClass == false) || 
					(m_hasClass == true && i != m_classIndex)) {
				FString.append((m_starting[i] + 1));
				didPrint = true;
			}

			if (i == (m_starting.length - 1)) {
				FString.append("");
			}
			else {
				if (didPrint) {
					FString.append(",");
				}
			}
		}

		return FString.toString();
	}


	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String reportFrequencyTipText() {
		return "Set how frequently reports are generated. Default is equal to "
		+"the number of iterations meaning that a report will be printed for "
		+"initial and final generations. Setting the value to 5 will result in "
		+"a report being printed every 5 iterations.";
	}
	

	/**
	 * set how often reports are generated
	 * @param f generate reports every f iterations
	 */
	public void setReportFrequency(int f) {
		m_reportFrequency = f;
	}
	
	
	/**
	 * get how often reports are generated
	 * @return how often reports are generated
	 */
	public int getReportFrequency() {
		return m_reportFrequency;
	}

	
	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String seedTipText() {
		return "Set the random seed.";
	}

	
	/**
	 * set the seed for random number generation
	 * @param s seed value
	 */
	public void setSeed(int s) {
		m_seed = s;
	}

	
	/**
	 * get the value of the random number generator's seed
	 * @return the seed for random number generation
	 */
	public int getSeed() {
		return m_seed;
	}	
	
	
	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */	
	public String logFileTipText() {
		return "Set the log file name. Log file just keeps track of the best fitness "
               +"found through all the iterations.";
	}
	
	
	/** 
	 * set the filename for the log file
	 * @param filename
	 */
	public void setLogFile(File filename) {
		m_logFile = filename;
	}
	
	
	/**
	 * get the log file descriptor 
	 * @return the log file descriptor
	 */
	public File getLogFile() {
		return m_logFile;
	}

	/**
	 * Returns an instance of a TechnicalInformation object, containing 
	 * detailed information about the technical background of this class,
	 * e.g., paper reference or book this class is based on.
	 * 
	 * @return the technical information about this class
	 */
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result;
		
		result = new TechnicalInformation(Type.INPROCEEDINGS);
		result.setValue(Field.AUTHOR, "Moraglio, A. and Di Chio, C. and Poli, R.");
		result.setValue(Field.TITLE, "Geometric Particle Swarm Optimisation");
		result.setValue(Field.BOOKTITLE, "Proceedings of the 10th European Conference on Genetic Programming");
		result.setValue(Field.SERIES, "EuroGP'07. LNCS 4445");
		result.setValue(Field.YEAR, "2007");
		result.setValue(Field.PAGES, "125-136");
		result.setValue(Field.PUBLISHER, "Springer-Verlag");
		result.setValue(Field.ADDRESS, "Berlin, Heidelberg");
		
		return result;
	}
	
	
	public TechnicalInformation getTechnicalInformationNEO() {
		TechnicalInformation result;
		
		result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(Field.AUTHOR, "García-Nieto, J.M. and Alba, E. and Jourdan, L. and Talbi, E.-G.");
		result.setValue(Field.TITLE, "Sensitivity and specificity based multiobjective approach for feature selection:" +
				                     " Application to cancer diagnosis");
		result.setValue(Field.JOURNAL, "Information Processing Letters");
		result.setValue(Field.YEAR, "2009");
		result.setValue(Field.VOLUME, "109");
		result.setValue(Field.NUMBER, "16");
		result.setValue(Field.PAGES, "887-896");
		result.setValue(Field.MONTH, "July");
		
		return result;
	}
	
	
	/**
	 * Returns a string describing this search method
	 * @return a description of the search suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String globalInfo() {
		return 
		"PSOSearch explores the attribute space using the Particle "
		+ "Swarm Optimization (PSO) algorithm. For more information, see:\n\n"
		+ getTechnicalInformation().toString()
		+ "\n\nFor an application to "
		+ "gene expression data classification, see:\n\n"
		+ getTechnicalInformationNEO().toString()
		+ "\n\nIMPORTANT: To ensure the correct behavior of PSOSearch"
		+ " it is mandatory for the class attribute to be the last one in the ARFF file.";
	}

}
