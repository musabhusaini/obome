package edu.sabanciuniv.dataMining.util;

import java.util.Comparator;
import java.util.Map.Entry;

/**
 * A {@link Comparator} that compares values of a given key/value entry.
 * @author Mus'ab Husaini
 * @param <T> Type of key.
 * @param <K> Type of value.
 */
public class EntryValueComparator<T,K extends Comparable<K>> implements Comparator<Entry<T,K>> {
	@Override
	public int compare(Entry<T, K> arg0, Entry<T, K> arg1) {
		return arg0.getValue().compareTo(arg1.getValue());
	}
}