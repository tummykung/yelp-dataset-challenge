package weka.attributeSelection;

import java.util.HashMap;
import java.util.Random;

/**
 * This class performs the single-point crossover in a population of individuals.
 *
 */
public class SinglePointCrossover extends Crossover {

	/** for serialization */
	private static final long serialVersionUID = 8377380071416052580L;
	
	/** crossover probability */
	private double crossoverProbability;
	
	/** random number generator */
	private Random random;
	
	/** size of individuals */
	private int sizeOfIndividuals;

	
	/**
	 * Constructor for a new single-point crossover
	 * @param parameters crossoverProbability, random number generator
	 */
	public SinglePointCrossover(HashMap<String, Object> parameters) {
		
		super(parameters);
		
		if (parameters.get("crossoverProbability") != null) {
			crossoverProbability = (Double) parameters.get("crossoverProbability");
		}
		
		if (parameters.get("random") != null) {
			random = (Random) parameters.get("random");
		}
		
		if (parameters.get("sizeOfIndividuals") != null) {
			sizeOfIndividuals = (Integer) parameters.get("sizeOfIndividuals");
		}
		
	}

	/**
	 * Performs the single point crossover
	 * @param population to apply the crossover (elitism is used)
	 * @throws EvolutionaryException if CloneNotSupportedException is thrown
	 */
	public void doSinglePointCrossover(Population population)
	throws EvolutionaryException {
		int i, j, cp;

		if (population.getSize() % 2 != 0) {
			throw new EvolutionaryException("Exception in SinglePointCrossover: " +
					                        "the intermediate population should have an even number of individuals");
		} // if - even number of individuals in the population
		
		if (population.getSize() < 1) {
			throw new EvolutionaryException("Exception in SinglePointCrossover: " +
					                        "the population should have, at least, 2 individuals and it has " + population.getSize());
		} // if - minimum number of individuals in the population
		
		// note that the two first individuals from the population
		// are the best individuals in the selection phase (elitism)
		// and they are not modified by the crossover operator
		for (i = 2; i < population.getSize(); i = i + 2) {

	        if (sizeOfIndividuals >= 3) {
	            if (random.nextDouble() < crossoverProbability) {
	              // Cross Point
	              cp = Math.abs(random.nextInt());
	              cp %= (sizeOfIndividuals - 2);
	              cp ++;
	              
	              // exchange the genetic info up to the cross point
	              for (j = 0; j < cp; j++) {
	            	  if (population.getIndividual(i).get(j)) {
	            		  if (!population.getIndividual(i+1).get(j)) {
	            			  population.getIndividual(i).clear(j);
	            		  }
	            		  population.getIndividual(i+1).set(j);
	            	  } else {
	            		  if (population.getIndividual(i+1).get(j)) {
	            			  population.getIndividual(i).set(j);
	            		  }
	            		  population.getIndividual(i+1).clear(j);
	            	  } // if-exchange
	              } // for-individual
	            } // if-probability
	          } // if-size
		} // for-population
		
	}	
	
	/**
	 * execute method, inherited from the Operator abstract class
	 */	
	@Override
	public Object execute(Object object) throws EvolutionaryException {
		
		Population population = (Population) object;
		
		doSinglePointCrossover(population);
		
		return population;
	}

}
