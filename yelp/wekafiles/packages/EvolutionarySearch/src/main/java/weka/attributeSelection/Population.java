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

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import weka.core.Utils;

/**
 * This class is used to model the concept of population
 * in Evolutionary Algorithms (EAs).
 * 
 * @author Sebastian Luna Valero
 *
 */
public class Population implements Cloneable, Serializable {

	/** for serialization */
	private static final long serialVersionUID = 1315209696221178349L;
		
	/** population itself */
	private Vector<Individual> population;

	
	/**
	 * Constructor of this class
	 * @param size
	 */
	public Population(int size) {
		population = new Vector<Individual>(size);
	}

	/**
	 * Add one individual to the population.
	 * 
	 * @param individual to be added
	 */
	public void addIndividual(Individual individual) {
		population.add(individual);
	}
	
	/**
	 * Get the individual at the specified index.
	 * 
	 * @param index of the individual to get
	 * @return the individual itself
	 */
	public Individual getIndividual(int index) {
		return population.get(index);
	}
	
	/**
	 * Remove the individual at the specified index.
	 * 
	 * @param index of the individual to be removed
	 * @return the removed individual
	 */
	public Individual removeIndividual(int index) {
		return population.remove(index);
	}
	
	/** 
	 * @return the size of the population
	 */
	public int getSize() {
		return population.size();
	}
	
	/**
	 * Remove all the elements in this population.
	 */
	public void clear() {
		population.clear();
	}
	
	/**
	 * Sort the population according to the comparator parameter.
	 * 
	 * @param comparator used to compare individual's fitness
	 * @throws EvolutionaryException if the given comparator is null
	 */
	public void sort(Comparator<Individual> comparator) throws EvolutionaryException {
		if (comparator == null) {
			throw new EvolutionaryException("Null comparator is not allowed!");
		} else {
			Collections.sort(population, comparator);
		}
	}
	
	/**
	 * 
	 * @return the individual with highest fitness value and lowest number of selected attributes
	 */
	public Individual getBestIndividual() {
		Iterator<Individual> iterator;
		Individual aux, result;
		
		result = getIndividual(0);
		iterator = getIterator();
		while (iterator.hasNext()) {
			aux = iterator.next();
			if (Utils.gr(aux.getFitness(),result.getFitness())) {
				result = aux;
			} else if (Utils.eq(aux.getFitness(), result.getFitness())) {
				if (aux.countOnes() < result.countOnes()) {
					result = aux;
				} // if - number of bits set to 1
			} // if - better fitness
		} // while - iterator
		
		return result;
	}
	
	/**
	 * @return a string representation of the population
	 */
	public String toString() {
		StringBuffer result;
		Iterator<Individual> iterator;
		
		result = new StringBuffer();
		iterator = getIterator();
		while (iterator.hasNext()) {
			result.append(iterator.next());
			result.append("\n");
		}

		return result.toString();
	}
	
	/**
	 * @return an iterator of the population
	 */
	public Iterator<Individual> getIterator() {
		return population.iterator();
	}

	/**
	 * Makes a copy of this population.
	 * 
	 * @return a copy of the population
	 * @throws CloneNotSupportedException if something goes wrong
	 */
	public Population clone() throws CloneNotSupportedException {
		Population result;
		Iterator<Individual> iterator;
		Individual individual;
		
		result = new Population(getSize());
		iterator = getIterator();
		while (iterator.hasNext()) {
			individual = iterator.next();
			result.addIndividual(individual.clone());
		}
		
		return result;
	}

}
