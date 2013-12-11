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

import java.util.AbstractCollection;
import java.util.Iterator;

import com.github.krukow.clj_ds.PersistentSequence;
import com.github.krukow.clj_ds.PersistentStack;

/**
 * conses onto rear, peeks/pops from front See Okasaki's Batched Queues This
 * differs in that it uses a PersistentVector as the rear, which is in-order, so
 * no reversing or suspensions required for persistent use
 */
// TODO, not really a stack
public class PersistentQueue<T> extends AbstractCollection<T> implements PersistentStack<T> {

    final private static PersistentQueue EMPTY = new PersistentQueue(0, null, null);

    private final int cnt;
    private final PersistentSequence f;
    private final PersistentVector r;
    // static final int INITIAL_REAR_SIZE = 4;

    private PersistentQueue(int cnt, PersistentSequence f, PersistentVector r) {
        this.cnt = cnt;
        this.f = f;
        this.r = r;
    }

    @Override
    public T peek() {
        return (T) RT.first(f);
    }

    @Override
    public PersistentQueue<T> minus() {
        if (f == null)  // hmmm... pop of empty queue -> empty queue?
            return this;
        // throw new IllegalStateException("popping empty queue");
        PersistentSequence<T> f1 = f.minus();
        PersistentVector r1 = r;
        if (f1 == null)
        {
            f1 = r.asFifoSequence();
            r1 = PersistentVector.emptyVector();
        }
        return new PersistentQueue<T>(cnt - 1, f1, r1);
    }

    @Override
    public int size() {
        return cnt;
    }

    @Override
    public PersistentQueue<T> plus(T o) {
        if (f == null)     // empty
            return new PersistentQueue<T>(cnt + 1, PersistentConsList.create(o), null);
        else
            return new PersistentQueue<T>(cnt + 1, f, (r != null ? r : PersistentVector.emptyVector()).plus(o));
    }

    @Override
    public PersistentQueue<T> zero() {
        return EMPTY;
    }

    static class Seq<T> extends ASeq<T> {
        final ISeq<T> f;
        final ISeq<T> rseq;

        Seq(ISeq<T> f, ISeq<T> rseq) {
            this.f = f;
            this.rseq = rseq;
        }

        public T first() {
            return f.first();
        }

        public ISeq<T> next() {
            ISeq<T> f1 = f.next();
            ISeq<T> r1 = rseq;
            if (f1 == null)
            {
                if (rseq == null)
                    return null;
                f1 = rseq;
                r1 = null;
            }
            return new Seq<T>(f1, r1);
        }

        public int count() {
            return RT.count(f) + RT.count(rseq);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private final Iterator<T> first = f.iterator();
            private final Iterator<T> second = r.iterator();

            @Override
            public boolean hasNext() {
                return first.hasNext() || second.hasNext();
            }

            @Override
            public T next() {
                return first.hasNext() ? first.next() : second.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
