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

import java.util.BitSet;
import java.util.Random;

/**
 * A bit set for the geometric PSO algorithm. Inherited from GABitSet.
 * 
 * @author Sebastian Luna Valero
 *
 */

public class PSOBitSet extends GABitSet {

	/** for serialization */
	private static final long serialVersionUID = 6139265110227962552L;
	
	/** the particle size */
	private int size;
	
	
	/**
	 * Constructs a new PSOBitSet object
	 * 
	 * @param size of the particle
	 */
	public PSOBitSet(int size) {
		super();
		this.size = size;
	}
	
	
	/**
	 * Performs the Three parent-based mask-crossover (3PBMCX) defined for the geometric PSO
	 * 
	 * @param r random number generator
	 * @param currentW inertia weight
	 * @param bestGW social weight
	 * @param bestPW individual weight
	 * @param bestG best global particle
	 * @param bestP best individual particle
	 */
	public void threePBMCX(Random r, double currentW, double bestGW, double bestPW, BitSet bestG, BitSet bestP) {
		int i;
		double p;
		
		for (i = 0; i < this.size-1; i++) {
			p = r.nextDouble();
			if (p <= currentW) {
				// nothing to do!
			} else if (p <= (currentW + bestGW)) {
				this.getChromosome().set(i,bestG.get(i));
			} else {
				this.getChromosome().set(i,bestP.get(i));
			} // if
		} // for
	} // ThreePBMCX
		
	
	/**
	 * apply the mutation selected by the user
	 * 
	 * @param r random number generator
	 * @param mutationP mutation probability
	 */
	public void mutation(int mutationType, Random r, double mutationP) {
		
		switch (mutationType) {
			case 0: bitFlipMutation(r, mutationP);	
			        break;
			
			case 1: bitOffMutation(r, mutationP);	
			        break;
			
			default: System.err.println("Unrecognized mutation type: Using default bit-flip!"); 
			         bitFlipMutation(r, mutationP); 
			         break;
		} // switch-case
	} // mutationType
	
	
	/**
	 * performs bit-flip mutation of the given particle
	 * 
	 * @param r random number generator
	 * @param mutationP mutation probability
	 */
	public void bitFlipMutation(Random r, double mutationP) {
		int i;
		double p;
		
		for (i = 0; i < this.size-1; i++) {
			p = r.nextDouble();
			if (p < mutationP) {
				if (this.get(i)) {
					this.clear(i);
				} else {
					this.set(i);
				}
			} // if-mutationP
		} // for
	} // bit-flip mutation
	
	
	/**
	 * switch off bits with the given probability
	 * 
	 * @param r random number generator
	 * @param mutationP mutation probatility
	 */
	public void bitOffMutation(Random r, double mutationP) {
		int i;
		double p;
		
		for (i = 0; i < this.size-1; i++) {
			p = r.nextDouble();
			if (p < mutationP) {
				if (this.get(i)) {
					this.clear(i);
				} 
			} // if-mutationP
		} // for		
	} // bit-off mutation
	
	
	/**
	 * count the number of bits set to 1
	 * 
	 * @return the number of bits set to 1
	 */
	public int countOnes() {
		int i, result = 0;
		
		for (i = 0; i < this.size; i++) {
			if (this.get(i)) {
				result++;
			}
		}
		
		return result;
	} // countOnes
	
	
	/**
	 * Implements a toString method for a PSOBitSet object.
	 *
	 * @return a string representing the bit set
	 */
	public String toString() {
		StringBuffer result = new StringBuffer();
		
		for (int i = 0; i < this.size; i++) {
			if (this.get(i)) {
				result.append("1");
			} else {
				result.append("0");
			}
		}
		
		return result.toString();
	}

	
	/**
	 * makes a copy of this PSOBitSet
	 * @return a copy of the object
	 * @throws CloneNotSupportedException if something goes wrong
	 */
	public Object clone() throws CloneNotSupportedException {
		PSOBitSet temp = new PSOBitSet(this.size);

		temp.setObjective(this.getObjective());
		temp.setFitness(this.getFitness());
		temp.setChromosome((BitSet)(this.getChromosome().clone()));
		return temp;
		
	}
	
	
	/**
	 * Returns the size of this PSOBitSet
	 * 
	 * @return the size of this PSOBitSet
	 */
	public int getSize() {
		return this.size;
	}
	
}
