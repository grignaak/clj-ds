/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

package persistent;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * A persistent, threadSafe, singly-linked list. Adding and removing by default
 * happens at the <em>beginning</em> of the sequence, and is done in O(1) time.
 * Access at an arbitrary index is done in O(n) time.
 */
public class ConsList<T> extends Sequence<T> {

    private final static EmptyList<?> EMPTY = new EmptyList<>();
    
    private final T _first;
    private final ConsList<T> _rest;
    private final int _count;


    @SuppressWarnings("unchecked")
    public static <T> Sequence<T> emptyList() {
        return (EmptyList<T>) EMPTY;
    }

    private ConsList(T _first, ConsList<T> _rest, int _count) {
        this._first = _first;
        this._rest = _rest;
        this._count = _count;
    }

    @SafeVarargs
    public static <T> Sequence<T> create(T... init) {
        Sequence<T> ret = emptyList();
        for (int i = init.length - 1; i >= 0; i--) {
            ret = ret.plus(init[i]);
        }
        return ret;
    }

    public static <T> Sequence<T> create(Iterable<? extends T> init) {
        return create(init instanceof List<?> ? (List<? extends T>)init : Vector.create(init));
    }

    public static <T> Sequence<T> create(List<? extends T> init) {
        Sequence<T> ret = emptyList();
        for (ListIterator<? extends T> i = init.listIterator(init.size()); i.hasPrevious();) {
            ret = ret.plus(i.previous());
        }
        return ret;
    }

    public T peek() {
        return _first;
    }

    public Sequence<T> minus() {
        return _rest != null ? _rest : ConsList.<T>emptyList();
    }

    public int size() {
        return _count;
    }

    public ConsList<T> plus(T o) {
        return new ConsList<T>(o, this, _count + 1);
    }

    private static final class ConsListIterator<T> extends ImmutableListIterator<T> {
        private Sequence<Sequence<T>> prior = ConsList.emptyList();
        private Sequence<T> cur;

        public ConsListIterator(Sequence<T> list, int startIndex) {
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
    }

    private static class EmptyList<T> extends Sequence<T> {
        public ConsList<T> plus(T o) {
            return new ConsList<T>(o, null, 1);
        }

        public EmptyList<T> zero() {
            return this;
        }

        public T peek() {
            throw new NoSuchElementException();
        }

        public ConsList<T> minus() {
            throw new NoSuchElementException("Can't remove from an empty list");
        }

        @Override
        public int size() { return 0; }

        @Override
        public boolean contains(Object o) {
            return false;
        }
        
        @Override
        public ImmutableIterator<T> iterator() {
            return new EmptyIterator<>();
        }

        @Override
        public ImmutableListIterator<T> listIterator(int index) {
            return new ConsListIterator<>(this, index);
        }

        @Override
        public T get(int index) {
            throw new IndexOutOfBoundsException("Empty list");
        }

        @Override
        public List<T> subList(int fromIndex, int toIndex) {
            return new SubSequence<>(this, fromIndex, toIndex);
        }

        @Override
        public Sequence<T> replace(int index, T e) {
            rangeCheckInclusive(index, 0);
            return plus(e);
        }

        @Override
        public SequenceBuilder<T> asBuilder() {
            return new WrappedSequenceBuilder<>(currentThread(), this);
        }
    }

    @Override
    public Sequence<T> zero() {
        return emptyList();
    }

    @Override
    public ImmutableListIterator<T> listIterator(int index) {
        return new ConsListIterator<>(this, index);
    }

    @Override
    public ImmutableIterator<T> iterator() {
        /* The SeriesIterator takes up less memory, and does less work than the
         * ListIterator */
        return new SeriesIterator<>(this);
    }

    @Override
    public T get(int index) {
        rangeCheck(index, _count);
        
        ConsList<T> current = this;
        for (int i = 0; i < index; i++) {
            current = current._rest;
        }
        
        return current._first;
    }

    @Override
    public Sequence<T> replace(int index, T e) {
        if (index == 0) {
            return minus().plus(e);
        }
        
        rangeCheckInclusive(index, _count);
        
        Sequence<T> front = emptyList();
        Sequence<T> rear = this;
        
        for (int i = 0; i < index; i++) {
            front = front.plus(rear.peek());
            rear = rear.minus();
        }
        rear = (rear.isEmpty() ? rear : rear.minus()).plus(e);
        
        for (; !front.isEmpty(); front = front.minus()) {
            rear = rear.plus(front.peek());
        }
        return rear;
    }

    @Override
    public SequenceBuilder<T> asBuilder() {
        return new WrappedSequenceBuilder<>(currentThread(), this);
    }
}
