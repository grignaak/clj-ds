package com.github.krukow.clj_ds;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import com.github.krukow.clj_lang.PersistentArrayMap;
import com.github.krukow.clj_lang.PersistentConsList;
import com.github.krukow.clj_lang.PersistentHashMap;
import com.github.krukow.clj_lang.PersistentHashSet;

public final class Persistents {

	// Factory Methods

	/**
	 * @return An empty {@link PersistentVector}.
	 */
	public static <E> PersistentVector<E> vector() {
		return com.github.krukow.clj_lang.PersistentVector.emptyVector();
	}

	/**
	 * @return A singleton {@link PersistentVector} with the value val.
	 */
	public static <E> PersistentVector<E> vector(E val) {
		return Persistents.<E> vector().plus(val);
	}

	/**
	 * @return A {@link PersistentVector} consisting of the elements of the
	 *         array vals.
	 */
	public static <E> PersistentVector<E> vector(E... vals) {
		return com.github.krukow.clj_lang.PersistentVector.create(vals);
	}

	/**
	 * @return A {@link PersistentVector} consisting of the elements of the
	 *         {@link Iterable} vals
	 */
	public static <E> PersistentVector<E> vector(Iterable<? extends E> vals) {
		return com.github.krukow.clj_lang.PersistentVector.create(vals);
	}

	/**
	 * @return An empty {@link PersistentSet}; implemented as hash set.
	 */
	public static <E> PersistentSet<E> hashSet() {
		return PersistentHashSet.emptySet();
	}

	/**
	 * @return A singleton {@link PersistentSet} with the value val; implemented
	 *         as hash set.
	 */
	public static <E> PersistentSet<E> hashSet(E val) {
		return Persistents.<E> hashSet().plus(val);
	}

	/**
	 * @return A {@link PersistentSet} consisting of the elements of the array
	 *         vals; implemented as hash set.
	 */
	public static <E> PersistentSet<E> hashSet(E... vals) {
		return PersistentHashSet.create(vals);
	}

	/**
	 * @return A {@link PersistentSet} consisting of the elements of the
	 *         {@link Iterable} vals; implemented as hash set.
	 */
	public static <E> PersistentSet<E> hashSet(Iterable<? extends E> vals) {
		return PersistentHashSet.create(vals);
	}

	/**
	 * @return An empty {@link Dictionary}; implemented as hash map.
	 */
	public static <K, V> Dictionary<K, V> hashMap() {
		return PersistentHashMap.emptyMap();
	}

	/**
	 * @return A singleton {@link Dictionary} associating the given key with
	 *         the given value; implemented as hash map.
	 */
	public static <K, V> Dictionary<K, V> hashMap(K key, V val) {
		return Persistents.<K, V> hashMap().plus(key, val);
	}

	/**
	 * @param keyValues
	 *            An array containing key/value pairs; no type checking is made.
	 * @return A {@link Dictionary} with the given associations; implemented
	 *         as hash map.
	 */
	public static <K, V> Dictionary<K, V> hashMap(Object... keyValues) {
		return PersistentHashMap.create(keyValues);
	}

	/**
	 * @return A {@link Dictionary} consisting of the associations of the map
	 *         init; implemented as hash map.
	 */
	public static <K, V> Dictionary<K, V> hashMap(Map<? extends K, ? extends V> init) {
		return PersistentHashMap.create(init);
	}

	/**
	 * @return An empty {@link Dictionary}; implemented as array map.
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Dictionary<K, V> arrayMap() {
		return PersistentArrayMap.empty();
	}

	/**
	 * @return A {@link Dictionary} consisting of the associations of the map
	 *         init; implemented as array map.
	 */
	public static <K, V> Dictionary<K, V> arrayMap(Map<? extends K, ? extends V> init) {
		return PersistentArrayMap.create(init);
	}

//	/**
//	 * @return An empty {@link PersistentSortedMap}; implemented as a tree map.
//	 */
//	@SuppressWarnings("unchecked")
//	public static <K, V> PersistentSortedMap<K, V> treeMap() {
//		return PersistentTreeMap.EMPTY;
//	}
//
//	/**
//	 * @return A singleton {@link PersistentSortedMap} associating the given key
//	 *         with the given value; implemented as tree map.
//	 */
//	public static <K, V> PersistentSortedMap<K, V> treeMap(K key, V val) {
//		return Persistents.<K, V> treeMap().plus(key, val);
//	}
//
//	/**
//	 * @return A {@link PersistentSortedMap} consisting of the associations of
//	 *         the map init; implemented as tree map.
//	 */
//	public static <K, V> PersistentSortedMap<K, V> treeMap(Map<? extends K, ? extends V> init) {
//		return PersistentTreeMap.create(init);
//	}
//
//	/**
//	 * @return An empty {@link PersistentSortedSet}; implemented as tree set.
//	 */
//	@SuppressWarnings("unchecked")
//	public static <E> PersistentSortedSet<E> treeSet() {
//		return PersistentTreeSet.empty();
//	}

	/**
	 * @return An empty {@link PersistentList}; implemented as linked list.
	 */
	public static <E> PersistentList<E> linkedList() {
		return com.github.krukow.clj_lang.PersistentConsList.emptyList();
	}

	/**
	 * @return An singleton {@link PersistentList} with the value val;
	 *         implemented as linked list.
	 */
	public static <E> PersistentList<E> linkedList(E val) {
		return Persistents.<E> linkedList().plus(val);
	}

	/**
	 * @return A {@link PersistentList} consisting of the element of the array
	 *         vals; implemented as linked list.
	 */
	public static <E> PersistentList<E> linkedList(E... vals) {
		return com.github.krukow.clj_lang.PersistentConsList.create(vals);
	}

	/**
	 * @return A {@link PersistentList} consisting of the element of the
	 *         {@link Iterator} vals; implemented as linked list.
	 */
	public static <E> PersistentList<E> linkedList(Iterable<? extends E> vals) {
		return com.github.krukow.clj_lang.PersistentConsList.create(vals);
	}

	// Utilities

	/**
	 * Applies successively the method {@link PersistentList#plus(E)}.
	 */
	public static <E> PersistentList<E> plusAll(PersistentList<E> list, Iterable<? extends E> others) {
		return PersistentConsList.plusAll(list, others);
	}

	/**
	 * Applies successively the method {@link PersistentList#plus(E)}.
	 */
	public static <E> PersistentList<E> plusAll(PersistentList<E> list, E... others) {
	    return plusAll(list, Arrays.asList(others));
	}

	/**
	 * Applies successively the method {@link PersistentVector#plus(E)}.
	 */
	public static <E> PersistentVector<E> plusAll(PersistentVector<E> vec, Iterable<? extends E> others) {
		TransientVector<E> result = vec.asTransient();
		for (E other : others) {
			result = result.plus(other);
		}
		return result.persist();
	}

	/**
	 * Applies successively the method {@link PersistentVector#plus(E)}.
	 */
	public static <E> PersistentVector<E> plusAll(PersistentVector<E> vec, E... others) {
		TransientVector<E> result = vec.asTransient();
		for (E other : others) {
			result = result.plus(other);
		}
		return result.persist();
	}

	/**
	 * Applies successively the method {@link PersistentSet#plus(E)}.
	 */
	public static <E> PersistentSet<E> plusAll(PersistentSet<E> set, Iterable<? extends E> others) {
		TransientSet<E> result = (TransientSet<E>) set.asTransient();
		for (E other : others) {
			result = result.plus(other);
		}
		return result.persist();
	}

	/**
	 * Applies successively the method {@link PersistentSet#plus(E)}.
	 */
	public static <E> PersistentSet<E> plusAll(PersistentSet<E> set, E... others) {
		TransientSet<E> result = (TransientSet<E>) set.asTransient();
		for (E other : others) {
			result = result.plus(other);
		}
		return result.persist();
	}

	/**
	 * Applies successively the method {@link PersistentSet#minus(E)}.
	 */
	public static <E> PersistentSet<E> minusAll(PersistentSet<E> set, Iterable<? extends E> others) {
		TransientSet<E> result = (TransientSet<E>) set.asTransient();
		for (E other : others) {
			result = result.minus(other);
		}
		return result.persist();
	}

	/**
	 * Applies successively the method {@link PersistentSet#minus(E)}.
	 */
	public static <E> PersistentSet<E> minusAll(PersistentSet<E> set, E... others) {
		TransientSet<E> result = (TransientSet<E>) set.asTransient();
		for (E other : others) {
			result = result.minus(other);
		}
		return result.persist();
	}

	/**
	 * Applies successively the method {@link PersistentSortedSet#plus(E)}.
	 */
	public static <E> PersistentSortedSet<E> plusAll(PersistentSortedSet<E> set, Iterable<? extends E> others) {
		PersistentSortedSet<E> result = set;
		for (E other : others) {
			result = result.plus(other);
		}
		return result;
	}

	/**
	 * Applies successively the method {@link PersistentSortedSet#plus(E)}.
	 */
	public static <E> PersistentSortedSet<E> plusAll(PersistentSortedSet<E> set, E... others) {
		PersistentSortedSet<E> result = set;
		for (E other : others) {
			result = result.plus(other);
		}
		return result;
	}

	/**
	 * Applies successively the method {@link PersistentSortedSet#minus(E)}.
	 */
	public static <E> PersistentSortedSet<E> minusAll(PersistentSortedSet<E> set, Iterable<? extends E> others) {
		PersistentSortedSet<E> result = set;
		for (E other : others) {
			result = result.minus(other);
		}
		return result;
	}

	/**
	 * Applies successively the method {@link PersistentSortedSet#minus(E)}.
	 */
	public static <E> PersistentSortedSet<E> minusAll(PersistentSortedSet<E> set, E... others) {
		PersistentSortedSet<E> result = set;
		for (E other : others) {
			result = result.minus(other);
		}
		return result;
	}

	// Empty Constructor
	private Persistents() {
	}

}
