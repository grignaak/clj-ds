/**
 * Copyright (c) Rich Hickey. All rights reserved. The use and distribution
 * terms for this software are covered by the Eclipse Public License 1.0
 * (http://opensource.org/licenses/eclipse-1.0.php) which can be found in the
 * file epl-v10.html at the root of this distribution. By using this software in
 * any fashion, you are agreeing to be bound by the terms of this license. You
 * must not remove this notice, or any other, from this software.
 **/

/* rich Mar 25, 2006 11:01:29 AM */

package persistent;

import persistent.AbstractBuilder.Owner;


final class Cons<T> extends AbstractContainer<T> implements Cursor<T> {

    private final T _first;
    private final Container<T> _more;
    private final int _size;

    Cons(T first, Container<T> _more) {
        this._first = first;
        this._more = _more;
        this._size = _more.size() + 1;
    }

    @Override
    public int size() {
        return _size;
    }

    @Override
    public Container<T> zero() {
        return TrieVector.<T>emptyVector();
    }

    @Override
    public Container<T> plus(T val) {
        return new Cons<T>(val, this);
    }

    @Override
    public ImmutableIterator<T> iterator() {
        return new AbstractCursor.CursorIterator<>(this);
    }

    @Override
    public persistent.Container.ContainerBuilder<T> asBuilder() {
        return new WrappedContainerBuilder<T>(new Owner(), this);
    }

    @Override
    public Cursor<T> cursor() {
        return this;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public Cursor<T> tail() {
        return _more.cursor();
    }

    @Override
    public T head() {
        return _first;
    }
}
