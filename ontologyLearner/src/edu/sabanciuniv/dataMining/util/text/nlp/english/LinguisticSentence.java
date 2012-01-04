package edu.sabanciuniv.dataMining.util.text.nlp.english;

import java.util.ArrayList;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * Class for a linguistic sentence.
 * @author Mus'ab Husaini
 */
public class LinguisticSentence extends LinguisticEntity {
	private CoreMap sentence;
	private boolean areTokensLemmatized;
	
	private LinguisticSentence(String sentence, boolean areTokensLemmatized) {
		super(sentence);
		this.areTokensLemmatized = areTokensLemmatized;
	}

	/**
	 * Creates an instance of {@link LinguisticSentence}.
	 * @param sentence The {@link CoreMap} representation of the sentence.
	 * @param areTokensLemmatized A flag indicating whether the tokens will be represented by their lemmas or not.
	 */
	public LinguisticSentence(CoreMap sentence, boolean areTokensLemmatized) {
		this(sentence.get(TextAnnotation.class), areTokensLemmatized);
		this.sentence = sentence;
	}
	
	/**
	 * Creates an instance of {@link LinguisticSentence}.
	 * @param sentence The {@link CoreMap} representation of the sentence.
	 */
	public LinguisticSentence(CoreMap sentence) {
		this(sentence, false);
	}
	
	/**
	 * Gets all the tokens in this sentence.
	 * @return The tokens in this sentence.
	 */
	public Iterable<LinguisticToken> getTokens() {
		class CreateTokenFunction implements Function<CoreLabel, LinguisticToken> {
			private LinguisticSentence sentence;
			
			public CreateTokenFunction(LinguisticSentence sentence) {
				this.sentence = sentence;
			}
			
			@Override
			public LinguisticToken apply(CoreLabel input) {
				LinguisticToken newToken = new LinguisticToken(input, areTokensLemmatized);
				newToken.setOffset(sentence.getOffset());
				return newToken;
			}
		}
		
		return Iterables.transform(this.sentence.get(TokensAnnotation.class), new CreateTokenFunction(this));
	}
	
	/**
	 * Get a flag indicating whether the tokens in this sentence are represented by their lemmas or not.
	 * @return A flag indicating whether the tokens in this sentence are represented by their lemmas or not.
	 */
	public boolean areTokensLemmatized() {
		return this.areTokensLemmatized;
	}
	
	/**
	 * Sets the flag indicating whether the tokens in this sentence are represented by their lemmas or not.
	 * @param areTokensLemmatized A flag indicating whether the tokens in this sentence are represented by their lemmas or not.
	 */
	public void setAreTokensLemmatized(boolean areTokensLemmatized) {
		this.areTokensLemmatized = areTokensLemmatized;
	}
	
	/**
	 * Gets a form of this sentence that can be tagged.
	 * @return A taggable sentence.
	 */
	public LinguisticSentence getTaggableSentence() {
		LinguisticToken firstToken = Iterables.getFirst(this.getTokens(), null);
		if (firstToken != null && firstToken.token.containsKey(PartOfSpeechAnnotation.class)){
			return this;
		}
		
		LinguisticSentence newSentence = new LinguisticSentence(Iterables.getFirst(NlpWrapperEN.getTagger().getSentences(this.getText()), null));
		newSentence.setOffset(this.getAbsoluteBeginPosition());
		return newSentence;
	}
	
	/**
	 * Gets a form of this sentence that can be parsed.
	 * @return A parsable sentence.
	 */
	public LinguisticSentence getParsableSentence() {
		LinguisticToken firstToken = Iterables.getFirst(this.getTokens(), null);
		if (firstToken != null && firstToken.token.containsKey(TreeAnnotation.class)){
			return this;
		}

		LinguisticSentence newSentence = new LinguisticSentence(Iterables.getFirst(NlpWrapperEN.getParser().getSentences(this.getText()), null));
		newSentence.setOffset(this.getAbsoluteBeginPosition());
		return newSentence;
	}
	
	/**
	 * Gets all the typed dependencies in this sentence.
	 * @return Typed dependencies in this sentence.
	 */
	public Iterable<TypedDependency> getTypedDependencies() {
		if (this.sentence.containsKey(CollapsedCCProcessedDependenciesAnnotation.class)) {
			return this.sentence.get(CollapsedCCProcessedDependenciesAnnotation.class).typedDependencies();
		}
		return new ArrayList<TypedDependency>();
	}
	
	@Override
	public int getRelativeBeginPosition() {
		return this.sentence.get(CharacterOffsetBeginAnnotation.class);
	}
	
	@Override
	public int getRelativeEndPosition() {
		return this.sentence.get(CharacterOffsetEndAnnotation.class);
	}
}