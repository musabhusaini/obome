package edu.sabanciuniv.dataMining.data.sampling;

import java.util.Random;

import com.google.common.collect.Iterables;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.data.factory.ObjectFactory;
import edu.sabanciuniv.dataMining.data.writer.DataWriter;

public class Sampler {
	private ObjectFactory<IdentifiableObject> factory;
	private CategorySet categorySet;
	private DataWriter writer;
	private Random random;
	
	public Sampler(ObjectFactory<IdentifiableObject> factory, CategorySet categorySet, DataWriter writer) {
		this.setFactory(factory);
		this.setCategorySet(categorySet);
		this.setWriter(writer);
		this.reset();
	}
		
	private Category getNextCategory() {
		double rand = this.random.nextDouble();
		double ratio = 0;
		for (Category category : this.getCategorySet().getCategories()) {
			ratio += category.getRatio();
			if (rand <= ratio) {
				return category;
			}
		}
		
		return Iterables.getLast(this.getCategorySet().getCategories(), null);
	}
	
	public void sample() {
		IdentifiableObject obj;
		while ((obj = this.getFactory().create()) != null) {
			this.getWriter().write(obj, this.getNextCategory());
		}
	}

	public ObjectFactory<IdentifiableObject> getFactory() {
		return factory;
	}

	public CategorySet getCategorySet() {
		return categorySet;
	}

	public DataWriter getWriter() {
		return writer;
	}

	public void setFactory(ObjectFactory<IdentifiableObject> factory) {
		if (factory == null) {
			throw new IllegalArgumentException("Must provide a factory.");
		}

		this.factory = factory;
	}

	public void setCategorySet(CategorySet categorySet) {
		if (categorySet == null) {
			throw new IllegalArgumentException("Must provide a category set.");
		}
		if (!categorySet.isClosed()) {
			throw new IllegalArgumentException("Category set must be closed.");
		}

		this.categorySet = categorySet;
	}

	public void setWriter(DataWriter writer) {
		if (writer == null) {
			throw new IllegalArgumentException("Must provide a writer.");
		}
		
		this.writer = writer;
	}
	
	public void reset() {
		this.factory.reset();
		this.random = new Random();
	}
	
	public void close() {
		this.factory.close();
		this.writer.close();
	}
}