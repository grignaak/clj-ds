/**
 * Copyright (c) Rich Hickey. All rights reserved. The use and distribution
 * terms for this software are covered by the Eclipse Public License 1.0
 * (http://opensource.org/licenses/eclipse-1.0.php) which can be found in the
 * file epl-v10.html at the root of this distribution. By using this software in
 * any fashion, you are agreeing to be bound by the terms of this license. You
 * must not remove this notice, or any other, from this software.
 **/

/* rich Mar 25, 2006 11:01:29 AM */

package com.github.krukow.clj_lang;

import java.util.AbstractCollection;
import java.util.Iterator;

import com.github.krukow.clj_ds.PersistentCollection;
import com.github.krukow.clj_ds.PersistentSequence;

final public class Cons<T> extends AbstractCollection<T> implements PersistentSequence<T> {

    private final T _first;
    private final PersistentSequence<T> _more;

    public Cons(T first, PersistentSequence<T> _more) {
        this._first = first;
        this._more = _more;
    }

    @Override
    public T peek() {
        return _first;
    }

    @Override
    public PersistentSequence<T> minus() {
        return _more;
    }

    @Override
    public int size() {
        return 1 + _more.size();
    }

    @Override
    public PersistentSequence<T> zero() {
        return PersistentConsList.empty();
    }

    @Override
    public PersistentCollection<T> plus(T val) {
        return new Cons<T>(val, this);
    }

    @Override
    public Iterator<T> iterator() {
        // TODO unimplemented
        throw new RuntimeException("Unimplemented: AbstractCollection<T>.iterator");
    }
}
