package edu.sabanciuniv.dataMining.data.sampling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Iterables;

public class CategorySet {
	private List<Category> categories;
	
	public CategorySet(Iterable<Category> categories) {
		this.categories = new ArrayList<Category>();
		for (Category category : categories) {
			if (!this.categories.contains(category)) {
				this.categories.add(category);
			}
		}
		
		if (this.getTotalRatio() > 1.0) {
			throw new IllegalArgumentException("Total data composition must not exceed 1.0");
		}
	}
	
	public CategorySet() {
		this(new ArrayList<Category>());
	}
	
	public Iterable<Category> getCategories() {
		return Iterables.unmodifiableIterable(this.categories);
	}
	
	public double getTotalRatio() {
		double totalRatio = 0;
		for (Category category : this.categories) {
			totalRatio += category.getRatio();
		}
		return totalRatio;
	}
	
	public boolean add(Category category) {
		if (this.isClosed()) {
			return false;
		}
		if (this.categories.contains(category)) {
			return false;
		}
		if (this.getTotalRatio() + category.getRatio() > 1.0) {
			return false;
		}
		
		this.categories.add(category);
		Collections.sort(this.categories, Collections.reverseOrder());
		return true;
	}
	
	public boolean isClosed() {
		return this.getTotalRatio() == 1.0;
	}
	
	public void reset() {
		this.categories.clear();
	}
//	
//	public boolean remove(Category category) {
//		return this.categories.remove(category);
//	}
	
	@Override
	public String toString() {
		return this.categories.toString();
	}
}