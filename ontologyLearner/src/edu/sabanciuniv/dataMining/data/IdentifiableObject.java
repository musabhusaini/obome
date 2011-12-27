package edu.sabanciuniv.dataMining.data;

import java.util.UUID;

/**
 * An object that has an identifier.
 * @author Mus'ab Husaini
 */
public class IdentifiableObject implements Identifiable {
	private UUID id;

	/**
	 * Creates a new object of type {@link IdentifiableObject}.
	 */
	public IdentifiableObject() {
		this(UUID.randomUUID());
	}
	
	/**
	 * Creates a new object of type {@link IdentifiableObject}.
	 * @param id Identifier of this object.
	 */
	public IdentifiableObject(UUID id) {
		this.setIdentifier(id);
	}
	
	/**
	 * Creates a new object of type {@link IdentifiableObject}.
	 * @param id Identifier of the object.
	 */
	public IdentifiableObject(String id) {
		this(UUID.fromString(id));
	}
	
	/**
	 * Sets the identifier for this object.
	 * @param id The identifier to set.
	 */
	public void setIdentifier(String id) {
		this.setIdentifier(UUID.fromString(id));
	}
	
	/**
	 * Sets the identifier for this object.
	 * @param id The identifier to set.
	 */
	public void setIdentifier(UUID id) {
		if (id == null) {
			throw new IllegalArgumentException();
		}
		
		this.id = id;
	}
	
	@Override
	public UUID getIdentifier() {
		return this.id;
	}
	
	@Override
	public String toString() {
		return this.id.toString();
	}
}
