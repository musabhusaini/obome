package edu.sabanciuniv.dataMining.util.text.nlp.english;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.util.CoreMap;

/**
 * Class for any arbitrary linguistic text.
 * @author Mus'ab Husaini
 */
public class LinguisticText extends LinguisticEntity {
	private Iterable<CoreMap> sentences;

	/**
	 * Creates an instance of {@link LinguisticText} with the specified text.
	 * @param text The text value of this instance.
	 */
	public LinguisticText(String text) {
		super(text);
		this.sentences = NlpWrapperEN.getBasic().getSentences(text);
	}

	/**
	 * Gets all the sentences in this text.
	 * @return The sentences in this text.
	 */
	public Iterable<LinguisticSentence> getSentences() {
		return Iterables.transform(this.sentences, new Function<CoreMap, LinguisticSentence>() {
			@Override
			public LinguisticSentence apply(CoreMap sentence) {
				return new LinguisticSentence(sentence);
			}
		});
	}
	
	/**
	 * Gets all the tokens in this text.
	 * @return The tokens in this text.
	 */
	public Iterable<LinguisticToken> getTokens() {
		List<LinguisticToken> tokens = new ArrayList<LinguisticToken>();
		for (LinguisticSentence sentence : this.getSentences()) {
			Iterables.addAll(tokens, sentence.getTokens());
		}
		return tokens;
	}
	
	@Override
	public int getAbsoluteBeginPosition() {
		return Iterables.getFirst(this.sentences, null).get(CharacterOffsetBeginAnnotation.class);
	}
	
	@Override
	public int getAbsoluteEndPosition() {
		return Iterables.getLast(this.sentences, null).get(CharacterOffsetEndAnnotation.class);
	}
}