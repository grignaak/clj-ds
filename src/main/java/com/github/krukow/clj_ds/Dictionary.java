package com.github.krukow.clj_ds;

import java.util.Map;

/**
 * {@link Dictionary}s
 * 
 * @param <K>
 *            The type of the keys
 * @param <V>
 *            The type of the values
 */
public interface Dictionary<K, V> extends Map<K, V>, ThouShaltNotMutateThisMap<K, V> {

	/**
	 * @return An empty instance of this kind of {@link Dictionary}
	 */
	Dictionary<K, V> zero();

	/**
	 * @return A new {@link Dictionary} consisting of the content of the
	 *         current {@link Dictionary} where the given key is associated
	 *         to the value val. The new association may replace a previous
	 *         association.
	 */
	Dictionary<K, V> plus(K key, V val);

	/**
	 * @return A new {@link Dictionary} consisting of the content of the
	 *         current {@link Dictionary} where the given key is associated
	 *         to the value val.
	 * @throws java.lang.RuntimeException
	 *             If the key is already present in the {@link Dictionary}.
	 */
	Dictionary<K, V> plusEx(K key, V val);

	/**
	 * @return A new {@link Dictionary} consisting of the content of the
	 *         current {@link Dictionary} without the assocation to the given
	 *         key.
	 */
	Dictionary<K, V> minus(K key);

    TransientMap<K, V> asBuilder();

}
