package persistent;

import java.util.NoSuchElementException;


public final class EmptyIterator<T> extends ImmutableIterator<T> {
    @Override public boolean hasNext() { return false; }
    @Override public T next() { throw new NoSuchElementException(); }
}
