/**
 * 
 */
package edu.sabanciuniv.dataMining.data.options.text;

import edu.sabanciuniv.dataMining.data.options.Options;

/**
 * Class containing all options needed to create a {@link TextDocument} object.
 * @author Mus'ab Husaini
 */
public class TextDocumentOptions extends Options {
	
	/**
	 * Types of features.
	 * @author Mus'ab Husaini
	 */
	public static enum FeatureType {
		NONE,
		NOUNS,
		NOUNS_ADJECTIVES,
		ADJECTIVES,
		SMART_NOUNS
	}
	
	private FeatureType featureType;

	/**
	 * Creates a new instance of {@link TextDocumentOptions}.
	 */
	public TextDocumentOptions() {
		this.featureType = FeatureType.NOUNS;
	}
	
	/**
	 * Gets the type of features.
	 * @return Type of features.
	 */
	public FeatureType getFeatureType() {
		return this.featureType;
	}
	
	/**
	 * Sets the type of features.
	 * @param featureType The type of features.
	 * @return The type of features that was set.
	 */
	public FeatureType setFeatureType(FeatureType featureType) {
		return (this.featureType = featureType);
	}
}