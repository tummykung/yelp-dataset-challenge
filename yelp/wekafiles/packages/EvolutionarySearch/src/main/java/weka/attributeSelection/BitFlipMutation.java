package weka.attributeSelection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

/**
 * This class performs the bit-flip mutation in a population of individuals.
 *
 */
public class BitFlipMutation extends Mutation {

	/** for serialization */
	private static final long serialVersionUID = -633202364032253621L;
	
	/** mutation probability */
	private double mutationProb;
	
	/** random number generator */
	private Random random;
	
	/** index of the class attribute */
	private int classIndex;
		
	
	/**
	 * Construct a new Bit Flip Mutation operator
	 * @param parameters random, classIndex and probability 
	 */
	public BitFlipMutation(HashMap<String, Object> parameters) {
		
		super(parameters);
		
		if (parameters.get("probability") != null) {
			mutationProb = (Double) parameters.get("probability"); 
		}
		
		if (parameters.get("random") != null) {
			random = (Random) parameters.get("random");
		}
		
		if (parameters.get("classIndex") != null) {
			classIndex = (Integer) parameters.get("classIndex");
		}
				
	}
	
	/**
	 * Applies bit-flip mutation to a population of individuals
	 * 
	 * @param population to apply the bit-flip mutation
	 */
	public void doMutation(Population population) {

		Iterator<Individual> iterator;
		Individual individual;
		
		iterator = population.getIterator();
		
		// the two first individuals are the best one of the previous generation
		// so we avoid applying mutation to them (elitism)
		iterator.next();
		iterator.next();
		
		// we apply the bit-flip mutation to the remaining individuals
		while (iterator.hasNext()) {
			
			individual = iterator.next();
			
			for (int i = 0; i < individual.getSize(); i++) {
				
				if (i != classIndex) {
					
					if (random.nextDouble() < mutationProb) {
						
						individual.flip(i);
						
					} // if random
					
				} // if classIndex
				
			} // for each individual
			
		} // while - iterator
		
	}

	/**
	 * execute method, inherited from the Operator abstract class
	 */
	@Override
	public Object execute(Object object) throws EvolutionaryException {
		
		Population population = (Population) object;
		
		doMutation(population);
		
		return population;
		
	}

}
