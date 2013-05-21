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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import weka.core.Debug;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.Range;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.Utils;

/**
 * EvolutionarySearch explores the attribute space using an Evolutionary Algorithm (EA).<br/>
 * <br/>
 * The EA is a (mu, lambda) one with the following operators:<br/>
 * - uniform random initialization<br/>
 * - binary tournament selection<br/>
 * - single point crossover<br/>
 * - bit flip mutation and<br/>
 * - generational replacement with elitism (i.e., the best individual is always kept)<br/>
 * <br/>
 * This package has been properly designed to easily add new operators.<br/>
 * Please, see the document entitled developers-guide.pdf available within this package.<br/>
 * <br/>
 * For more information about similar algorithms see: <a href="http://neo.lcc.uma.es">the NEO Group website</a><br/>
 * <br/>
 * 
 * @author Sebastian Luna Valero
 *
 */
public class EvolutionarySearch extends ASSearch
	implements StartSetHandler, OptionHandler {

	/** for serialization */
	private static final long serialVersionUID = 5496214978041680642L;
	
	/** random number generator */
	private Random random;
	
	/** seed for random number generation */
	private int seed;
	
	/** the lookup table to avoid re-evaluating individuals */
	private Hashtable<BitSet, Individual> lookupTable;
	
	/** number of entries in the lookup table */
	private int lookupTableSize;
	
	/** 
	 * Holds a starting set as an array of attributes. 
	 * It becomes one member of the initial random population.
	 */
	private int [] starting;

	/** holds the start set for the search as a Range */	
	private Range startRange;
	
	/** does the data have a class? */
	private boolean hasClass;

	/** holds the class index */
	private int classIndex;

	/** number of attributes in the data */
	private int sizeOfIndividuals;
	
	/** number of generations to evolve the population */
	private int generations;
	
	/** populations of individuals to evolve during the evolutionary search */
	private Population population, offspring, replacementArray[];
	
	/** population size */
	private int populationSize;
	
	/** class to store some statistics about the execution */
	private EvolutionaryStatistics stats;
	
	/** 
	 * frequency (in number of generations) to print 
	 * the status of the evolutionary search 
	 */
	private int reportFrequency;
	
	/** store the status of the evolutionary search */
	private StringBuffer generationReport;
	
	/** file to log important events/data */
	private File logFile;
	
	/** operators to characterize the evolutionary search */
	private Operator initOperator, 
	                 selectOperator, 
	                 crossoverOperator, 
	                 mutationOperator, 
	                 replacementOperator;
	
	/** type of initialization */
	private int initializationOp;
	
	protected static final int RANDOM_INITIALIZATION = 0;
	
	public static final Tag [] INITIALIZATION_TAG = {
		new Tag(RANDOM_INITIALIZATION, "random-init")
	};
	
	/** type of selection operator */
	private int selectionOp;
	
	protected static final int RANDOM_SELECTION = 0;
	protected static final int TOURNAMENT_SELECTION = 1;
	
	public static final Tag [] SELECTION_TAG = {
		new Tag(RANDOM_SELECTION, "random-selection"),
		new Tag(TOURNAMENT_SELECTION, "tournament-selection")
	};
	
	/** type of crossover */
	private int crossoverOp;
	
	protected static final int SINGLE_POINT_CROSSOVER = 0;
	
	public static final Tag [] CROSSOVER_TAG = {
		new Tag(SINGLE_POINT_CROSSOVER,  "spx-crossover")
	};
	
	/** crossover probability */
	private double crossoverProbability;
	
	/** type of mutation */
	private int mutationOp;
	
	protected static final int BIT_FLIP_MUTATION = 0;
	
	public static final Tag [] MUTATION_TAG = {
		new Tag(BIT_FLIP_MUTATION, "bit-flip")
	};
	
	/** mutation probability */
	private double mutationProbability;
	
	/** type of replacement */
	private int replacementOp;
	
	protected static final int GENERATIONAL_REPLACEMENT = 0;
	
	public static final Tag [] REPLACEMENT_TAG = {
		new Tag(GENERATIONAL_REPLACEMENT, "generational")
	};
	
	
	// --- Methods
	
	
	/** Constructor for this class */
	public EvolutionarySearch() {
		resetOptions();
	}
		
	/** 
	 * This is the inherited method to implement the search process
	 * in the selection of attributes. In our case, this method
	 * implements the Evolutionary Algorithm (EA).
	 * 
	 * @param ASEval is the attribute evaluator (selected by the user)
	 * @param data are the training instances
	 * @return an array of the attributes selected by the EA (array of indexes)
	 * @throws Exception if search can not be completed
	 */
	@Override
	public int[] search(ASEvaluation ASEval, Instances data)
	throws Exception {
		int i = 1;
		
		if (!(ASEval instanceof SubsetEvaluator)) {
			throw  new Exception(ASEval.getClass().getName() 
					+ " is not a " 
					+ "Subset evaluator!");
		}

		if (ASEval instanceof UnsupervisedSubsetEvaluator) {
			hasClass = false;
		}
		else {
			hasClass = true;
			classIndex = data.classIndex();
		}
		
		SubsetEvaluator ASEvaluator = (SubsetEvaluator)ASEval;
		sizeOfIndividuals = data.numAttributes();

		startRange.setUpper(sizeOfIndividuals-1);
		if (!(getStartSet().equals(""))) {
			starting = startRange.getSelection();
		}
		
		// check if the configuration parameters are correctly set!
		checkOptions();
				
		// initialize operators
		initOperators();
		// initialize the object to gather statistics
		stats = new EvolutionaryStatistics(populationSize, sizeOfIndividuals);

		// initialize population
		initOperator.execute(population);
		// evaluate population
		evaluate(ASEvaluator, population);
		// gather initial statistics
		stats.updateStats(population, 0); 
		// log the current max fitness
		writeLog("Max fitness track:");
		writeLog(Utils.doubleToString(stats.getMaxCurrentFitness(),6,4));
		// output first status report
		generationReport.append(populationReport(0));
		
		while (i <= generations) {
			// selection
			offspring = (Population) selectOperator.execute(population);
			// crossover
			crossoverOperator.execute(offspring);
			// mutation
			mutationOperator.execute(offspring);
			// replacement
			replacementArray[0] = population;
			replacementArray[1] = offspring;
			population = (Population) replacementOperator.execute(replacementArray);
			// re-evaluate the new population
			evaluate(ASEvaluator, population);
			// update statistics
			stats.updateStats(population, i);
			// log the current max fitness
			writeLog(Utils.doubleToString(stats.getMaxCurrentFitness(),6,4));
			// output status report, if required
			if ((i == generations)||(i % reportFrequency == 0)) {
				generationReport.append(populationReport(i));
			}
			// go to next generation
			i++;
		} // evolutionary loop

		// log the final statistics
		writeLog(stats.toString());
		
		return population.getBestIndividual().attributeList();
		
	} // evolutionary search

	/**
	 * Evaluates the fitness of all individuals in the population. 
	 * It uses a small cache to avoid re-evaluating the individuals. 
	 * 
	 * @param evaluator selected by the user 
	 * @param population of individuals to evaluate
	 * @throws Exception if something goes wrong with the evaluation
	 */
	private void evaluate(SubsetEvaluator evaluator, Population population)
	throws Exception {
		Iterator<Individual> iterator;
		Individual next, aux;
		BitSet rep;
		double fitness;
		
		iterator = population.getIterator();
		while (iterator.hasNext()) {
			next = iterator.next();
			// check if the individual has been previously evaluated
			if (lookupTable.containsKey(next.getChromosome())) {
				aux = lookupTable.get(next.getChromosome());
				next.setFitness(aux.getFitness());
			} else {
				fitness = evaluator.evaluateSubset(next.getChromosome());
				next.setFitness(fitness);
				rep = (BitSet) next.getChromosome().clone();
				aux = next.clone();
				lookupTable.put(rep, aux);
			} // if - lookup table
		} // while - iterator
	} // evaluate
	
	/**
	 * This method initializes the operators of the Evolutionary Algorithm:
	 * initialization, selection, crossover, mutation and replacement.
	 * It configures each operator with the input parameters entered by the user.
	 * 
	 * @throws EvolutionaryException if something goes wrong with the configuration
	 */
	private void initOperators() throws EvolutionaryException {
		HashMap<String, Object> parameters;
		
		// configure initialization
		parameters = new HashMap<String, Object>();
		parameters.put("random", random);
		parameters.put("populationSize", populationSize);
		parameters.put("classIndex", classIndex);
		parameters.put("sizeOfIndividuals", sizeOfIndividuals);
		parameters.put("hasClass", hasClass);
		parameters.put("startingSet", starting);
		initOperator = InitializationFactory.getInitializationOperator("RandomInitialization"
		                                                               ,parameters);
		parameters.clear();
		
		// configure selection
		parameters = new HashMap<String, Object>();
		if (selectionOp == RANDOM_SELECTION) {
			parameters.put("random", random);
			selectOperator = SelectionFactory.getSelectionOperator("RandomSelection"
															       ,parameters);
		} else {
			// default selection is tournament
			parameters.put("random", random);
			selectOperator = SelectionFactory.getSelectionOperator("BinaryTournament"
															       ,parameters);
		} // if - selection
		parameters.clear();
		
		// configure crossover
		parameters = new HashMap<String, Object>();
		parameters.put("random", random);
		parameters.put("crossoverProbability", crossoverProbability);
		parameters.put("sizeOfIndividuals", sizeOfIndividuals);
		crossoverOperator = CrossoverFactory.getCrossoverOperation("SinglePointCrossover"
				                                                   ,parameters);
		parameters.clear();
		
		// configure mutation
		parameters = new HashMap<String, Object>();
		parameters.put("probability", mutationProbability);
		parameters.put("random", random);
		parameters.put("classIndex", classIndex);
		mutationOperator = MutationFactory.getMutationOperator("BitFlipMutation"
				                                               ,parameters);
		parameters.clear();
		
		// configure replacement
		parameters = new HashMap<String, Object>();
        replacementOperator = ReplacementFactory.getReplacementOperator("GenerationalReplacement"
                                                                        ,parameters);
        parameters.clear();
        
        // array to hold parents and offspring and make the replacement
        replacementArray = new Population[2];
        
	} // initOperators
	
	/**
	 * Set the default values for the main variable in this class.
	 */
	private void resetOptions() {
		seed = 1;
		random = new Random(seed);
		lookupTableSize = 1001;
		lookupTable = new Hashtable<BitSet, Individual>(lookupTableSize);
		generations = 20;
		populationSize = 20;
		population = new Population(populationSize);
		offspring = new Population(populationSize);
		stats = null;
		initializationOp = RANDOM_INITIALIZATION;
		selectionOp = TOURNAMENT_SELECTION;
		crossoverOp = SINGLE_POINT_CROSSOVER;
		crossoverProbability = 0.6;
		mutationOp = BIT_FLIP_MUTATION;
		mutationProbability = 0.01;
		replacementOp = GENERATIONAL_REPLACEMENT;
		starting = null;
		startRange = new Range();
		reportFrequency = generations;
		generationReport = new StringBuffer();
		logFile = new File(System.getProperty("user.dir"));
	} // resetOptions
	
	/**
	 * Before running the EA, this method checks that the user parameters
	 * are correctly set.
	 * 
	 * @throws EvolutionaryException if any parameter has received a wrong value.
	 */
	private void checkOptions() throws EvolutionaryException {
		
		if (populationSize < 2) {
			throw new EvolutionaryException("Population size can not be: " + populationSize + 
					", but greater than or equal to 2!");
		} // if - population size
		
		if (sizeOfIndividuals < 2) {
			throw new EvolutionaryException("Number of attributes can not be: " + sizeOfIndividuals +
					", but greater than or equal to 2! (including the class attribute)");
		} // if - number of attributes
		
		if ((mutationProbability < 0.0)||(mutationProbability > 0.1)) {
			throw new EvolutionaryException("Probability of mutation can not be: " + mutationProbability +
					", but between 0.0 and 1.0!");
		} // if - mutation probability
		
		if ((crossoverProbability < 0.0)||(crossoverProbability > 1.0)) {
			throw new EvolutionaryException("Probability of crossover can not be: " + crossoverProbability +
					", but between 0.0 and 1.0!");
		} // if - crossover probability
		
		if (generations < 1) {
			throw new EvolutionaryException("Number of evaluations can not be: " + generations +
					", but greater than or equal to 1!");
		} // if - number of generations
		
	} // checkOptions

	/**
	 * Returns an enumeration describing the available options.
	 * @return an enumeration of all the available options.
	 **/
	public Enumeration<Option> listOptions() {

		Vector<Option> options = new Vector<Option>(13);
		
		options.addElement(new Option("\tNumber of individuals in the population (default = 20)"
									  ,"population-size",1,"-population-size <population size>"));
		
		options.addElement(new Option("\tNumber of generations to evolve the population (default = 20)"
				                      ,"generations",1,"-generations <number of generations>"));
		
		options.addElement(new Option("\tSet how to initialize the population: "
									 +"\n\t" + new SelectedTag(RANDOM_INITIALIZATION, INITIALIZATION_TAG) + " -> random initialization (default)"
				                      ,"init-op",1,"-init-op " + Tag.toOptionList(INITIALIZATION_TAG)));
		
		options.addElement(new Option("\tSet the type of selection:"
				                     +"\n\t" + new SelectedTag(RANDOM_SELECTION, SELECTION_TAG) + " -> random selection"
				                     +"\n\t" + new SelectedTag(TOURNAMENT_SELECTION, SELECTION_TAG) + " -> binary tournament selection (default)"
				                      ,"selection-op",1,"-selection-op " + Tag.toOptionList(SELECTION_TAG)));
		
		options.addElement(new Option("\tSet the type of crossover:"
									 +"\n\t" + new SelectedTag(SINGLE_POINT_CROSSOVER, CROSSOVER_TAG) + " -> single point crossover (default)"
				                      ,"crossover-op",1,"-crossover-op " + Tag.toOptionList(CROSSOVER_TAG)));
		
		options.addElement(new Option("\tSet the crossover probability (default = 0.6)"
				                      ,"crossover-probability",1,"-crossover-probability <probability>"));
		
		options.addElement(new Option("\tType of mutation:"
									 +"\n\t" + new SelectedTag(BIT_FLIP_MUTATION, MUTATION_TAG) + " -> bit flip mutation (default)"
				                      ,"mutation-op",1,"-mutation-op " + Tag.toOptionList(MUTATION_TAG)));
		
		options.addElement(new Option("\tSet the mutation probability (default = 0.01)"
									  ,"mutation-probability",1,"-mutation-probability <probability>"));
		
		options.addElement(new Option("\tSet the type of replacement:"
									 +"\n\t" + new SelectedTag(GENERATIONAL_REPLACEMENT, REPLACEMENT_TAG) + " -> generational replacement (default)"
				                     ,"replacement-op",1,"-replacement-op " + Tag.toOptionList(REPLACEMENT_TAG)));
		
	    options.addElement(new Option("\tSpecify a starting set of attributes." 
                					  + "\n\tEg. 1,3,5-7."
                					  +"If supplied, the starting set becomes"
                					  +"\n\tone member of the initial random"
                					  +"\n\tpopulation."
                					  ,"starting-set",1, "-starting-set <start set>"));
	    	    
	    options.addElement(new Option("\tSet the random number seed."
                					  +"\n\t(default = 1)" 
                					  ,"seed",1,"-seed <seed>"));
	    
	    options.addElement(new Option("\tSet the frequency to print the status of the evolutionary search."
	    		                      ,"report-frequency",1,"-report-frequency <frequency>"));
	    
	    options.addElement(new Option("\tSet the name (location) for the log file."
	    		                      ,"log-file",1,"-log-file <name>"));
	    
		return options.elements();
		
	} // listOptions

	/**
	 * Parses a given list of options.
	 * 
	 * <pre> -population-size &lt;number of individuals&gt
	 * The number of individuals in the population.
	 * (default = 20)</pre>
	 * 
	 * <pre> -generations &lt;number of generations&gt
	 * The number of generations to perform.
	 * (default = 20)</pre>
	 * 
	 * <pre> -init-op &lt;type of initialization&gt
	 * The operator to initialize the population.
	 * Available operators:
	 * 0 = random initialization (default)</pre>
	 * 
	 * <pre> -selection-op &lt;type of selection&gt
	 * The operator to select individuals from the population.
	 * Available operators:
	 * 0 = random selection
	 * 1 = binary tournament (default)</pre>
	 * 
	 * <pre> -crossover-op &lt;type of crossover&gt
	 * The operator to cross individuals with each other.
	 * Available operators: 
	 * 0 = single-point crossover (default)</pre>
	 * 
	 * <pre> -crossover-probability &lt;probability&gt
	 * The probability of crossover.
	 * (default = 0.6)</pre>
	 * 
	 * <pre> -mutation-op &lt;type of mutation&gt
	 * The operator to mutate individuals.
	 * Available operators:
	 * 0 = bit-flip mutation (default)</pre>
	 * 
	 * <pre> -mutation-probability &lt;probability&gt
	 * The probability of mutation.
	 * (default = 0.01)</pre>
	 * 
	 * <pre> -replacement-op &lt;type of replacement&gt
	 * The operator to perform replacement.
	 * Available operators:
	 * 0 = generational with elitism (default)</pre>
	 * 
	 * <pre> -report-frequency &lt;number of iterations&gt
	 * The number of iterations to report the population status.
	 * (default = 20)</pre>
	 * 
	 * <pre> -seed &lt;seed&gt
	 * Set the random number seed.
	 * (default = 1)</pre>
	 * 
	 * <pre> -starting-set &lt;list of attribute indexes&gt
	 * Specify a starting set of attributes.
	 * Eg. 1,3,5-7.If supplied, the starting set becomes
	 * one member of the initial population.</pre>
	 * 
	 * <pre> -log-file &lt;file name&gt
	 * Set the log file location.
	 * Log file keeps track of the best fitness found
	 * during all the iterations.
	 * (default = null) </pre>
	 * 
	 * @param options the list of options as an array of strings.
	 * @throws Exception if an option is not supported.
	 */
	public void setOptions(String[] options) throws Exception {
		String optionString;
		
		resetOptions();
		
		optionString = Utils.getOption("population-size", options);
		if (optionString.length() != 0) {
			setPopulationSize(Integer.parseInt(optionString));
		}
		
		optionString = Utils.getOption("generations", options);
		if (optionString.length() != 0) {
			setGenerations(Integer.parseInt(optionString));
		}
		
		optionString = Utils.getOption("init-op", options);
		if (optionString.length() != 0) {
			setInitializationOperator(new SelectedTag(Integer.parseInt(optionString), INITIALIZATION_TAG));
		}
		
		optionString = Utils.getOption("selection-op", options);
		if (optionString.length() != 0) {
			setSelectionOperator(new SelectedTag(Integer.parseInt(optionString), SELECTION_TAG));
		}
		
		optionString = Utils.getOption("crossover-op", options);
		if (optionString.length() != 0) {
			setCrossoverOperator(new SelectedTag(Integer.parseInt(optionString), CROSSOVER_TAG));
		}
		
		optionString = Utils.getOption("crossover-probability", options);
		if (optionString.length() != 0) {
			setCrossoverProbability(Double.parseDouble(optionString));
		}
		
		optionString = Utils.getOption("mutation-op", options);
		if (optionString.length() != 0) {
			setMutationOperator(new SelectedTag(Integer.parseInt(optionString), MUTATION_TAG));
		} 
		
		optionString = Utils.getOption("mutation-probability", options);
		if (optionString.length() != 0) {
			setMutationProbability(Double.parseDouble(optionString));
		}
		
		optionString = Utils.getOption("replacement-op", options);
		if (optionString.length() != 0) {
			setReplacementOperator(new SelectedTag(Integer.parseInt(optionString), REPLACEMENT_TAG));
		}
		
	    optionString = Utils.getOption("starting-set", options);
	    if (optionString.length() != 0) {
	      setStartSet(optionString);
	    }
	    		
	    optionString = Utils.getOption("seed", options);
	    if (optionString.length() != 0) {
	      setSeed(Integer.parseInt(optionString));
	    }
	    
	    optionString = Utils.getOption("report-frequency", options);
	    if (optionString.length() != 0) {
	    	setReportFrequency(Integer.parseInt(optionString));
	    }
	    
	    optionString = Utils.getOption("log-file", options);
	    if (optionString.length() != 0) {
	    	setLogFile(new File(optionString));
	    }
		
	} // setOptions
	
	/**
	 * 
	 * @return an array of strings suitable for passing to setOptions()
	 */
	public String[] getOptions() {
		String [] options = new String[26];
		int current = 0;
		
		options[current++] = "-population-size";
		options[current++] = "" + getPopulationSize();
		
		options[current++] = "-generations";
		options[current++] = "" + getGenerations();
		
		options[current++] = "-init-op";
		options[current++] = "" + getInitializationOperator();
		
		options[current++] = "-selection-op";
		options[current++] = "" + getSelectionOperator();
		
		options[current++] = "-crossover-op";
		options[current++] = "" + getCrossoverOperator();
		
		options[current++] = "-crossover-probability";
		options[current++] = "" + getCrossoverProbability();
		
		options[current++] = "-mutation-op";
		options[current++] = "" + getMutationOperator();
		
		options[current++] = "-mutation-probability";
		options[current++] = "" + getMutationProbability();
		
		options[current++] = "-replacement-op";
		options[current++] = "" + getReplacementOperator();
		
		if (!(getStartSet().equals(""))) {
			options[current++] = "-starting-set";
			options[current++] = "" + startSetToString();
		}
		
		options[current++] = "-seed";
		options[current++] = "" + getSeed();
		
		options[current++] = "-report-frequency";
		options[current++] = "" + getReportFrequency();
		
		options[current++] = "-log-file";
		options[current++] = "" + getLogFile();
		
		while (current < options.length) {
			options[current++] = "";
		}
		
		return options;
		
	} // getOptions

	/**
	 * @return a description of the search.
	 */
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append("\tEvolutionary Search.\n\tStart set: ");
		
		if (starting == null) {
			result.append("no attributes\n");
		} else {
			result.append(startSetToString() + "\n");
		}
		
		result.append("\tPopulation size: " + populationSize );
		result.append("\n\tNumber of generations: " + generations);
		
		result.append("\n\tInitialization: ");
		switch (initializationOp) {
			default: result.append("random initialization");
		} // switch - initialization
		
		result.append("\n\tSelection: ");
		switch (selectionOp) {
			case RANDOM_SELECTION: result.append("random selection");
			break;
			default: result.append("tournament selection");
		} // switch - selection
		
		result.append("\n\tCrossover: ");
		switch (crossoverOp) {
			default: result.append("single-point crossover");
		} // switch - crossover
		
		result.append("\n\tMutation: ");
		switch (mutationOp) {
			default: result.append("bit-flip mutation");
		} // switch - mutation
		
		result.append("\n\tReplacement: ");
		switch (replacementOp) {
			default: result.append("generational replacement with elitism");
		} // switch - replacement
		
		result.append("\n\tReport frequency: " + reportFrequency);
		result.append("\n\tSeed: " + seed);
		result.append("\n\tLog file: " + logFile + "\n");
		
		result.append(generationReport.toString());
		
		result.append(stats.toString());
		
		return result.toString();
		
	} // toString
	
	/**
	 * Returns a description of the population.
	 * 
	 * @param generation in which this method is called.
	 * @return a description of the population.
	 */
	private String populationReport(int generation) {
		StringBuffer result = new StringBuffer();
		
		if (generation == 0) {
			result.append("\nInitial population\n");
		} else {
			result.append("\nGeneration: " + generation + "\n");
		}
		
		result.append("fitness -> subset\n");
		result.append(population.toString());
		
		return result.toString();
	}
	
	private void writeLog(String message) {
		if (!getLogFile().isDirectory()) {
			Debug.writeToFile(getLogFile().getAbsolutePath(), message, true);
		}
	}
	
	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String populationSizeTipText() {
		return "Set the number of individuals in the population";
	}

	/**
	 * set the population size
	 * @param p the size of the population
	 */
	public void setPopulationSize(int p) {
		populationSize = p;
	}

	/**
	 * get the size of the population
	 * @return the population size
	 */
	public int getPopulationSize() {
		return populationSize;
	}	
	
	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String generationsTipText() {
		return "Set the number of generations to evolve the population";
	}

	/**
	 * set the number of generations
	 * @param i the number of generations
	 */
	public void setGenerations(int i) {
		generations = i;
	}
	
	/**
	 * get the number of generations
	 * @return the number of generations
	 */
	public int getGenerations() {
		return generations;
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
		seed = s;
	}

	
	/**
	 * get the value of the random number generator's seed
	 * @return the seed for random number generation
	 */
	public int getSeed() {
		return seed;
	}	
	
	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */	
	public String initializationOperatorTipText() {
		return "Set the initialization operator";
	}
	
	/**
	 * Set the initialization operator
	 * 
	 * @param t type of initialization
	 */
	public void setInitializationOperator(SelectedTag t) {
		if (t.getTags() == INITIALIZATION_TAG) {
			initializationOp = t.getSelectedTag().getID();
		}
	}
	
	/**
	 * Get the initialization operator
	 * 
	 * @return the initialization operator
	 */
	public SelectedTag getInitializationOperator() {
		return new SelectedTag(initializationOp, INITIALIZATION_TAG);
	}
	
	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */	
	public String selectionOperatorTipText() {
		return "Set the selection operator";
	}
	
	/**
	 * Set the selection operator
	 * 
	 * @param t type of selection
	 */
	public void setSelectionOperator(SelectedTag t) {
		if (t.getTags() == SELECTION_TAG) {
			selectionOp = t.getSelectedTag().getID();
		}
	}
	
	/**
	 * Get the selection operator
	 * 
	 * @return the selection operator
	 */
	public SelectedTag getSelectionOperator() {
		return new SelectedTag(selectionOp, SELECTION_TAG);
	}
	
	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */	
	public String crossoverOperatorTipText() {
		return "Set the crossover operator";
	}
	
	/**
	 * Set the crossover operator
	 * 
	 * @param t type of crossover
	 */
	public void setCrossoverOperator(SelectedTag t) {
		if (t.getTags() == CROSSOVER_TAG) {
			crossoverOp = t.getSelectedTag().getID();
		}
	}
	
	/**
	 * Get the crossover operator
	 * 
	 * @return the crossover operator 
	 */
	public SelectedTag getCrossoverOperator() {
		return new SelectedTag(crossoverOp, CROSSOVER_TAG);
	}
	
	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */	
	public String crossoverProbabilityTipText() {
		return "Set the crossover probability";
	}
	
	/**
	 * Set the probability of crossover
	 * 
	 * @param c the probability of crossover
	 */
	public void setCrossoverProbability(double c) {
		crossoverProbability = c;
	}
	
	/**
	 * Return the probability of crossover
	 * 
	 * @return the probability of crossover
	 */
	public double getCrossoverProbability() {
		return crossoverProbability;
	}
	
	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */	
	public String mutationOperatorTipText() {
		return "Set the mutation operator";
	}
	
	/**
	 * Set the mutation operator
	 * 
	 * @param t type of mutation
	 */
	public void setMutationOperator(SelectedTag t) {
		if (t.getTags() == MUTATION_TAG) {
			mutationOp = t.getSelectedTag().getID();
		}
	}
	
	/**
	 * Get the mutation operator
	 * 
	 * @return the mutation type
	 */
	public SelectedTag getMutationOperator() {
		return new SelectedTag(mutationOp, MUTATION_TAG);
	}
	
	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String mutationProbabilityTipText() {
		return "Set the probability of mutation.";
	}

	
	/**
	 * Set the probability of mutation
	 * 
	 * @param m the probability for mutation
	 */
	public void setMutationProbability(double m) {
		mutationProbability = m;
	}

	
	/**
	 * Get the probability of mutation
	 * 
	 * @return the probability of mutation
	 */
	public double getMutationProbability() {
		return mutationProbability;
	}
	
	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */	
	public String replacementOperatorTipText() {
		return "Set the replacement operator";
	}
	
	/**
	 * Set the replacement operator
	 * 
	 * @param t type of replacement
	 */
	public void setReplacementOperator(SelectedTag t) {
		if (t.getTags() == REPLACEMENT_TAG) {
			replacementOp = t.getSelectedTag().getID();
		}
	}
	
	/**
	 * Get the replacement operator
	 * 
	 * @return the replacement operator
	 */
	public SelectedTag getReplacementOperator() {
		return new SelectedTag(replacementOp, REPLACEMENT_TAG);
	}
	
	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String startSetTipText() {
		return "Set a start point for the search. This is specified as a comma "
		+"seperated list of attribute indexes starting at 1. It can include "
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
		startRange.setRanges(startSet);
	}

	
	/**
	 * @return a list of attributes (and or attribute ranges)
	 */
	public String getStartSet () {
		return startRange.getRanges();
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

		if (starting == null) {
			return getStartSet();
		}

		for (int i = 0; i < starting.length; i++) {
			didPrint = false;

			if ((hasClass == false) || 
					(hasClass == true && i != classIndex)) {
				FString.append((starting[i] + 1));
				didPrint = true;
			}

			if (i == (starting.length - 1)) {
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
		return "Set the frequency to print the status of the evolutionary search.";
	}

	/**
	 * set the report frequency
	 * @param f the frequency of the report
	 */
	public void setReportFrequency(int f) {
		reportFrequency = f;
	}

	/**
	 * get the frequency of the report
	 * @return the frequency of the report
	 */
	public int getReportFrequency() {
		return reportFrequency;
	}

	/**
	 * Returns the tip text for this property
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String logFileTipText() {
		return "Set the name for the log file.";
	}
	
	/**
	 * set the name for the log file 
	 * @param filename the name for the log file
	 */
	public void setLogFile(File filename) {
		logFile = filename;
	}
	
	/**
	 * get the log file descriptor
	 * @return the log file descriptor
	 */
	public File getLogFile() {
		return logFile;
	}
	
	
	/**
	 * Returns a string describing this search method
	 * @return a description of the search suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String globalInfo() {
		return 
		"EvolutionarySearch explores the attribute space using "
		+ "an Evolutionary Algorithm (EA)."
		+ "\n\n The EA is a (mu, lambda) one with the following operators:"
		+ "\n - uniform random initialization"
		+ "\n - binary tournament selection"
		+ "\n - single point crossover"
		+ "\n - bit flip mutation and"
		+ "\n - generational replacement with elitism"
		+ "\n  (i.e., the best individual is always kept)"
		+ "\n\n This package has been properly designed to easily add new operators."
		+ "\n\n Please, see the document entitled developers-guide.pdf available within this package."
		+ "\n\n For more information about similar algorithms see: http://neo.lcc.uma.es";
	}
}
