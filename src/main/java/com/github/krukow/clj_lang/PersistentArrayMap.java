/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse private License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

package com.github.krukow.clj_lang;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.krukow.clj_ds.Dictionary;
import com.github.krukow.clj_ds.TransientMap;

/**
 * Simple implementation of persistent map on an array
 * <p/>
 * Note that instances of this class are constant values i.e. add/remove etc
 * return new values
 * <p/>
 * Copies array on every change, so only appropriate for _very_small_ maps
 * <p/>
 * null keys and values are ok, but you won't be able to distinguish a null
 * value via valAt - use contains/entryAt
 */

public class PersistentArrayMap<K, V> extends AbstractMap<K, V> implements Dictionary<K, V> {
    private static final int HASHTABLE_THRESHOLD = 16;
    private static final PersistentArrayMap EMPTY = new PersistentArrayMap();

    private final Object[] array;

    
    public static <K, V> PersistentArrayMap<K, V> empty() {
        return (PersistentArrayMap<K,V>)EMPTY;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Dictionary<K, V> create(Map<? extends K, ? extends V> other) {
        TransientMap<K, V> ret = EMPTY.asBuilder();
        for (Map.Entry<? extends K, ? extends V> e : other.entrySet())
        {
            ret = ret.plus(e.getKey(), e.getValue());
        }
        return (Dictionary<K, V>) ret.persist();
    }

    protected PersistentArrayMap() {
        this.array = new Object[] {};
    }

    private PersistentArrayMap<K, V> create(Object... init) {
        return new PersistentArrayMap<K, V>(init);
    }

    private PersistentHashMap<K, V> createHT(Object[] init) {
        return PersistentHashMap.create(init);
    }

    /**
     * This ctor captures/aliases the passed array, so do not modify later
     * 
     * @param init
     *            {key1,val1,key2,val2,...}
     */
    private PersistentArrayMap(Object[] init) {
        this.array = init;
    }
    
    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<Entry<K, V>>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                return new Iter<K, V>(array);
            }

            @Override
            public int size() {
                return array.length / 2;
            }
            
        };
    }
    
    @Override
    public Dictionary<K, V> zero() {
        return empty();
    }

    @Override
    public boolean containsKey(Object key) {
        return indexOf(key) >= 0;
    }

    public Dictionary<K, V> plusEx(K key, V val) {
        int i = indexOf(key);
        Object[] newArray;
        if (i >= 0)
        {
            throw Util.runtimeException("Key already present");
        }
        else // didn't have key, grow
        {
            if (array.length > HASHTABLE_THRESHOLD)
                return createHT(array).plusEx(key, val);
            newArray = new Object[array.length + 2];
            if (array.length > 0)
                System.arraycopy(array, 0, newArray, 2, array.length);
            newArray[0] = key;
            newArray[1] = val;
        }
        return create(newArray);
    }

    @Override
    public Dictionary<K, V> plus(K key, V val) {
        int i = indexOf(key);
        Object[] newArray;
        if (i >= 0) // already have key, same-sized replacement
        {
            if (array[i + 1] == val) // no change, no op
                return this;
            newArray = array.clone();
            newArray[i + 1] = val;
        }
        else // didn't have key, grow
        {
            if (array.length > HASHTABLE_THRESHOLD)
                return createHT(array).plus(key, val);
            newArray = new Object[array.length + 2];
            if (array.length > 0)
                System.arraycopy(array, 0, newArray, 2, array.length);
            newArray[0] = key;
            newArray[1] = val;
        }
        return create(newArray);
    }

    @Override
    public PersistentArrayMap<K, V> minus(K key) {
        int i = indexOf(key);
        if (i >= 0) // have key, will remove
        {
            int newlen = array.length - 2;
            if (newlen == 0)
                return empty();
            Object[] newArray = new Object[newlen];
            for (int s = 0, d = 0; s < array.length; s += 2)
            {
                if (!equalKey(array[s], key)) // skip removal key
                {
                    newArray[d] = array[s];
                    newArray[d + 1] = array[s + 1];
                    d += 2;
                }
            }
            return create(newArray);
        }
        // don't have key, no op
        return this;
    }

    @Override
    public V get(Object key) {
        int i = indexOf((K) key);
        if (i >= 0)
            return (V) array[i + 1];
        return null;
    }

    private static int indexOf(Object key, Object[] array, int len) {
        for (int i = 0; i < len; i += 2) {
            if (equalKey(array[i], key))
                return i;
        }
        return -1;
    }
    
    private int indexOf(Object key) {
        return indexOf(key, array, array.length);
    }

    private static boolean equalKey(Object k1, Object k2) {
        return Objects.equals(k1, k2);
    }

    public TransientArrayMap asBuilder() {
        return new TransientArrayMap(array);
    }

    private static class Iter<K, V> implements Iterator<Entry<K, V>> {
        private final Object[] array;
        private int i = -2;
        
        private Iter(Object[] array) {
            this.array = array;
        }

        @Override
        public boolean hasNext() {
            return i < array.length - 2;
        }

        @Override
        public Entry<K, V> next() {
            i += 2;
            return new AbstractMap.SimpleImmutableEntry<>((K)array[i], (V)array[i + 1]);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class TransientArrayMap<K, V> extends ATransientMap<K, V> implements TransientMap<K, V> {
        private int len;
        private final Object[] array;

        private TransientArrayMap(Object[] array) {
            this.array = new Object[Math.max(HASHTABLE_THRESHOLD, array.length)];
            System.arraycopy(array, 0, this.array, 0, array.length);
            this.len = array.length;
        }

        private int indexOf(Object key) {
            return PersistentArrayMap.indexOf(key, array, len);
        }

        protected TransientMap<K, V> doAssoc(K key, V val) {
            int i = indexOf(key);
            if (i >= 0) // already have key,
            {
                if (array[i + 1] != val) // no change, no op
                    array[i + 1] = val;
            }
            else // didn't have key, grow
            {
                if (len >= array.length)
                    return PersistentHashMap.create(array).asBuilder().plus(key, val);
                array[len++] = key;
                array[len++] = val;
            }
            return this;
        }

        protected TransientArrayMap<K, V> doWithout(K key) {
            int i = indexOf(key);
            if (i >= 0) // have key, will remove
            {
                if (len >= 2)
                {
                    array[i] = array[len - 2];
                    array[i + 1] = array[len - 1];
                }
                len -= 2;
            }
            return this;
        }

        protected V doValAt(K key) {
            int i = indexOf(key);
            if (i >= 0)
                return (V) array[i + 1];
            return null;
        }

        protected int doCount() {
            return len / 2;
        }

        protected PersistentArrayMap<K, V> doPersistent() {
            Object[] a = new Object[len];
            System.arraycopy(array, 0, a, 0, len);
            return new PersistentArrayMap<K, V>(a);
        }

        @Override
        public Set<Entry<K, V>> doEntrySet() {
            return new AbstractSet<Entry<K,V>>() {
                @Override
                public Iterator<Entry<K, V>> iterator() {
                    return new Iter<K,V>(array) {
                        @Override
                        public boolean hasNext() {
                            return super.hasNext();
                        }
                        
                        @Override
                        public Entry<K, V> next() {
                            return super.next();
                        }
                    };
                }

                @Override
                public int size() {
                    return TransientArrayMap.this.size();
                }
            };
        }

    }
}
