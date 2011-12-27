/**
 * 
 */
package edu.sabanciuniv.dataMining.data.factory;

/**
 * An abstract implementation of the {@link ObjectFactory} interface.
 * @author Mus'ab Husaini
 * @param <T> Type of objects that this factory can create.
 */
public abstract class AbstractObjectFactory<T> implements ObjectFactory<T> {
	private int count;
	
	@Override
	public T create() {
		this.setCount(this.getCount() + 1);
		return null;
	}

	/**
	 * Gets the number of objects that this factory has created since it was last reset.
	 * @return The object count.
	 */
	public int getCount() {
		return this.count;
	}
	
	/**
	 * Sets the object count.
	 * @param count The count to set.
	 * @return Returns the value that was set.
	 */
	protected int setCount(int count) {
		return (this.count = count);
	}
	
	@Override
	public void close() {
	}
	
	@Override
	public boolean reset() {
		this.setCount(0);
		return true;
	}
}