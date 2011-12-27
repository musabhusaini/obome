package edu.sabanciuniv.dataMining.util.text.nlp.english;

import java.security.InvalidParameterException;

/**
 * Abstract class for any linguistic entity.
 * @author Mus'ab Husaini
 */
public abstract class LinguisticEntity implements Comparable<LinguisticEntity> {
	protected String text;

	/**
	 * Creates an instasnce of {@link LinguisticEntity}.
	 */
	protected LinguisticEntity() {
		this("");
	}
	
	/**
	 * Creates an instance of {@link LinguisticEntity} with the specified text value.
	 * @param text The text value.
	 */
	protected LinguisticEntity(String text) {
		if (text == null) {
			throw new InvalidParameterException("Must provide non-null text");
		}
		
		this.text = text;
	}
	
	/**
	 * Gets the text value of this entity.
	 * @return The text value of this entity.
	 */
	public String getText() {
		return this.text;
	}
	
	@Override
	public String toString() {
		return this.getText();
	}
	
	@Override
	public int compareTo(LinguisticEntity other) {
		return this.getText().compareTo(other.getText());
	}
}