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

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import persistent.AbstractBuilder.Owner;
import persistent.TrieVector.TrieVectorBuilder;

/**
 * A series where elements are added onto the rear and removed from the front.
 * <p>
 * This differs from Okasaki's Batched Queues in that it uses a Vector as the
 * rear, which is in-order, so no reversing or suspensions required for
 * persistent use.
 */
public class TrieQueue<T> extends AbstractSeries<T> {

    private static class TrieQueueIterator<T> extends AbstractImmutableIterator<T> {
        private final Iterator<T> first;
        private final Iterator<T> second;

        public TrieQueueIterator(TrieQueue<T> queue) {
            first = queue.front.iterator();
            second = queue.rear.iterator();
        }

        @Override
        public boolean hasNext() {
            return first.hasNext() || second.hasNext();
        }

        @Override
        public T next() {
            return first.hasNext() ? first.next() : second.next();
        }
    }
    
    public static class TrieQueueBuilder<T> extends AbstractSeriesBuilder<T> {
        private final int size;
        private final Cursor<T> front;
        private final TrieVector.TrieVectorBuilder<T> rear;

        TrieQueueBuilder(Owner owner, int size, Cursor<T> front, TrieVector.TrieVectorBuilder<T> rear) {
            super(owner);
            this.size = size;
            this.front = front;
            this.rear = rear;
        }

        @Override
        public TrieQueueBuilder<T> plus(T e) {
            owner.ensureEditable();
            if (size == 0)
                return new TrieQueueBuilder<>(owner, size + 1, ConsList.create(e).cursor(), rear);
            else
                return new TrieQueueBuilder<>(owner, size + 1, front, rear.plus(e));
        }
        
        @Override
        public TrieQueueBuilder<T> plusAll(Collection<? extends T> more) {
            return (TrieQueueBuilder<T>) super.plusAll(more);
        }

        @Override
        public TrieQueueBuilder<T> zero() {
            owner.ensureEditable();
            return TrieQueue.<T>emptyQueue().asBuilder();
        }

        @Override
        public TrieQueue<T> build() {
            owner.ensureEditable();
            return built(new TrieQueue<>(size, front, rear.build()));
        }

        @Override
        public TrieQueueBuilder<T> minus() {
            owner.ensureEditable();
            if (front.isDone())
                return this;

            Cursor<T> newFront = front.tail();
            TrieVectorBuilder<T> newRear = rear;
            
            if (newFront.isDone()) {
                /* Switching owners here because we're building the vector, it
                 * ends up being the same thread anyway. */
                newFront = rear.build().cursor();
                newRear = TrieVector.<T>emptyVector().asBuilder();
            }
            
            return new TrieQueueBuilder<T>(newRear.owner, size - 1, newFront, newRear);
        }
    }

    private final static TrieQueue<?> EMPTY = new TrieQueue<>(0, ConsList.emptyList().cursor(), TrieVector.emptyVector());
    
    @SuppressWarnings("unchecked")
    public static <T> TrieQueue<T> emptyQueue() {
        return (TrieQueue<T>) EMPTY;
    }

    private final int size;
    
    /* Invariant: If front is empty, size is 0 */
    private final Cursor<T> front;
    private final TrieVector<T> rear;

    private TrieQueue(int size, Cursor<T> front, TrieVector<T> rear) {
        this.size = size;
        this.front = front;
        this.rear = rear;
    }

    @Override
    public T peek() {
        return front.head();
    }

    @Override
    public TrieQueue<T> minus() {
        if (front.isDone())
            throw new NoSuchElementException("removing from empty queue");

        Cursor<T> newFront = front.tail();
        TrieVector<T> newRear = rear;
        if (newFront.isDone()) {
            newFront = rear.cursor();
            newRear = TrieVector.<T>emptyVector();
        }
        return new TrieQueue<T>(size - 1, newFront, newRear);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public TrieQueue<T> plus(T o) {
        if (size == 0)
            return new TrieQueue<T>(size + 1, ConsList.create(o).cursor(), rear);
        else
            return new TrieQueue<T>(size + 1, front, rear.plus(o));
    }

    @Override
    public TrieQueue<T> zero() {
        return emptyQueue();
    }

    @Override
    public ImmutableIterator<T> iterator() {
        return new TrieQueueIterator<T>(this);
    }

    @Override
    public TrieQueueBuilder<T> asBuilder() {
        return new TrieQueueBuilder<>(new Owner(), size, front, rear.asBuilder());
    }

    public static <T> TrieQueueBuilder<T> newBuilder() {
        return TrieQueue.<T>emptyQueue().asBuilder();
    }
}
