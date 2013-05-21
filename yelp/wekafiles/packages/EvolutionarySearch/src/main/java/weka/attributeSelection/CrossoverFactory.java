/*  CrossoverFactory.java in jMetal library: 
 *  http://jmetal.sourceforge.net/
 *
 *  Authors:
 *       Antonio J. Nebro <antonio@lcc.uma.es>
 *       Juan J. Durillo <durillo@lcc.uma.es>
 *
 *  Adapted to Weka by:
 *       Sebastian Luna <sebastian@lcc.uma.es>
 * 
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

/**
 * This class allows you to create different crossover operators.
 *
 */
public class CrossoverFactory {
	
	public static Crossover getCrossoverOperation(String name, HashMap<String, Object> parameters)
	throws EvolutionaryException {
		
		if (name.equalsIgnoreCase("SinglePointCrossover")) {
			return new SinglePointCrossover(parameters);
		} else {
			throw new EvolutionaryException("Crossover operator: " + name + "'not found'!");
		}
	}

}
