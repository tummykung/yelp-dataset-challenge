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
import java.util.BitSet;
import weka.core.Utils;

/**
 * This class is used to represent an individual
 * in the context of Evolutionary Algorithms (EAs).
 * 
 * @author Sebastian Luna Valero
 *
 */
public class Individual implements Cloneable, Serializable {

	/** for serialization */
	private static final long serialVersionUID = -8261024970984469773L;

	/** the chromosome of the individual */
	private BitSet chromosome;
	
	/** the size of the individual */
	private int size;
	
	/** the fitness of the individual */
	private double fitness;

	
	/**
	 * Constructor of this class
	 * @param size of the individual
	 */
	public Individual(int size) {
		this.chromosome = new BitSet();
		this.size = size;
		this.setFitness(-Double.MAX_VALUE);
	}
	
	/**
	 * unset the bit at position i
	 * @param index of the bit
	 */
	public void clear(int index) {
		this.chromosome.clear(index);
	}
	
	/**
	 * set the bit at position i
	 * @param index of the bit
	 */
	public void set(int index) {
		this.chromosome.set(index);
	}

	/**
	 * obtain the value of the i-th bit
	 * @param index of the bit
	 * @return the value of the bit
	 */
	public boolean get(int index) {
		return this.chromosome.get(index);
	}
	
	/**
	 * flip the value of bit i
	 * @param index of the bit
	 */
	public void flip(int index) {
		this.chromosome.flip(index);
	}

	/**
	 * @param chromosome the chromosome to set
	 */
	public void setChromosome(BitSet chromosome) {
		this.chromosome = chromosome;
	}

	/**
	 * @return the chromosome
	 */
	public BitSet getChromosome() {
		return chromosome;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * @return the number of bits set to 1
	 */
	public int countOnes() {
		return chromosome.cardinality();
	}

	/**
	 * 
	 * @return the list of attribute indexes selected by this individual
	 */
	public int[] attributeList() {
		BitSet group = getChromosome();
		int count = group.cardinality();
		int[] list = new int[count];
		
		count = 0;

		for (int i = 0; i < getSize(); i++) {
			if (group.get(i)) {
				list[count] = i;
				count++;
			}
		}

		return list;
	}

	/**
	 * This method returns the number of different bits between two individuals.
	 * 
	 * @param individual to compare
	 * @return the number of differences in both individuals
	 * @throws CloneNotSupportedException
	 */
	public int countDifferences(Individual individual) throws CloneNotSupportedException {
		Individual copy;
		copy = this.clone();
		copy.getChromosome().xor(individual.getChromosome());
		
		return copy.getChromosome().cardinality();
	}

	/**
	 * @param fitness the fitness to set
	 */
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	/**
	 * @return the fitness
	 */
	public double getFitness() {
		return fitness;
	}
	
	/**
	 * @return the string representation of the individual
	 */
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		result.append(Utils.doubleToString(fitness, 6, 4) + " -> ");
		
		for (int i = 0; i < size; i++) {
			if (chromosome.get(i)) {
				result.append(i+1 + " ");
			}
		}
		
		return result.toString();
	}
	
	/**
	 * This method is related to the toString method.
	 * The difference is that this method only prints
	 * the list of attributes selected by this individual.
	 * 
	 * @return the list of selected attributes (indexes).
	 */
	public String printIndividual() {
		StringBuffer result = new StringBuffer();
		
		for (int i = 0; i < size; i++) {
			if (chromosome.get(i)) {
				result.append(i+1 + " ");
			}
		}
		
		return result.toString();
	}
	
	/**
	 * makes a copy of this class
	 * @return a copy of the individual
	 */
	public Individual clone() {
		Individual tmp = new Individual(this.size);
		tmp.setFitness(this.fitness);
		tmp.setChromosome((BitSet) this.chromosome.clone());
		return tmp;
	}
	
}
