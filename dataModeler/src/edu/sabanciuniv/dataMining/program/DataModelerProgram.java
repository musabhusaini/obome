package edu.sabanciuniv.dataMining.program;

import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.DatabaseLoader;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.unsupervised.attribute.NominalToString;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.filters.unsupervised.instance.RemovePercentage;

import com.google.common.base.Function;

import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions;
import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.data.text.filter.ModifyText;
import edu.sabanciuniv.dataMining.data.text.filter.RemoveInvariant;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticToken;

public class DataModelerProgram {

	private static void printEvaluationStats(Evaluation eval, String name) throws Exception {
		System.out.println();
		System.out.println(eval.toSummaryString(name + " Summary\n=============\n", false));
		
		System.out.println();
		System.out.println(eval.toClassDetailsString(name + " Class Details\n=============\n"));
		
		//System.out.println(eval.toMatrixString("\n" + name + " Matrix\n==========\n"));
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		DatabaseLoader loader = new DatabaseLoader();
		int n = 30000;
		loader.setSource("jdbc:mysql://localhost/trip_advisor", "java_user", "java_user_pwd");

//		loader.setQuery("(SELECT uuid, content, rating > 2 AS rating FROM reviews WHERE rating < 3 LIMIT " + n/2 + ") UNION " +
//				"(SELECT uuid, content, rating > 2 AS rating FROM reviews WHERE rating > 2 LIMIT " + n/2 + ");");

		loader.setQuery("(SELECT uuid, content, rating FROM reviews WHERE rating=1 LIMIT " + n/5 + ") UNION " +
				"(SELECT uuid, content, rating FROM reviews WHERE rating=2 LIMIT " + n/5 + ") UNION " +
				"(SELECT uuid, content, rating FROM reviews WHERE rating=3 LIMIT " + n/5 + ") UNION " +
				"(SELECT uuid, content, rating FROM reviews WHERE rating=4 LIMIT " + n/5 + ") UNION " +
				"(SELECT uuid, content, rating FROM reviews WHERE rating=5 LIMIT " + n/5 + ");");

		loader.setKeys("uuid");
		Instances dataset = loader.getDataSet();
		
		NumericToNominal num2nomFilter = new NumericToNominal();
		num2nomFilter.setAttributeIndicesArray(new int[]{dataset.attribute("rating").index()});

		NominalToString nom2strFilter = new NominalToString();
		nom2strFilter.setAttributeIndexes(String.valueOf(dataset.attribute("content").index()+1));
		
		MultiFilter multiFilter = new MultiFilter();
		multiFilter.setFilters(new Filter[]{num2nomFilter, nom2strFilter});
		multiFilter.setInputFormat(dataset);
		dataset = Filter.useFilter(dataset, multiFilter);
		
		dataset.setClass(dataset.attribute("rating"));

		StringToWordVector strToWVFilter = new StringToWordVector();
		strToWVFilter.setAttributeIndicesArray(new int[]{dataset.attribute("content").index()});
		strToWVFilter.setAttributeNamePrefix("word_");

		ModifyText modifyTextFilter = new ModifyText();
		modifyTextFilter.setAttributeIndicesArray(new int[]{dataset.attribute("content").index()});
		modifyTextFilter.setModifierFunction(new Function<String,String>() {
			@Override
			public String apply(String input) {
//				System.out.println(input);
//				return input;
				
				TextDocumentOptions options = new TextDocumentOptions();
				options.setFeatureType(TextDocumentOptions.FeatureType.NOUNS_ADJECTIVES);
				TextDocument doc = new TextDocument(options);
				doc.setText(input);
				
				String features = "";
				for (LinguisticToken feature : doc.getFeatures()) {
					features += feature.word() + " ";
				}
				features = features.trim();
				
				System.out.println(features);
				return features;
			}
		});

		Remove remAttrFilter = new Remove();
		remAttrFilter.setAttributeIndicesArray(new int[]{dataset.attribute("uuid").index()});

		num2nomFilter = new NumericToNominal();
		num2nomFilter.setAttributeIndices("first-last");

		RemoveInvariant remInvariantFilter = new RemoveInvariant();
		remInvariantFilter.setMaximumVariancePercentageAllowed(95);

		multiFilter = new MultiFilter();
		multiFilter.setFilters(new Filter[]{modifyTextFilter, strToWVFilter, remAttrFilter, num2nomFilter, remInvariantFilter});
		multiFilter.setInputFormat(dataset);
		dataset = Filter.useFilter(dataset, multiFilter);

		Random rand = dataset.getRandomNumberGenerator(146);
		dataset.randomize(rand);
		
		RemovePercentage rpf = new RemovePercentage();
		rpf.setPercentage(25.0);
		rpf.setInputFormat(dataset);
		Instances training = Filter.useFilter(dataset, rpf);
		training.numInstances();
		rpf.setInvertSelection(true);
		Instances testing = Filter.useFilter(dataset, rpf);
		
		Evaluation eval;
		
		NaiveBayes nb = new NaiveBayes();
		nb.buildClassifier(training);
		eval = new Evaluation(training);
		eval.evaluateModel(nb, testing);
		printEvaluationStats(eval, "Naive Bayes'");
		
		int k = 5;
		IBk ibk = new IBk(k);
		ibk.buildClassifier(training);
		eval = new Evaluation(training);
		eval.evaluateModel(ibk, testing);
		printEvaluationStats(eval, k + "-NN");
		
		J48 j48 = new J48();
		j48.buildClassifier(training);
		eval = new Evaluation(training);
		eval.evaluateModel(j48, testing);
		printEvaluationStats(eval, "J48");

//		Vote vote = new Vote();
//		vote.setClassifiers(new Classifier[]{nb, ibk, j48});
//		vote.buildClassifier(training);
//		eval = new Evaluation(training);
//		eval.evaluateModel(vote, testing);
//		printEvaluationStats(eval, "Voted Classifier");
		
//		RiskFunction<Short> riskFunction = new RiskFunction<Short>() {
//			@Override
//			public double getRisk(Short actual, Short predicted) {
//				return actual.equals(predicted) ? 0.0 : 1.0 + Math.abs(actual - predicted) * 0.0;
//			}
//		};
//		
//		HotelNaiveBayesModel model = new HotelNaiveBayesModel(riskFunction);
//		HotelReviewFactory factory = new HotelReviewFactory("training");
//		model.train(factory);
//		factory.close();
//		
//		factory = new HotelReviewFactory("testing");
//		ConfusionMatrix<Short> confusionMatrix = model.calculatePerformance(factory);
//		factory.close();
//		
//		System.out.println();
//		
//		Iterable<Short> classes = confusionMatrix.getAllClasses();
//		
//		for (Short classification : classes) {
//			System.out.println("For class: " + classification);
//			System.out.println("Precision: " + confusionMatrix.getPrecision(classification));
//			System.out.println("Accuracy: " + confusionMatrix.getAccuracy(classification));
//			System.out.println("Recall: " + confusionMatrix.getRecall(classification));
//			System.out.println("Specificity: " + confusionMatrix.getSpecificity(classification));
//			System.out.println();
//		}
//		
//		System.out.print(confusionMatrix);
	}
}
