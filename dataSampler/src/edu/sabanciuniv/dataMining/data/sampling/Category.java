package edu.sabanciuniv.dataMining.data.sampling;

public class Category implements Comparable<Category> {
	private String name;
	private double ratio;
	
	public Category() {
		this(new Double(Math.random()).toString());
	}

	public Category(String name) {
		this(name, 1.0);
	}
	
	public Category(String name, double ratio) {
		if (name == null || name == "") {
			throw new IllegalArgumentException("Must provide a valid name");
		}
		if (ratio < 0 || ratio > 1) {
			throw new IllegalArgumentException("Ration must be between 0 and 1");
		}
		
		this.setName(name);
		this.setRatio(ratio);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the ratio
	 */
	public double getRatio() {
		return ratio;
	}

	/**
	 * @param ratio the ratio to set
	 */
	public void setRatio(double ratio) {
		this.ratio = ratio;
	}

	@Override
	public int compareTo(Category other) {
		return (int)((this.getRatio() - other.getRatio()) * 100);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Category) {
			return this.name.equals(((Category)o).name);
		}
		
		return super.equals(o);
	}
	
	@Override
	public String toString() {
		return this.name + "/" + (Math.round(this.ratio*100)/100.0);
	}
}
