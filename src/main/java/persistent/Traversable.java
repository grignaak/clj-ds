package persistent;


public interface Traversable<E> extends Iterable<E> {
    Cursor<E> cursor();
    @Override ImmutableIterator<E> iterator();
    
}
