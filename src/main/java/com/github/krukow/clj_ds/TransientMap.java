package com.github.krukow.clj_ds;

import java.util.Map;

public interface TransientMap<K, V> extends Map<K, V>, ThouShaltNotMutateThisMap<K, V> {

	/**
	 * @return A new {@link Dictionary} consisting of the content of the
	 *         current {@link Dictionary} where the given key is associated
	 *         to the value val (no guarantees are made on the current map). The
	 *         new association may replace a previous association.
	 */
	TransientMap<K, V> plus(K key, V val);

	/**
	 * @return A new {@link Dictionary} consisting of the content of the
	 *         current {@link Dictionary} without the association to the
	 *         given key (no guarantees are made on the current map).
	 */
	TransientMap<K, V> minus(K key);

	Dictionary<K, V> persist();

}
