/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Dec 18, 2007 */

package com.github.krukow.clj_lang;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

public abstract class APersistentVector<T> extends AbstractList<T> implements List<T>, RandomAccess {
    private int _hash = -1;

    public int hashCode() {
        if (_hash == -1)
        {
            int hash = 1;
            Iterator i = iterator();
            while (i.hasNext())
            {
                Object obj = i.next();
                hash = 31 * hash + (obj == null ? 0 : obj.hashCode());
            }
            this._hash = hash;
        }
        return _hash;
    }

    public T remove(int i) {
        throw new UnsupportedOperationException();
    }

    public ListIterator<T> listIterator(final int index) {
        return new ListIterator<T>() {
            int nexti = index;

            public boolean hasNext() {
                return nexti < size();
            }

            public T next() {
                return get(nexti++);
            }

            public boolean hasPrevious() {
                return nexti > 0;
            }

            public T previous() {
                return get(--nexti);
            }

            public int nextIndex() {
                return nexti;
            }

            public int previousIndex() {
                return nexti - 1;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public void set(Object o) {
                throw new UnsupportedOperationException();
            }

            public void add(Object o) {
                throw new UnsupportedOperationException();
            }
        };
    }

    public T set(int i, T o) {
        throw new UnsupportedOperationException();
    }

    public void add(int i, T o) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(int i, Collection c) {
        throw new UnsupportedOperationException();
    }

    // java.util.Collection implementation

    public boolean add(T o) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(Collection c) {
        for (Object o : c)
        {
            if (!contains(o))
                return false;
        }
        return true;
    }

    static class Seq<T> extends ASeq<T> implements IndexedSeq<T> {
        // todo - something more efficient
        final IPersistentVector<T> v;
        final int i;

        public Seq(IPersistentVector<T> v, int i) {
            this.v = v;
            this.i = i;
        }

        public T first() {
            return v.nth(i);
        }

        public ISeq<T> next() {
            if (i + 1 < v.count())
                return new APersistentVector.Seq<T>(v, i + 1);
            return null;
        }

        public int index() {
            return i;
        }

        public int count() {
            return v.count() - i;
        }
    }
}
