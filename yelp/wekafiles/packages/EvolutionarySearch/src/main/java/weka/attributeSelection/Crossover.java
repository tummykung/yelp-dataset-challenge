/*  Crossover.java in jMetal library: 
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
 * This class represents a crossover operator.
 * Every new crossover operator must extend this class.
 * This way, we keep an organized design. 
 *
 */
public abstract class Crossover extends Operator {

	/** for serialization */
	private static final long serialVersionUID = 4078630726308335792L;

	/** constructor for this class */
	public Crossover(HashMap<String, Object> parameters) {
		super(parameters);
	}

}
