package persistent;

import java.util.NoSuchElementException;


public final class EmptyIterator<T> extends AbstractImmutableListIterator<T> {
    @Override public boolean hasNext() { return false; }
    @Override public boolean hasPrevious() { return false; }
    
    @Override public T next() { throw new NoSuchElementException(); }
    @Override public T previous() { throw new NoSuchElementException(); }
    
    @Override public int nextIndex() { return 0; }
    @Override public int previousIndex() { return -1; }
}
