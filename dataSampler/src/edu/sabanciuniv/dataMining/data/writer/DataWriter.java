package edu.sabanciuniv.dataMining.data.writer;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.data.sampling.Category;

public abstract class DataWriter {
	public abstract boolean write(IdentifiableObject obj, Category category);
	public abstract void close();
}