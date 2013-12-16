package persistent;

public interface Cursor<E> {
    public abstract boolean isDone();
    public abstract Cursor<E> tail();
    public abstract E head();
    public abstract ImmutableIterator<E> iterator();

}
