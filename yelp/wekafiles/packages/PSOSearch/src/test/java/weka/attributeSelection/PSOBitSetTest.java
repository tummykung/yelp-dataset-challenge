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

import java.util.Random;

import junit.framework.TestCase;

/**
 * JUnit test for the PSOBitSet class.
 * 
 * @author Sebastian Luna Valero
 *
 */
public class PSOBitSetTest extends TestCase {

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public PSOBitSetTest(String name) {
		super(name);
	}

	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	
	/**
	 * Test method for {@link weka.attributeSelection.PSOBitSet#threePBMCX(java.util.Random, double, double, double, java.util.BitSet, java.util.BitSet)}.
	 * 
	 * In order to test the 3PBMCX correctly we need each particle to contain a different alphabet.
	 * Therefore, we test the 3PBMCX operator by using the auxTreePBMCX auxiliary operator.
	 * It works exactly in the same way that the original one but with characters instead of bits.
	 */	
	public void testAuxThreePBMCX() {
		char[] current, bestG, bestP;
		int i, size = 100, seed, times = 10000;
		int [][] count;
		Random r;
		double w1 = 0.4, w2 = 0.3, w3 = 0.3;
		double avgA, avgB, avgC, totalA, totalB, totalC;
		long resultA, expectedA, resultB, expectedB, resultC, expectedC;
		String result;

		// initialize particles
		current = new char[size];
		bestG = new char[size];
		bestP = new char[size];

		// each particle has its own alphabet!
		for (i = 0; i < size; i++) {
			current[i] = 'a';
			bestG[i] = 'b';
			bestP[i] = 'c';
		}

		count = new int[times][3];

		// test the 3PBMCX operator with several different seeds
		for (seed = 0; seed < times; seed++) {
			r = new Random(seed);
			
			// apply 3PBMCX operator
			result = auxThreePBMCX(r, w1, w2, w3, current, bestG, bestP);
			
			// save the result
			count[seed] = countABC(result);			
		}
		
		totalA = totalB = totalC = 0.0;
		
		for (i = 0; i < times; i++) {
			totalA = totalA + count[i][0];
			totalB = totalB + count[i][1];
			totalC = totalC + count[i][2];
		}
		
		avgA = totalA / times;
		avgB = totalB / times;
		avgC = totalC / times;
		
		resultA = Math.round(avgA);
		resultB = Math.round(avgB);
		resultC = Math.round(avgC);
		
		expectedA = Math.round(size * w1);
		expectedB = Math.round(size * w2);
		expectedC = Math.round(size * w3);
		
		assertTrue(resultA == expectedA);
		assertTrue(resultB == expectedB);
		assertTrue(resultC == expectedC);		
		
	}
	
	
	public String auxThreePBMCX(Random r, double currentW, double bestGW, double bestpPW, char[] current, char[] bestG, char[] bestP) {
		StringBuffer result = new StringBuffer();
		double p;
		
		for (int i = 0; i < current.length; i++) {
			p = r.nextDouble();
			if (p <= currentW) {
				result.append(current[i]);
			} else if (p <= (currentW + bestGW)) {
				result.append(bestG[i]);
			} else {
				result.append(bestP[i]);
			}			
		}
		
		return result.toString();
	}
	
	
	public int [] countABC(String string) {
		int [] result;
		
		result = new int[3];
		// count 'a'
		result[0] = 0;
		// count 'b'
		result[1] = 0;
		// count 'c'
		result[2] = 0;
		
		// count how many times each char appears 
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == 'a') {
				result[0]++;
			} else if (string.charAt(i) == 'b') {
				result[1]++;
			} else if (string.charAt(i) == 'c'){
				result[2]++;
			}
		}
		
		return result;
	}
	

	
	/**
	 * Test method for {@link weka.attributeSelection.PSOBitSet#mutation(int, java.util.Random, double)}.
	 */
	public void testMutation() {
		int i, seed, size = 100, times = 100;
		long result, expected;
		int [] count;
		Random r;
		double total, avg, p = 0.1;
		PSOBitSet tester = new PSOBitSet(size);
		
		count = new int[times];
		
		// test the mutation operator with several different seeds
		for (seed = 0; seed < times; seed++) {
			r = new Random(seed);
			
			// initialize tester - set to 1 the entire bit set
			for (i = 0; i < size; i++) {
				tester.set(i);
			}

			// apply bit-flip mutation
			// warning: remember that java.util.BitSet grows according to the user needs!
			// warning: to work properly with java.util.BitSet, we need to specify the length of the particle!			
			tester.mutation(0, r, p);
			
			// save the number of bits mutated
			count[seed] = size - tester.countOnes();
			
		} // for

		// compute the average of bits mutated
		total = 0.0;
		for (i = 0; i < times; i++) {
			total = total + count[i];
		}
		
		avg = total / times;		
		result = Math.round(avg);
		expected = Math.round(size * p);
		
		assertTrue(result == expected);
		
	}
	
	
	/**
	 * Test method for {@link weka.attributeSelection.PSOBitSet#countOnes()}.
	 * 
	 */
	public void testCountOnes() {
		int i, result, size = 10;
		PSOBitSet tester = new PSOBitSet(size);
		
		// initialize tester
		for (i = 0; i < size; i++) {
			if (i % 2 == 0) {
				tester.set(i);
			}
		}

		// warning: remember that java.util.BitSet grows according to the user needs!
		// warning: to work properly with java.util.BitSet, we need to specify the length of the particle!
		result = tester.countOnes();
		
		assertTrue(result == size/2);
		
	}

}
