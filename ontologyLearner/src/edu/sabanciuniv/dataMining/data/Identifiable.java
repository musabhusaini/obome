package edu.sabanciuniv.dataMining.data;

import java.util.UUID;

/**
 * An object that has a UUID.
 * @author Mus'ab Husaini
 */
public interface Identifiable {
	/**
	 * Gets the identifier of this instance.
	 * @return The identifier of this instance.
	 */
	public UUID getIdentifier();
}
