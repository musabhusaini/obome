package edu.sabanciuniv.dataMining.data.classification;

public interface Classifiable<T,C> {
	public C getClassification();
	public T getInstance();
}
