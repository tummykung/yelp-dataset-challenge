package weka.classifiers.bayes.SGM;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Hashtable;

public class TFIDF implements Serializable{
	private static final long serialVersionUID = -3376037288335722173L;
	int train_count;
	int normalized;
	float length_norm;
	float length_scale;
	float idf_lift;
	Hashtable<Integer, Float> idfs;
	int use_tfidf;

	public TFIDF(double length_scale, double idf_lift, int use_tfidf) {
		//public TFIDF(double length_norm, double length_scale, double idf_lift) {
		//this.length_norm= (float) length_norm;
		//if (length_norm!=0.0 && length_norm<0.33) this.length_norm= (float)0.33;
		this.length_scale= (float) length_scale;
		this.idf_lift= (float) idf_lift;
		idfs= new Hashtable<Integer, Float>();
		normalized= 0;
		train_count= 0;
		this.use_tfidf= use_tfidf;
	}

	public void add_count(Integer term) {
		Float idf= idfs.get(term);
		idf= (idf==null) ? 1: idf+1;
		idfs.put(term, idf);
	}

	public void normalize(double min_count) {
		if (normalized==1) return;
		for (Iterator<Integer> d = idfs.keySet().iterator(); d.hasNext();) {
			Integer term= d.next();
			//System.out.println(term+" "+idfs.get(term)+" "+get_idf(term));
			Float idf= (float)get_idf(term);
			if (idf<=0.0 || idfs.get(term)<min_count) {
				//System.out.println("X:"+term+" "+idfs.get(term)+" "+idfs.get(term)/train_count+" "+idf+" "+min_count);
				d.remove();
			}
			else idfs.put(term, idf);
		}
		normalized= 1;
	}

	public double logsum(double val1, double val2) {
		if (val1+20.0 < val2) return val2;
		if (val2+20.0 < val1) return val1;
		if (val1 > val2) return Math.log(Math.exp(val2 - val1) + 1.0) + val1;
		return Math.log(Math.exp(val1 - val2) + 1.0) + val2;
	}

	public void length_normalize(int[] terms, float[] counts){
		if (normalized==0) {
			train_count++;
			for (int term:terms) add_count(term);
		}
		if (use_tfidf!=1 && use_tfidf!=2 && use_tfidf!=6 && use_tfidf!=7) return;
		int t= 0;
		if (use_tfidf==6) for (float count: counts) counts[t++]= (float)Math.log(1.0+count);
		else if (use_tfidf==7) for (float count: counts) counts[t++]/= terms.length;
		else {
			float norm= (float)1.0/ (float)terms.length;
			float norm2= (float) Math.pow(norm, length_scale);
			norm= (float) Math.pow(norm, 1.0-length_scale);
			for (float count: counts) counts[t++]= (float)Math.log(1.0+count * norm)* norm2;
		}
	}

	public double get_idf(Integer term) {
		//if (use_tfidf==0 || use_tfidf==2) return idfs.containsKey(term) ? 1.0 : 0.0;
		if (normalized==1) return idfs.containsKey(term) ? idfs.get(term): 0.0;
		float idf= idfs.containsKey(term) ? idfs.get(term): (float)0.0;
		return Math.log(Math.max(idf_lift+(double)train_count/idf, 1.0));
	}
}