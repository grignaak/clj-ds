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


public final class Cons<T> extends Series<T> {

    private final T _first;
    private final Series<T> _more;
    private final int _size;

    Cons(T first, Series<T> _more) {
        this._first = first;
        this._more = _more;
        this._size = _more.size() + 1;
    }

    @Override
    public T peek() {
        return _first;
    }

    @Override
    public Series<T> minus() {
        return _more;
    }

    @Override
    public int size() {
        return _size;
    }

    @Override
    public Series<T> zero() {
        /* Improves the performance, I should think. */
        return ConsList.emptyList();
    }

    @Override
    public Series<T> plus(T val) {
        return new Cons<T>(val, this);
    }

    @Override
    public ImmutableIterator<T> iterator() {
        return new SeriesIterator<>(this);
    }

    @Override
    public SeriesBuilder<T> asBuilder() {
        return new WrappedSeriesBuilder<T>(currentThread(), this);
    }
}
