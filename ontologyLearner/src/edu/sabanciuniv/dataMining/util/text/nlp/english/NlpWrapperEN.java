package edu.sabanciuniv.dataMining.util.text.nlp.english;

import java.util.List;
import java.util.Properties;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * Wraps some basic NLP functionality for the English language.
 * @author Mus'ab Husaini
 */
public class NlpWrapperEN {
	private static List<String> basicAnnotators = ImmutableList.of(StanfordCoreNLP.STANFORD_TOKENIZE, StanfordCoreNLP.STANFORD_SSPLIT);
	private static NlpWrapperEN basic;
	private static NlpWrapperEN tagger;
	private static NlpWrapperEN parser;

	/**
	 * Gets an instance of {@link NlpWrapperEN} that can detect sentences and tokenize.
	 * @return A basic {@link NlpWrapperEN} instance.
	 */
	public static NlpWrapperEN getBasic() {
		return NlpWrapperEN.getBasic(false);
	}
	
	/**
	 * Gets an instance of {@link NlpWrapperEN} that can detect sentences and tokenize.
	 * @param reinitialize A flag indicating whether to re-initialize the instance or use the old one.
	 * @return A basic {@link NlpWrapperEN} instance.
	 */
	public static NlpWrapperEN getBasic(boolean reinitialize) {
		if (reinitialize || NlpWrapperEN.basic == null || NlpWrapperEN.basic.pipeline == null) {
			NlpWrapperEN.basic = new NlpWrapperEN();
		}
		
		return NlpWrapperEN.basic;
	}

	/**
	 * Gets an instance of {@link NlpWrapperEN} that can find POS tags.
	 * @return A tagger {@link NlpWrapperEN} instance.
	 */
	public static NlpWrapperEN getTagger() {
		return NlpWrapperEN.getTagger(false);
	}

	/**
	 * Gets an instance of {@link NlpWrapperEN} that can find POS tags. 
	 * @param reinitialize A flag indicating whether to re-initialize the instance or use the old one.
	 * @return A tagger {@link NlpWrapperEN} instance.
	 */
	public static NlpWrapperEN getTagger(boolean reinitialize) {
		if (reinitialize || NlpWrapperEN.tagger == null || NlpWrapperEN.tagger.pipeline == null) {
			NlpWrapperEN.tagger = new NlpWrapperEN();
			NlpWrapperEN.tagger.makeTagger();
		}
		return NlpWrapperEN.tagger;
	}

	/**
	 * Gets an instance of {@link NlpWrapperEN} that can parse texts.
	 * @return A parser {@link NlpWrapperEN} instance.
	 */
	public static NlpWrapperEN getParser() {
		return NlpWrapperEN.getParser(false);
	}
	
	/**
	 * Gets an instance of {@link NlpWrapperEN} that can parse texts.
	 * @param reinitialize A flag indicating whether to re-initialize the instance or use the old one.
	 * @return A parser {@link NlpWrapperEN} instance.
	 */
	public static NlpWrapperEN getParser(boolean reinitialize) {
		if (reinitialize || NlpWrapperEN.parser == null || NlpWrapperEN.parser.pipeline == null) {
			NlpWrapperEN.parser = new NlpWrapperEN();
			NlpWrapperEN.parser.makeParser();
		}
		
		return NlpWrapperEN.parser;
	}
	
	private List<String> currentAnnotators;
	private StanfordCoreNLP pipeline;
	
	/**
	 * Creates an instance of {@link NlpWrapperEN}.
	 */
	protected NlpWrapperEN() {
		this(NlpWrapperEN.basicAnnotators);
	}

	private NlpWrapperEN(Iterable<String> annotators) {
		this.preparePipeline(annotators);
	}
	
	private void preparePipeline(Iterable<String> annotators) {
		this.currentAnnotators = Lists.newArrayList(annotators);
	}
	
	private StanfordCoreNLP createPipeline() {
		Properties props = new Properties();
	    props.put("annotators", Joiner.on(", ").join(this.currentAnnotators));
	    return this.pipeline = new StanfordCoreNLP(props);
	}
	
	private void addAnnotators(String annotators) {
		String[] annotatorsArr = annotators.split(",");
		for(String annotator : annotatorsArr) {
			if (!this.currentAnnotators.contains(annotator)) {
				this.getPipeline().addAnnotator(StanfordCoreNLP.getExistingAnnotator(annotator.trim()));
			    this.currentAnnotators.add(annotator);
			}
		}
	}
	
	private StanfordCoreNLP getPipeline() {
		if (this.pipeline == null) {
			this.createPipeline();
		}
		
		return this.pipeline;
	}

	private Annotation annotate(String text) {
		Annotation document = new Annotation(text);
		this.getPipeline().annotate(document);
		return document;
	}
	
	private void makeTagger() {
		this.addAnnotators(StanfordCoreNLP.STANFORD_POS);
		this.addAnnotators(StanfordCoreNLP.STANFORD_LEMMA);
	}
	
	private void makeParser() {
		this.addAnnotators(StanfordCoreNLP.STANFORD_PARSE);
		this.addAnnotators(StanfordCoreNLP.STANFORD_LEMMA);
	}

	/**
	 * Gets all the sentences in a text.
	 * @param text The text to detect sentences in.
	 * @return A list of sentences.
	 */
	public Iterable<CoreMap> getSentences(String text) {
		Annotation document = this.annotate(text);
		return document.get(SentencesAnnotation.class);
	}
	
	/**
	 * Gets all the tokens in a sentence.
	 * @param sentence The sentence to tokenize.
	 * @return The tokens in this sentence.
	 */
	public Iterable<CoreLabel> getTokens(String sentence) {
		Annotation document = this.annotate(sentence);
		return document.get(TokensAnnotation.class);
	}
	
	/**
	 * Close this instance of release resources.
	 */
	public void close() {
		this.pipeline = null;
	}
}