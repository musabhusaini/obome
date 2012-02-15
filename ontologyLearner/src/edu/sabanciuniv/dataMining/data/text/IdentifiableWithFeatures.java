package edu.sabanciuniv.dataMining.data.text;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.ImmutableSet;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.stanford.nlp.ling.HasWord;

/**
 * An identifiable object that has a list of features. 
 * @author Mus'ab Husaini
 * @param <T> Type of features; must extend {@link HasWord}.
 */
public abstract class IdentifiableWithFeatures<T extends HasWord> extends IdentifiableObject implements HasFeatures<T>, Comparable<IdentifiableWithFeatures<T>> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Set<T> features;
	
	/**
	 * Creates a new instance of {@link IdentifiableWithFeatures}.
	 */
	protected IdentifiableWithFeatures() {
		this(UUID.randomUUID());
	}
	
	/**
	 * Creates a new instance of {@link IdentifiableWithFeatures}.
	 * @param id The identifier of this object.
	 */
	protected IdentifiableWithFeatures(UUID id) {
		this(id, new ArrayList<T>());
	}
	
	/**
	 * Creates a new instance of {@link IdentifiableWithFeatures}.
	 * @param id The identifier of this object.
	 * @param features The features for this object.
	 */
	protected IdentifiableWithFeatures(UUID id, Iterable<T> features) {
		super(id);
		this.setFeatures(features);
	}
	
	/**
	 * Sets the features.
	 * @param features The features to set.
	 * @return Features of this instance.
	 */
	protected Set<T> setFeatures(Iterable<T> features) {
		if (features == null) {
			throw new IllegalArgumentException("Must supply non-null features.");
		}
		
		this.features = ImmutableSet.copyOf(features);
		return this.features;
	}
	
	@Override
	public Set<T> getFeatures() {
		return ImmutableSet.copyOf(this.features);
	}

	@Override
	public abstract IdentifiableWithFeatures<T> cloneOut(Iterable<T> otherFeatures);
	
	@Override
	public String toString() {
		return this.getFeatures().toString();
	}
	
	@Override
	public int compareTo(IdentifiableWithFeatures<T> other) {
		return this.getFeatures().size() - other.getFeatures().size();
	}
}