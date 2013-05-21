package weka.attributeSelection;

import java.util.HashMap;

/**
 * This class performs a generational replacement.
 *
 */
public class GenerationalReplacement extends Replacement {
	
	/** for serialization */
	private static final long serialVersionUID = -5195268576344463069L;

	
	/**
	 * Constructs an elitist generational replacement operator
	 * @param parameters for this class
	 */
	public GenerationalReplacement(HashMap<String, Object> parameters) {
		super(parameters);
	}
	
	/**
	 * Performs a simple generational replacement, where the offspring replaces
	 * the parents generation
	 * 
	 * @param parentsAndOffspring both population of individuals
	 * @return the offspring
	 */
	public Population doReplacement(Population[] parentsAndOffspring) {
		Population parents, offspring;

		parents   = parentsAndOffspring[0];
		offspring = parentsAndOffspring[1];
		
		parents.clear();
		
		return offspring;
	}
	
	/**
	 * execute method, inherited from the Operator abstract class
	 */		
	@Override
	public Object execute(Object object) throws EvolutionaryException {
		Population[] parentsAndOffspring;
		Population result;
		
		parentsAndOffspring = (Population[]) object;
		result = doReplacement(parentsAndOffspring);
		
		return result;
	}

}
