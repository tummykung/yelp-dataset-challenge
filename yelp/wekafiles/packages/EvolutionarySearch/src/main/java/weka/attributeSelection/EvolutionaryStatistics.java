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

import java.util.Iterator;

import weka.core.Utils;

/**
 * This class gathers statistics for the Evolutionary Algorithm.
 * So far, it contains basic operators/operations but this is the class
 * to place new and more complex ones.
 * 
 * @author Sebastian Luna Valero
 *
 */
public class EvolutionaryStatistics {
	
	/** population size */
	private int populationSize;
	
	/** size of individuals */
	private int sizeOfIndividuals;

	/** minimum value of fitness in the current iteration */
	private double minCurrentFitness;
	
	/** individual with minimum current fitness */
	private Individual individualMinCurrentFitness;
	
	/** minimum value of fitness found during all the iterations */
	private double minFitnessFound;
	
	/** iteration in which the minimum fitness was found */
	private int iterationMinFitnessFound;
	
	/** individual with the minimum fitness found during all the iterations */
	private Individual individualMinFitnessFound;
	
	/** maximum value of fitness in the current iteration */
	private double maxCurrentFitness;
	
	/** individual with the maximum current fitness */
	private Individual individualMaxCurrentFitness;
	
	/** maximum value of fitness found during all the iterations */
	private double maxFitnessFound;
	
	/** iteration in which the maximum fitness was found */
	private int iterationMaxFitnessFound;
	
	/** individual with maximum fitness found during all the iterations */
	private Individual individualMaxFitnessFound;
	
	/** stores the fitness sum of the population in the current iteration */
	private double currentFitnessSum;
	
	/** last iteration in which this class gathered the statistics */
	private int lastUpdatedIteration;

	
	/**
	 * Constructor for this class.
	 */
	public EvolutionaryStatistics(int populationSize, int sizeOfIndividuals) {
		lastUpdatedIteration = -1;
		
		this.populationSize = populationSize;
		this.sizeOfIndividuals = sizeOfIndividuals;
		
		minCurrentFitness = Double.MAX_VALUE;
		minFitnessFound = Double.MAX_VALUE;
		iterationMinFitnessFound = -1;
		
		maxCurrentFitness = -Double.MAX_VALUE;
		maxFitnessFound = -Double.MAX_VALUE;
		iterationMaxFitnessFound = -1;
		
		currentFitnessSum = 0.0;
		
		individualMinCurrentFitness = new Individual(this.sizeOfIndividuals);
		individualMinFitnessFound = new Individual(this.sizeOfIndividuals);
		individualMaxCurrentFitness = new Individual(this.sizeOfIndividuals);
		individualMaxFitnessFound = new Individual(this.sizeOfIndividuals);
		
	} // constructor
	
	/**
	 * This method updates the statistics with the new population
	 * in the current iteration.
	 * 
	 * @param population to gather statistics from
	 * @param iteration in which the Evolutionary Algorithm is
	 * @throws EvolutionaryException if this method is called with an incorrect iteration number
	 */
	public void updateStats(Population population, int iteration) throws EvolutionaryException {
		Iterator<Individual> iterator;
		Individual aux;
		
		if (lastUpdatedIteration >= iteration) {
			throw new EvolutionaryException("Error within the updateStats procedure." + 
					"\nCurrent iteration is: " + iteration +
					"\nLast update in iteration: " + lastUpdatedIteration);
		}
		
		minCurrentFitness = maxCurrentFitness = population.getIndividual(0).getFitness();
		currentFitnessSum = 0.0;
		individualMaxCurrentFitness = population.getIndividual(0).clone();
		individualMinCurrentFitness = population.getIndividual(0).clone();
		
		iterator = population.getIterator();
		while(iterator.hasNext()) {
			aux = iterator.next();
			
			if (Utils.gr(aux.getFitness(), maxFitnessFound)) {
				maxFitnessFound = aux.getFitness();
				iterationMaxFitnessFound = iteration;
				individualMaxFitnessFound = aux.clone();
			}
			
			if (Utils.gr(aux.getFitness(), maxCurrentFitness)) {
				maxCurrentFitness = aux.getFitness();
				individualMaxCurrentFitness = aux.clone();
			}
			
			if (Utils.sm(aux.getFitness(), minFitnessFound)) {
				minFitnessFound = aux.getFitness();
				iterationMinFitnessFound = iteration;
				individualMinFitnessFound = aux.clone();
			}
			
			if (Utils.sm(aux.getFitness(), minCurrentFitness)) {
				minCurrentFitness = aux.getFitness();
				individualMinCurrentFitness = aux.clone();
			}
			
			currentFitnessSum += aux.getFitness();
			
		} // while - iterator
		
		lastUpdatedIteration = iteration;
		
	} // updateStats
	
	/**
	 * @return the lastIterationUpdated
	 */
	public int getLastUpdatedIteration() {
		
		return lastUpdatedIteration;
		
	}

	/**
	 * @return the minCurrentFitness
	 */
	public double getMinCurrentFitness() {
		return minCurrentFitness;
	}

	/**
	 * @return the individualMinCurrentFitness
	 */
	public Individual getIndividualMinCurrentFitness() {
		return individualMinCurrentFitness;
	}

	/**
	 * @return the minFitnessFound
	 */
	public double getMinFitnessFound() {
		return minFitnessFound;
	}

	/**
	 * @return the iterationMinFitnessFound
	 */
	public int getIterationMinFitnessFound() {
		return iterationMinFitnessFound;
	}

	/**
	 * @return the individualMinFitnessFound
	 */
	public Individual getIndividualMinFitnessFound() {
		return individualMinFitnessFound;
	}

	/**
	 * @return the maxCurrentFitness
	 */
	public double getMaxCurrentFitness() {
		return maxCurrentFitness;
	}

	/**
	 * @return the individualMaxCurrentFitness
	 */
	public Individual getIndividualMaxCurrentFitness() {
		return individualMaxCurrentFitness;
	}

	/**
	 * @return the maxFitnessFound
	 */
	public double getMaxFitnessFound() {
		return maxFitnessFound;
	}

	/**
	 * @return the iterationMaxFitnessFound
	 */
	public int getIterationMaxFitnessFound() {
		return iterationMaxFitnessFound;
	}

	/**
	 * @return the individualMaxFitnessFound
	 */
	public Individual getIndividualMaxFitnessFound() {
		return individualMaxFitnessFound;
	}

	/**
	 * @return the currentFitnessSum
	 */
	public double getCurrentFitnessSum() {
		return currentFitnessSum;
	}
	
	/**
	 * @return a string with information about the statistics 
	 */
	public String toString() {
		StringBuffer result;
		
		result = new StringBuffer();
		result.append("\n=== Statistics of the Evolutionary Search ===\n");
		result.append(" Last updated iteration: " + lastUpdatedIteration + "\n");
		result.append(" Current mean fitness: " + Utils.doubleToString((Double)currentFitnessSum/populationSize,6,4) + "\n");
		result.append(" Current max fitness: " + Utils.doubleToString(maxCurrentFitness,6,4) + "\n");
		result.append(" Current max fitness with individual: " + individualMaxCurrentFitness.printIndividual() + "\n");
		result.append(" Current min fitness: " + Utils.doubleToString(minCurrentFitness,6,4) + "\n");
		result.append(" Current min fitness with individual: " + individualMinCurrentFitness.printIndividual() + "\n");
		result.append(" Max fitness found: " + Utils.doubleToString(maxFitnessFound,6,4) + "\n");
		result.append(" Max fitness found in iteration: " + iterationMaxFitnessFound + "\n");
		result.append(" Max fitness found with individual: " + individualMaxFitnessFound.printIndividual() + "\n");
		result.append(" Min fitness found: " + Utils.doubleToString(minFitnessFound,6,4) + "\n");
		result.append(" Min fitness found in iteration: " + iterationMinFitnessFound + "\n");
		result.append(" Min fitness found with individual: " + individualMinFitnessFound.printIndividual() + "\n");
		
		return result.toString();
		
	}
	
}
