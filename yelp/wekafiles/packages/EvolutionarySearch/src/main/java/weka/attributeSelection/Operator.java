/*  Operator.java in jMetal library: 
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

import java.io.Serializable;
import java.util.HashMap;

/**
 * Class representing a general operator.
 *
 */
public abstract class Operator implements Serializable {

	/** for serialization */
	private static final long serialVersionUID = 6345840082684082175L;

	/** hash map to store any parameters */
	private HashMap<String,Object> parameters;
	
	
	/** Constructor */
	public Operator(HashMap<String,Object> parameters) {
		this.parameters = parameters;
	}
	
	abstract public Object execute(Object object) throws EvolutionaryException ;

	/**
	 * Set a new parameter to the operator
	 * @param name of the parameter
	 * @param value of the parameter
	 */
	public void setParameter(String name, Object value) {
		parameters.put(name, value);
	}
	
	/**
	 * Get the value of one parameter
	 * @param name of the parameter
	 * @return the value of the parameter
	 */
	public Object getParameter(String name) {
		return parameters.get(name);
	}
}
