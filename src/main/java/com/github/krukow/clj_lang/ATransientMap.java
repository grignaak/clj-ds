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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.github.krukow.clj_ds.PersistentMap;
import com.github.krukow.clj_ds.TransientMap;


abstract class ATransientMap<K, V> extends AbstractMap<K,V> implements TransientMap<K, V> {
    protected final AtomicReference<Thread> owner = new AtomicReference<>(Thread.currentThread());

    private final void ensureEditable() {
        final Thread thread = owner.get();
        if (thread == Thread.currentThread())
            return;
        if (thread != null)
            throw new IllegalAccessError("Transient used by non-owner thread");
        throw new IllegalAccessError("Transient used after persistent! call");
    }

    @Override
    public final boolean containsValue(Object value) {
        ensureEditable();
        return doContainsValue(value);
    }
    protected boolean doContainsValue(Object value) { return super.containsValue(value); }

    @Override
    public boolean containsKey(Object key) {
        ensureEditable();
        return doContainsKey(key);
    }
    protected boolean doContainsKey(Object key) { return super.containsKey(key); }

    @Override
    public V get(Object key) {
        ensureEditable();
        return doValAt((K)key);
    }
    protected V doValAt(K key) { return super.get(key); }

    @Override
    public Set<K> keySet() {
        ensureEditable();
        return super.keySet();
    }

    @Override
    public Collection<V> values() {
        ensureEditable();
        return super.values();
    }

    @Override
    public boolean equals(Object o) {
        ensureEditable();
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        ensureEditable();
        return super.hashCode();
    }

    @Override
    public String toString() {
        ensureEditable();
        return super.toString();
    }
    

    

    public final TransientMap<K, V> plus(K key, V val) {
        ensureEditable();
        return doAssoc(key, val);
    }
    protected abstract TransientMap<K, V> doAssoc(K key, V val);

    public final TransientMap<K, V> minus(K key) {
        ensureEditable();
        return doWithout(key);
    }
    protected abstract TransientMap<K, V> doWithout(K key);

    public final PersistentMap<K, V> persist() {
        ensureEditable();
        owner.set(null);
        return doPersistent();
    }
    protected abstract PersistentMap<K, V> doPersistent();

    public final int size() {
        ensureEditable();
        return doCount();
    }
    protected int doCount() { return super.size(); }
    

    @Override
    public boolean isEmpty() {
        ensureEditable();
        return doIsEmpty();
    }
    protected boolean doIsEmpty() { return doCount() == 0; }

    @Override
    public final Set<Map.Entry<K, V>> entrySet() {
        return new AbstractSet<Map.Entry<K,V>>() {
            private final Set<Map.Entry<K, V>> actual = doEntrySet();
            
            @Override
            public final Iterator<Map.Entry<K, V>> iterator() {
                return new Iterator<Map.Entry<K, V>>() {
                    private final Iterator<Map.Entry<K, V>> it = actual.iterator();
                    
                    @Override
                    public boolean hasNext() {
                        ensureEditable();
                        return it.hasNext();
                    }

                    @Override
                    public Map.Entry<K, V> next() {
                        ensureEditable();
                        return it.next();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
            
            // TODO ensure editable all the other methods too

            @Override
            public int size() {
                ensureEditable();
                return actual.size();
            }
        };
    }
    protected abstract Set<Map.Entry<K, V>> doEntrySet();
}
