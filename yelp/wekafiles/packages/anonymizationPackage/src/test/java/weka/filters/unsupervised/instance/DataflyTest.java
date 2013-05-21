package weka.filters.unsupervised.instance;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;
import weka.core.Instances;
import weka.filters.AbstractFilterTest;
import weka.filters.Filter;

/**
 * Tests Datafly. Run from the command line with:
 * <p>
 * java weka.filters.unsupervised.instance.DataflyTest
 * 
 * @author Matthias Niemann
 * @author Daniel Rotar
 * @author Maximilian Schroeder
 * @version $Revision: 1.0 $
 */
public class DataflyTest extends AbstractFilterTest {

	public DataflyTest(String name) {
		super(name);
	}
	 /**
	  * Called by JUnit before each test method. This implementation creates
	  * the default filter to test and loads a test set of Instances.
	  *
	  * @throws Exception if an error occurs reading the example instances.
	  */
	 protected void setUp() throws Exception {
	   m_Filter = getFilter();	   
	   m_Instances = new Instances(new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("weka/filters/data/AnonFilterTest.arff"))));
	   m_Instances.setClassIndex(1);
	   m_OptionTester = getOptionTester();
	   m_GOETester = getGOETester();
	   m_FilteredClassifier = getFilteredClassifier();
	 }
	 
	 /** Called by JUnit after each test method */
	 protected void tearDown() {
	   m_Filter = null;
	   m_Instances = null;
	   m_OptionTester = null;
	   m_GOETester  = null;
	   m_FilteredClassifier = null;	   
	 }

	/** Creates a default Datafly */
	public Filter getFilter() {
		Datafly f = new Datafly();
		return f;
	}
	
	//----------------------------------------<k-anonymity>----------------------------------------
	/**
	 * Tests k-anonymity on testdata. No suppression, k=2
	 */
	public void testKAnonymityForNumberWithoutSuppressionWithK2() {
		/**
		 * Set parameter
		 */
		((Datafly) m_Filter).setAllowDropDataRows(false);
		((Datafly) m_Filter).setDebug(false);
		((Datafly) m_Filter).setDelimiter("_");
		((Datafly) m_Filter).setIdentifierAttributes("col1, col2, col4, _col5, col7");
		((Datafly) m_Filter).setK(2);
		((Datafly) m_Filter).setL(1.0);
		((Datafly) m_Filter).setLDiversity(false);
		((Datafly) m_Filter).setSensitiveAttributes("col6");

		Instances result = useFilter();
		
		assertEquals(m_Instances.numInstances(), result.numInstances()); //no line has been dropped
		int[] qIDs = {2,3};
		Collection<Integer> bs = getBlockSizes(result, qIDs);
		 
		for (int i : bs)
		{
			assertEquals(true, i>=2);
		}
	}
	/**
	 * Tests k-anonymity on testdata. No suppression, k=3
	 */
	public void testKAnonymityForNumberWithoutSuppressionWithK3() {
		/**
		 * Set parameter
		 */
		((Datafly) m_Filter).setAllowDropDataRows(false);
		((Datafly) m_Filter).setDebug(false);
		((Datafly) m_Filter).setDelimiter("_");
		((Datafly) m_Filter).setIdentifierAttributes("col1, col2, col4, _col5, col7");
		((Datafly) m_Filter).setK(3);
		((Datafly) m_Filter).setL(1.0);
		((Datafly) m_Filter).setLDiversity(false);
		((Datafly) m_Filter).setSensitiveAttributes("col6");

		Instances result = useFilter();
		
		assertEquals(m_Instances.numInstances(), result.numInstances()); //no line has been dropped
		int[] qIDs = {2,3};
		Collection<Integer> bs = getBlockSizes(result, qIDs);
		 
		for (int i : bs)
		{
			assertEquals(true, i>=3);
		}
	}
	/**
	 * Tests k-anonymity on testdata. Suppression, k=3
	 */
	public void testKAnonymityForNumberWithSuppressionWithK3() {
		/**
		 * Set parameter
		 */
		((Datafly) m_Filter).setAllowDropDataRows(true);
		((Datafly) m_Filter).setDebug(false);
		((Datafly) m_Filter).setDelimiter("_");
		((Datafly) m_Filter).setIdentifierAttributes("col1, col2, col4, _col5, col7");
		((Datafly) m_Filter).setK(3);
		((Datafly) m_Filter).setL(1.0);
		((Datafly) m_Filter).setLDiversity(false);
		((Datafly) m_Filter).setSensitiveAttributes("col6");

		Instances result = useFilter();
		
		int[] qIDs = {2,3};
		Collection<Integer> bs = getBlockSizes(result, qIDs);
		 
		for (int i : bs)
		{
			assertEquals(true, i>=3);
		}
	}
	//----------------------------------------</k-anonymity>----------------------------------------
	
	//----------------------------------------<l-diversity>----------------------------------------
	/**
	 * Tests l-diversity on testdata. l=1.4. Two sensitive attributes.
	 */
	public void testLDiversityL14TwoSensitiveAttributes() {
		/**
		 * Set parameter
		 */
		((Datafly) m_Filter).setAllowDropDataRows(false);
		((Datafly) m_Filter).setDebug(false);
		((Datafly) m_Filter).setDelimiter("_");
		((Datafly) m_Filter).setIdentifierAttributes("col3, _col5");
		((Datafly) m_Filter).setK(2);
		((Datafly) m_Filter).setL(1.4);
		((Datafly) m_Filter).setLDiversity(true);
		((Datafly) m_Filter).setSensitiveAttributes("col6, col7");

		Instances result = useFilter();
		
		assertEquals(m_Instances.numInstances(), result.numInstances()); //no line has been dropped
		int[] qIDs = {0,1,3};
		Collection<Integer> bs = getBlockSizes(result, qIDs);
		
		Iterator<Integer> it = bs.iterator();
		assertEquals(7, (int)it.next());
		assertEquals(6, (int)it.next());	
	}
	/**
	 * Tests l-diversity on testdata. l=1.4. One sensitive attributes.
	 */
	public void testLDiversityL14OneSensitiveAttribute() {
		/**
		 * Set parameter
		 */
		((Datafly) m_Filter).setAllowDropDataRows(false);
		((Datafly) m_Filter).setDebug(false);
		((Datafly) m_Filter).setDelimiter("_");
		((Datafly) m_Filter).setIdentifierAttributes("col3, _col5, col7");
		((Datafly) m_Filter).setK(2);
		((Datafly) m_Filter).setL(1.4);
		((Datafly) m_Filter).setLDiversity(true);
		((Datafly) m_Filter).setSensitiveAttributes("col6");

		Instances result = useFilter();
		
		assertEquals(m_Instances.numInstances(), result.numInstances()); //no line has been dropped
		int[] qIDs = {0,1,3};
		Collection<Integer> bs = getBlockSizes(result, qIDs);
		
		Iterator<Integer> it = bs.iterator();
		assertEquals(7, (int)it.next());
		assertEquals(6, (int)it.next());
	}
	/**
	 * Tests l-diversity on testdata. l=2.5. Two sensitive attributes.
	 */
	public void testLDiversityL25TwoSensitiveAttributes() {
		/**
		 * Set parameter
		 */
		((Datafly) m_Filter).setAllowDropDataRows(false);
		((Datafly) m_Filter).setDebug(false);
		((Datafly) m_Filter).setDelimiter("_");
		((Datafly) m_Filter).setIdentifierAttributes("col3, _col5");
		((Datafly) m_Filter).setK(2);
		((Datafly) m_Filter).setL(2.5);
		((Datafly) m_Filter).setLDiversity(true);
		((Datafly) m_Filter).setSensitiveAttributes("col6, col7");

		Instances result = useFilter();
		
		assertEquals(m_Instances.numInstances(), result.numInstances()); //no line has been dropped
		int[] qIDs = {0,1,3};
		Collection<Integer> bs = getBlockSizes(result, qIDs);
		
		Iterator<Integer> it = bs.iterator();
		assertEquals(7, (int)it.next());
		assertEquals(6, (int)it.next());
	}
	/**
	 * Tests l-diversity on testdata. l=2.5. One sensitive attributes.
	 */
	public void testLDiversityL25OneSensitiveAttribute() {
		/**
		 * Set parameter
		 */
		((Datafly) m_Filter).setAllowDropDataRows(false);
		((Datafly) m_Filter).setDebug(false);
		((Datafly) m_Filter).setDelimiter("_");
		((Datafly) m_Filter).setIdentifierAttributes("col3, _col5, col7");
		((Datafly) m_Filter).setK(2);
		((Datafly) m_Filter).setL(2.5);
		((Datafly) m_Filter).setLDiversity(true);
		((Datafly) m_Filter).setSensitiveAttributes("col6");

		Instances result = useFilter();
		
		assertEquals(m_Instances.numInstances(), result.numInstances()); //no line has been dropped
		int[] qIDs = {0,1,3};
		Collection<Integer> bs = getBlockSizes(result, qIDs);
		
		Iterator<Integer> it = bs.iterator();
		assertEquals(7, (int)it.next());
		assertEquals(6, (int)it.next());
	}
	//----------------------------------------</l-diversity>----------------------------------------
	
	//----------------------------------------<datatypes>----------------------------------------
	/**
	 * Tests k-anonymity on testdata. No suppression, k=2, "_"-delimited string-hierarchy
	 */
	public void testKAnonymityWithStringHierarchy() {
		/**
		 * Set parameter
		 */
		((Datafly) m_Filter).setAllowDropDataRows(false);
		((Datafly) m_Filter).setDebug(false);
		((Datafly) m_Filter).setDelimiter("_");
		((Datafly) m_Filter).setIdentifierAttributes("col1, col2, col3, col4, col7");
		((Datafly) m_Filter).setK(2);
		((Datafly) m_Filter).setL(1.0);
		((Datafly) m_Filter).setLDiversity(false);
		((Datafly) m_Filter).setSensitiveAttributes("col6");

		Instances result = useFilter();
		
		assertEquals(m_Instances.numInstances(), result.numInstances()); //no line has been dropped
		int[] qIDs = {4};
		Collection<Integer> bs = getBlockSizes(result, qIDs);
		 
		for (int i : bs)
		{
			assertEquals(true, i>=2);
		}
	}
	/**
	 * Tests k-anonymity on testdata. No suppression, k=#Instances, "_"-delimited string-hierarchy
	 */
	public void testKAnonymityWithStringHierarchyMaxK() {
		/**
		 * Set parameter
		 */
		((Datafly) m_Filter).setAllowDropDataRows(false);
		((Datafly) m_Filter).setDebug(false);
		((Datafly) m_Filter).setDelimiter("_");
		((Datafly) m_Filter).setIdentifierAttributes("col1, col2, col3, col4, col7");
		((Datafly) m_Filter).setK(m_Instances.numInstances());
		((Datafly) m_Filter).setL(0.0);
		((Datafly) m_Filter).setLDiversity(false);
		((Datafly) m_Filter).setSensitiveAttributes("col6");

		Instances result = useFilter();
		
		assertEquals(m_Instances.numInstances(), result.numInstances()); //no line has been dropped
		int[] qIDs = {4};
		Collection<Integer> bs = getBlockSizes(result, qIDs);
		assertEquals(1, bs.size()); // one eq-block
		Iterator<Integer> it = bs.iterator();
		assertEquals(m_Instances.numInstances(), (int)it.next());
	}
	/**
	 * Tests k-anonymity on testdata. No suppression, k=2, multiple sensitive attributes
	 */
	public void testKAnonymityWithMultipleSensitiveAttributesK() {
		/**
		 * Set parameter
		 */
		((Datafly) m_Filter).setAllowDropDataRows(false);
		((Datafly) m_Filter).setDebug(false);
		((Datafly) m_Filter).setDelimiter("_");
		((Datafly) m_Filter).setIdentifierAttributes("col1, col2, col3, col4");
		((Datafly) m_Filter).setK(2);
		((Datafly) m_Filter).setL(0.0);
		((Datafly) m_Filter).setLDiversity(false);
		((Datafly) m_Filter).setSensitiveAttributes("col6, col7");

		Instances result = useFilter();
		
		assertEquals(m_Instances.numInstances(), result.numInstances()); //no line has been dropped
		int[] qIDs = {4};
		Collection<Integer> bs = getBlockSizes(result, qIDs);
		 
		for (int i : bs)
		{
			assertEquals(true, i>=2);
		}
	}
	/**
	 * Tests k-anonymity on testdata. No suppression, k=2, generalize date
	 */
	public void testKAnonymityWithDate() {
		/**
		 * Set parameter
		 */
		((Datafly) m_Filter).setAllowDropDataRows(false);
		((Datafly) m_Filter).setDebug(false);
		((Datafly) m_Filter).setDelimiter("_");
		((Datafly) m_Filter).setIdentifierAttributes("col1, col2, col3, _col5, col7");
		((Datafly) m_Filter).setK(2);
		((Datafly) m_Filter).setL(0.0);
		((Datafly) m_Filter).setLDiversity(false);
		((Datafly) m_Filter).setSensitiveAttributes("col6");

		Instances result = useFilter();
		
		assertEquals(m_Instances.numInstances(), result.numInstances()); //no line has been dropped
		int[] qIDs = {3};
		Collection<Integer> bs = getBlockSizes(result, qIDs);
		 
		for (int i : bs)
		{
			assertEquals(true, i>=2);
		}
	}
	//----------------------------------------</datatypes>----------------------------------------
	
	//----------------------------------------<wrong parameter>---------------------------------------- 
		/**
		 * Fail tests for exceptions are not possible due to weka "AbstractFilterTest" architecture
		 */
	//----------------------------------------</wrong parameter>----------------------------------------
	
	public static Test suite() {
		return new TestSuite(DataflyTest.class);
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	//----------------------------------------<helper methods>----------------------------------------
	/**
	 * Determines the size of equivalent blocks, which is a necessary criterion for 
	 * k-anonymity and l-diversity.
	 * @param instances result instance which contains all data and blocks
	 * @param qIDs array with quasi identifier numbers for block identification
	 * @return collection with all block sizes
	 */
	private Collection<Integer> getBlockSizes (Instances instances, int[] qIDs){
		HashMap<String,Integer> freqOfInst = new HashMap<String,Integer>();
		double[] qIDValues = new double[qIDs.length];
		
		for(int i = 0; i < instances.numInstances(); i++){
			for(int j = 0; j < qIDs.length; j++){
				qIDValues[j] =  instances.instance(i).value(qIDs[j]);
			}
		
			String qIDValuesString = "";
			for (double d : qIDValues)
			{
				qIDValuesString += d + ";";
			}
			
			if(freqOfInst.containsKey(qIDValuesString)){
				freqOfInst.put(qIDValuesString, (freqOfInst.get(qIDValuesString) + 1) );
			}
			else{
				freqOfInst.put(qIDValuesString, 1);
			}
		}
		
		return freqOfInst.values();
	}
	//----------------------------------------<helper methods>----------------------------------------
}
