package edu.sabanciuniv.dataMining.util.text.nlp.english;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.stanford.nlp.ling.CoreAnnotations.IndexAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;

import edu.sabanciuniv.dataMining.util.MPredicates;

/**
 * Class for a linguistic phrase.
 * @author Mus'ab Husaini
 */
public class LinguisticPhrase extends LinguisticToken {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8011721327357310681L;

	static class TestAllWordsPredicate implements Predicate<LinguisticPhrase> {
		Predicate<HasWord> predicate;
		
		public TestAllWordsPredicate(Predicate<HasWord> predicate) {
			if (predicate == null) {
				throw new InvalidParameterException("Must provide a predicate");
			}
			
			this.predicate = predicate;
		}
		@Override
		public boolean apply(LinguisticPhrase input) {
			return Iterables.all(input.getAllTokens(), this.predicate);
		}
	}
	
	/**
	 * Gets a predicate for testing all words in the phrase using the provided predicate.
	 * @param predicate The predicate to apply.
	 * @return The predicate.
	 */
	public static Predicate<LinguisticPhrase> testAllWordsPredicate(Predicate<HasWord> predicate) {
		return new TestAllWordsPredicate(predicate);
	}

	/**
	 * Gets a predicate for testing the validity of an English phrase.
	 * @return The predicate.
	 */
	public static Predicate<LinguisticPhrase> isValidPhrasePredicate() {
		return LinguisticPhrase.testAllWordsPredicate(LinguisticToken.isValidWordPredicate());
	}
		
	private List<CoreLabel> tokens;

	private String getAuxString() {
		if (this.tokens != null) {
			String aux = "";
			for (CoreLabel token : this.tokens) {
				aux += token.word() + " ";
			}
			return aux.trim();
		}
		return "";
	}
	
	private Iterable<CoreLabel> getAllTokens() {
		Iterable<CoreLabel> last = Lists.newArrayList(this.token);
		if (this.tokens == null) {
			return last;
		}
		
		return Iterables.concat(this.tokens, last);
	}

	/**
	 * Creates an instance of {@link LinguisticPhrase}.
	 * @param tokens A list of {@link CoreLabel} tokens representing this phrase. 
	 */
	public LinguisticPhrase(Iterable<CoreLabel> tokens) {
		this(tokens, false);
	}

	/**
	 * Creates an instance of {@link LinguisticPhrase}.
	 * @param tokens A list of {@link CoreLabel} tokens representing this phrase.
	 * @param isLemmatized A flag indicating whether this phrase will be represented by its lemma.
	 */
	public LinguisticPhrase(Iterable<CoreLabel> tokens, boolean isLemmatized) {
		super(Iterables.getLast(tokens, null), isLemmatized);
		
		int size = Iterables.size(tokens);
		if (size > 1) {
			this.tokens = Lists.newArrayList(Iterables.limit(tokens, size-1));
		} else {
			this.tokens = null;
		}
	}
	
	/**
	 * Gets a flag indicating whether this phrase contains the given token or not.
	 * @param token The token to find.
	 * @return The flag.
	 */
	public boolean contains(CoreLabel token) {
		int tokenIndex = token.get(IndexAnnotation.class);
		String tokenText = token.word();
		
		if (tokenIndex == this.token.get(IndexAnnotation.class) && tokenText.equals(this.token.value())) {
			return true;
		}
		
		for (CoreLabel otherToken : this.tokens) {
			if (tokenIndex == otherToken.get(IndexAnnotation.class) && tokenText.equals(otherToken.value())) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Adds the given token to this phrase in the appropriate position based on the index.
	 * @param token The token to be added.
	 * @return A value indicating whether the operation was successful or not.
	 */
	public boolean add(CoreLabel token) {
		int relativeIndex = 0;
		int tokenIndex = token.get(IndexAnnotation.class);
		
		// Find out where to add this token.
		if (this.tokens != null) {
			for (CoreLabel otherToken : this.tokens) {
				if (otherToken.get(IndexAnnotation.class) > tokenIndex) {
					break;
				}
				relativeIndex++;
			}
			if (relativeIndex < this.tokens.size()) {
				this.tokens.add(relativeIndex, token);
				return true;
			}
		}
		
		int otherIndex = this.token.get(IndexAnnotation.class);
		if (tokenIndex == otherIndex) {
			return false;
		}
		
		if (this.tokens == null) {
			this.tokens = new ArrayList<CoreLabel>();
		}
		
		if (tokenIndex < otherIndex) {
			this.tokens.add(token);
		} else {
			this.tokens.add(this.token);
			this.token = token;
		}
		
		return true;
	}

	/**
	 * Gets a flag indicating whether this phrase is a valid phrase or not.
	 * @return A flag indicating the result. 
	 */
	public boolean isValidPhrase() {
		return LinguisticPhrase.isValidPhrasePredicate().apply(this);
	}
	
	/**
	 * Gets a flag indicating whether words in this phrase all satisfy the predicate.
	 * @param predicate The predicate to apply.
	 * @return A flag indicating the result.
	 */
	public boolean testAllWords(Predicate<HasWord> predicate) {
		return LinguisticPhrase.testAllWordsPredicate(predicate).apply(this);
	}
	
	@Override
	public boolean existsInDictionary() {
		if (this.tokens != null && !Iterables.all(this.tokens, MPredicates.wordExistsInDictionary())) {
			return false;
		}
		
		return super.existsInDictionary();
	}
	
	@Override
	public String getLemma() {
		String auxString = this.getAuxString() + " ";
		if (!this.isProperNoun()) {
			auxString = auxString.toLowerCase();
		}
		return (auxString + super.getLemma()).trim();
	}
	
	@Override
	public String getText() {
		return (this.getAuxString() + " " + super.getText()).trim();
	}
}