package persistent;

import java.util.ListIterator;

/**
 * A {@link ListIterator} that does not support the mutation methods.
 */
public abstract class AbstractImmutableListIterator<E> extends AbstractImmutableIterator<E> implements ImmutableListIterator<E> {
    @Deprecated @Override
    public final void set(E e) { throw AbstractContainer.mutate(); }
    @Deprecated @Override
    public void add(E e) { throw AbstractContainer.mutate(); }
}