package edu.sabanciuniv.dataMining.program;

import edu.sabanciuniv.dataMining.data.classification.ConfusionMatrix;
import edu.sabanciuniv.dataMining.data.classification.RiskFunction;
import edu.sabanciuniv.dataMining.data.classification.text.opinion.HotelNaiveBayesModel;
import edu.sabanciuniv.dataMining.data.factory.text.opinion.HotelReviewFactory;

public class DataModelerProgram {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RiskFunction<Short> riskFunction = new RiskFunction<Short>() {
			@Override
			public double getRisk(Short actual, Short predicted) {
				return actual.equals(predicted) ? 0.0 : 1.0 + Math.abs(actual - predicted) * 0.0;
			}
		};
		
		HotelNaiveBayesModel model = new HotelNaiveBayesModel(riskFunction);
		HotelReviewFactory factory = new HotelReviewFactory("training");
		model.train(factory);
		factory.close();
		
		factory = new HotelReviewFactory("testing");
		ConfusionMatrix<Short> confusionMatrix = model.calculatePerformance(factory);
		factory.close();
		
		System.out.println();
		
		Iterable<Short> classes = confusionMatrix.getAllClasses();
		
		for (Short classification : classes) {
			System.out.println("For class: " + classification);
			System.out.println("Precision: " + confusionMatrix.getPrecision(classification));
			System.out.println("Accuracy: " + confusionMatrix.getAccuracy(classification));
			System.out.println("Recall: " + confusionMatrix.getRecall(classification));
			System.out.println("Specificity: " + confusionMatrix.getSpecificity(classification));
			System.out.println();
		}
		
		System.out.print(confusionMatrix);
	}
}
