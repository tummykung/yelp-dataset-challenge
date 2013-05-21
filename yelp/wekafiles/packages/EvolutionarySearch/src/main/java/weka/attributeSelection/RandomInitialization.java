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

import java.util.HashMap;
import java.util.Random;

/**
 * This class creates a population of randomly initialized individuals.
 *
 */
public class RandomInitialization extends Initialization {

	/** for serialization */
	private static final long serialVersionUID = 3308142281862324347L;
	
	/** random number generator */
	private Random random;
	
	/** starting set of attributes */
	private int [] startingSet;
	
	/** class index */
	private int classIndex;
	
	/** size of individuals */
	private int sizeOfIndividuals;
	
	/** denote if the training data has class */
	private boolean hasClass;
	
	/** size of the population to be created */
	private int populationSize;
	
	
	/** Constructor */
	public RandomInitialization(HashMap<String, Object> parameters) {
		
		super(parameters);
		
		if (parameters.get("random") != null) {
			random = (Random) parameters.get("random");
		}
		
		if (parameters.get("populationSize") != null) {
			populationSize = (Integer) parameters.get("populationSize");
		}
		
		if (parameters.get("startingSet") != null) {
			startingSet = (int []) parameters.get("startingSet");
		}
		
		if (parameters.get("classIndex") != null) {
			classIndex = (Integer) parameters.get("classIndex");
		}
		
		if (parameters.get("sizeOfIndividuals") != null) {
			sizeOfIndividuals = (Integer) parameters.get("sizeOfIndividuals");
		}
		
		if (parameters.get("hasClass") != null) {
			hasClass = (Boolean) parameters.get("hasClass");
		}
		
	}

	/**
	 * Randomly initialize each individual of the population
	 * 
	 * @param population to be initialized
	 */
	public void doInitialization(Population population) {
		// IMPORTANT NOTE: according to our tests, the class attribute
		// should not be set to 1 since it causes problems in WEKA
		// when running the attribute selection method.
		// It seems that WEKA already include the class attribute even
		// if it has not been selected by the attribute selection method.
		// Therefore, selecting the class attribute in this code may cause
		// a problem, with the message: "Attribute names are not unique!"
		// when the class attribute is labelled as "class" in the arff file.
		int i, j, bit, num_bits, start;
		boolean ok;
		Individual individual;
		
		start = 0;
		
		// add the start set as the first population member (if specified)
		if (startingSet != null) {
			individual = new Individual(sizeOfIndividuals);
			
			for (i = 0; i < startingSet.length; i++) {
				if ((startingSet[i] != classIndex)
				  &&(startingSet[i] >= 0)
				  &&(startingSet[i] < sizeOfIndividuals)) {
						
					individual.set(startingSet[i]);
				}
			} 
			population.addIndividual(individual);
			start = 1;
		} // if - startingSet
				
		for (i = start; i < populationSize; i++) {			
			individual = new Individual(sizeOfIndividuals);
						
			num_bits = random.nextInt(sizeOfIndividuals - 1);
			
			// At least 1 attribute should always be selected			
			if (num_bits == 0) {
				num_bits++;
			}

			for (j = 0; j < num_bits; j++) {
				ok = false;
				do {
					
					bit = random.nextInt(sizeOfIndividuals);
					if (hasClass) {
						if (bit != classIndex) {
							ok = true;
						}
					}
					else {
						ok = true;
					}
				} while (!ok);
				
				individual.set(bit);
				
			} // for num_bits
			
			population.addIndividual(individual);
			
		} // for population
		
	}

	/**
	 * execute method, inherited from the Operator abstract class
	 */	
	@Override
	public Object execute(Object object) throws EvolutionaryException {
		
		Population population = (Population) object;
		
		doInitialization(population);
		
		return population;
	}

}
