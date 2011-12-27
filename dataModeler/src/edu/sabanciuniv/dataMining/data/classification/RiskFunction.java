package edu.sabanciuniv.dataMining.data.classification;

public interface RiskFunction<C> {
	public double getRisk(C actual, C predicted);
}
