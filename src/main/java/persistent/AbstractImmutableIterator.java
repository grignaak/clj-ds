package persistent;

import java.util.Iterator;

/**
 * An {@link Iterator} that does not support the mutation methods.
 */
public abstract class AbstractImmutableIterator<E> implements Iterator<E>, ImmutableIterator<E> {
    @Deprecated @Override public final void remove() { AbstractContainer.mutate(); }
}