package edu.sabanciuniv.dataMining.data;

/**
 * An object that is a summary of another object.
 * @author Mus'ab Husaini
 * @param <T> The type of object being summarized.
 */
public interface Summary<T> {
	/**
	 * Initialize this instance from the elaborate form. 
	 * @param elaborate Elaborate form of the object.
	 */
	public void initialize(Summarizable<T> elaborate);
}
