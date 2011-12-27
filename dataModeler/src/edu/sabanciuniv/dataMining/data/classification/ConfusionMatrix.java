package edu.sabanciuniv.dataMining.data.classification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;

import edu.stanford.nlp.util.Sets;

public class ConfusionMatrix<C> {
	private HashMap<C,HashMap<C,Long>> matrix;
	
	private void increment(C actual, C predicted) {
		if (actual == null || predicted == null) {
			throw new IllegalArgumentException("Must provide valid classification.");
		}

		HashMap<C,Long> map = this.matrix.get(actual);
		if (map == null) {
			map = new HashMap<>();
			this.matrix.put(actual, map);
		}
		
		Long val = map.get(predicted);
		if (val == null) {
			val = 0L;
		}
		map.put(predicted, val+1);
	}
	
	private long getValue(C actual, C predicted) {
		if (actual == null || predicted == null) {
			throw new IllegalArgumentException("Must provide valid classification.");
		}

		HashMap<C,Long> map = this.getActualMap(actual);
		if (!map.containsKey(predicted)) {
			return 0L;
		}
		return map.get(predicted);
	}

	private HashMap<C,Long> getActualMap(C actual) {
		if (actual == null) {
			throw new IllegalArgumentException("Must provide valid classification.");
		}

		HashMap<C,Long> map = this.matrix.get(actual);
		if (map == null) {
			map = new HashMap<>();
		}
		return map;
	}
	
	private HashMap<C,Long> getPredictedMap(C predicted) {
		if (predicted == null) {
			throw new IllegalArgumentException("Must provide valid classification.");
		}

		HashMap<C,Long> map = new HashMap<>();
		for (C actual : this.matrix.keySet()) {
			map.put(actual, this.getValue(actual, predicted));
		}
		return map;
	}
	
	public ConfusionMatrix() {
		this.matrix = new HashMap<>();
	}
	
	public boolean see(Classifiable<?,C> instance, C predicted) {
		if (instance == null) {
			throw new IllegalArgumentException("Must provide valid instance.");
		}
		
		C actual = instance.getClassification();
		this.increment(actual, predicted);
		return actual.equals(predicted);
	}
	
	public Iterable<C> getAllClasses() {
		Set<C> classes = this.matrix.keySet();
		for (C key : this.matrix.keySet()) {
			classes = Sets.union(classes, this.matrix.get(key).keySet());
		}
		return classes;
	}
	
	public long getTotal() {
		long t = 0L;
		for (C key : this.matrix.keySet()) {
			t += this.getTotal(key);
		}
		return t;
	}
	
	public long getTotal(C classification) {
		long t = 0L;
		HashMap<C,Long> map = this.getActualMap(classification);
		for (C key : map.keySet()) {
			t += map.get(key);
		}
		return t;
	}
	
	public long getTruePositives() {
		long tp = 0L;
		for (C key : this.matrix.keySet()) {
			tp += this.getTruePositives(key);
		}
		return tp;
	}

	public long getTruePositives(C classification) {
		return this.getValue(classification, classification);
	}

	public long getFalsePositives() {
		long fp = 0L;
		for (C key : this.matrix.keySet()) {
			fp += this.getFalsePositives(key);
		}
		return fp;
	}
	
	public long getFalsePositives(C classification) {
		Map<C,Long> map = Maps.filterKeys(this.getPredictedMap(classification), Predicates.not(Predicates.equalTo(classification)));
		long fp = 0L;
		for (C key : map.keySet()) {
			fp += map.get(key);
		}
		return fp;
	}
	
	public long getFalseNegatives() {
		long fn = 0L;
		for (C key : this.matrix.keySet()) {
			fn += this.getFalseNegatives(key);
		}
		return fn;
	}
	
	public long getFalseNegatives(C classification) {
		Map<C,Long> map = Maps.filterKeys(this.getActualMap(classification), Predicates.not(Predicates.equalTo(classification)));
		long fn = 0L;
		for (C key : map.keySet()) {
			fn += map.get(key);
		}
		return fn;
	}
	
	// Might not be as meaningful; we'll see.
	public long getTrueNegatives() {
		long tn = 0L;
		for (C key : this.matrix.keySet()) {
			tn += this.getTrueNegatives(key);
		}
		return tn;
	}
	
	public long getTrueNegatives(C classification) {
		Map<C,HashMap<C,Long>> actualsMap = Maps.filterKeys(this.matrix, Predicates.not(Predicates.equalTo(classification)));
		long tn = 0L;
		for (C actualKey : actualsMap.keySet()) {
			Map<C,Long> predictedMap = Maps.filterKeys(actualsMap.get(actualKey), Predicates.not(Predicates.equalTo(classification)));
			for (C predictedKey : predictedMap.keySet()) {
				tn += predictedMap.get(predictedKey);
			}
		}
		return tn;
	}
	
	public double getPrecision() {
		double tp = this.getTruePositives();
		double fp = this.getFalsePositives();
		if (tp == 0) {
			return 0;
		}
		
		return tp / (tp + fp);
	}
	
	public double getPrecision(C classification) {
		double tp = this.getTruePositives(classification);
		double fp = this.getFalsePositives(classification);
		if (tp == 0) {
			return 0;
		}

		return tp / (tp + fp);
	}
	
	public double getRecall() {
		double tp = this.getTruePositives();
		double fn = this.getFalseNegatives();
		if (tp == 0) {
			return 0;
		}
		
		return tp / (tp + fn);
	}

	public double getRecall(C classification) {
		double tp = this.getTruePositives(classification);
		double fn = this.getFalseNegatives(classification);
		if (tp == 0) {
			return 0;
		}
		
		return tp / (tp + fn);
	}

	public double getSpecificity() {
		double tn = this.getTrueNegatives();
		double fp = this.getFalsePositives();
		if (tn == 0) {
			return 0;
		}
		
		return tn / (tn + fp);
	}

	public double getSpecificity(C classification) {
		double tn = this.getTrueNegatives(classification);
		double fp = this.getFalsePositives(classification);
		if (tn == 0) {
			return 0;
		}
		
		return tn / (tn + fp);
	}

	public double getAccuracy() {
		double tp = this.getTruePositives();
		double tn = this.getTrueNegatives();
		double fp = this.getFalsePositives();
		double fn = this.getFalseNegatives();
		if (tp + tn == 0) {
			return 0;
		}
		
		return (tp + tn) / (tp + tn + fp + fn);
	}

	public double getAccuracy(C classification) {
		double tp = this.getTruePositives(classification);
		double tn = this.getTrueNegatives(classification);
		double fp = this.getFalsePositives(classification);
		double fn = this.getFalseNegatives(classification);
		if (tp + tn == 0) {
			return 0;
		}
		
		return (tp + tn) / (tp + tn + fp + fn);
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		Iterable<C> classes = this.getAllClasses();
		builder.append("A\\P|");
		builder.append(Joiner.on("|").join(classes));
		builder.append("\n");
		for (C key : classes) {
			builder.append(key + "|");
			List<Long> vals = new ArrayList<>();
			Map<C,Long> actuals = this.getActualMap(key);
			for (C key1 : classes) {
				Long val = actuals.get(key1);
				if (val != null) {
					vals.add(val);
				} else {
					vals.add(0L);
				}
			}
			builder.append(Joiner.on("|").join(vals));
			builder.append("\n");
		}
		
		return builder.toString();
	}
}