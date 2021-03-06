/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.core.util.collection;

import java.util.Comparator;
import java.util.Objects;

import com.google.common.base.Function;


/**
 * A pair of values, inspired by STL
 * 
 * @author Alexander Lex
 * @param <T>
 *            first type
 * @param <E>
 *            second type
 */
public class Pair<T, E> {

	/** The first element of the pair */
	private T first;
	/** The second element of the pair */
	private E second;

	public Pair() {

	}

	/**
	 * Constructor
	 *
	 * @param first
	 *            the first value
	 * @param second
	 *            the second value
	 */
	public Pair(T first, E second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * @return the first, see {@link #first}
	 */
	public T getFirst() {
		return first;
	}

	/**
	 * @return the second, see {@link #second}
	 */
	public E getSecond() {
		return second;
	}

	/**
	 * @param first
	 *            setter, see {@link #first}
	 */
	public void setFirst(T first) {
		this.first = first;
	}

	/**
	 * @param second
	 *            setter, see {@link #second}
	 */
	public void setSecond(E second) {
		this.second = second;
	}

	@Override
	public String toString() {
		return "<" + first + ", " + second + ">";
	}

	/**
	 * factory method for a pair
	 *
	 * @param first
	 * @param second
	 * @return
	 */
	public static <T, E> Pair<T, E> make(T first, E second) {
		return new Pair<T, E>(first, second);
	}

	/**
	 * factory method for a pair
	 *
	 * @param first
	 * @param second
	 * @return
	 */
	public static <T extends Comparable<T>, E> ComparablePair<T, E> make(T first, E second) {
		return new ComparablePair<T, E>(first, second);
	}


	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		Pair<T, E> other = (Pair<T, E>) obj;
		return Objects.equals(this.first, other.first) && Objects.equals(this.second, other.second);
	}

	/**
	 * returns a {@link Function}, which maps the pair to the first element
	 *
	 * @return
	 */
	public static final <T1, T2> Function<Pair<T1, T2>, T1> mapFirst() {
		return new Function<Pair<T1,T2>,T1>() {
			@Override
			public T1 apply(Pair<T1, T2> arg0) {
				return arg0 == null ? null : arg0.getFirst();
			}
		};
	}

	/**
	 * returns a {@link Function}, which maps the pair to the second element
	 *
	 * @return
	 */
	public static final <T1, T2> Function<Pair<T1, T2>, T2> mapSecond() {
		return new Function<Pair<T1, T2>, T2>() {
			@Override
			public T2 apply(Pair<T1, T2> arg0) {
				return arg0 == null ? null : arg0.getSecond();
			}
		};
	}

	/**
	 * returns a comparator, which compares the first element
	 *
	 * @return
	 */
	public static <T extends Comparable<T>> Comparator<Pair<T, ?>> compareFirst() {
		return new Comparator<Pair<T, ?>>() {
			@Override
			public int compare(Pair<T, ?> o1, Pair<T, ?> o2) {
				return o1.first.compareTo(o2.first);
			}
		};
	}

	/**
	 * returns a comparator, which compares the second element
	 *
	 * @return
	 */
	public static <T extends Comparable<T>> Comparator<Pair<?, T>> compareSecond() {
		return new Comparator<Pair<?, T>>() {
			@Override
			public int compare(Pair<?, T> o1, Pair<?, T> o2) {
				return o1.second.compareTo(o2.second);
			}
		};
	}

	public static class ComparablePair<T extends Comparable<T>, E> extends Pair<T, E> implements
			Comparable<ComparablePair<T, E>> {
		public ComparablePair(T first, E second) {
			super(first, second);
		}

		@Override
		public int compareTo(ComparablePair<T, E> o) {
			return this.getFirst().compareTo(o.getFirst());
		}

	}

}
