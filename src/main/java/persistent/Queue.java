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
import java.util.concurrent.atomic.AtomicReference;

import persistent.Vector.VectorBuilder;

/**
 * A series where elements are added onto the rear and removed from the front.
 * <p>
 * This differs from Okasaki's Batched Queues in that it uses a Vector as the
 * rear, which is in-order, so no reversing or suspensions required for
 * persistent use.
 */
public class Queue<T> extends Series<T> {

    private static class QueueIterator<T> extends ImmutableIterator<T> {
        private final Iterator<T> first;
        private final Iterator<T> second;

        public QueueIterator(Queue<T> queue) {
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
    
    public static class QueueBuilder<T> extends SeriesBuilder<T> {
        private final int size;
        private final Cursor<T> front;
        private final Vector.VectorBuilder<T> rear;

        QueueBuilder(AtomicReference<Thread> owner, int size, Cursor<T> front, Vector.VectorBuilder<T> rear) {
            super(owner);
            this.size = size;
            this.front = front;
            this.rear = rear;
        }

        @Override
        public QueueBuilder<T> plus(T e) {
            ensureEditable();
            if (size == 0)
                return new QueueBuilder<>(owner, size + 1, ConsList.create(e).cursor(), rear);
            else
                return new QueueBuilder<>(owner, size + 1, front, rear.plus(e));
        }
        
        @Override
        public QueueBuilder<T> plusAll(Collection<? extends T> more) {
            return (QueueBuilder<T>) super.plusAll(more);
        }

        @Override
        public QueueBuilder<T> zero() {
            ensureEditable();
            return Queue.<T>emptyQueue().asBuilder();
        }

        @Override
        public Queue<T> build() {
            ensureEditable();
            return built(new Queue<>(size, front, rear.build()));
        }

        @Override
        public QueueBuilder<T> minus() {
            ensureEditable();
            if (front.isDone())
                return this;

            Cursor<T> newFront = front.tail();
            VectorBuilder<T> newRear = rear;
            
            if (newFront.isDone()) {
                /* Switching owners here because we're building the vector, it
                 * ends up being the same thread anyway. */
                newFront = rear.build().cursor();
                newRear = Vector.<T>emptyVector().asBuilder();
            }
            
            return new QueueBuilder<T>(newRear.owner, size - 1, newFront, newRear);
        }
    }

    private final static Queue<?> EMPTY = new Queue<>(0, ConsList.emptyList().cursor(), Vector.emptyVector());
    
    @SuppressWarnings("unchecked")
    public static <T> Queue<T> emptyQueue() {
        return (Queue<T>) EMPTY;
    }

    private final int size;
    
    /* Invariant: If front is empty, size is 0 */
    private final Cursor<T> front;
    private final Vector<T> rear;

    private Queue(int size, Cursor<T> front, Vector<T> rear) {
        this.size = size;
        this.front = front;
        this.rear = rear;
    }

    @Override
    public T peek() {
        return front.head();
    }

    @Override
    public Queue<T> minus() {
        if (front.isDone())
            throw new NoSuchElementException("removing from empty queue");

        Cursor<T> newFront = front.tail();
        Vector<T> newRear = rear;
        if (newFront.isDone()) {
            newFront = rear.cursor();
            newRear = Vector.<T>emptyVector();
        }
        return new Queue<T>(size - 1, newFront, newRear);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Queue<T> plus(T o) {
        if (size == 0)
            return new Queue<T>(size + 1, ConsList.create(o).cursor(), rear);
        else
            return new Queue<T>(size + 1, front, rear.plus(o));
    }

    @Override
    public Queue<T> zero() {
        return emptyQueue();
    }

    @Override
    public ImmutableIterator<T> iterator() {
        return new QueueIterator<T>(this);
    }

    @Override
    public QueueBuilder<T> asBuilder() {
        return new QueueBuilder<>(currentThread(), size, front, rear.asBuilder());
    }

    public static <T> QueueBuilder<T> newBuilder() {
        return Queue.<T>emptyQueue().asBuilder();
    }
}
