package weka.attributeSelection;

import java.util.HashMap;
import java.util.Random;

/**
 * Class to perform a binary tournament selection in a population of individuals. 
 *
 */
public class BinaryTournamentSelection extends Selection {
	
	/** for serialization */
	private static final long serialVersionUID = -764477252060156933L;
	
	private Random random;

	/**
	 * Constructor for this class
	 * 
	 * @param parameters 
	 */
	public BinaryTournamentSelection(HashMap<String, Object> parameters) {
		
		super(parameters);
		
		if (parameters.get("random") != null) {
			random = (Random) parameters.get("random");
		}
		
	}
	
	/**
	 * Performs the binary tournament selection
	 * 
	 * @param population to select individuals from
	 * @return a population with the selected individuals
	 */
	public Population doSelection(Population population) {
		Population offspring;
		IndividualComparator ic = new IndividualComparator();
		int size, i1, i2, times, flag;
		
		// initialization
		size = population.getSize();
		offspring = new Population(size);
		
		try {
			// elitism is ensured by adding the best individual to the next generation
			// we sort the population: individuals with higher fitness and lower number
			// of attributes are placed first
			population.sort(ic);
			// then, the individual in the first positions will have better fitness after sorting
			offspring.addIndividual(population.getIndividual(0).clone());
			offspring.addIndividual(population.getIndividual(1).clone());
		} catch (EvolutionaryException e) {
			System.err.println("Problem sorting population: " + population);
			e.printStackTrace();
			System.exit(-1);
		}
				
		times = 2;
		while (times < size) {
			i1 = random.nextInt(size);
			i2 = random.nextInt(size);
			while (i1 == i2) {
				i2 = random.nextInt(size);
			} // while-indexes

			flag = ic.compare(population.getIndividual(i1), population.getIndividual(i2));
			
			if (flag == -1) {
				// i1 is better than i2 (according to our comparator)
				offspring.addIndividual(population.getIndividual(i1).clone());
			} else if (flag == 1) {
				// i2 is better than i1 (according to our comparator)
				offspring.addIndividual(population.getIndividual(i2).clone());
			} else {
				// i1 is equal to i2
				// choose one of them randomly
				if (random.nextDouble() < 0.5) {
					offspring.addIndividual(population.getIndividual(i1).clone());
				} else {
					offspring.addIndividual(population.getIndividual(i2).clone());
				}
			} // if - IndividualComparator
			
			times++;
		} // while - times
		
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
