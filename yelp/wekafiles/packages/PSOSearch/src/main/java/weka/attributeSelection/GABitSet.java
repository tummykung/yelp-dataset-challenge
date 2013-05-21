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
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;

/**
 * A bitset for the genetic algorithm
 * 
 * @author Mark Hall (mhall@cs.waikato.ac.nz)
 * @version $Revision: 6759 $
 */

public class GABitSet implements Cloneable, Serializable, RevisionHandler {

    /** for serialization */
	private static final long serialVersionUID = 3789832354511857755L;

	/** the bitset */
    private BitSet m_chromosome;

    /** holds raw merit */
    private double m_objective = -Double.MAX_VALUE;
    
    /** the fitness */
    private double m_fitness;
    
    /**
     * Constructor
     */
    public GABitSet () {
      m_chromosome = new BitSet();
    }

    /**
     * makes a copy of this GABitSet
     * @return a copy of the object
     * @throws CloneNotSupportedException if something goes wrong
     */
    public Object clone() throws CloneNotSupportedException {
      GABitSet temp = new GABitSet();
      
      temp.setObjective(this.getObjective());
      temp.setFitness(this.getFitness());
      temp.setChromosome((BitSet)(this.m_chromosome.clone()));
      return temp;
      //return super.clone();
    }

    /**
     * sets the objective merit value
     * @param objective the objective value of this population member
     */
    public void setObjective(double objective) {
      m_objective = objective;
    }
      
    /**
     * gets the objective merit
     * @return the objective merit of this population member
     */
    public double getObjective() {
      return m_objective;
    }

    /**
     * sets the scaled fitness
     * @param fitness the scaled fitness of this population member
     */
    public void setFitness(double fitness) {
      m_fitness = fitness;
    }

    /**
     * gets the scaled fitness
     * @return the scaled fitness of this population member
     */
    public double getFitness() {
      return m_fitness;
    }

    /**
     * get the chromosome
     * @return the chromosome of this population member
     */
    public BitSet getChromosome() {
      return m_chromosome;
    }

    /**
     * set the chromosome
     * @param c the chromosome to be set for this population member
     */
    public void setChromosome(BitSet c) {
      m_chromosome = c;
    }

    /**
     * unset a bit in the chromosome
     * @param bit the bit to be cleared
     */
    public void clear(int bit) {
      m_chromosome.clear(bit);
    }

    /**
     * set a bit in the chromosome
     * @param bit the bit to be set
     */
    public void set(int bit) {
      m_chromosome.set(bit);
    }

    /**
     * get the value of a bit in the chromosome
     * @param bit the bit to query
     * @return the value of the bit
     */
    public boolean get(int bit) {
      return m_chromosome.get(bit);
    }
    
    /**
     * Returns the revision string.
     * 
     * @return		the revision
     */
    public String getRevision() {
      return RevisionUtils.extract("$Revision: 6759 $");
    }
    
}
