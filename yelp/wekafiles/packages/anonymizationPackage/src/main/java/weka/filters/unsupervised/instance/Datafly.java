package weka.filters.unsupervised.instance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Capabilities.Capability;
import weka.filters.SimpleBatchFilter;

/**
 * This package contains an anonymization-filter, that is used as a preprocessing-tool. To make \
 * sure, no human being can be identified by the quasi-identifiers that occur in the data, \
 * you can do an anonymization. The kind of anonymization is variable, as well as the strength. \
 * The datafly-algorithm is used. Mostly, it will not find the optimal solution, but it is the fastest. \
 * For l-diversity, we apply a modified datafly-algorithm, that generalizes data as long as the l-diversity-\
 * criterion is not fulfilled.
 * 
 * @author Matthias Niemann
 * @author Daniel Rotar
 * @author Maximilian Schroeder
 *
 */
public class Datafly extends SimpleBatchFilter {
	
	/**
	 * for serialization
	 */
	private static final long serialVersionUID = -364772879162584092L;
	
	public static void main(String[] args) {
		runFilter(new Datafly(), args);
	}

	// ----------------------------------------<User_Parameters>----------------------------------------
	// ____________________<k>____________________
	/**
	 * The k-value for k-Anonymity.
	 */
	private int _k = 1;
	
	/**
	 * Returns the tip text for this property.
	 *
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String kTipText()
	{
		return "The k-value for k-anonymity.";
	}
	
	/**
	 * Gets the k-value for k-Anonymity.
	 * @return The k-value for k-Anonymity.
	 */
	public int getK()
	{
		return _k;
	}
	
	/**
	 * Sets the k-value for k-Anonymity.
	 * @param k The k-value for k-Anonymity.
	 */
	public void setK(int k)
	{
		_k = k;
	}
	// ____________________</k>____________________
	
	
	// ____________________<l>____________________
	/**
	 * The l-value for entropy l-diversity.
	 */
	private double _l = 1;
	
	/**
	 * Returns the tip text for this property.
	 *
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String lTipText()
	{
		return "The l-value for entropy l-diversity.";
	}
	
	/**
	 * Gets the l-value for entropy l-diversity.
	 * @return The l-value for entropy l-diversity.
	 */
	public double getL()
	{
		return _l;
	}
	
	/**
	 * Sets the l-value for entropy l-diversity.
	 * @param l The l-value for entropy l-diversity.
	 */
	public void setL(double l)
	{
		_l = l;
	}
	// ____________________</l>____________________
	
	
	// ____________________<identifierAttributes>____________________
	/**
	 * The identifier attributes (names of the columns separated by ',').
	 */
	private String _identifierAttributes = "";
	
	/**
	 * Returns the tip text for this property.
	 *
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui.
	 */
	public String identifierAttributesTipText()
	{
		return "The identifier attributes (names of the columns seperated by ',').";
	}
	
	/**
	 * Gets the identifier attributes.
	 * @return The identifier attributes (names of the columns separated by ',').
	 */
	public String getIdentifierAttributes()
	{
		return _identifierAttributes;
	}
	
	/**
	 * Sets the identifier attributes.
	 * @param identifierAttributes The identifier attributes (names of the columns separated by ',').
	 */
	public void setIdentifierAttributes(String identifierAttributes)
	{
		_identifierAttributes = identifierAttributes;
	}
	// ____________________</identifierAttributes>____________________
	
	
	// ____________________<sensitiveAttributes>____________________
	/**
	 * The sensitive attributes (names of the columns separated by ',').
	 */
	private String _sensitiveAttributes = "";
	
	/**
	 * Returns the tip text for this property.
	 *
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui.
	 */
	public String sensitiveAttributesTipText()
	{
		return "The sensitive attributes (names of the columns seperated by ',').";
	}
	
	/**
	 * Gets the sensitive attributes.
	 * @return The sensitive attributes (names of the columns separated by ',').
	 */
	public String getSensitiveAttributes()
	{
		return _sensitiveAttributes;
	}
	
	/**
	 * Sets the sensitive attributes.
	 * @param sensitiveAttributes The sensitive attributes (names of the columns separated by ',').
	 */
	public void setSensitiveAttributes(String sensitiveAttributes)
	{
		_sensitiveAttributes = sensitiveAttributes;
	}
	// ____________________</sensitiveAttributes>____________________
	
	
	// ____________________<allowDropDataRows>____________________
	/**
	 * Determines whether a DataRow can be deleted in order to achieve k-Anonymity.
	 */
	private boolean _allowDropDataRows = true;
	
	/**
	 * Returns the tip text for this property.
	 *
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui.
	 */
	public String allowDropDataRowsTipText()
	{
		return "Determines whether a DataRow can be deleted in order to achieve k-Anonymity.";
	}
	
	/**
	 * Gets the value that determines whether a DataRow can be deleted in order to achieve k-Anonymity.
	 * @return true if a DataRow can be deleted in order to achieve k-Anonymity, else false.
	 */
	public boolean getAllowDropDataRows()
	{
		return _allowDropDataRows;
	}
	
	/**
	 * Sets the value that determines whether a DataRow can be deleted in order to achieve k-Anonymity.
	 * @param allowDropDataRows true if a DataRow can be deleted in order to achieve k-Anonymity, else false.
	 */
	public void setAllowDropDataRows(boolean allowDropDataRows)
	{
		_allowDropDataRows = allowDropDataRows;
	}
	// ____________________</allowDropDataRows>____________________
	
	
	// ____________________<lDiversity>____________________
	/**
	 * Determines whether l-Diversity should be applied after k-Anonymity is reached.
	 */
	private boolean _lDiversity = false;
	
	/**
	 * Returns the tip text for this property.
	 *
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui.
	 */
	public String LDiversityTipText()
	{
		return "Determines whether l-Diversity should be applied after k-Anonymity is reached.";
	}
	
	/**
	 * Gets the value that determines whether l-Diversity should be applied after k-Anonymity is reached.
	 * @return true if l-Diversity should be applied after k-Anonymity is reached, else false.
	 */
	public boolean getLDiversity()
	{
		return _lDiversity;
	}
	
	/**
	 * Sets the value that determines whether l-Diversity should be applied after k-Anonymity is reached.
	 * @param allowDropDataRows true if l-Diversity should be applied after k-Anonymity is reached, else false.
	 */
	public void setLDiversity(boolean lDiversity)
	{
		_lDiversity = lDiversity;
	}
	// ____________________</lDiversity>____________________
	
	
	// ____________________<delimiter>____________________
	/**
	 * The delimiter of the string-hierarchies.
	 */
	String _delimiter = "_";
	
	/**
	 * Returns the tip text for this property.
	 *
	 * @return tip text for this property suitable for
	 * displaying in the explorer/experimenter gui.
	 */
	public String delimiterTipText()
	{
		return "The delimiter of the string-hierarchies.";
	}
	
	/**
	 * Gets the delimiter of the string-hierarchies.
	 * @return The delimiter of the string-hierarchies.
	 */
	public String getDelimiter()
	{
		return _delimiter;
	}
	
	/**
	 * Sets the delimiter of the string-hierarchies.
	 * @param delimiter The delimiter of the string-hierarchies.
	 */
	public void setDelimiter(String delimiter)
	{
		_delimiter = delimiter;
	}
	// ____________________</delimiter>____________________
	// ----------------------------------------</User_Parameters>----------------------------------------
	
	
	
	
	
	// ----------------------------------------<Private_Global_Variables>----------------------------------------
	/**
	 * The indexes of the identifier attributes in the output instance format (only the 'from' one - not the 'to' one).
	 */
	private LinkedList<Integer> iDIndexes;
	/**
	 * The indexes of the quasi identifier attributes in the output instance format (only the 'from' one - not the 'to' one).
	 */
	private LinkedList<Integer> qIDIndexes;
	/**
	 * The indexes of the hierarchy quasi identifier attributes in the output instance format.
	 */
	private LinkedList<Integer> hqIDIndexes;
	/**
	 * The indexes of the sensitive attributes in the output instance format.
	 */
	private LinkedList<Integer> sIndexes;
	/**
	 * The HashMap that maps the indexes of the output instance to the input instance.
	 */
	private HashMap<Integer,Integer> outputToInputMap;
	/**
	 * The String which should be placed before the from attribute.
	 */
	private String _fromPrefixNotation = "";
	/**
	 * The String which should be placed after the from attribute.
	 */
	private String _fromPostfixNotation = "_from";
	/**
	 * The String which should be placed before the to attribute.
	 */
	private String _toPrefixNotation = "";
	/**
	 * The String which should be placed after the to attribute.
	 */
	private String _toPostfixNotation = "_to";
	// ----------------------------------------</Private_Global_Variables>----------------------------------------

	
	
	
	
	// ----------------------------------------<SimpleBatchFilter_Abstract_Methods>----------------------------------------
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
		result.enableAllAttributes();
		result.enableAllClasses();
		result.enable(Capability.NO_CLASS); // filter doesn't need class to be set
		return result;
	}
	
	/**
	 * Returns the global info.
	 * @return The global info.
	 */
	public String globalInfo() {
		return "Datafly-algorithm to provide k-anonymity";
	}

	/**
	 * Determine the Output format.
	 * @param inputFormat The input format.
	 * @return The output format.
	 */
	protected Instances determineOutputFormat(Instances inputFormat) {
		Instances result = new Instances(inputFormat, 0);
		result.setClassIndex(-1);
		
		checkUserInputs(inputFormat);
		
		iDIndexes = new LinkedList<Integer>();
		qIDIndexes = new LinkedList<Integer>();
		hqIDIndexes = new LinkedList<Integer>();
		sIndexes = new LinkedList<Integer>();
		outputToInputMap = new HashMap<Integer,Integer>();
		
		LinkedList<Integer> iDIndexes_old = new LinkedList<Integer>();
		LinkedList<Integer> sIndexes_old = new LinkedList<Integer>();
		
		// get the indexes of the old (input) format
		// identifier indexes:
		if (!getIdentifierAttributes().trim().equals(""))
		{
			String[] iDAs = getIdentifierAttributes().split(",");
			for (String iDA : iDAs)
			{
				Attribute attr = inputFormat.attribute(iDA.trim());
				if (attr != null)
				{
					iDIndexes_old.add(attr.index());
				}
			}	
		}
		// sensitive indexes:
		if (!getSensitiveAttributes().trim().equals(""))
		{
			String[] sAs = getSensitiveAttributes().split(",");
			for (String sA : sAs)
			{
				Attribute attr = inputFormat.attribute(sA.trim());
				if (attr != null)
				{
					sIndexes_old.add(attr.index());
				}
			}	
		}
		
		// iterate over the (original) columns/attributes.
		int index_new = 0;
		for (int i = 0; i < inputFormat.numAttributes(); i++)
		{
			if (iDIndexes_old.contains(i))
			{
				// This is an identifier attribute
				// delete this attribute an create a new one with a generalized value
				Attribute originalAttr = inputFormat.attribute(i);
				String originalName = originalAttr.name();
				result.deleteAttributeAt(index_new); //Delete the old attribute in the output format
				
				FastVector fv = new FastVector(1);
				fv.addElement("*");
				Attribute generalizedAttr = new Attribute(originalName, fv);
				result.insertAttributeAt(generalizedAttr, index_new);
				
				iDIndexes.add(index_new);
				outputToInputMap.put(index_new, i);
				index_new++;
			}
			else if (inputFormat.attribute(i).name().startsWith(getDelimiter()) &&
					inputFormat.attribute(i).name().length() > 1)
			{
				// This is a hierarchy quasi identifier attribute
				hqIDIndexes.add(index_new);
				qIDIndexes.add(index_new);
				outputToInputMap.put(index_new, i);
				index_new++;
			}
			else if (sIndexes_old.contains(i))
			{
				// This is a sensitive attribute
				sIndexes.add(index_new);
				outputToInputMap.put(index_new, i);
				index_new++;
			}
			else
			{
				// This is a quasi identifier attribute
				Attribute originalAttr = inputFormat.attribute(i);
				Attribute fromAttr = originalAttr.copy(_fromPrefixNotation + originalAttr.name() + _fromPostfixNotation);
				Attribute toAttr = originalAttr.copy(_toPrefixNotation + originalAttr.name() + _toPostfixNotation);
				result.deleteAttributeAt(index_new); //Delete the old attribute
				

				result.insertAttributeAt(fromAttr, index_new);
				result.insertAttributeAt(toAttr, index_new+1);
				
				qIDIndexes.add(index_new);
				outputToInputMap.put(index_new, i);
				outputToInputMap.put(index_new+1, i);
				index_new += 2;
			}
		}
		
		result.setClassIndex(result.numAttributes()-1);
		return result;
	}

	/**
	 * Process the given instance to obtain k-Anonymity (and if needed l-Diversity).
	 * @param inst The instance on which k-Anonymity should be applied.
	 * @return An k-Anonymity instance (and if selected l-Diversity).
	 * @throws LDiversityException Exception, if strict l-Diversity cannot reached
	 */
	protected Instances process(Instances inst) throws LDiversityException {
		Instances result = new Instances(determineOutputFormat(inst), 0);
		copyValues(inst, result);
		
		applyKAnonymity(result);
		if (getLDiversity())
		{
			applyLDiversity(result);
		}
		return result;
	}
	
	/**
	 * Returns the revision string.
	 * @return The revision.
	 */
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 1.0.1 $");
	}
	// ----------------------------------------</SimpleBatchFilter_Abstract_Methods>----------------------------------------
	
	
	
	
	
	// ----------------------------------------<Private_Methods>----------------------------------------
	/**
	 * Checks the user input for illegal Arguments.
	 * @param inputFormat The input format.
	 */
	private void checkUserInputs(Instances inputFormat)
	{
		// <IllegalArgument>
		// k cannot be smaller then 1
		if (getK()<1)
		{
			throw new IllegalArgumentException("The k value must be at least 1!");
		}
		// </IllegalArgument>
		
		// <IllegalArgument>
		// l cannot be smaller then 0, because it's the minimal value of entropy 
		if (_lDiversity && getL()<0)
		{
			throw new IllegalArgumentException("The l value must be at least 0.0!");
		}
		// </IllegalArgument>
		
		// <IllegalArgument>
		// every named identifier attribute must exist.
		LinkedList<Integer> iDIndexes_temp = new LinkedList<Integer>();
		
		if (!getIdentifierAttributes().equals(""))
		{
			String[] iDAs = getIdentifierAttributes().split(",");
			for (String iDA : iDAs)
			{ 
				Attribute originalAtrr = inputFormat.attribute(iDA.trim());
				if (originalAtrr == null)
				{
					throw new IllegalArgumentException("The identifier attribute '" + iDA + "' does not exist!");
				}
				else
				{
					iDIndexes_temp.add(originalAtrr.index());
				}
			}	
		}
		// </IllegalArgument>
		
		// <IllegalArgument>
		// every named sensitive attribute must exist.
		LinkedList<Integer> sIndexes_temp = new LinkedList<Integer>();
		
		if (!getSensitiveAttributes().equals(""))
		{
			String[] senAs = getSensitiveAttributes().split(",");
			for (String senA : senAs)
			{
				Attribute originalAtrr = inputFormat.attribute(senA.trim());
				if (originalAtrr == null)
				{
					throw new IllegalArgumentException("The sensitive attribute '" + senA + "' does not exist!");
				}
				else
				{
					sIndexes_temp.add(originalAtrr.index());
				}
			}	
		}
		// </IllegalArgument>
		
		// <IllegalArgument>
		// the subset of  identifier attribute and sensitive attribute must be empty.
		for (int i : iDIndexes_temp)
		{
			if (sIndexes_temp.contains(i))
			{
				throw new IllegalArgumentException("The subset of identifier attribute and sensitive attribute must be empty!");
			}
		}
		// </IllegalArgument>
		
		//TODO @All: Weitere Fehlerhafte Inputs abfangen
	}
	
	/**
	 * Copys the value from the source instance to the destination instance.
	 * When an identifier attribute is detected the value of getGeneralizedIdentifierValue() is used for this attribute.
	 * @param source The source instance from which the DataRows should be copied.
	 * @param destionation The destination instance to which the DataRows should be copied.
	 */
	private void copyValues(Instances source, Instances destionation)
	{
	    for (int i = 0; i < source.numInstances(); i++) 
	    {
	        double[] values = new double[destionation.numAttributes()];
	        for (int n = 0; n < destionation.numAttributes(); n++)
	        {
	        	if (iDIndexes.contains(n))
	        	{
	        		// this is an identifier attribute
	        		values[n] = 0; //This is always the numeric value of getGeneralizedIdentifierValue()
	        	}
	        	else
	        	{
	        		// this is an non-identifier attribute
	        		values[n] = source.instance(i).value(outputToInputMap.get(n));
	        	}	        	
	        }
	        destionation.add(new DenseInstance(1, values));
	      }
	}
	
	/**
	 * Process the given instance to obtain k-Anonymity.
	 * @param inst The instance on which k-Anonymity should be applied.
	 */
	private void applyKAnonymity(Instances inst)
	{
		if (getK() > inst.numInstances()){
			throw new IllegalArgumentException("The k-value must be less or equal than the number of rows.");
		}
		if (getK() == 1) return;
		
		HashMap<Elem, Numbers> freq = countFrequency(qIDIndexes,inst);
		
		/**
		 * Sum of elements, that doesn't fulfil k-anonymity.
		 */
		int sum = sumUpElementsLessThanK(freq, getK());
		
		boolean iterate;
		if (getAllowDropDataRows()){
			iterate = sum > getK();
		}
		else{
			iterate = sum > 0;
		}
		/**
		 * if data-suppression is activated: while there are quasi-identifier with less than k values, but summed up more than k values
		 * if data-suppression is deactivated: while there are quasi-identifiers with less than k values
		 */
		while (iterate){
			if (m_Debug) System.out.println("summed up values > k " + sum + " > " + getK());
			
			/**
			 * find the attribute with the most distinct values
			 */
			int maxAttr = maximumDistinctValuesInColumn(qIDIndexes, inst);
			
			
			/**
			 * generalize that attribute
			 */
			generalizeColumn(maxAttr, inst);
			
			if (m_Debug) System.out.println("data is generalized");
			
			/**
			 * check for anonymity
			 */
			freq = countFrequency(qIDIndexes,inst);
			sum = sumUpElementsLessThanK(freq,getK());
			if (m_Debug) System.out.println("checked again");
			
			if (getAllowDropDataRows()){
				iterate = sum > getK();
			}
			else{
				iterate = sum > 0;
			}
		}
		
		if (getAllowDropDataRows()){
			if (m_Debug) System.out.println("suppress the rest");
			
			/**
			 * Suppress the lines, with quasi-attributes that have less than k lines.
			 */
			
			/**
			 * Set of indices to all quasi-identifiers.
			 */
			Set<Elem> keys = freq.keySet();
			/**
			 * Contains all indices of all quasi-identifiers, that doesn't have at least k lines.
			 */
			LinkedList<Integer> removeItems = new LinkedList<Integer>();
			/**
			 * Iterate over the set of indices.
			 */
			Iterator <Elem> keyIterator = keys.iterator();
			while (keyIterator.hasNext()){
				if (m_Debug) System.out.println("new number-tupel");
				Elem e = keyIterator.next();
				/**
				 * Check whether there are less than k elements for that quasi-tupel.
				 */
				if (freq.get(e).getCount()<getK()){
					/**
					 * Save that indices, to delete them later
					 */
					removeItems.addAll(freq.get(e).getNumbers());
				}
			}
			/**
			 * Sort the list of lines that should be deleted soon.
			 */
			Collections.sort(removeItems);
			
			/**
			 * Iterate from top to bottom and delete the lines.
			 */
			Iterator <Integer> removeIterator = removeItems.descendingIterator();
			while (removeIterator.hasNext()){
				int currRem = removeIterator.next();
				if (m_Debug) System.out.println("remove " + currRem);
				/**
				 * Delete the element from the data set.
				 */
				inst.delete(currRem);	
			}
		}
		
	}
	
	/**
	 * Check given instance on entropy l-diversity.
	 * @param inst The instance on which l-Diversity should be checked.
	 * @return true, if inst is l-diverse, false otherwise
	 */
	private boolean isLDiverse(Instances inst)
	{		
		/**
		 * the quasi identifiers will be sorted, to archive a priority based quasi identifier sort
		 */
		for(int i = qIDIndexes.size()-1 ; i >=0;i--){
			inst.sort(qIDIndexes.get(i));
		}
		
		/** 
		 * determine q*-Blocks for l-diversity
		 */
		ArrayList<QBlockWrapper> qBWrappers = getTmpQBlockWrapper(inst);
		
		/**
		 * check for entropy-l-diversity
		 */
		
		/**
		 * merge belonging q*-Blocks to a LinkedList
		 */
		HashMap<Integer, LinkedList<QBlockWrapper>> tempQBlocks = new HashMap<Integer, LinkedList<QBlockWrapper>>();
		for(QBlockWrapper qBW : qBWrappers){
			if(tempQBlocks.containsKey(qBW.getQBlock())){
				LinkedList<QBlockWrapper> wList = tempQBlocks.get(qBW.getQBlock());
				wList.add(qBW);
				tempQBlocks.put(qBW.getQBlock(), wList);
			}
			else{
				LinkedList<QBlockWrapper> wList = new LinkedList<QBlockWrapper>();
				wList.add(qBW);
				tempQBlocks.put(qBW.getQBlock(), wList);
			}
		}
		
		/**
		 * iterate over every q*Block list and check for valid entropy-l-diversity
		 */
		Collection<LinkedList<QBlockWrapper>> blockCollection = tempQBlocks.values();
		Iterator<LinkedList<QBlockWrapper>> collectionIterator = blockCollection.iterator();
		
		while(collectionIterator.hasNext()){
			LinkedList<QBlockWrapper> listOfQBlocks = collectionIterator.next();

				/**
				 * merge all arrays of the current list into a single one
				 */
				double[] singleArray = getSingleDoubleArray(listOfQBlocks);
				/**
				 * single frequencies of values
				 */
				HashMap<Double,Double> valFrequency = new HashMap<Double, Double>();
				for(double faVal : singleArray){
					if(valFrequency.containsKey(faVal)){
						valFrequency.put(faVal, (valFrequency.get(faVal) + 1.0) );
					}
					else{
						valFrequency.put(faVal, 1.0);
					}
				}
				/**
				 * sum up all frequencies
				 */
				double sumOfFreq = 0;
				Collection<Double> valCollection = valFrequency.values();
				Iterator<Double> colIt = valCollection.iterator();
				while(colIt.hasNext()){
					sumOfFreq += colIt.next();
				}
				if(m_Debug) System.out.println("Frequencies " + valFrequency.values() + " sum " + sumOfFreq);
				
				/**
				 * Check for entropy
				 * 
				 * if entropy is not valid, try to generalize more
				 */
				if( ! (entropyDiversity(getL(), valFrequency.values(), sumOfFreq)) ){
					/**
					 * entropy is not valid:
					 */
					if (m_Debug) System.out.println("l-div false");
					return false;
				}
		}
		if (m_Debug) System.out.println("l-div checked successfully");
		return true;
	}
	
	/**
	 * Applies entropy l-diversity on a given data set. If l-diversity is not reached,
	 * try to generalize the data set an check for l-diversity again.
	 * @param inst data set on which the l-diversity should be applied on
	 * @throws LDiversityException Exception, if l-diversity cannot be reached
	 */
	private void applyLDiversity(Instances inst) throws LDiversityException{
		boolean canGeneralize = true;
		boolean islDiverse = false;
		while ((! (islDiverse = isLDiverse(inst))) && canGeneralize){
			if (m_Debug) System.out.println("generalize column, l-div isn't achieved yet...");
			
			/**
			 * Get the column with the most distinct values and generalize it
			 */
			//int maxValColumn = maximumDistinctValuesInColumn(qIDIndexes, inst);
			//canGeneralize = generalizeColumn(maxValColumn,  inst);
			
			/**
			 * Get the column, which changes most regarding to the sensitive attributes
			 */
			LinkedList<Numbers> columnHasMin = new LinkedList<Numbers>();
			for (Integer currentQuasiIdentifier : qIDIndexes){
				/**
				 * Compute frequency of sensitive attributes and one quasi-identifier
				 */
				if (m_Debug) System.out.println("computing frequency for sensitive attributes and column " + currentQuasiIdentifier);
				LinkedList<Integer> sensitiveAndCurrQuasiIdentifier = new LinkedList<Integer>();
				sensitiveAndCurrQuasiIdentifier.addAll(sIndexes);
				sensitiveAndCurrQuasiIdentifier.add(currentQuasiIdentifier);
				if (m_Debug){
					System.out.print("columns: ");
					for (int i : sensitiveAndCurrQuasiIdentifier){
						System.out.print(i + ", ");
					}
					System.out.println();
				}
				
				HashMap<Elem, Numbers> freq = countFrequency(sensitiveAndCurrQuasiIdentifier,inst);
				
				/**
				 * Get the minimum and save it to the list of minima
				 */
				int minVal = Integer.MAX_VALUE;
				for (Elem currQIElement : freq.keySet()){
					if (freq.get(currQIElement).getCount() < minVal){
						minVal = freq.get(currQIElement).getCount();	
					}
				}
				
				
				Numbers currMedian = new Numbers();
				currMedian.addIndex(currentQuasiIdentifier);
				currMedian.addIndex(minVal);
				columnHasMin.add(currMedian);
			}
			
			/**
			 * Look for the column smallest minimum
			 */
			if (m_Debug) System.out.println("search the maximum median");
			int minCol = 0;
			int minVal = Integer.MAX_VALUE;
			for (Numbers currMin : columnHasMin){
				if (m_Debug) System.out.println("Column: " + currMin.getNumbers().get(1) + " with median: " + currMin.getNumbers().get(0));
				if (currMin.getNumbers().get(0) < minVal){
					minVal = currMin.getNumbers().get(0);
					minCol = currMin.getNumbers().get(1);
				}
			}
			
			/**
			 * Generalize that column
			 */
			if (m_Debug) {
				int maxValColumn = maximumDistinctValuesInColumn(qIDIndexes, inst);
				System.out.println("Generalize " + minCol + " normal heuristic " + maxValColumn);
			}
			canGeneralize = generalizeColumn(minCol,  inst);
		}
		/**
		 * Exception handling, if l-diversity cannot be reached
		 */
		if((! islDiverse) && (! canGeneralize)){
			throw new LDiversityException("Cannot generalize more and " +
				"l-diversity is still not reached. Try a smaller l value.");
		}
	}
	
	/**
	 * Merges two double arrays into one double array
	 * @param ar1 double array number one
	 * @param ar2 double array number two
	 * @return new double array with size of (array one + array two) and their values
	 */
	private double[] mergeTwoArrays(double[] ar1, double[] ar2){
		double[] resultArray = new double[ar1.length+ar2.length];
		int i = 0;
		for(double val1 : ar1){
			resultArray[i] = val1;
			i++;
		}
		for(double val2 : ar2){
			resultArray[i] = val2;
			i++;
		}
		return resultArray;
	}
	
	/**
	 * builds one double array from an non empty LinkedList
	 * @param currentQBlockElements LinkedList which contains "n" double arrays
	 * @return double array with values of all arrays which were in the LinkedList
	 */
	private double[] getSingleDoubleArray(LinkedList<QBlockWrapper> currentQBlockElements){
		int timesRuned = 0;
		double[] resultArray = new double[0];
		Iterator<QBlockWrapper> listIt = currentQBlockElements.iterator();
		while(listIt.hasNext()){
			double[] tmpArray = listIt.next().getSValues();
			if(currentQBlockElements.size() > 1 && timesRuned > 0){
				resultArray = mergeTwoArrays(resultArray,tmpArray);
			}
			else{
				resultArray = tmpArray.clone();
			}
			timesRuned++;
		}
		return resultArray;
	}
	
	/**
	 * checks if the entropy-l-diversity inequation is hold
	 * @param lval l-value for entropy-l-diversity inequation
	 * @param sFrequencies collection of frequencies (of sensitive attributes)
	 * @param freqSum sum of all frequency-values
	 * @return boolean, if the entropy-l-diversity inequation is hold
	 */
	private boolean entropyDiversity(double lval, Collection<Double> sFrequencies, double freqSum){
		double entropySum = 0;
		Iterator<Double> sFIt = sFrequencies.iterator();
		while(sFIt.hasNext()){
			double fraqOfTup = sFIt.next();
			entropySum += ((fraqOfTup/freqSum) + (Math.log10( (fraqOfTup/freqSum) )) );
		}
		return ((-entropySum) >= Math.log10( lval ));
	}
	
	/**
	 * builds an ArrayList of QBlockWrappers, which contain all information needed for applyLDiversity
	 * @param inst Instance of current data set
	 * @return ArrayList of QBlockWrappers
	 */
	private ArrayList<QBlockWrapper> getTmpQBlockWrapper(Instances inst){
		ArrayList<QBlockWrapper> qBWrappers = new ArrayList<QBlockWrapper>();
		for (int i = 0; i < inst.numInstances(); i++)
        {   
			int j = 0;
			double[] qIdentValues = new double[qIDIndexes.size()*2]; //every QI got two values
			double[] sIdentValues = new double[sIndexes.size()];
			QBlockWrapper wi = new QBlockWrapper(); //wrapper for quasi identifier attributes
			wi.setQBlock(i);
            /**
             * collect quasi identifier values
             */
            for (int qID : qIDIndexes) 
            { 
                 qIdentValues[j] = inst.instance(i).value(qID); //'from' column
                 qIdentValues[j+1] = inst.instance(i).value(qID); //'to' column 
                 j += 2; 
            }
            wi.setQIValues(qIdentValues);
            
            /**
             * collect sensitive attribute values
             */
            j = 0;
            for (int sID : sIndexes) 
            { 
            	sIdentValues[j] = inst.instance(i).value(sID);
                 j += 1; 
            }
            wi.setSValues(sIdentValues);
            qBWrappers.add(wi);
            
            /**
             * only check, if there is more than one wrapper saved
             */
            if(i > 0){
            	for(int k = i; k > 0; k--){
        			QBlockWrapper k1Wrapper = qBWrappers.get(k);
        			QBlockWrapper k2Wrapper = qBWrappers.get(k-1);
            		
        			/** check for value equality of "k-th saved array" and "current value array" */
        			if(Arrays.equals(k1Wrapper.getQIValues(), k2Wrapper.getQIValues())){
        				/** set new q*-Block to k-th q*-Block-value */
        				k1Wrapper.setQBlock(k2Wrapper.getQBlock());
        				qBWrappers.set(k, k1Wrapper);
        				
        				/** no more change of q*-Block-value needed */
        				break;
        			}
            			
            	}
            }
            
        }
		Collections.sort(qBWrappers);
		
		if (m_Debug){
			Iterator<QBlockWrapper> qBlockNrIt = qBWrappers.iterator();
			while(qBlockNrIt.hasNext()){
				QBlockWrapper debugW = qBlockNrIt.next();
				System.out.println("q*-Block number: " + debugW.getQBlock() + " ");
			}
		}
		return qBWrappers;
	}
	
	/**
	 * Sums up all elements of quasi-attributes with less than k elements.
	 * @param freq Frequency hash-map
	 * @param kValue k
	 * @return sum of elements
	 */
	private int sumUpElementsLessThanK(HashMap<Elem, Numbers> freq, int kValue) {
		int sum = 0;
		/**
		 * Indices of the lines of all quasi-identifiers.
		 */
		Collection<Numbers> numbers = freq.values();
		Iterator<Numbers> iterator = numbers.iterator();
		/**
		 * Sum up the number of indices, of quasi-identifiers with less than k elements
		 */
		while (iterator.hasNext()){
			if (m_Debug) System.out.println("new number-tupel");
			Numbers n = iterator.next();
			if (n.getCount()<kValue){
				sum += n.getCount();
			}
			if (m_Debug) System.out.println("sum of elements less than k " + sum + " (was " + n.getCount() + ")");
		}
		return sum;
	}
	/**
	 * Returns the column with the most distinct values.
	 * @param quasiIdentifierColumns columns to search in
	 * @param data set the data
	 * @return the index of the column.
	 */
	private int maximumDistinctValuesInColumn(LinkedList<Integer> quasiIdentifierColumns, Instances dataset) {
		Iterator <Integer> attributes = quasiIdentifierColumns.iterator();
		int maxNum = 0;
		int currNum;
		int maxAttr = -1;
		while (attributes.hasNext()){
			int currAttr = attributes.next();
			if ((currNum=distinctValuesInColumn(currAttr, dataset)) > maxNum){
				maxAttr = currAttr;
				maxNum = currNum;	
			}
			if (m_Debug) System.out.println("attribute " + currAttr + " with " + currNum + " distinct values");
		}
		return maxAttr;
	}
	/**
	 * Returns the number of distinct values of the column.
	 * @param column index of the column
	 * @param data data set
	 * @return number of distinct values
	 */
	private int distinctValuesInColumn(int column, Instances data){
		/**
		 * Create a dummy-list.
		 */
		LinkedList<Integer> singleColumn = new LinkedList<Integer>();
		singleColumn.add(column);
		/**
		 * Count the number of values in that column and save the indices.
		 */
		HashMap<Elem,Numbers> singleElement = countFrequency(singleColumn, data);
		/**
		 * Returns how many different values are there.
		 */
		return singleElement.size();
	}
	/**
	 * Count the number of elements for every quasi-identifier and save the indices of the lines.
	 * @param columns quasi-identifiers
	 * @param data data set
	 * @return hash-map with all used quasi-identifiers and the indices of the lines, that are identified by it.
	 */
	private HashMap<Elem, Numbers> countFrequency(LinkedList <Integer> columns, Instances data){
		/**
		 * Generate new map.
		 */
		HashMap<Elem, Numbers> freq = new HashMap<Elem, Numbers>();
		
		//if (m_Debug) System.out.println("iterate over lines");
		/**
		 * Iterate over all lines.
		 */
		for (int i = 0; i < data.numInstances(); i++) {
			/**
			 * Generate a new key for the hashtable. For every attribute, generate a "from"- and a "to"-column
			 */
			int size = 0;
			for (int currColumn : columns){
				if (qIDIndexes.contains(currColumn)){
					size += 2;
				}
				if (hqIDIndexes.contains(currColumn)){
					size -= 1;
				}
				if (sIndexes.contains(currColumn)){
					size += 1;
				}
			}
			Elem key = new Elem(size);
			//if (m_Debug) System.out.println("key-size=" + size);
			/**
			 * Iterate over the columns in which the quasi-identifier is.
			 */
			Iterator<Integer> iterator = columns.iterator();
			//if (m_Debug) System.out.println("generate key");
			while (iterator.hasNext()){
				Integer actIteration = (Integer) iterator.next();
				
				//if (m_Debug) System.out.println("add column to key");
				/**
				 * Fill the key-value with the values of the quasi-identifier.
				 * Differentiate between numeric and string.
				 */
				if (data.instance(i).attribute(actIteration).isNumeric() || data.instance(i).attribute(actIteration).isDate()){
					//if (m_Debug) System.out.print("try to add " + data.instance(i).value(actIteration));
					key.addArgument(data.instance(i).value(actIteration) + "");
					if (!hqIDIndexes.contains(actIteration) && !sIndexes.contains(actIteration)){
						//if (m_Debug) System.out.print(" and " + data.instance(i).value(actIteration + 1));
						key.addArgument(data.instance(i).value(actIteration + 1) + "");
					}
					//if(m_Debug) System.out.println();
				}
				else{
					//if (m_Debug) System.out.print("try to add " + data.instance(i).stringValue(actIteration));
					key.addArgument(data.instance(i).stringValue(actIteration));
					if (!hqIDIndexes.contains(actIteration) && !sIndexes.contains(actIteration)){
						//if (m_Debug) System.out.print(" and " + data.instance(i).stringValue(actIteration + 1));
						key.addArgument(data.instance(i).stringValue(actIteration + 1));
					}
					//if (m_Debug) System.out.println();
				}
			}
			
			/**
			 * If the key is already in the hash table.
			 */
			if (freq.containsKey(key)){
				/**
				 * Add the index to the key.
				 */
				freq.get(key).addIndex(i);
				//if (m_Debug) System.out.println("added index " + i + " for key " + key.print());
			}
			else{
				/**
				 * Generate a new entry and add the index.
				 */
				Numbers v = new Numbers();
				v.addIndex(i);
				freq.put(key, v);
				//if (m_Debug) System.out.println("new index " + i + " for key " + key.print());
			}
		}
		return freq;
	}
	/**
	 * Generalizes a column.
	 * @param column index of the column
	 * @param data data set
	 * @return data set was generalized
	 */
	@SuppressWarnings("unchecked")
	private boolean generalizeColumn(int column, Instances data){
		if (m_Debug) System.out.println("generalizing column " + column);
		
		/**
		 * differentiate between an automatic generalization with auto-creation of ranges and string-hierarchies
		 */

		if (!hqIDIndexes.contains(column)){
			/**
			 * This column is not marked as a string-hierarchy.
			 * Analyze data and generate groups.
			 * 
			 * First, generate a set of groups.
			 */
			LinkedList<Range> groups = new LinkedList<Range>();
			/**
			 * Read out the distinct start- and end-values
			 */
			LinkedList<Range> values = new LinkedList<Range>();
			for (int i = 0; i < data.numInstances(); i++){
				Range curr = null;
				if (data.instance(i).attribute(column).isNumeric() || data.instance(i).attribute(column).isDate()){
					curr = new Range(data.instance(i).value(column), data.instance(i).value(column + 1));
				}
				
				else{
					curr = new Range(data.instance(i).stringValue(column), data.instance(i).stringValue(column + 1));
				}
				if (!values.contains(curr)){
					values.add(curr);					
				}
			}
			
			/**
			 * Sort ranges
			 */
			if (m_Debug) System.out.println("sorting...");
			Collections.sort(values);
			if (m_Debug){
				System.out.println("sorted range:");
				for (Range curr : values){
					System.out.println(curr.getFrom() + " " + curr.getTo());
				}
			}
			
			/**
			 * If head- and tail-value equals, there is only one range, before generalizing.
			 * In this case, the algorithm can't generalize even more.
			 */
			if (values.getFirst().equals((Range)values.getLast())){
				return false;
			}
			
			/**
			 * Generate groups.
			 */
			if (m_Debug) System.out.println("generate groups");
			
			for (int i = 0; i < values.size(); i++){
				/**
				 * There are still elements left, so there must be a new group.
				 */
				Range actValue = values.get(i);
				if (m_Debug) System.out.println("new group " + values.get(i).getFrom());
				
				/**
				 * Add the following range to the current range
				 */
				if (i+1 < values.size()){
					i++;
					actValue.addToRange(values.get(i));
					if (m_Debug) System.out.println("adding " + values.get(i).getFrom());
				}
				/**
				 * If the next but one range is the last range overall, add it, so it isn't alone.
				 */
				if (i+2 == values.size()){
					i++;
					actValue.addToRange(values.get(i));
					if (m_Debug) System.out.println("adding " + values.get(i).getFrom());
				}
				/**
				 * Add the current range to the group-list.
				 */
				groups.add(actValue);
				if (m_Debug) System.out.println("generated group " + actValue.getFrom() + " to " + actValue.getTo());
			}
			
			/**
			 * rewrite groups in data
			 */
			for (int i = 0; i < data.numInstances(); i++){
				/**
				 * write out the current attribute
				 */
				Instance currInstance = data.instance(i);
				Range currRange;
				if (currInstance.attribute(column).isNumeric() || data.instance(i).attribute(column).isDate()){
					currRange = new Range(currInstance.value(column), currInstance.value(column + 1));
				}
				else{
					currRange = new Range(currInstance.stringValue(column), currInstance.stringValue(column + 1));
				}
				/**
				 * search for the right group for the current instance
				 */
				for (Range currGroup : groups){
					if (currGroup.containsRange(currRange)){
						/**
						 * found group, rewrite the range in the instance
						 */
						if (currInstance.attribute(column).isNumeric() ||currInstance.attribute(column).isDate()){
							currInstance.setValue(currInstance.attribute(column), (Double) currGroup.getFrom());
							currInstance.setValue(currInstance.attribute(column + 1), (Double) currGroup.getTo());
						}
						else{
							currInstance.setValue(currInstance.attribute(column), "" + currGroup.getFrom());
							currInstance.setValue(currInstance.attribute(column + 1), "" + currGroup.getTo());
						}
					}
				}
			}
		}
		else{
			/**
			 * This column is a known string-hierarchy. Each level is divided by a given delimiter.
			 * If there is no level left, replace the last character by asterisks.
			 */
			
			/**
			 * Count the maximum number of delimiter-symbols per instance. Just cut the leafs in the maximum depth.
			 * That's a normalization, so A_B_C and A_B are generalized to A_B and A_B in one step and not to A_B and A.
			 */
			int maxDelimiters = 0;
			int maxLength = 0;
			for (int i = 0; i < data.numInstances(); i++){
				Instance currInstance = data.instance(i);
				/**
				 * Get the string.
				 */
				String value = currInstance.stringValue(column);
				maxLength = Math.max(maxLength, value.length());
				int count = 0;
				int pos;
				while ((pos=value.lastIndexOf(getDelimiter()))>=0){
					count++;
					value = value.substring(0,pos);
				}
				maxDelimiters = Math.max(maxDelimiters,count);
			}
			if (m_Debug) System.out.println("For string-hierarchy: maxDelims " + maxDelimiters + " maxSize " + maxLength);
			
			boolean allDataGeneralized = true;
			for (int i = 0; i < data.numInstances(); i++){
				Instance currInstance = data.instance(i);
				/**
				 * Get the string.
				 */
				String value = currInstance.stringValue(column);
				
				/**
				 * If the value not already "*", it can be generalized even more.
				 */
				if (!value.equals("*")){
					allDataGeneralized = false;
				}
				/**
				 * If there are delimiters left in the whole table, try to cut off the rightmost part.
				 */
				if (maxDelimiters > 0){
					if (m_Debug) System.out.println("cut off rightmost part");
					/**
					 * Count, how many delimiters are in here. Cut off only if needed.
					 */
					String valueCopy = value;
					int count = 0;
					int pos;
					while ((pos=valueCopy.lastIndexOf(getDelimiter()))>=0){
						count++;
						valueCopy = valueCopy.substring(0,pos);
					}
					if (count==maxDelimiters){
						value = value.substring(0,value.lastIndexOf(getDelimiter()));
					}
				}
				else{
					/**
					 * If there are already tailing asterisks, swap the last symbol before the first asterisk.
					 */
					if (m_Debug) System.out.println("cut off last non-asterisk-character");
					if (value.length()== maxLength){
						if (value.charAt(value.length()-1) == '*'){
							value = value.substring(0,value.length()-2) + "*";
						}
						else{
							value = value.substring(0,value.length()-1) + "*";
						}
					}
					else{
						if (value.charAt(value.length()-1) != '*'){
							value = value + "*";
						}
					}
					
				}
				if (m_Debug) System.out.println("new value is " + value);
				currInstance.setValue(column, value);
			}
			return !allDataGeneralized;
		}
		return true;
	}
	// ----------------------------------------</Private_Methods>----------------------------------------
	
	
	
	
	
	// ----------------------------------------<Private_Classes>----------------------------------------
	/**
	 * A Wrapper for building correct q*-Blocks for l-diversity
	 * containing quasi identifier values, sensitive attribute 
	 * values and q*-Block numbers 
	 * @author Maximilian
	 *
	 */
	private class QBlockWrapper implements Comparable<QBlockWrapper>{
		private double[] qiValues = null;
		private double[] sValues = null;
		private int qBlock = -1;
		
		public int getQBlock() { return qBlock; }
		public void setQBlock(int q_Block) { qBlock = q_Block; }
		public double[] getQIValues() { return qiValues; }
		public void setQIValues(double[] qIdentValues) { qiValues = qIdentValues; }
		public double[] getSValues() { return sValues; }
		public void setSValues(double[] sAttValues) { sValues = sAttValues; }
		
		public int compareTo(QBlockWrapper argument ) {
	        if( qBlock < argument.getQBlock() ) { return -1; }
	        if( qBlock > argument.getQBlock() ) { return 1; }
	        return 0;
	    }
	}

	/**
	 * A n-tupel of strings and some useful functions on.
	 * @author Matthias
	 *
	 */	
	public class Elem {
		/**
		 * The data, a n-tupel of strings.
		 */
		private String[] arguments;
		/**
		 * The position of the last insertion of data.
		 */
		private int maxIndex = 0;
		/**
		 * Creates a new n-tupel of strings with the given size.
		 * @param size maximum number of strings
		 */
		public Elem(int size){
			arguments = new String[size];
		}
		/**
		 * Returns the full set of strings.
		 * @return Array of strings
		 */
		public String[] getArguments(){
			return arguments;
		}
		/**
		 * Add a string to the n-tupel, if it isn't full yet.
		 * @param p String that should be added
		 */
		public void addArgument(String p){
			if (maxIndex < arguments.length){
				arguments[maxIndex] = p;
				maxIndex++;
			}
		}
		/**
		 * Returns a semicolon-separated string of all strings, saved in the n-tupel.
		 * @return concatenated string
		 */
		public String print(){
			String temp = "";
			/**
			 * Iterate over the set of data
			 */
			for (int i = 0; i < arguments.length; i++){
				/**
				 * Add the string and a semicolon
				 */
				temp += arguments[i] + "; ";
			}
			return temp;
		}
		/**
		 * Overridden test on equality.
		 * Returns true, if every single string equals.
		 * Returns false, if the data type or the length doesn't match or at least one string isn't equal.
		 */
		public boolean equals(Object input) {
			if (input instanceof Elem && ((Elem)input).getArguments().length == arguments.length){
				/**
				 * Iterate over all elements
				 */
				for (int i = 0; i < arguments.length; i++){
					/**
					 * Check for string-equality
					 */
					if (!arguments[i].equals(((Elem)input).getArguments()[i])){
						return false;
					}
				}
				return true;
		    }
			return false;
		}
		/**
		 * Overrides the hash-code-computation.
		 */
		public int hashCode()
		{
			int p= 0;
			for (int i = 0; i < arguments.length; i++){
				p += arguments[i].hashCode();
			}
			return p;
		}
	}
	
	/**
	 * Saves an unlimited number of integers and provides some useful functions.
	 * @author Matthias
	 *
	 */
	public class Numbers{
		/**
		 * data set
		 */
		private LinkedList<Integer> indices;
		/**
		 * Generates a new list of elements.
		 */
		public Numbers(){
			indices = new LinkedList<Integer>();
		}
		/**
		 * Adds a new number to the list.
		 * @param i value
		 */
		public void addIndex(int i){
			indices.addFirst(i);
		}
		/**
		 * Returns the number of elements in the list.
		 * @return number of integers
		 */
		public int getCount(){
			return indices.size();
		}
		/**
		 * Returns the list of elements.
		 * @return list of numbers
		 */
		public LinkedList<Integer> getNumbers(){
			return indices;
		}
	}
	/**
	 * Saves a from-value and a to-value. The values only need to be comparable.
	 * Provides useful functions on these ranges
	 * @author Matthias
	 *
	 */
	@SuppressWarnings("all")
	public class Range implements Comparable{
		private Comparable from;
		private Comparable to;
		public Range(Comparable from, Comparable to){
			this.from = from;
			this.to = to;
		}
		public Comparable getFrom() {
			return from;
		}
		public void setFrom(Comparable from) {
			this.from = from;
		}
		public Comparable getTo() {
			return to;
		}
		public void setTo(Comparable to) {
			this.to = to;
		}
		public void addToRange(Range input){
			if (input.getFrom().compareTo(from) < 0){
				from = input.getFrom();
			}
			if (input.getTo().compareTo(to) > 0){
				to = input.getTo();
			}
		}
		public int compareTo(Object o) {
			return (from.compareTo(((Range) o).getFrom()));
		}
		public boolean containsRange(Range input){
			return (from.compareTo(input.getFrom()) <= 0 && to.compareTo(input.getTo()) >= 0); 
		}
		public boolean equals(Object o){
		    if ( this == o) {
		    	return true;
	    	}
		    
		    if (o == null){
		    	return false;
		    }
		    
		    if (!(o instanceof Range)){
		    	return false;
	    	}
		    else {
		    	Range r = (Range)o;
		    	return (from.equals(r.getFrom()) && to.equals(r.getTo())); 
		    }
		}
		
	}
	/**
	 * Exception class for signaling the user, that l-diversity cannot be reached.
	 * Occurs, if l-value is too high (is to restrictive) or the given data set cannot be generalized more.
	 * @author Maximilian
	 *
	 */
	private class LDiversityException extends Exception{
		private static final long serialVersionUID = 3663327923723863814L;
		public LDiversityException(String message){
			super(message);
		}
	}
	// ----------------------------------------</Private_Classes>----------------------------------------
}