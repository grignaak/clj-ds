/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

package com.github.krukow.clj_lang;

import java.util.Map;

import com.github.krukow.clj_ds.PersistentMap;
import com.github.krukow.clj_ds.TransientMap;


abstract class ATransientMap<K,V> implements TransientMap<K, V> {
	abstract void ensureEditable();
	abstract TransientMap<K,V> doAssoc(K key, V val);
	abstract TransientMap<K,V> doWithout(K key);
	abstract V doValAt(K key, V notFound);
	abstract int doCount();
	abstract PersistentMap<K,V> doPersistent();

	public TransientMap<K,V> conj(Map.Entry<K, V> o) {
		ensureEditable();
		return plus(o.getKey(), o.getValue());
	}

	public final Object invoke(Object arg1) {
		return valAt((K) arg1);
	}

	public final Object invoke(Object arg1, Object notFound) {
		return valAt((K)arg1, (V) notFound);
	}

	public final V valAt(K key) {
		return valAt(key, null);
	}

	public final TransientMap<K,V> assoc(K key, V val) {
		ensureEditable();
		return doAssoc(key, val);
	}

	public final TransientMap<K,V> without(K key) {
		ensureEditable();
		return doWithout(key);
	}

	public final PersistentMap<K,V> persistentMap() {
		ensureEditable();
		return doPersistent();
	}

	public final V valAt(K key, V notFound) {
		ensureEditable();
		return doValAt(key, notFound);
	}

	public final int count() {
		ensureEditable();
		return doCount();
	}
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }
    

    @Override
    public int size() {
        return count();
    }

    @Override
    public boolean containsKey(Object key) {
        V fake = (V)new Object();
        return valAt((K) key, fake) != fake;
    }

    @Override
    public boolean containsValue(Object value) {
        if (value != null) {
            for (V v : values()) {
                if (value.equals(v))
                    return true;
            }
        } else {
            for (V v : values()) {
                if (v == null)
                    return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        return valAt((K)key);
    }
}
