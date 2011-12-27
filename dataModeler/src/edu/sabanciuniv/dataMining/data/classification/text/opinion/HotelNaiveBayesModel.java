package edu.sabanciuniv.dataMining.data.classification.text.opinion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.Iterables;

import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions;
import edu.sabanciuniv.dataMining.data.classification.Classifiable;
import edu.sabanciuniv.dataMining.data.classification.RiskFunction;
import edu.sabanciuniv.dataMining.data.text.opinion.UnratedHotelReview;
import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.util.EntryValueComparator;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticToken;

public class HotelNaiveBayesModel extends HotelDataModel {
	private static final TextDocumentOptions.FeatureType featureType = TextDocumentOptions.FeatureType.NOUNS_ADJECTIVES;
	
	private HashMap<String,HashMap<Short,Long>> featureRatingMap;
	private HashMap<Short,Long> ratingMap;
	private long totalCount;
	private RiskFunction<Short> riskFunction;
	
	private long getCount(String word, Short rating) {
		HashMap<Short,Long> map = this.featureRatingMap.get(word);
		if (map != null) {
			Long count = map.get(rating);
			if (count != null) {
				return count;
			}
		}
		
		return 0L;
	}

	private long getCount(Short rating) {
		Long count = this.ratingMap.get(rating);
		if (count != null) {
			return count;
		}
		
		return 0L;
	}
	
	private long getCount() {
		return this.totalCount;
	}
	
	private void increment(String word, Short rating) {
		HashMap<Short,Long> map = this.featureRatingMap.get(word);
		if (map == null) {
			map = new HashMap<>();
			this.featureRatingMap.put(word, map);
		}
		
		Long val = map.get(rating);
		if (val == null) {
			val = 0L;
		}
		map.put(rating, val+1);
		
		val = this.ratingMap.get(rating);
		if (val == null) {
			val = 0L;
		}
		this.ratingMap.put(rating, val+1);
		
		this.totalCount++;
	}

	public HotelNaiveBayesModel() {
		this(null);
	}
	
	public HotelNaiveBayesModel(RiskFunction<Short> riskFunction) {
		this.featureRatingMap = new HashMap<String,HashMap<Short,Long>>();
		this.ratingMap = new HashMap<Short,Long>();
		this.totalCount = 0L;
		
		if (riskFunction == null) {
			riskFunction = new RiskFunction<Short>() {
				@Override
				public double getRisk(Short actual, Short predicted) {
					return (actual.equals(predicted) ? 0.0 : 1.0);
				}
			};
		}
		this.riskFunction = riskFunction;
	}
	
	@Override
	public void learn(Classifiable<UnratedHotelReview, Short> instance) {
		Short rating = instance.getClassification();
		UnratedHotelReview review = instance.getInstance();
		
		if (review == null || review.getContent() == null) {
			throw new IllegalArgumentException("Invalid instance.");
		}
		
		TextDocumentOptions options = new TextDocumentOptions();
		options.setFeatureType(HotelNaiveBayesModel.featureType);
		TextDocument document = new TextDocument(options);
		document.setIdentifier(review.getIdentifier());
		document.setText(review.getContent());
		Iterable<LinguisticToken> features = document.getFeatures();
		for(LinguisticToken feature : features) {
			this.increment(feature.word(), rating);
		}
	}

	@Override
	public Short classify(UnratedHotelReview instance) {
		HashMap<Short,Double> scores = new HashMap<Short,Double>();
		
		TextDocumentOptions options = new TextDocumentOptions();
		options.setFeatureType(HotelNaiveBayesModel.featureType);
		TextDocument document = new TextDocument(options);
		document.setIdentifier(instance.getIdentifier());
		document.setText(instance.getContent());
		Iterable<LinguisticToken> features = document.getSummary().getFeatures();
		for (Short rating : this.ratingMap.keySet()) {
			double score = 0;
			for(LinguisticToken feature : features) {
				score += Math.log((this.getCount(feature.word(), rating)+0.5)/(this.getCount(rating)+1));
			}
			score += Math.log(this.getCount(rating)/(double)this.getCount());
			scores.put(rating, score);
		}
		
		HashMap<Short,Double> risks = new HashMap<Short,Double>();
		for (Short actual : this.ratingMap.keySet()) {
			double risk = 0.0;
			for (Short prediction : scores.keySet()) { 
				risk += scores.get(prediction) * this.riskFunction.getRisk(actual, prediction);
			}
			risks.put(actual, risk);
		}
		
		List<Entry<Short,Double>> sortedRisks = new ArrayList<Entry<Short,Double>>(risks.entrySet());
		Collections.sort(sortedRisks, new EntryValueComparator<Short,Double>());
		return Iterables.getFirst(sortedRisks, null).getKey();
	}
}