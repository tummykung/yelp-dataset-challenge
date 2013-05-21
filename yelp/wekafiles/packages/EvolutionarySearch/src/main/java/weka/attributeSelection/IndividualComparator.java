package weka.attributeSelection;

import java.util.Comparator;

import weka.core.Utils;

/**
 * This class is used to sort the population. Individuals with higher fitness
 * and lower number of selected attributes are placed first.
 *
 */
public class IndividualComparator implements Comparator<Individual> {

	/**
	 * Compares two individuals by both their fitness values
	 * and the number of selected attributes on each one. 
	 * 
	 * @param i1 first individual to compare
	 * @param i2 second individual to compare
	 * @return positive integer, zero or negative integer
	 * if i1 is less than, equal to or greater than i2, respectively
	 */	
	public int compare(Individual i1, Individual i2) {
		double f1, f2;
		int n1, n2, result = 0;
		
		// first, check if individuals are null
		if (i1 == null) {
			return -1;
		} else if (i2 == null) {
			return 1;
		}

		f1 = i1.getFitness();
		n1 = i1.countOnes();
		
		f2 = i2.getFitness();
		n2 = i2.countOnes();
		
		if (Utils.gr(f1, f2)) {
			// f1 > f2
			result = -1;
		} else if (Utils.sm(f1, f2)) {
			// f1 < f2
			result = 1;
		} else {
			// f1 == f2
			if (n1 < n2) {
				result = -1;
			} else if (n1 > n2) {
				result = 1;
			} // if - number of selected attributes
		} // if - fitness
		
		return result;
	}

}
