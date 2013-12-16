package persistent;

import java.util.NoSuchElementException;


/**
 * A cursor represents is a persistent, thread-safe way to traverse a data structure.
 */
public abstract class AbstractCursor<E> implements Iterable<E>, Cursor<E> {

    public static class CursorIterator<E> extends AbstractImmutableIterator<E> {
        private Cursor<E> current;
        
        public CursorIterator(Cursor<E> start) {
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


    @Override
    public ImmutableIterator<E> iterator() {
        return new CursorIterator<E>(this);
    }
}
