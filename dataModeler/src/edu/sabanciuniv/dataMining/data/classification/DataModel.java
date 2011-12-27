package edu.sabanciuniv.dataMining.data.classification;

public interface DataModel<T,C> {
	public void learn(Classifiable<T,C> instance);
	public C classify(T instance);
}
