package persistent;

import java.util.NoSuchElementException;


/**
 * A cursor represents is a persistent, thread-safe way to traverse a data structure.
 */
public abstract class Cursor<E> implements Iterable<E> {
    
    private static final Cursor<?> EMPTY = new Cursor<Object>() {
        @Override public boolean isDone() { return true; }
        @Override public Cursor<Object> tail() { return this; }
        @Override public Object head() { throw new NoSuchElementException("Empty cursor"); }
    };
    
    Cursor() {/* You can't see me, I'm the gingerbread man */}
    
    @SuppressWarnings("unchecked")
    public static <E> Cursor<E> emptyCursor() {
        return (Cursor<E>) EMPTY;
    }
    
    public abstract boolean isDone();
    public abstract Cursor<E> tail();
    public abstract E head();


    @Override
    public ImmutableIterator<E> iterator() {
        return new CursorIterator<E>(this);
    }
}


class CursorIterator<E> extends ImmutableIterator<E> {
    private Cursor<E> current;
    
    CursorIterator(Cursor<E> start) {
        this.current = start;
    }

    @Override
    public boolean hasNext() {
        return !current.isDone();
    }

    @Override
    public E next() {
        if (current.isDone())
            throw new NoSuchElementException("Exhausted iterator");
        
        E head = current.head();
        current = current.tail();
        return head;
    }
    
}
