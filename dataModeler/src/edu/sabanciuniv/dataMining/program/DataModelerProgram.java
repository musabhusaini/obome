package edu.sabanciuniv.dataMining.program;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.J48;
import weka.core.Instances;
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

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		DatabaseLoader loader = new DatabaseLoader();
		loader.setSource("jdbc:mysql://localhost/trip_advisor", "java_user", "java_user_pwd");
		loader.setQuery("SELECT uuid, content, rating FROM reviews WHERE rating > 0 LIMIT 50000");
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

		dataset.randomize(dataset.getRandomNumberGenerator(146));
		
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
		System.out.println(eval.toSummaryString("\nNaive Bayes' Summary:\n==========\n", false));
		System.out.println(eval.toMatrixString("\nNaive Bayes' Matrix:\n==========\n"));

		IBk ibk = new IBk(3);
		ibk.buildClassifier(training);
		eval = new Evaluation(training);
		eval.evaluateModel(ibk, testing);
		System.out.println(eval.toSummaryString("\n3-NN Results:\n==========\n", false));
		System.out.println(eval.toMatrixString("\n3-NN Matrix:\n==========\n"));
		
		J48 j48 = new J48();
		j48.buildClassifier(training);
		eval = new Evaluation(training);
		eval.evaluateModel(j48, testing);
		System.out.println(eval.toSummaryString("\nJ48 Results:\n==========\n", false));
		System.out.println(eval.toMatrixString("\nJ48 Matrix:\n==========\n"));
		
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
