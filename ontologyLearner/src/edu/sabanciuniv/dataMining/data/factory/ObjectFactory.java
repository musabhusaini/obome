package edu.sabanciuniv.dataMining.data.factory;

/**
 * An interface for objects that can create other objects.
 * @author Mus'ab Husaini
 * @param <T> Types of objects that can be created by this factory.
 */
public interface ObjectFactory<T> {
	/**
	 * Creates a new object.
	 * @return Newly created object.
	 */
	public T create();
	
	/**
	 * Gets a flag indicating whether this factory is at its initial state or not.
	 * @return A flag indicating whether this factory has been used or not.
	 */
	public boolean isPristine();
	
	/**
	 * Resets this factory to the initial position.
	 * @return A flag indicating if this was successfully completed.
	 */
	public boolean reset();
	
	/**
	 * Closes this factory and release resources.
	 */
	public void close();
}
