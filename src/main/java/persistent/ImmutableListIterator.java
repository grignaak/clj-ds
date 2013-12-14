package persistent;

import java.util.ListIterator;

/**
 * A {@link ListIterator} that does not support the mutation methods.
 */
public abstract class ImmutableListIterator<E> extends ImmutableIterator<E> implements ListIterator<E> {
    ImmutableListIterator() {/* package bolted down */}
    @Deprecated @Override
    public final void set(E e) { throw Container.mutate(); }
    @Deprecated @Override
    public void add(E e) { throw Container.mutate(); }
}