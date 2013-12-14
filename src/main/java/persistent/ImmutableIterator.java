package persistent;

import java.util.Iterator;

/**
 * An {@link Iterator} that does not support the mutation methods.
 */
public abstract class ImmutableIterator<E> implements Iterator<E> {
    ImmutableIterator() {/* fort knox */}
    
    @Deprecated @Override
    public final void remove() { Container.mutate(); }
}