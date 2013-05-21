package weka.classifiers.bayes.SGM;
import java.util.HashSet; 
import java.util.Hashtable; 
import java.nio.IntBuffer;
import java.nio.FloatBuffer;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.Math;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class SGM implements Serializable{
	private static final long serialVersionUID = -3376037288335722173L;
	public int debug;
	private BufferedReader input_file;
	public SparseData data;
	public int num_classified;
	Hashtable<Integer, IntBuffer> inverted_index;
	Hashtable<Integer, FloatBuffer> inverted_index2;
	int prior_max_label;
	public double prune_count_insert;
	double label_threshold;
	double kernel_smooth_weight;
	int cond_hashsize;
	public SGM_Params model;

	HashSet<Integer> label_constrain;
	TFIDF tfidf;
	int num_classes;
	public boolean reverse_nb;
	public int norm_posteriors;
	Hashtable<IntBuffer, Integer> labels2powerset;
	Hashtable<Integer, IntBuffer> powerset2labels;
	Hashtable<Integer, Integer> component_counts;
	public int topset_size;
	public int max_retrieved;
	public float combination;
	public float cond_norm;
	int tp, fp, fn, tp0, fp0, fn0;
	double rec, prec, fscore, rec0, prec0, fscore0, map, meanjaccard;

	public SGM() {
	}

	public String hello() {
		return("Hello!");
	}

	public void init_model(int cond_hashsize) throws Exception {
		//System.out.println("SGM Initializing model");
		debug= 0;
		this.cond_hashsize= cond_hashsize;
		model= new SGM_Params(cond_hashsize);
		label_constrain= null;
		reverse_nb= false;
		prior_max_label= -1;
		labels2powerset= null;
		powerset2labels= null;
		norm_posteriors= 1;
		cond_norm= 1;
	}

	public void init_model(int cond_hashsize, TFIDF tfidf, double cond_norm) throws Exception {
		init_model(cond_hashsize);
		this.tfidf= tfidf;
		this.cond_norm= (float)cond_norm;
	}

	public void train_model(int batch_size, double prune_count_insert) throws Exception {
		this.prune_count_insert= prune_count_insert;
		if (debug>0) System.out.println("Updating model " + data.doc_count + " "+ model.train_count);
		if (data.label_weights==null) for (int w= 0; w < data.doc_count; w++) add_instance(data.terms[w], data.counts[w], data.labels[w], null);
		else for (int w= 0; w < data.doc_count; w++) add_instance(data.terms[w], data.counts[w], data.labels[w], data.label_weights[w]);
	}

	public int[] get_label_powerset(int[] labels) {
		int[] labels2= Arrays.copyOf(labels, labels.length);
		if (labels2.length>1) Arrays.sort(labels2);
		IntBuffer wrap_labels= IntBuffer.wrap(labels2);
		Integer powerset= labels2powerset.get(wrap_labels);
		if (powerset==null) {
			powerset= labels2powerset.size();
			labels2powerset.put(wrap_labels, powerset);
		}
		labels= new int[1];
		labels[0]= powerset;
		return labels;
	}

	public void add_instance(int[] terms, float[] counts, int[] labels, float[] label_weights) {
		tfidf.length_normalize(terms, counts);
		if (labels2powerset!=null) labels= get_label_powerset(labels);
		//if (labels.length>1) Arrays.sort(labels);
		if (model.prior_lprobs!=null) {
			for (int label:labels) {
				Integer label2= label;
				Float lsp= model.prior_lprobs.get(label2);
				lsp= (lsp==null) ? (float) 0.0 : flogaddone(lsp);
				model.prior_lprobs.put(label2, lsp);
			}
		}
		if (model.node_links!=null) {
			int bo_label= -(labels[0]+1);
			model.node_links.put(model.train_count, bo_label);
			if (bo_label<model.min_encoded_label) model.min_encoded_label= bo_label;
			labels= new int[1];
			labels[0]= model.train_count;
		}
		model.train_count++;
		int t= 0, j;

		for (t= 0;t<terms.length;) counts[t]= (float)Math.log(counts[t++]);	
		if (label_weights!=null) for (t= 0;t<labels.length;) label_weights[t]= (float)Math.log(label_weights[t++]);
		for (t= 0; t<terms.length; t++) {
			int term= terms[t];
			Integer term2= new Integer(term);
			double prune= prune_count_insert;
			if (tfidf.use_tfidf==1 || tfidf.use_tfidf==3 || tfidf.use_tfidf==5) prune-= Math.log(tfidf.get_idf(term2));
			double lprob= counts[t];
			j= 0;
			for (int label:labels) {
				if (label_weights!=null) lprob+= label_weights[j++];
				else if (labels.length>1) lprob-= Math.log(labels.length);
				add_lprob(label, term, lprob, prune);
			}
		}
		//if (model.train_count % batch_size == 0) prune_counts(prune_count_insert, cond_hashsize);
	}

	public void add_lprob(int label, int term, double lprob2, double prune) {
		CountKey p_index= new CountKey(label, term);
		Float lprob= model.cond_lprobs.get(p_index);
		if (lprob==null) {
			label= -2147483648;
			if (model.cond_lprobs.size()==cond_hashsize) return;
			lprob= (float) lprob2;
		}
		else lprob= (float) logsum(lprob, lprob2);
		//System.out.println(label+" "+term+" "+lprob2+" "+prune); 
		if (lprob < prune) {if (label!=-2147483648) model.cond_lprobs.remove(p_index);}
		else model.cond_lprobs.put(p_index, lprob);
	}

	public void make_bo_models() throws Exception {
		//normalize_conditionals();
		CountKey p_index= new CountKey();
		Hashtable<Integer, Double> norms= get_cond_norms(model.cond_lprobs);
		Hashtable<CountKey, Float> bo_lprobs= new Hashtable<CountKey, Float>();
		for (Map.Entry<CountKey, Float> entry : model.cond_lprobs.entrySet()) {
			p_index= entry.getKey();
			int label= p_index.label;
			float lprob2= (float)(entry.getValue()- norms.get((Integer)label));
			Integer term2= p_index.term;
			Float cond_bg= model.cond_bgs.get(term2);
			cond_bg= (cond_bg==null) ? lprob2 : (float)logsum(cond_bg, lprob2);
			model.cond_bgs.put(term2, cond_bg);
			if (model.min_encoded_label!=0) {
				Integer label2= model.node_links.get((Integer)p_index.label);
				CountKey p_index2= new CountKey(label2, p_index.term);
				Float lprob= bo_lprobs.get(p_index2);
				if (lprob==null) lprob= lprob2;
				else lprob= (float)logsum(lprob, lprob2);
				bo_lprobs.put(p_index2, lprob);
			}
		}
		//System.out.println(bo_lprobs.size()+" "+model.cond_lprobs.size()+" "+cond_hashsize);
		if (bo_lprobs.size()+model.cond_lprobs.size()>cond_hashsize) prune_counts(-1000000.0, cond_hashsize-bo_lprobs.size());
		if (model.min_encoded_label!=0) {
			for (Map.Entry<CountKey, Float> entry : bo_lprobs.entrySet()) {
				p_index= entry.getKey();
				add_lprob(p_index.label, p_index.term, entry.getValue(), -1000000.0);
			}
		}
		//if (tfidf!=null) {
		Iterator<Entry<Integer, Float>> entries= tfidf.idfs.entrySet().iterator();
		while (entries.hasNext()) {
			Entry<Integer,  Float> entry= (Entry<Integer,  Float>) entries.next();
			if (!model.cond_bgs.containsKey(entry.getKey())) entries.remove();
		}
	}

	public void prune_counts(double prune_count_table, int cond_hashsize) {
		//this.cond_hashsize= cond_hashsize;
		if (debug>0) System.out.println("Pruning conditional hash table:"+ model.cond_lprobs.size()+ " " + prune_count_table);
		Iterator<Entry<CountKey, Float>> entries= model.cond_lprobs.entrySet().iterator();
		while (entries.hasNext()) {
			Entry<CountKey,  Float> entry= (Entry<CountKey,  Float>) entries.next();
			float lprob= (Float) entry.getValue();

			//if (tfidf.use_tfidf!=0 && tfidf.use_tfidf!=2) lprob+= Math.log(tfidf.get_idf(((CountKey)entry.getKey()).term));
			if (tfidf.use_tfidf==1 || tfidf.use_tfidf==3 || tfidf.use_tfidf==5) lprob+= Math.log(tfidf.get_idf(((CountKey)entry.getKey()).term));
			//System.out.println(lprob+" "+tfidf.get_idf(((CountKey)entry.getKey()).term)+ " "+prune_count_table);
			if (lprob <= prune_count_table) entries.remove();
		}
		if (debug>0) System.out.println("Hash table pruned:"+ model.cond_lprobs.size());
		prune_counts_size(model.cond_lprobs, cond_hashsize);
	}

	public void prune_counts2(double prune_count_table) {
		if (debug>0) System.out.println("Pruning conditional hash table:"+ model.cond_lprobs.size()+ " " + prune_count_table);
		Iterator<Entry<CountKey, Float>> entries= model.cond_lprobs.entrySet().iterator();
		while (entries.hasNext()) {
			Entry<CountKey,  Float> entry= (Entry<CountKey,  Float>) entries.next();
			float lprob= (Float) entry.getValue();
			if (lprob <= prune_count_table) entries.remove();
		}
		if (debug>0) System.out.println("Hash table pruned:"+ model.cond_lprobs.size());
	}

	public void prune_counts_size(Hashtable<CountKey, Float> counts, int hashsize) {
		if (counts.size() > hashsize) {
			int bins= (int) Math.log(counts.size());
			int i= 0, j= 0;
			double tmp[]= new double[1+counts.size()/bins];
			for (Map.Entry<CountKey, Float> entry : counts.entrySet()) {
				if (i++%bins==0) {
					float lprob= (Float)entry.getValue();
					if (tfidf.use_tfidf==1 || tfidf.use_tfidf==3 || tfidf.use_tfidf==5) lprob+= Math.log(tfidf.get_idf(((CountKey)entry.getKey()).term));
					//if (tfidf.use_tfidf!=0 && tfidf.use_tfidf!=2) lprob+= Math.log(tfidf.get_idf(((CountKey)entry.getKey()).term));
					tmp[j++]= lprob;
				}
			}
			Arrays.sort(tmp);
			double prune = tmp[(counts.size() - hashsize)/bins];
			Iterator<Entry<CountKey, Float>> entries= counts.entrySet().iterator();
			while (entries.hasNext()) {
				Entry<CountKey, Float> entry= (Entry<CountKey, Float>)entries.next();
				float lprob= (Float)entry.getValue();
				//if (tfidf.use_tfidf!=0 && tfidf.use_tfidf!=2) lprob+= Math.log(tfidf.get_idf(((CountKey)entry.getKey()).term));
				if (tfidf.use_tfidf==1 || tfidf.use_tfidf==3 || tfidf.use_tfidf==5) lprob+= Math.log(tfidf.get_idf(((CountKey)entry.getKey()).term));
				if (lprob <= prune) entries.remove();
			}
		}
		if (debug>0) System.out.println("Hash table pruned:"+ model.cond_lprobs.size());
	}

	public void use_icfs() throws Exception {
		for (Iterator<Integer> d = tfidf.idfs.keySet().iterator(); d.hasNext();) {
			Integer term= d.next();
			Float idf= (float) -model.cond_bgs.get(term);
			tfidf.idfs.put(term, idf);
		}
	}

	public void apply_idfs() throws Exception {
		//if (tfidf.use_tfidf==0) return;
		for (Iterator<Integer> d = model.cond_bgs.keySet().iterator(); d.hasNext();) {
			Integer term= d.next();
			Float idf= (float)tfidf.get_idf(term);
			if (!tfidf.idfs.containsKey(term) || idf<=0.0) d.remove(); 
			else {
				if (tfidf.use_tfidf!=1 && tfidf.use_tfidf!=3) idf= (float) 1;
				Float lprob= model.cond_bgs.get(term) +(float) Math.log(idf);
				model.cond_bgs.put(term, lprob);
				//System.out.println(term+" "+lprob);
			}
		}
		for (Iterator<CountKey> e= model.cond_lprobs.keySet().iterator(); e.hasNext();) {
			CountKey p_index= e.next();
			Integer term= p_index.term;
			Float idf= (float)tfidf.get_idf(term);
			if (!tfidf.idfs.containsKey(term) || idf<=0.0) {
				//System.out.println(idf+" "+term+" "+tfidf.idfs.containsKey(term));
				e.remove();
			}
			else {
				if (tfidf.use_tfidf!=1 && tfidf.use_tfidf!=3) idf= (float) 1;
				Float lprob= (float)(model.cond_lprobs.get(p_index))+(float)Math.log(idf);
				//Float lprob= (float)(model.cond_lprobs.get(p_index))+(float)Math.log(tfidf.get_idf(term));
				model.cond_lprobs.put(p_index, lprob);
			}
		}
	}

	public void scale_conditionals(double cond_scale) throws Exception {
		if (debug>0) System.out.println("Scaling conditionals");
		for (Map.Entry<CountKey, Float> entry : model.cond_lprobs.entrySet()) entry.setValue((Float)(entry.getValue()*(float)cond_scale));
	}

	public Hashtable<Integer, Double> get_cond_norms(Hashtable<CountKey, Float> lprobs) throws Exception {
		Hashtable<Integer, Double> norms= new Hashtable<Integer, Double>();
		for (Map.Entry<CountKey, Float> entry : lprobs.entrySet()) {
			Integer label= ((CountKey)entry.getKey()).label;
			if (reverse_nb) label= ((CountKey)entry.getKey()).term;
			Double lsum= norms.get(label);
			if (lsum == null) lsum= -100000.0;
			//lsum= logsum(lsum, entry.getValue());
			lsum= logsum(lsum, entry.getValue()* Math.abs(cond_norm));
			norms.put(label, lsum);
		}
		return norms;
	}

	public void normalize_conditionals() throws Exception {
		Hashtable<Integer, Double> norms= get_cond_norms(model.cond_lprobs);
		if (Math.abs(cond_norm)!=1) for (Map.Entry<Integer, Double> entry : norms.entrySet()) entry.setValue(entry.getValue() / Math.abs(cond_norm));
		for (Map.Entry<CountKey, Float> entry : model.cond_lprobs.entrySet())
			if (reverse_nb) entry.setValue((Float)(entry.getValue()- (float)(double)norms.get(((CountKey)entry.getKey()).term))); 
			else entry.setValue((Float)(entry.getValue()- (float)(double)norms.get(((CountKey)entry.getKey()).label)));
		double[] tmp_vals;
		int t= 0;
		float norm;
		if (model.cond_bgs.size()>0) {
			tmp_vals= new double[model.cond_bgs.size()];
			//for (Float lprob: model.cond_bgs.values()) tmp_vals[t++]= (double) lprob;
			//norm= (float) logsum_doubles(tmp_vals);
			for (Float lprob: model.cond_bgs.values()) tmp_vals[t++]= (double) lprob * Math.abs(cond_norm);
			norm= (float) logsum_doubles(tmp_vals) / Math.abs(cond_norm);
			for (Map.Entry<Integer, Float> entry : model.cond_bgs.entrySet()) entry.setValue((Float)(entry.getValue()- norm));
		}
	}

	public void normalize_model() throws Exception {
		normalize_conditionals();
		double[] tmp_vals;
		int t= 0;
		float norm;
		if (model.prior_lprobs!=null) {
			t= 0; tmp_vals= new double[model.prior_lprobs.size()];
			for (Float lprob: model.prior_lprobs.values()) tmp_vals[t++]= (double) lprob;
			norm= (float) logsum_doubles(tmp_vals);
			for (Map.Entry<Integer, Float> entry : model.prior_lprobs.entrySet()) entry.setValue((Float)(entry.getValue()- norm));
		}
	}

	public void smooth_conditionals(double cond_unif_weight, double bg_weight, double kernel_smooth_weight) {
		bg_weight= Math.max(Math.min(0.9999999999, bg_weight), 1.0E-60);
		cond_unif_weight= Math.max(Math.min(0.9999999999-bg_weight, cond_unif_weight), 1.0E-60);
		kernel_smooth_weight= Math.max(Math.min(0.9999999999, kernel_smooth_weight), 1.0E-60);
		double a4= Math.log(1.0 - cond_unif_weight - bg_weight);
		double a5= Math.log(bg_weight);
		if (reverse_nb) model.cond_uniform= Math.log(cond_unif_weight) - Math.log(model.prior_lprobs.size()); 
		else model.cond_uniform= Math.log(cond_unif_weight) - Math.log(model.cond_bgs.size());
		for (Map.Entry<Integer, Float> entry : model.cond_bgs.entrySet()) 
			entry.setValue((Float)(float)(logsum(a5 + entry.getValue(), model.cond_uniform)));
		for (Map.Entry<CountKey, Float> entry : model.cond_lprobs.entrySet())
			entry.setValue((Float)(float)(logsum(a4 + entry.getValue(), model.cond_bgs.get(((CountKey)entry.getKey()).term))));
		if (model.node_links!=null) {
			//System.out.println("F "+model.min_encoded_label);
			double node_weight= 1.0, bo_weight= 0.0;
			bo_weight= Math.max(1.0E-60, kernel_smooth_weight);
			node_weight= Math.max(1.0E-60, node_weight*(1.0-kernel_smooth_weight)); 
			smooth_cond_nodes(Math.log(node_weight), Math.log(bo_weight), model.min_encoded_label, 0, 100000000);
			//System.out.println(node_weight+" "+bo_weight);
		}

		if (cond_norm<0) {
			for (Map.Entry<Integer, Float> entry : model.cond_bgs.entrySet()) entry.setValue((float)Math.exp(entry.getValue()));
			for (Map.Entry<CountKey, Float> entry : model.cond_lprobs.entrySet()) entry.setValue((float)Math.exp(entry.getValue()));
		}

		for (Map.Entry<CountKey, Float> entry : model.cond_lprobs.entrySet()) entry.setValue(entry.getValue()-model.cond_bgs.get(((CountKey)entry.getKey()).term));
	}

	public void smooth_cond_nodes(double node_lweight, double bo_lweight, int top, int mid, int low) {
		CountKey p_index= new CountKey(), p_index2= new CountKey();
		for (Map.Entry<CountKey, Float> entry : model.cond_lprobs.entrySet()) {
			p_index= entry.getKey();
			if (p_index.label<mid || p_index.label>low) continue;
			p_index2.term= p_index.term;
			//System.out.println(mid+" "+low+" "+p_index.label);
			Integer label= model.node_links.get(p_index.label);
			if (label==null) continue;
			p_index2.label= label;
			//System.out.println(p_index2.label+" "+p_index2.term);
			entry.setValue((float)logsum(bo_lweight+model.cond_lprobs.get(p_index2), node_lweight+entry.getValue()));
			//if (entry.getValue()==0)System.out.println(p_index.label+"aa");
		}
		for (Map.Entry<CountKey, Float> entry : model.cond_lprobs.entrySet()) {
			p_index= entry.getKey();
			if (p_index.label>=mid || p_index.label<top) continue;
			entry.setValue((float)logsum(bo_lweight+ entry.getValue(), node_lweight+ model.cond_bgs.get((Integer)p_index.term)));
			//if (entry.getValue()==0)System.out.println(p_index.label+" "+p_index.term+" "+bo_lweight+" "+entry.getValue()+" "+node_lweight+" "+model.cond_bgs.get((Integer)p_index.term)+" "+ logsum(bo_lweight+ entry.getValue(), node_lweight+ model.cond_bgs.get((Integer)p_index.term)));
		}
	}

	public void smooth_prior(double prior_scale) {
		if (model.prior_lprobs!=null) {
			for (Map.Entry<Integer, Float> entry : model.prior_lprobs.entrySet()) entry.setValue((Float)(float) (entry.getValue() * prior_scale));
			if (cond_norm<0) for (Map.Entry<Integer, Float> entry : model.prior_lprobs.entrySet()) entry.setValue((float)Math.exp(entry.getValue()));
		}
	}

	public final int[] intbuf_add(int number, int[] buf) {
		int buf_size2= buf.length + 1;
		int[] buf2= new int[buf_size2];
		int h= 0;
		for (int j: buf) 
			if (j < number) buf2[h++]= j; 
			else break;
		buf2[h++]= number;
		for (; h < buf_size2;) buf2[h]= buf[(h++)-1];
		return buf2;
	}

	public final int[] intbuf_add2(int number, int[] buf, int[] buf2) {
		int buf_size2= buf2.length;
		int h= 0;
		for (int j: buf) 
			if (j < number) buf2[h++]= j; 
			else break;
		buf2[h++]= number;
		for (; h < buf_size2;) buf2[h]= buf[(h++)-1];
		return buf2;
	}

	public final int[] intbuf_remove(int number, int[] buf) {
		int buf_size2= buf.length - 1;
		int[] buf2= new int[buf_size2];
		int h= 0;
		for (int j:buf) if (j != number) buf2[h++]= j; else break;
		for (; h < buf_size2;) buf2[h]= buf[++h];
		return buf2;
	}

	public double logsum(double val1, double val2) {
		if (val1+20.0 < val2) return val2;
		if (val2+20.0 < val1) return val1;
		if (val1 > val2) return Math.log(Math.exp(val2 - val1) + 1.0) + val1;
		return Math.log(Math.exp(val1 - val2) + 1.0) + val2;
	}

	public double logsubstract(double val1, double val2) {
		// Note: negative values floored to 0
		if (val2+20.0 < val1) return val1;
		if (val1 > val2) return Math.log(-Math.exp(val2 - val1) + 1.0) + val1;
		return (-100000.0);
	}

	public float flogsum(float val1, float val2) {
		if (val1 > val2) return (float) Math.log(Math.exp(val2 - val1) + 1) + val1;
		else return (float) Math.log(Math.exp(val1 - val2) + 1) + val2;
	}

	public float flogaddone(float val1) {
		return (float) Math.log(Math.exp(val1) + 1.0);
	}

	public final double sum_doubles(double[] vals) {
		TreeSet<DoubleBuffer> treesort = new TreeSet<DoubleBuffer>();
		for (double val: vals) {
			double[] entry = {Math.abs(val), val};
			treesort.add(DoubleBuffer.wrap(entry));
		}             
		double sum= 0.0;
		for (Iterator<DoubleBuffer> e = treesort.descendingIterator(); e.hasNext();) sum+= e.next().get(1);	
		return sum;
		//while (treesort.size()>1){
		//   //Iterator<DoubleBuffer> e = treesort.descendingIterator();
		//    Iterator<DoubleBuffer> e = treesort.iterator();
		//    double val= e.next().get(1);
		//    e.remove();
		//    val+= e.next().get(1);
		//    e.remove();
		//    double[] entry = {Math.abs(val), val};
		//    treesort.add(DoubleBuffer.wrap(entry));
		//}
		//return treesort.first().get(1);
	}

	public final double logsum_doubles(double[] vals) {
		TreeSet<DoubleBuffer> treesort = new TreeSet<DoubleBuffer>();
		for (double val: vals) {
			double[] entry = {Math.abs(val), val};
			treesort.add(DoubleBuffer.wrap(entry));
		}
		double sum= 0.0;
		for (Iterator<DoubleBuffer> e = treesort.descendingIterator(); e.hasNext();) sum= logsum(sum, e.next().get(1));
		return sum;
	}

	public final double logsum_ndoubles(double[] vals) {
		//Note: Sorts original
		Arrays.sort(vals); //reduce double sum error
		double sum= -100000.0;
		for (double val: vals) sum= logsum(val, sum);
		return sum;
	}

	public final double sum_ndoubles2(double[] vals, int count) {
		Arrays.sort(vals, 0, count);
		double sum= 0.0;
		for (int i= 0; i<count;) sum+= vals[i++];
		return sum;
	}

	public final double sum_ndoubles(double[] vals) {
		//Note: Sorts original
		Arrays.sort(vals); //reduce double sum error
		double sum= 0.0;
		//double correct= 0.0;
		//double t, c;
		//for (double val: vals){
		//    val-= correct;
		//    t= sum + val;
		//    float tmp= (float)(t-sum); 
		//    correct= tmp- val;
		//    sum= t;
		//}

		for (double val: vals) sum+= val;
		return sum;

		//double mean= 0.0;
		//int i= 0;
		//for (double val: vals) mean+= (val - mean) / ++i;
		//return mean*i;
		//int fork= 1;
		//int length= vals.length;
		//while (fork< length) {
		//    for (int j= 0; j< length; j+= fork+fork) {
		//	if (j+fork< vals.length) vals[j]+= vals[j+fork];
		//	else {vals[j-fork-fork]+= vals[j]; length=j;}
		//	//System.out.println(j+" "+(j+fork)+" "+vals.length);
		//    }
		//   fork+=fork;
		//}
		//return vals[0];
	}

	/*
	private final boolean EQ(double d1, double d2){
		if (Math.abs(d1-d2)< 0.000000001)
			return true;
		return false;
	}

	private final boolean GE(double d1, double d2){
		if (EQ(d1, d2)) return true;
		return GT(d1, d2);
	}

	private final boolean GT(double d1, double d2){
		if (d1> d2 + 0.000000001) return true;
		return false;
	}*/

	public final SparseVector inference(int[] terms, float[] counts) {
		if (tfidf.use_tfidf==1 || tfidf.use_tfidf==2 || tfidf.use_tfidf==6 || tfidf.use_tfidf==7) tfidf.length_normalize(terms, counts);
		Integer term2;
		float count;
		//int term_count= 0;
		Hashtable<Integer, Double> lprobs= new Hashtable<Integer, Double>();
		for (int t= 0; t<terms.length; t++) {
			term2= terms[t];
			if (!inverted_index.containsKey(term2)) continue;
			count= counts[t];
			if (tfidf.use_tfidf==1 || tfidf.use_tfidf==3 || tfidf.use_tfidf==8) count*= tfidf.get_idf(term2);
			int[] nodes= inverted_index.get(term2).array();
			float[] cond_lprobs= inverted_index2.get(term2).array();
			for (int i= 0; i< nodes.length; i++) {
				Integer node= nodes[i];
				double lprob= cond_lprobs[i] * count;
				Double lprob2= lprobs.get(node);
				if (lprob2==null) lprob2= lprob;
				else lprob2+= lprob;
				lprobs.put(node, lprob2);
			}
			//terms[term_count]= term2;
			//counts[term_count]= count;
			//term_count++;
		}
		//double[] bg_lprobs= new double[term_count];
		//for (int i= 0; i< term_count; i++) bg_lprobs[i]= model.cond_bgs.get(terms[i]) * counts[i];

		int n= 0;
		int[] node_sort= new int[lprobs.size()];
		for (Map.Entry<Integer,Double> entry: lprobs.entrySet()) node_sort[n++]= (int)entry.getKey();
		Arrays.sort(node_sort);
		TreeSet<DoubleBuffer> retrieve_sort= new TreeSet<DoubleBuffer>();

		double lprob;
		double sum_bg_lprob= 0;// sum_ndoubles(bg_lprobs);
		double max_lprob= -1000000.0, topset_lprob= -1000000.0;
		Hashtable<Integer, Double> label_posteriors= new Hashtable<Integer, Double>();
		for (int i= 0; i<node_sort.length; i++) {
			Integer node= node_sort[i];
			if (node< model.min_encoded_label) continue;
			lprob= lprobs.get(node);
			if (model.node_links!=null) {
				Integer bo_node= model.node_links.get(node);
				if (bo_node!=null) {
					Double lprob2= lprobs.get(model.node_links.get(node));
					//System.out.println(node+" "+bo_node+" "+lprob2);
					if (lprob2!=null) lprob+= lprob2; 
					lprobs.put(node, lprob);
				}
				if (node<0) {
					if (model.prior_lprobs!=null) lprob+= model.prior_lprobs.get(-(node+1));
					label_posteriors.put(-(node+1), lprob* Math.abs(combination));
					continue;
				}
				//System.out.println(node+" "+bo_node+ " "+lprob);
				if (model.prior_lprobs!=null) lprob+= model.prior_lprobs.get(-(model.node_links.get(node)+1));
			} else if (model.prior_lprobs!=null) lprob+= model.prior_lprobs.get(node);
			if (lprob> topset_lprob) {
				if (retrieve_sort.size()== topset_size) {
					topset_lprob= Math.max(max_lprob+label_threshold, lprob);
					retrieve_sort.pollFirst();
				}
				double[] entry2 = {lprob, node};
				retrieve_sort.add(DoubleBuffer.wrap(entry2));
				if (lprob > max_lprob) max_lprob= lprob;
			}
		}
		if (retrieve_sort.size()==0 && label_posteriors.size()==0) {
			SparseVector results= new SparseVector(2);
			//System.out.println(prior_max_label);
			results.indices[0]= prior_max_label;
			results.values[0]= (float)sum_bg_lprob;
			if (model.prior_lprobs!=null) results.values[0]+= model.prior_lprobs.get(prior_max_label);
			results.indices[1]= -1;
			return results;
		}
		//System.out.println(label_posteriors.size()+ " "+retrieve_sort.size());
		Iterator<DoubleBuffer> f= retrieve_sort.descendingIterator();
		int labelsize= retrieve_sort.size();
		//System.out.println(labelsize+" "+topset_size);
		if (model.node_links!=null) {
			Hashtable<Integer, Integer> component_counts2= new Hashtable<Integer, Integer>();
			for (n= 0; n< labelsize; n++) {
				double[] entry2= f.next().array();
				Integer label= -(model.node_links.get((int)entry2[1])+1);
				Integer count2= component_counts2.get(label);
				if (model.prior_lprobs!=null) entry2[0]-=model.prior_lprobs.get(label);
				Double lprob4;
				if (count2==null) count2= 1; else count2++;
				lprob4= entry2[0]* Math.abs(combination);
				if (count2!=1) lprob4= logsum(label_posteriors.get(label), lprob4);
				component_counts2.put(label, count2);
				label_posteriors.put(label, lprob4);
			}
			retrieve_sort= new TreeSet<DoubleBuffer>();
			for (Map.Entry<Integer, Double> entry : label_posteriors.entrySet()) {
				Integer label= entry.getKey();
				double[] entry2 = {entry.getValue(), (int)label};
				Integer component_count= component_counts.get(-(label+1));
				if (component_count!=null && combination!=0) {
					Integer component_count2= component_counts2.get(label);
					if (component_count2!=null) {
						int missing_count= component_count - component_count2;
						if (missing_count!=0 && combination!=0) {
							Double lprob4= lprobs.get((Integer)(-(label+1)));
							if (lprob4==null) lprob4= -1000000.0;
							entry2[0]= logsum(entry2[0], Math.log(missing_count)+ lprob4);
						}
						if (combination>0) entry2[0]-= Math.log(component_count);
						if (model.prior_lprobs!=null) entry2[0]+=model.prior_lprobs.get(label);
					}
				}
				//entry2[0]+= sum_bg_lprob;
				//System.out.println("X "+label+" "+Math.exp(entry2[0])+" "+entry2[0]);
				retrieve_sort.add(DoubleBuffer.wrap(entry2));
			}
			f= retrieve_sort.descendingIterator();
		}
		if (label_threshold>-1000000) {
			max_lprob= f.next().array()[0];
			f= retrieve_sort.iterator();
			while (f.hasNext()) if (max_lprob+label_threshold > f.next().array()[0] || retrieve_sort.size()>topset_size) f.remove();
			else break;
			f= retrieve_sort.descendingIterator();
		}
		int labelsize2= Math.min(retrieve_sort.size(), max_retrieved);
		SparseVector result= new SparseVector(labelsize2+1);
		float sum= -1000000;
		for (n= 0; n< labelsize2;) {
			double[] entry2= f.next().array();
			result.indices[n]= (int)entry2[1];
			result.values[n++]= (float)entry2[0];
			sum= (float)logsum(sum, entry2[0]);
		}
		float bg_score= 0;
		int missing= num_classes- labelsize2;
		if (label_constrain!= null) missing= Math.min(missing, label_constrain.size()-labelsize2);
		if (missing!=0) bg_score= (float) (sum_bg_lprob - Math.log(inverted_index.size()));
		result.indices[labelsize2]= -1;
		result.values[labelsize2]= bg_score;
		if (norm_posteriors>0) {
			if (missing!= 0) sum= (float) logsum(sum, Math.log(missing)+ bg_score);
			for (n= 0; n< labelsize2+1;) result.values[n++]-= sum;
		}
		return result;
	}

	public void prepare_inference(int top_k, int max_retrieved, double label_threshold, double combination, int norm_posteriors) {
		this.label_threshold= (float) label_threshold;
		this.max_retrieved= max_retrieved;
		this.norm_posteriors= norm_posteriors;
		this.combination= (float)combination;
		if (model.prior_lprobs==null) {
			for (Enumeration<CountKey> e = model.cond_lprobs.keys(); e.hasMoreElements();) {
				CountKey key= e.nextElement();
				if (key.label<0) continue;
				prior_max_label= key.label;
				if (model.node_links!=null) prior_max_label= -(model.node_links.get(prior_max_label)+1);
				break;
			}
		}
		if (model.node_links!=null) {
			component_counts= new Hashtable<Integer, Integer>();
			for (Map.Entry<Integer, Integer> entry : model.node_links.entrySet()) {
				Integer label= entry.getValue();
				Integer count= component_counts.get(label);
				if (count==null) count= 1;
				else count++;
				component_counts.put(label, count);
			}
		}
		inverted_index= new Hashtable<Integer, IntBuffer>();
		inverted_index2= new Hashtable<Integer, FloatBuffer>();
		HashSet<Integer> tmp_labelset= new HashSet<Integer>();
		Hashtable<Integer, Integer> term_counts= new Hashtable<Integer, Integer>();
		for (Map.Entry<CountKey, Float> entry : model.cond_lprobs.entrySet()) {
			float lprob= entry.getValue();
			if (lprob==0) continue;
			Integer term= ((CountKey)entry.getKey()).term;
			Integer count= term_counts.get(term);
			if (count==null) count= 1;
			else count+= 1;
			term_counts.put(term, count);
		}
		for (Map.Entry<Integer, Integer> entry : term_counts.entrySet()) {
			Integer term= entry.getKey();
			Integer count= entry.getValue();
			int[] nodes= new int[count];
			IntBuffer wrap_nodes= IntBuffer.wrap(nodes);
			float[] lprobs= new float[count];
			FloatBuffer wrap_lprobs= FloatBuffer.wrap(lprobs);
			inverted_index.put(term, wrap_nodes);
			inverted_index2.put(term, wrap_lprobs);
			entry.setValue(0);
		}

		for (Map.Entry<CountKey, Float> entry : model.cond_lprobs.entrySet()) {
			float lprob= entry.getValue();
			CountKey p_index= entry.getKey();
			Integer term= p_index.term;
			Integer node= p_index.label;
			if (lprob==0) {
				//System.out.println("a "+term+" "+node+" "+lprob);
				continue;
			}//System.out.println(node +" "+term+" "+lprob);
			Integer count= term_counts.get(term);
			((IntBuffer)inverted_index.get(term)).put(count, node);
			((FloatBuffer)inverted_index2.get(term)).put(count, lprob);
			term_counts.put(term, count+1);
		}
		model.cond_lprobs= null;
		for (Map.Entry<Integer, IntBuffer> entry : inverted_index.entrySet()) {
			Integer term= entry.getKey();
			int[] nodes= entry.getValue().array();
			float[] lprobs= inverted_index2.get(term).array();
			TreeSet<DoubleBuffer> treesort= new TreeSet<DoubleBuffer>();
			for (int i= 0; i<nodes.length; i++) {
				double[] entry2= {nodes[i], lprobs[i]};
				treesort.add(DoubleBuffer.wrap(entry2));
			}
			Hashtable<Integer,Float> push_lprobs= new Hashtable<Integer,Float>();
			int count= 0;
			for (Iterator<DoubleBuffer> g= treesort.iterator(); g.hasNext();) {
				double[] entry2= g.next().array();
				Integer node= nodes[count]= (int)entry2[0];
				float lprob= (float)entry2[1];
				if (model.node_links!=null) {
					Integer bo_node= model.node_links.get(node);
					if (bo_node!=null) {
						Float lprob2= push_lprobs.get(bo_node);
						if (lprob2==null) {
							if (node<model.min_encoded_label) model.node_links.remove(node);
							//System.out.println(term+" "+node+" "+bo_node);
						}
						else lprob-= lprob2;
					}
				}
				if (lprob==0) continue;
				push_lprobs.put(node, lprob);
				//System.out.println("x "+term+" "+node+" "+bo_node+" "+lprob);
				nodes[count]= node;
				lprobs[count++]= lprob;
			}
			nodes= Arrays.copyOf(nodes, count);
			lprobs= Arrays.copyOf(lprobs, count);
			IntBuffer wrap_nodes= IntBuffer.wrap(nodes);
			FloatBuffer wrap_lprobs= FloatBuffer.wrap(lprobs);
			inverted_index.put(term, wrap_nodes);
			inverted_index2.put(term, wrap_lprobs);
		}

		num_classes= tmp_labelset.size();
		this.topset_size= (int)Math.max(1, top_k);
		//System.out.println(this.topset_size);
		if (labels2powerset!=null) {
			powerset2labels= new Hashtable<Integer, IntBuffer>(labels2powerset.size());
			for (Enumeration<IntBuffer> d= labels2powerset.keys(); d.hasMoreElements();) {
				IntBuffer labels= d.nextElement();
				Integer powerset= labels2powerset.get(labels);
				//System.out.println(powerset+" "+labels.array());
				powerset2labels.put(powerset, labels);
				labels2powerset.remove(labels);
			}
		}
		if (model.prior_lprobs!=null) {
			float max_lprob= -10000000;
			for (Map.Entry<Integer, Float> entry : model.prior_lprobs.entrySet()) if (entry.getValue()> max_lprob) {
				max_lprob= entry.getValue();
				prior_max_label= entry.getKey();
			}
			num_classes= model.prior_lprobs.size();
		}
	}


	public void infer_posteriors(PrintWriter resultsf, boolean constrain_labels) throws Exception {
		int[] labels;
		for (int w= 0; w < data.doc_count; w++) {
			labels= data.labels[w];
			if (constrain_labels) {
				float[] label_weights= data.label_weights[w];
				label_constrain= new HashSet<Integer>(labels.length);
				int label_count= 0;
				for (int i= 0; i < labels.length; i++) {
					label_constrain.add((Integer)labels[i]);
					if (label_weights[i]>0) label_count++;
				}
				int[] labels2= new int[label_count];
				label_count= 0;
				for (int i= 0; i < labels.length; i++) if (label_weights[i]>0) labels2[label_count++]= labels[i];
				labels= labels2;
			}
			//
			if (labels.length==0) continue;
			//
			SparseVector inference_results= inference(data.terms[w], data.counts[w]);

			if (powerset2labels!=null) {
				//SparseVector results= new SparseVector(powerset2labels.get((Integer)(int)f.next().array()[1]).array());
				//results.values= new float[1];
				//results.values[0]= (float)max_lprob;

				int[] labels2= powerset2labels.get((Integer)inference_results.indices[0]).array();
				inference_results= new SparseVector(Arrays.copyOf(labels2, labels2.length+1));
				inference_results.indices[labels2.length]= -1;
			}
			if (resultsf == null) {
				update_evaluation_results(inference_results.indices, labels, 1);
			} else {
				String results = "";
				for (int n = 0; n < inference_results.indices.length; n++)
					results += inference_results.indices[n] + " ";
				resultsf.println(results.trim());
			}
		}
	}

	public void use_powerset() {
		labels2powerset= new Hashtable<IntBuffer, Integer>();
	}

	public void kernel_densities() {
		model.node_links= new Hashtable<Integer, Integer>();
	}

	public void prepare_evaluation() {
		//prior_max_label= model.cond_lprobs.keys().nextElement().label;
		//if (powerset2labels!=null) prior_max_label= powerset2labels.get(prior_max_label).get(0);
		num_classified= tp= fp= fn= tp0= fp= fn0= 0;
		rec= prec= fscore= rec0= prec0= fscore0= map= 0;
	}

	private void update_evaluation_results(int[] labels, int[] ref_labels, int print_results) {
		String ref= num_classified + " Ref:"+Arrays.toString(ref_labels), res= num_classified + " Res:"+Arrays.toString(labels);
		HashSet<Integer> ref_labels2= new HashSet<Integer>(ref_labels.length);
		for (int label:ref_labels) ref_labels2.add((Integer)label); 
		int tp2= 0, fp2= 0;
		double ap= 0.0;
		int labels_length= labels.length-1;
		num_classified++;
		int map_div= Math.min(max_retrieved, ref_labels.length);
		for (int label:labels) {
			if (label==-1) continue;
			if (ref_labels2.contains((Integer)label)) {
				tp++;
				tp2++;
				//ap+= ((double)tp2/(tp2 + fp2)) / labels_length2;
				ap+= ((double)tp2/(tp2 + fp2))/map_div;
			} else {
				fp++; 
				fp2++;
				//ap+= ((double)tp2/(tp2 + fp2)) / labels_length2;
				//ap+= ((double)tp2/(tp2 + fp2)) /max_retrieved;
			}
		}
		//if (labels_length<max_retrieved) for (int i= labels_length; i<=max_retrieved; i++) ap+= tp2/i;
		//System.out.println(num_classified+" "+ ap+" "+max_retrieved+" "+ref_labels.length);
		if (tp2<map_div) for (int i= tp2; i<map_div; i++) ap+= ((double)tp2/(i + fp2))/map_div;
		double jaccard= (double)tp2/(labels_length+ref_labels.length-tp2);
		meanjaccard+= (jaccard-meanjaccard)/num_classified;
		fn+= ref_labels.length - tp2;
		rec= (double) tp / (tp + fn);
		prec= (double) tp / (tp + fp);
		fscore= (2.0 * rec * prec) / (rec + prec);

		/*if (labels[0]==0) if (ref_labels[0]== 0) tp0++; else fp0++;
	  else if (ref_labels[0]== 0) fn0++; 
	  rec0= (double) tp0 / (tp0 + fn0);	
	  prec0= (double) tp0 / (tp0 + fp0);
	  fscore0= (2.0 * rec0 * prec0) / (rec0 + prec0);
	  if ((rec0 == 0 && prec0 == 0) || (tp0 + fp0==0) || (tp0 + fn0==0)) fscore0= 0;
		 */

		//map+= (ap-map)/num_classified;
		map+= (ap-map)/num_classified;
		if ((rec == 0 && prec == 0) || (tp + fp==0) || (tp + fn==0)) fscore= 0;
		System.out.println(res);
		//System.out.println(ref + "      TP0:" + tp0 + " FN0:" + fn0 + " FP0:" + fp0 + " meanJaccard:"+meanjaccard+" miFscore:" + fscore + " MAP:" + map+" binFscore:"+fscore0);
		System.out.println(ref + "      TP:" + tp + " FN:" + fn + " FP:" + fp + " meanJaccard:"+meanjaccard+" miFscore:" + fscore + " MAP:" + map);
	}

	public void print_evaluation_summary() {
		String res= "";
		System.out.println("Results: meanJaccard:"+meanjaccard+" miFscore:" +fscore+ " MAP:" +map+ "  " +res);
		//System.out.println("Fscore: " +fscore+ "  " +res);
	}

	public void open_stream(String data_file, int docs, boolean use_label_weights) throws Exception {
		if (debug>0) System.out.println("SGM opening data stream: " + data_file);
		input_file= new BufferedReader(new FileReader(data_file));
		data= new SparseData(docs, use_label_weights);
		//if (data==null || docs!=data.doc_count) data= new SparseData(-1, docs, -1);
	}

	public void close_stream() throws Exception {
		input_file.close();
	}

	public int get_features(int docs) throws Exception {
		int w= 0;
		w= read_libsvm_stream(docs);
		return w;
	}

	public int read_libsvm_stream(int docs) throws Exception {
		String l;
		String[] splits, s;
		int[] labels, terms;
		float[] counts, label_weights= null;
		int w= 0;
		for (; w < docs; w++) {
			if ((l = input_file.readLine()) == null) break;
			int term_c= 0, i= 0;//, length= 0;
			for (char c: l.toCharArray()) if (c==':') term_c++;
			splits= l.split(" ");
			//System.out.println(splits.length+" "+term_c);
			int label_c= splits.length - term_c;
			data.labels[w]= labels= new int[label_c];
			data.terms[w]= terms= new int[term_c];
			data.counts[w]= counts= new float[term_c];
			if (data.label_weights!=null) data.label_weights[w]= label_weights= new float[label_c];
			for (; i < label_c; i++) {
				s= splits[i].split(",")[0].split(";");
				labels[i]= Integer.decode(s[0]);
				if (data.label_weights!=null)
					if (s.length>1) label_weights[i]= new Float(s[1]);
					else label_weights[i]= 1;
				//if (s.length>1 && data.label_weights!=null) label_weights[i]= new Float(s[1]);
			}
			for (; i < splits.length;) {
				//System.out.println(splits[i]);
				s= splits[i].split(":");
				Integer term= Integer.decode(s[0]);
				terms[i - label_c]= term;
				counts[i++ - label_c]= (float)Integer.decode(s[1]);
			}
		}
		if (w != docs) data.doc_count = w;
		return w;
	}

	public void save_model(String model_name) throws Exception {
		PrintWriter model_file = new PrintWriter(new FileWriter(model_name));
		model_file.println("train_count: " + model.train_count);
		model_file.println("cond_uniform: " + model.cond_uniform);
		if (model.prior_lprobs!=null) model_file.println("prior_lprobs: " + model.prior_lprobs.size());
		else model_file.println("prior_lprobs: 0");
		if (labels2powerset!=null) model_file.println("labels2powerset: " + labels2powerset.size());
		else model_file.println("labels2powerset: 0");
		model_file.println("tf_idf.normalized: "+ tfidf.normalized);
		model_file.println("idfs: " + tfidf.idfs.size());
		model_file.println("cond_bgs: " + model.cond_bgs.size());
		model_file.println("lprobs: " + model.cond_lprobs.size());
		if (model.node_links!=null) model_file.println("node_links: " + model.node_links.size());
		else model_file.println("node_links: 0");
		model_file.println("model.min_encoded_label: "+ model.min_encoded_label);
		if (model.prior_lprobs!=null)
			for (Map.Entry<Integer, Float> entry : model.prior_lprobs.entrySet()) model_file.println(entry.getKey()+" "+entry.getValue());
		if (labels2powerset!=null) 
			for (Enumeration<IntBuffer> d = labels2powerset.keys(); d.hasMoreElements();) {
				IntBuffer in = d.nextElement();
				int[] labelset= in.array();
				String tmp= "";
				for (int i= 0; i < labelset.length; i++) tmp+= labelset[i] + " ";
				model_file.println(tmp + labels2powerset.get(in));
			}
		for (Enumeration<Integer> d = tfidf.idfs.keys(); d.hasMoreElements();) {
			Integer in = d.nextElement();
			model_file.println(in + " " + tfidf.idfs.get(in));
		}
		for (Enumeration<Integer> d = model.cond_bgs.keys(); d.hasMoreElements();) {
			Integer in = d.nextElement();
			model_file.println(in + " " + model.cond_bgs.get(in));
		}
		for (Enumeration<CountKey> d = model.cond_lprobs.keys(); d.hasMoreElements();) {
			CountKey in = d.nextElement();
			model_file.println(in.label + " " + in.term + " " + model.cond_lprobs.get(in));
		}
		if (model.node_links!=null) for (Enumeration<Integer> d = model.node_links.keys(); d.hasMoreElements();) {
			Integer in = d.nextElement();
			model_file.println(in + " " + model.node_links.get(in));
		}
		model_file.close();
	}

	public void load_model(String model_name) throws Exception {
		model= new SGM_Params(10000000);
		BufferedReader input= new BufferedReader(new FileReader(model_name));
		model.train_count= tfidf.train_count= new Integer(input.readLine().split(" ")[1]);
		model.cond_uniform= new Double(input.readLine().split(" ")[1]);
		int prior_lprobs= new Integer(input.readLine().split(" ")[1]);
		int labels2powersets= new Integer(input.readLine().split(" ")[1]);
		tfidf.normalized= new Integer(input.readLine().split(" ")[1]);
		int idfs= new Integer(input.readLine().split(" ")[1]);
		int cond_bgs= new Integer(input.readLine().split(" ")[1]);
		int lprobs= new Integer(input.readLine().split(" ")[1]);
		int node_links= new Integer(input.readLine().split(" ")[1]);
		if (node_links!=0) model.node_links= new Hashtable<Integer, Integer>(node_links);
		model.min_encoded_label= new Integer(input.readLine().split(" ")[1]);
		if (prior_lprobs==0) model.prior_lprobs= null;
		while (prior_lprobs > 0) {
			String[] s= input.readLine().split(" ");
			Integer label= new Integer(s[0]);
			Float lprob = new Float(s[1]);
			model.prior_lprobs.put(label, lprob);
			prior_lprobs-= 1;
		}
		while (labels2powersets > 0) {
			String[] s= input.readLine().split(" ");
			int[] labelset = new int[s.length - 1];
			IntBuffer wrap_labelset = IntBuffer.wrap(labelset);
			for (int i = 0; i < labelset.length; i++) labelset[i] = new Integer(s[i]);
			Integer label= new Integer(s[s.length - 1]);
			labels2powerset.put(wrap_labelset, label);
			labels2powersets-= 1;
		}
		while (idfs > 0) {
			String[] s= input.readLine().split(" ");
			Integer label= new Integer(s[0]);
			Float idf= new Float(s[1]);
			tfidf.idfs.put(label, idf);
			idfs-= 1;
		}
		while (cond_bgs > 0) {
			String[] s= input.readLine().split(" ");
			Integer label= new Integer(s[0]);
			Float smooth= new Float(s[1]);
			model.cond_bgs.put(label, smooth);
			cond_bgs-= 1;
		}
		while (lprobs > 0) {
			String[] s= input.readLine().split(" ");
			Float lprob= new Float(s[2]);
			CountKey p_index= new CountKey(new Integer(s[0]), new Integer(s[1]));
			model.cond_lprobs.put(p_index, lprob);
			lprobs-= 1;
		}
		while (node_links > 0) {
			String[] s= input.readLine().split(" ");
			Integer node= new Integer(s[0]);
			Integer bo_node= new Integer(s[1]);
			model.node_links.put(node, bo_node);
			node_links-= 1;
		}
		input.close();
	}

}
