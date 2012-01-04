package edu.sabanciuniv.dataMining.util.text.nlp.english;

import java.security.InvalidParameterException;

/**
 * Abstract class for any linguistic entity.
 * @author Mus'ab Husaini
 */
public abstract class LinguisticEntity implements Comparable<LinguisticEntity> {
	protected String text;
	protected int offset;

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
	
	/**
	 * Gets the offset of this text in the document.
	 * @return The offset.
	 */
	public int getOffset() {
		return this.offset;
	}
	
	/**
	 * Sets the offset of this text in the document.
	 * @param offset The offset.
	 */
	public void setOffset(int offset) {
		if (offset < 0) {
			throw new IllegalArgumentException("Offset cannot be negative.");
		}
		
		this.offset = offset;
	}
	
	/**
	 * Gets the absolute begin position of this text in the document.
	 * @return The absolute begin position of the text.
	 */
	public int getAbsoluteBeginPosition() {
		return this.offset + this.getRelativeBeginPosition();
	}
	
	/**
	 * Gets the absolute end position of this text in the document.
	 * @return The absolute end position of the text.
	 */
	public int getAbsoluteEndPosition() {
		return this.offset + this.getRelativeEndPosition();
	}

	/**
	 * Gets the relative begin position of this text in the document.
	 * @return The relative begin position of the text.
	 */
	public abstract int getRelativeBeginPosition();
	
	/**
	 * Gets the relative end position of this text in the document.
	 * @return The relative end position of the text.
	 */
	public abstract int getRelativeEndPosition();
	
	@Override
	public String toString() {
		return this.getText();
	}
	
	@Override
	public int compareTo(LinguisticEntity other) {
		return this.getText().compareTo(other.getText());
	}
}