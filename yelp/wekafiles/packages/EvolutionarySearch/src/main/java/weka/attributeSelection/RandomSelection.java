package weka.attributeSelection;

import java.util.HashMap;
import java.util.Random;

/**
 * This class performs a random selection in a population of individuals.
 * 
 */
public class RandomSelection extends Selection {

	/** for serialization */
	private static final long serialVersionUID = 2312441194544475167L;
	
	/** random number generator */
	private Random random;
	
	
	/**
	 * Constructor for the RandomSelection operator
	 * 
	 * @param parameters 
	 */
	public RandomSelection(HashMap<String, Object> parameters) {
		
		super(parameters);
		
		if (parameters.get("random") != null) {
			random = (Random) parameters.get("random");
		}
		
	}

	/**
	 * Performs the random selection of the current population 
	 * 
	 * @param population to select individuals from
	 * @return a population with the selected individuals
	 */
	public Population doSelection(Population population) {
		Population offspring;
		IndividualComparator ic = new IndividualComparator();
		int times, i1, i2, size;
		
		// initialization
		size = population.getSize();
		offspring = new Population(size);
		
		try {
			// elitism is ensured by adding the best individual to the next generation
			// we sort the population according the their fitness value: from higher to lower
			population.sort(ic);
			// then, the individual in the first positions will have better fitness after sorting
			offspring.addIndividual(population.getIndividual(0).clone());
			offspring.addIndividual(population.getIndividual(1).clone());
		} catch (EvolutionaryException e) {
			System.err.println("Problem sorting population: " + population);
			e.printStackTrace();
			System.exit(-1);
		}
		
		// then, complete the next generation with randomly selected individuals
		times = 2;
		while (times < size) {
			i1 = random.nextInt(size);
			i2 = random.nextInt(size);
			while (i1 == i2) {
				i2 = random.nextInt(size);
			} // while-indexes
			offspring.addIndividual(population.getIndividual(i1).clone());
			offspring.addIndividual(population.getIndividual(i2).clone());
			times += 2;
		} // while-times		
		
		return offspring;
		
	}
	
	/**
	 * execute method, inherited from the Operator abstract class
	 */		
	@Override
	public Object execute(Object object) throws EvolutionaryException {
		Population population = (Population) object;
		Population offspring;
		
		offspring = doSelection(population);
		
		return offspring;
	}

}
