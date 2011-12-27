package edu.sabanciuniv.dataMining.util.text.nlp.english;

import com.google.common.base.Predicate;

import edu.sabanciuniv.dataMining.util.MPredicates;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.HasTag;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.WordTag;

/**
 * Class for a linguistic token.
 * @author Mus'ab Husaini
 */
public class LinguisticToken extends LinguisticEntity implements HasWord, HasTag {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6286142074867582530L;

	/**
	 * Gets a predicate for testing the validity of an English word.
	 * @return The predicate.
	 */
	public static Predicate<HasWord> isValidWordPredicate() {
		return MPredicates.wordMatches("^[a-zA-Z]+(?:s\\'|\\'s)?$");
	}

	/**
	 * Gets a predicate for testing for proper nouns.
	 * @return The predicate.
	 */
	public static Predicate<HasTag> isProperNounPredicate() {
		return MPredicates.tagMatches("^NNP(?:S?)$");
	}
		
	/**
	 * Gets a predicate for testing for improper nouns.
	 * @return The predicate.
	 */
	public static Predicate<HasTag> isImproperNounPredicate() {
		return MPredicates.tagMatches("^NN(?:S?)$");
	}
		
	/**
	 * Gets a predicate for testing for adjectives.
	 * @return The predicate.
	 */
	public static Predicate<HasTag> isAdjectivePredicate() {
		return MPredicates.tagMatches("^JJ(?:S?)$");
	}

	protected CoreLabel token;
	private boolean isLemmatized;
	
	/**
	 * Creates an instance of {@link LinguisticToken}.
	 * @param token The {@link CoreLabel} representation of the token.
	 */
	public LinguisticToken(CoreLabel token) {
		this(token, false);
	}

	/**
	 * Creates an instance of {@link LinguisticToken}.
	 * @param token The {@link CoreLabel} representation of the token.
	 * @param isLemmatized A flag indicating whether this token will be represented by its lemma.
	 */
	public LinguisticToken(CoreLabel token, boolean isLemmatized) {
		super(token.word());
		this.token = token;
		this.isLemmatized = isLemmatized;
	}
	
	/**
	 * Gets the {@link WordTag} representation of this token.
	 * @return The {@link WordTag} representation of this token.
	 */
	public WordTag getWordTag() {
		return new WordTag(this.word(), this.tag());
	}
	
	/**
	 * Gets the lemma of this token.
	 * @return The lemma.
	 */
	public String getLemma() {
		return this.token.lemma();
	}

	/**
	 * Gets a flag indicating whether this token is represented by its lemma.
	 * @return The flag.
	 */
	public boolean isLemmatized() {
		return this.isLemmatized;
	}
	
	/**
	 * Sets a flag indicating whether this token is represented by its lemma.
	 * @param isLemmatized A flag.
	 */
	public void setIsLemmatized(boolean isLemmatized) {
		this.isLemmatized = isLemmatized;
	}

	/**
	 * Tests to see if this token is a valid word.
	 * @return A flag indicating the result.
	 */
	public boolean isValidWord() {
		return LinguisticToken.isValidWordPredicate().apply(this);
	}

	/**
	 * Tests to see if this token is a proper noun. 
	 * @return A flag indicating the result.
	 */
	public boolean isProperNoun() {
		return LinguisticToken.isProperNounPredicate().apply(this);
	}
	
	/**
	 * Tests to see if this token is an improper noun.
	 * @return A flag indicating the result.
	 */
	public boolean isImproperNoun() {
		return LinguisticToken.isImproperNounPredicate().apply(this);
	}
	
	/**
	 * Tests to see if this token is an adjective.
	 * @return A flag indicating the result.
	 */
	public boolean isAdjective() {
		return LinguisticToken.isAdjectivePredicate().apply(this);
	}

	/**
	 * Tests to see if this token exists in dictionary.
	 * @return A flag indicating the result.
	 */
	public boolean existsInDictionary() {
		return MPredicates.wordExistsInDictionary().apply(this.token);
	}
	
	/**
	 * Tests to see if this token satisfies the given predicate.
	 * @param predicate The predicate to test.
	 * @return A flag indicating the result.
	 */
	public boolean testWord(Predicate<HasWord> predicate) {
		return predicate.apply(this);
	}
	
	@Override
	public String tag() {
		if (this.token.containsKey(PartOfSpeechAnnotation.class)) {
			return this.token.tag();
		}
		
		return null;
	}

	@Override
	public void setTag(String tag) {
		// Can't set tag.
	}

	@Override
	public String word() {
		if (this.isLemmatized) {
			return this.getLemma();
		}
		
		return this.getText();
	}

	@Override
	public void setWord(String word) {
		// Can't set.
	}
	
	@Override
	public String toString() {
		return this.getWordTag().toString();
	}
	
	@Override
	public int hashCode() {
		return this.word().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof LinguisticToken) {
			LinguisticToken other = (LinguisticToken)o;
			return this.compareTo(other) == 0;
		}
		
		return super.equals(o);
	}
	
	@Override
	public int compareTo(LinguisticEntity other) {
		if (other instanceof LinguisticToken) {
			LinguisticToken otherToken = (LinguisticToken)other;
			if (this.isLemmatized || otherToken.isLemmatized) {
				return this.word().compareTo(otherToken.word());
			}
			return this.getWordTag().compareTo(otherToken.getWordTag());
		}
		
		return super.compareTo(other);
	}
}