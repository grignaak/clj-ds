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

import java.util.AbstractSequentialList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.github.krukow.clj_ds.PersistentList;

public class PersistentConsList<T> extends AbstractSequentialList<T> implements PersistentList<T> {

    private final T _first;
    private final PersistentConsList<T> _rest;
    private final int _count;

    final public static EmptyList EMPTY = new EmptyList();

    public static final <T> EmptyList<T> emptyList() {
        return EMPTY;
    }

    public PersistentConsList(T first) {
        this._first = first;
        this._rest = null;

        this._count = 1;
    }

    PersistentConsList(T _first, PersistentConsList<T> _rest, int _count) {
        this._first = _first;
        this._rest = _rest;
        this._count = _count;
    }

    public static <T> PersistentList<T> create(T... init) {
        PersistentList<T> ret = emptyList();
        for (int i = init.length - 1; i >= 0; i--)
        {
            ret = ret.plus(init[i]);
        }
        return ret;
    }

    public static <T> PersistentList<T> create(Iterable<? extends T> init) {
        PersistentVector<T> initVector = PersistentVector.create(init);
        return create(initVector);
    }

    public static <T> PersistentList<T> create(List<? extends T> init) {
        PersistentList<T> ret = emptyList();
        for (ListIterator<? extends T> i = init.listIterator(init.size()); i.hasPrevious();)
        {
            ret = ret.plus(i.previous());
        }
        return ret;
    }

    public T peek() {
        return _first;
    }

    public PersistentList<T> minus() {
        if (_rest == null)
            return EMPTY;
        return _rest;
    }

    public int size() {
        return _count;
    }

    public PersistentConsList<T> plus(T o) {
        return new PersistentConsList<T>(o, this, _count + 1);
    }

    public static <T> EmptyList<T> empty() {
        return EMPTY;
    }

    private static final class ConsListIterator<T> implements ListIterator<T> {
        private PersistentList<PersistentList<T>> prior = PersistentConsList.empty();
        private PersistentList<T> cur;

        public ConsListIterator(PersistentList<T> list, int startIndex) {
            if (startIndex < 0 || startIndex > list.size()) {
                String msg = String.format("Attempting to get index %d on list of size %d", startIndex, list.size());
                throw new IndexOutOfBoundsException(msg);
            }
            
            cur = list;
            for (int i = 0; i < startIndex; i++) {
                next();
            }
        }

        @Override
        public boolean hasNext() {
            return !cur.isEmpty();
        }

        @Override
        public T next() {
            T head = cur.peek();
            prior = prior.plus(cur);
            cur = cur.minus();
            
            return head;
        }

        @Override
        public boolean hasPrevious() {
            return !prior.isEmpty();
        }

        @Override
        public T previous() {
            cur = prior.peek();
            prior = prior.minus();
            return cur.peek();
        }

        @Override
        public int nextIndex() {
            return prior.size();
        }

        @Override
        public int previousIndex() {
            return prior.size() - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(T e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(T e) {
            throw new UnsupportedOperationException();
        }
    }

    private static class EmptyList<T> extends AbstractSequentialList<T> implements PersistentList<T> {
        public PersistentConsList<T> plus(T o) {
            return new PersistentConsList<T>(o, null, 1);
        }

        public EmptyList<T> zero() {
            return this;
        }

        public T peek() {
            return null;
        }

        public PersistentConsList<T> minus() {
            throw new IllegalStateException("Can't remove from an empty list");
        }

        public int size() {
            return 0;
        }

        public boolean contains(Object o) {
            return false;
        }

        public ListIterator<T> listIterator(int index) {
            return new ConsListIterator<>(this, index);
        }
    }

    public static <T> PersistentList<T> plusAll(PersistentList<T> list, Iterable<? extends T> others) {
        for (T other : others) {
            list = list.plus(other);
        }
        return list;
    }

    @Override
    public PersistentList<T> zero() {
        return empty();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new ConsListIterator(this, index);
    }
    
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private PersistentList<T> cur = PersistentConsList.this;

            @Override
            public boolean hasNext() {
                return !cur.isEmpty();
            }

            @Override
            public T next() {
                T head = cur.peek();
                cur = cur.minus();
                return head;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
