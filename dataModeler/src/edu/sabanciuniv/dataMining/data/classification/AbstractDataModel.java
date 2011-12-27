package edu.sabanciuniv.dataMining.data.classification;

import edu.sabanciuniv.dataMining.data.factory.ObjectFactory;

public abstract class AbstractDataModel<T,C> implements DataModel<T,C> {
	
	public void train(ObjectFactory<? extends Classifiable<T,C>> trainingFactory) {
		if (trainingFactory == null) {
			throw new IllegalArgumentException("Must supply a valid factory.");
		}
		
		Classifiable<T,C> instance = null;
		while ((instance = trainingFactory.create()) != null) {
			this.learn(instance);
		}
	}
	
	public ConfusionMatrix<C> calculatePerformance(ObjectFactory<? extends Classifiable<T,C>> testingFactory) {
		if (testingFactory == null) {
			throw new IllegalArgumentException("Must supply a valid factory.");
		}

		ConfusionMatrix<C> confusion = new ConfusionMatrix<>();
		Classifiable<T,C> instance = null;
		while ((instance = testingFactory.create()) != null) {
			C prediction = this.classify(instance.getInstance());
			confusion.see(instance, prediction);
		}
		
		return confusion;
	}
}