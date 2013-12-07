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

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.github.krukow.clj_ds.PersistentMap;

public abstract class APersistentMap<K, V> implements PersistentMap<K, V>, Map<K, V>, Iterable<Map.Entry<K, V>>,
        Counted, Serializable {
    int _hash = -1;
    
    public String toString() {
        return RT.printString(this);
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Map))
            return false;
        Map m = (Map) obj;

        if (m.size() != size())
            return false;

        for (Map.Entry e : this) {
            boolean found = m.containsKey(e.getKey());

            if (!found || !Util.equals(e.getValue(), m.get(e.getKey())))
                return false;
        }
        return true;
    }

    public int hashCode() {
        if (_hash == -1)
        {
            int hash = 0;
            for (Map.Entry e : this) {
                hash += (e.getKey() == null ? 0 : e.getKey().hashCode()) ^
                        (e.getValue() == null ? 0 : e.getValue().hashCode());
            }
            this._hash = hash;
        }
        return _hash;
    }

    static public class KeySeq extends ASeq {
        ISeq seq;

        static public KeySeq create(ISeq seq) {
            if (seq == null)
                return null;
            return new KeySeq(seq);
        }

        private KeySeq(ISeq seq) {
            this.seq = seq;
        }

        public Object first() {
            return ((Map.Entry) seq.first()).getKey();
        }

        public ISeq next() {
            return create(seq.next());
        }
    }

    static public class ValSeq extends ASeq {
        ISeq seq;

        static public ValSeq create(ISeq seq) {
            if (seq == null)
                return null;
            return new ValSeq(seq);
        }

        private ValSeq(ISeq seq) {
            this.seq = seq;
        }

        public Object first() {
            return ((Map.Entry) seq.first()).getValue();
        }

        public ISeq next() {
            return create(seq.next());
        }
    }

    // java.util.Map implementation

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public boolean containsValue(Object value) {
        return values().contains(value);
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return new AbstractSet<Map.Entry<K, V>>() {

            public Iterator<Map.Entry<K, V>> iterator() {
                return APersistentMap.this.iterator();
            }

            public int size() {
                return count();
            }

            public int hashCode() {
                return APersistentMap.this.hashCode();
            }

            public boolean contains(Object o) {
                if (o instanceof Entry)
                {
                    Entry e = (Entry) o;
                    Entry found = entryAt((K) e.getKey());
                    if (found != null && Util.equals(found.getValue(), e.getValue()))
                        return true;
                }
                return false;
            }
        };
    }
    
    protected abstract Entry entryAt(K key);

    public boolean isEmpty() {
        return count() == 0;
    }

    public Set<K> keySet() {
        return new AbstractSet<K>() {

            public Iterator<K> iterator() {
                final Iterator<Map.Entry<K, V>> mi = APersistentMap.this.iterator();

                return new Iterator<K>() {

                    public boolean hasNext() {
                        return mi.hasNext();
                    }

                    public K next() {
                        Entry<K, V> e = (Entry<K, V>) mi.next();
                        return e.getKey();
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            public int size() {
                return count();
            }

            public boolean contains(Object o) {
                return APersistentMap.this.containsKey(o);
            }
        };
    }

    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    public void putAll(Map<? extends K, ? extends V> t) {
        throw new UnsupportedOperationException();
    }

    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    public int size() {
        return count();
    }

    public Collection<V> values() {
        return new AbstractCollection<V>() {

            public Iterator<V> iterator() {
                final Iterator<Map.Entry<K, V>> mi = APersistentMap.this.iterator();

                return new Iterator<V>() {

                    public boolean hasNext() {
                        return mi.hasNext();
                    }

                    public V next() {
                        return mi.next().getValue();
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            public int size() {
                return count();
            }
        };
    }
}
