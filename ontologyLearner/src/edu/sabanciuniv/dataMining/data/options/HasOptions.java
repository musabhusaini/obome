package edu.sabanciuniv.dataMining.data.options;

/**
 * An object that implements this interface has some underlying {@link Options}.
 * @author Mus'ab Husaini
 * @param <T> Type of options; must extend {@link Options}.
 */
public interface HasOptions<T extends Options> {
	
	/**
	 * Gets the options. 
	 * @return The options.
	 */
	public T getOptions();
}
