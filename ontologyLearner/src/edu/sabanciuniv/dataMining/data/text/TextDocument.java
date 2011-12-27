package edu.sabanciuniv.dataMining.data.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.sabanciuniv.dataMining.data.Summarizable;
import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions;
import edu.sabanciuniv.dataMining.data.options.HasOptions;
import edu.sabanciuniv.dataMining.util.MPredicates;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticSentence;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticText;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticToken;
import edu.sabanciuniv.dataMining.util.text.nlp.english.LinguisticPhrase;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.trees.TypedDependency;

/**
 * A text document that can be mined for information.
 * @author Mus'ab Husaini
 */
public class TextDocument extends IdentifiableWithFeatures<LinguisticToken> implements Summarizable<TextDocument>, HasOptions<TextDocumentOptions> {
	private static List<String> depRels = ImmutableList.of("nsubj", "nsubjpass");
	private static List<String> govRels = ImmutableList.of("amod");
	private static String compoundNounRel = "nn";
	private static Predicate<HasWord> isAdmissableWordPredicate =
			Predicates.and(ImmutableList.of(MPredicates.wordLengthBetween(3, 20), LinguisticToken.isValidWordPredicate()));

	private final int maxSentenceSize = 50;
	
	private TextDocumentSummary summary; 
	private LinguisticText text;
	private Iterable<LinguisticToken> taggedText;
	private TextDocumentOptions options;
	
	private void tagText() {
		List<LinguisticToken> features = new ArrayList<LinguisticToken>();
		
		if (this.options.getFeatureType() != TextDocumentOptions.FeatureType.NONE) {
			// Extract features from each sentence.
			for (LinguisticSentence sentence : this.text.getSentences()) {
				if (Iterables.size(sentence.getTokens()) > this.maxSentenceSize) {
					continue;
				}
				
				LinguisticSentence taggableSentence = sentence.getTaggableSentence();
				Iterable<LinguisticToken> sentenceFeatures;
				switch(this.options.getFeatureType()) {
					case NOUNS:
						sentenceFeatures = this.extractNouns(taggableSentence);
						break;
					case ADJECTIVES:
						sentenceFeatures = this.extractAdjectives(taggableSentence);
						break;
					case NOUNS_ADJECTIVES:
						sentenceFeatures = this.extractAdjectives(taggableSentence);
						sentenceFeatures = Iterables.concat(sentenceFeatures, this.extractNouns(taggableSentence));
						break;
					case SMART_NOUNS:
						sentenceFeatures = this.extractSmartSentenceFeatures(sentence);
						if (sentenceFeatures == null) {
							sentenceFeatures = this.extractNouns(taggableSentence);
						}
						break;
					case NONE:
						sentenceFeatures = new ArrayList<LinguisticToken>();
						break;
					default:
						sentenceFeatures = taggableSentence.getTokens();
				}
				
				Iterables.addAll(features, sentenceFeatures);
			}
			
			// Make features unique.
			features = Lists.newArrayList(Sets.newHashSet(features));
		}
		
		this.summary = new TextDocumentSummary(this.getIdentifier(), features);
	}

	private Iterable<LinguisticToken> extractAdjectives(LinguisticSentence sentence) {
		sentence.setAreTokensLemmatized(true);
		Iterable<LinguisticToken> sentenceAdjectives = Iterables.filter(sentence.getTokens(), TextDocument.isAdmissableWordPredicate);
		sentenceAdjectives = Iterables.filter(sentenceAdjectives, LinguisticToken.isAdjectivePredicate());
		return sentenceAdjectives;
	}

	private Iterable<LinguisticToken> extractNouns(LinguisticSentence sentence) {
		sentence.setAreTokensLemmatized(true);
		Iterable<LinguisticToken> sentenceNouns = Iterables.filter(sentence.getTokens(), TextDocument.isAdmissableWordPredicate);
		sentenceNouns = Iterables.filter(sentenceNouns, LinguisticToken.isImproperNounPredicate());
		return sentenceNouns;
	}
	
	private Iterable<LinguisticToken> extractSmartSentenceFeatures(LinguisticSentence sentence) {
		List<LinguisticToken> features = new ArrayList<LinguisticToken>();
		try {
			if (Iterables.size(sentence.getTokens()) > this.maxSentenceSize) {
				return null;
			}
			sentence = sentence.getParsableSentence();
		} catch(OutOfMemoryError e) {
			return null;
		}
		
		// Attempt to find nouns that can be features.
		Iterable<TypedDependency> dependencies;
		dependencies = sentence.getTypedDependencies();
		
		// Filter down dependencies for now. 
		Iterable<TypedDependency> relevantDependencies =
				Iterables.filter(dependencies, MPredicates.relationshipEquals(Iterables.concat(TextDocument.depRels, TextDocument.govRels)));
		
		// Go thru relevant dependencies.
		for (TypedDependency dependency : relevantDependencies) {
			LinguisticToken token = null;
			
			// Find the relevant operand based on the kind of relationship.
			if (TextDocument.depRels.contains(dependency.reln().getShortName())) {
				// Only take this token if the gov is an adjective.
				LinguisticToken gov = new LinguisticToken(dependency.gov().label());
				if (gov.isAdjective()) {
					token = new LinguisticToken(dependency.dep().label());
				}
			} else if (TextDocument.govRels.contains(dependency.reln().getShortName())) {
				token = new LinguisticToken(dependency.gov().label());
			}
			
			// Add if it's a noun.
			if (token != null && token.isImproperNoun()) {
				// Need to lemmatize token before we start looking for it.
				token.setIsLemmatized(true);
				if (Iterables.find(features, MPredicates.wordEquals(token.word()), null) == null &&
						token.testWord(TextDocument.isAdmissableWordPredicate) && token.existsInDictionary()) {
					features.add(token);
				}
			}
		}
		
		// Go thru the features list again and get all compound nouns.
		relevantDependencies = Iterables.filter(dependencies, MPredicates.relationshipEquals(TextDocument.compoundNounRel));
		List<LinguisticPhrase> featurePhrases = new ArrayList<LinguisticPhrase>();
		for (TypedDependency dependency : relevantDependencies) {
			LinguisticToken depToken = new LinguisticToken(dependency.dep().label(), true);
			LinguisticToken govToken = new LinguisticToken(dependency.gov().label(), true);
			// One of the parts of the relationship should be a feature, otherwise, we don't care.
			if (!features.contains(depToken) && !features.contains(govToken)) {
				continue;
			}
			
			// Search for a phrase that already has this governor.
			LinguisticPhrase found = null;
			CoreLabel otherLabel = null;
			for (LinguisticPhrase phrase : featurePhrases) {
				if (phrase.contains(dependency.gov().label())) {
					found = phrase;
					otherLabel = dependency.dep().label();
					break;					
				}
			}
			
			// If found, add the dep; otherwise, start a new phrase.
			if (found != null) {
				LinguisticToken token = new LinguisticToken(otherLabel);
				if (token.isImproperNoun() && token.testWord(TextDocument.isAdmissableWordPredicate) && token.existsInDictionary()) {
					found.add(otherLabel);
				} else {
					// If the token doesn't fit the bill, remove the phrase altogether.
					// TODO: evaluate this.
					featurePhrases.remove(found);
				}
			} else {
				found = new LinguisticPhrase(ImmutableList.of((CoreLabel)dependency.dep().label()), true);
				found.add(dependency.gov().label());
				if (found.isImproperNoun() && found.testAllWords(TextDocument.isAdmissableWordPredicate) && found.existsInDictionary()) {
					featurePhrases.add(found);
				}
			}
		}
		
		// Add all phrases to the set.
		for (LinguisticPhrase phrase : featurePhrases) {
			features.add(phrase);
		}
		
		return features;
	}
	
	/**
	 * Creates an empty instance of {@link TextDocument}.
	 */
	public TextDocument() {
		this(null);
	}

	/**
	 * Creates an instance of {@link TextDocument}.
	 * @param options Options for this instance.
	 */
	public TextDocument(TextDocumentOptions options) {
		if (options == null) {
			options = new TextDocumentOptions();
		}
		
		this.summary = new TextDocumentSummary();
		this.text = null;
		this.taggedText = null;
		this.options = options;
	}
	
	@Override
	public UUID getIdentifier() {
		if (this.summary != null) {
			return this.summary.getIdentifier();
		}
		return super.getIdentifier();
	}
	
	@Override
	public void setIdentifier(UUID uuid) {
		if (this.summary != null) {
			this.summary.setIdentifier(uuid);
		} else {
			super.setIdentifier(uuid);
		}
	}
	
	/**
	 * Gets document full-text.
	 * @return Full-text of the document.
	 */
	public String getText() {
		return this.text.getText();
	}
	
	/**
	 * Sets document text.
	 * @param text Text to set.
	 */
	public void setText(String text) {
		this.text = new LinguisticText(text);
		this.tagText();
	}

	/**
	 * Gets POS-tagged version of this document.
	 * @return POS-tagged version of the document.
	 */
	public Iterable<LinguisticToken> getTaggedText() {
		return this.taggedText;
	}

	/**
	 * Gets the options for this instance.
	 * @return Options for this instance.
	 */
	public TextDocumentOptions getOptions() {
		return this.options;
	}
	
	@Override
	public TextDocumentSummary getSummary() {
		return this.summary;
	}

	@Override
	public String toString() {
		return this.getText();
	}
	
	@Override
	public Set<LinguisticToken> getFeatures() {
		if (this.summary != null) {
			return this.summary.getFeatures();
		}
		return super.getFeatures();
	}

	@Override
	public TextDocument cloneOut(Iterable<LinguisticToken> otherFeatures) {
		TextDocument newDocument = new TextDocument();
		newDocument.summary = this.summary.cloneOut(otherFeatures);
		newDocument.text = this.text;
		newDocument.taggedText = this.taggedText;
		return newDocument;
	}

	@Override
	public TextDocument getElaborate() {
		// TODO Auto-generated method stub
		return null;
	}
}