package edu.sabanciuniv.dataMining.util;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import edu.stanford.nlp.ling.HasTag;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.trees.TypedDependency;

import edu.sabanciuniv.dataMining.data.Identifiable;
import edu.sabanciuniv.dataMining.util.text.nlp.english.DictionaryEN;

public final class MPredicates {
	private MPredicates() {
		// Do nothing.
	}
	
	static class TagMatchesPredicate implements Predicate<HasTag> {
		private String pattern;
		
		public TagMatchesPredicate(String pattern) {
			this.pattern = pattern;
		}
		
		@Override
		public boolean apply(HasTag t) {
			return t.tag().matches(this.pattern);
		}
	}
	
	static class WordEqualsPredicate implements Predicate<HasWord> {
		private String word;
		
		public WordEqualsPredicate(String word) {
			this.word = word;
		}
		
		@Override
		public boolean apply(HasWord w) {
			return this.word.equals(w.word());
		}
	}
	
	static class WordMatchesPredicate implements Predicate<HasWord> {
		private String pattern;
		
		public WordMatchesPredicate(String pattern) {
			this.pattern = pattern;
		}
		
		@Override
		public boolean apply(HasWord w) {
			return w.word().matches(this.pattern);
		}
	}
	
	static class WordExistsInDictionaryPredicate<T extends HasWord & HasTag> implements Predicate<T> {
		@Override
		public boolean apply(T w) {
//			return DictionaryEN.wordExists(w);
			return true;
		}
	}
	
	static class WordLengthBetweenPredicate implements Predicate<HasWord> {
		private int lowThreshold;
		private int highThreshold;
		
		public WordLengthBetweenPredicate() {
			this(0);
		}
		
		public WordLengthBetweenPredicate(int lowThreshold) {
			this(lowThreshold, -1);
		}
		
		public WordLengthBetweenPredicate(int lowThreshold, int highThreshold) {
			this.lowThreshold = lowThreshold;
			this.highThreshold = highThreshold;
		}
		
		@Override
		public boolean apply(HasWord w) {
			if (w.word().length() < this.lowThreshold) {
				return false;
			} else if (this.highThreshold != -1 && w.word().length() > this.highThreshold) {
				return false;
			}
			
			return true;
		}
	}
	
	static class RelationshipEqualsPredicate implements Predicate<TypedDependency> {
		Iterable<String> relationships;
		
		public RelationshipEqualsPredicate(String relationship) {
			this(ImmutableList.of(relationship));
		}
		
		public RelationshipEqualsPredicate(Iterable<String> relationships) {
			this.relationships = relationships;
		}
		
		@Override
		public boolean apply(TypedDependency td) {
			return Iterables.contains(this.relationships, td.reln().getShortName());
		}
		
	}
	
	static class IdentifierEqualsPredicate implements Predicate<Identifiable> {
		Identifiable id;
		
		public IdentifierEqualsPredicate(Identifiable id) {
			this.id = id;
		}
		
		@Override
		public boolean apply(Identifiable arg0) {
			return this.id.getIdentifier().equals(arg0.getIdentifier());
		}
	}
	
	public static Predicate<HasTag> tagMatches(String pattern) {		
		return new MPredicates.TagMatchesPredicate(pattern);
	}
	
	public static Predicate<HasWord> wordEquals(String word) {		
		return new MPredicates.WordEqualsPredicate(word);
	}
	
	public static Predicate<HasWord> wordMatches(String pattern) {		
		return new MPredicates.WordMatchesPredicate(pattern);
	}
	
	public static <T extends HasWord & HasTag> Predicate<T> wordExistsInDictionary() {
		return new MPredicates.WordExistsInDictionaryPredicate<T>();
	}
	
	public static Predicate<HasWord> wordLengthBetween(int lowThreshold, int highThreshold) {
		return new MPredicates.WordLengthBetweenPredicate(lowThreshold, highThreshold);
	}
	
	public static Predicate<HasWord> wordLengthBetween(int lowThreshold) {
		return new MPredicates.WordLengthBetweenPredicate(lowThreshold);
	}
	
	public static Predicate<HasWord> wordLengthBetween() {
		return new MPredicates.WordLengthBetweenPredicate();
	}

	public static Predicate<TypedDependency> relationshipEquals(String relationship) {
		return new MPredicates.RelationshipEqualsPredicate(relationship);
	}
	
	public static Predicate<TypedDependency> relationshipEquals(Iterable<String> relationships) {
		return new MPredicates.RelationshipEqualsPredicate(relationships);
	}
	
	public static Predicate<Identifiable> identifierEquals(Identifiable id) {
		return new MPredicates.IdentifierEqualsPredicate(id);
	}
}