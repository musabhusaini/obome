package edu.sabanciuniv.dataMining.data.text;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import edu.sabanciuniv.dataMining.data.Summarizable;
import edu.sabanciuniv.dataMining.data.Summary;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticToken;

/**
 * A text document that contains only mined data and necessary information to retrieve the document from data source.
 * @author Mus'ab Husaini
 */
public class TextDocumentSummary extends IdentifiableWithFeatures<LinguisticToken> implements Summary<TextDocument> {
	/**
	 * Creates a new instance of {@link TextDocumentSummary}.
	 * @param uuid UUID of the original document.
	 * @param features Features that have been mined from the original document.
	 */
	public TextDocumentSummary(UUID uuid, Iterable<LinguisticToken> features) {
		super(uuid, features);
	}

	/**
	 * Creates a new instance of {@link TextDocumentSummary}.
	 * @param features Features that have been mined from the original document.
	 */
	public TextDocumentSummary(Iterable<LinguisticToken> features) {
		this(UUID.randomUUID(), features);
	}
	
	/**
	 * Creates an empty instance of {@link TextDocumentSummary}.
	 */
	public TextDocumentSummary() {
		this(new ArrayList<LinguisticToken>());
	}

	@Override
	public TextDocumentSummary cloneOut(Iterable<LinguisticToken> otherFeatures) {
		Set<LinguisticToken> newFeatures = Sets.intersection(Sets.newHashSet(otherFeatures), this.getFeatures());
		return new TextDocumentSummary(this.getIdentifier(), newFeatures);
	}

	@Override
	public void initialize(Summarizable<TextDocument> elaborate) {
		this.setIdentifier(elaborate.getElaborate().getIdentifier());
		this.setFeatures(elaborate.getElaborate().getFeatures());
	}
}