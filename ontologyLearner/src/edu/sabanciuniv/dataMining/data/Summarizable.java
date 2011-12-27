package edu.sabanciuniv.dataMining.data;

/**
 * An object that can be summarized into a smaller description.
 * @author Mus'ab Husaini
 * @param <T> Type of object that is summarized.
 */
public interface Summarizable<T> {
	/**
	 * Gets a summarized form of this object.
	 * @return The summary form.
	 */
	public Summary<T> getSummary();
	
	/**
	 * Gets the elaborate form of the object.
	 * @return The elaborate form.
	 */
	public T getElaborate();
}
