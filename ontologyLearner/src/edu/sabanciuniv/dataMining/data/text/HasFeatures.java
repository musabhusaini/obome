package edu.sabanciuniv.dataMining.data.text;

import java.util.Set;

import edu.stanford.nlp.ling.HasWord;

/**
 * An object that has a list of features.
 * @author Mus'ab Husaini
 * @param <T> Type of features; must extend {@link HasWord}.
 */
public interface HasFeatures<T extends HasWord> {
	/**
	 * Gets the list of features associated with this instance.
	 * @return Set of features.
	 */
	public Set<T> getFeatures();
	
	/**
	 * Creates a clone of this instance with only features that exist in both this instance and the argument.
	 * @param otherFeatures Features to look for.
	 * @return The cloned instance.
	 */
	public HasFeatures<T> cloneOut(Iterable<T> otherFeatures);
}
