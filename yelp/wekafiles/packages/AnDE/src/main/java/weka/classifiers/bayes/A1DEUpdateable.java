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

/*
 *    AODE.java
 *    Copyright (C) 2003
 *    Algorithm developed by: Geoff Webb
 *    Code written by: Janice Boughton & Zhihai Wang & Nayyar Zaidi
 */

package weka.classifiers.bayes.AveragedNDependenceEstimators;

import weka.classifiers.UpdateableClassifier;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;

/**
<!-- globalinfo-start -->
 * AODE achieves highly accurate classification by averaging over all of a small space 
 * of alternative naive-Bayes-like models that have weaker (and hence less detrimental) 
 * independence assumptions than naive Bayes. The resulting algorithm is computationally 
 * efficient while delivering highly accurate classification on many learning  tasks. <br/>
 * <br/>
 * For more information, see<br/>
 * <br/> G. Webb, J. Boughton, Z. Wang (2005). 
 * Not So Naive Bayes: Aggregating One-Dependence Estimators. Machine Learning. 58(1):5-24.<br/>
 * <br/>
 * Further papers are available at<br/> http://www.csse.monash.edu.au/~webb/.<br/>
 * <br/>
 * Default frequency limit set to 1.
 * <p/>
<!-- globalinfo-end -->
 * 
<!-- technical-bibtex-start -->
 * BibTeX:
 * <pre>
 * &#64;article{Webb2005,
 *    author = {G. Webb and J. Boughton and Z. Wang},
 *    journal = {Machine Learning},
 *    number = {1},
 *    pages = {5-24},
 *    title = {Not So Naive Bayes: Aggregating One-Dependence Estimators},
 *    volume = {58},
 *    year = {2005}
 * }
 * </pre>
 * <p/>
<!-- technical-bibtex-end -->
 *
<!-- options-start -->
 * Valid options are: <p/>
 * 
 *  <pre> -F &lt;int&gt;
 *  Impose a frequency limit for superParents (default is 1). 
 *  </pre>
 * 
 *  <pre> -M &lt;int&gt;
 *  Specify a weight to use with m-estimate (default is 1). 
 *  </pre>
 *    
 *  <pre> -S (Optional) &lt;int&gt;
 *  Specify critical value of specialization-generalization for 
 *  Subsumption Resolution (default is false).
 *  Results in lowering bias and increasing variance of classification. 
 *  Recommended for large training data.
 *  See: 
 *  &#64;inproceedings{Zheng2006,
 *    author = {Fei Zheng and Geoffrey I. Webb},
 *    booktitle = {Proceedings of the Twenty-third International Conference on Machine  Learning (ICML 2006)},
 *    pages = {1113-1120},
 *    publisher = {ACM Press},
 *    title = {Efficient Lazy Elimination for Averaged-One Dependence Estimators},
 *    year = {2006},
 *    ISBN = {1-59593-383-2}
 * }
 *  </pre>
 * 
 *  Can not use weighting for A1DEUpdateable classifier (-W flag can be used only with A1DE).
 *  
<!-- options-end -->
 *
 * @author Janice Boughton (jrbought@csse.monash.edu.au)
 * @author Zhihai Wang (zhw@csse.monash.edu.au)
 * @author Nayyar Zaidi (nayyar.zaidi@monash.edu)
 * @version $Revision: 2 $
 */

public class A1DEUpdateable extends A1DE 
implements UpdateableClassifier {

	/**
	 * Returns default capabilities of the classifier.
	 *
	 * @return      the capabilities of this classifier
	 */
	@Override
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
		result.disableAll();

		// attributes
		result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.MISSING_VALUES);

		// class
		result.enable(Capability.NOMINAL_CLASS);
		result.enable(Capability.MISSING_CLASS_VALUES);

		// instances
		result.setMinimumNumberInstances(0);

		return result;
	}
	
	/**
	 * Main method for testing this class.
	 *
	 * @param argv the options
	 */
	public static void main(String [] argv) {	
		m_Incremental = true;
		m_UseDiscretization = false;
		runClassifier(new A1DEUpdateable(), argv);
	}
	
}
