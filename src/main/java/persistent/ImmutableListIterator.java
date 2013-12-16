package persistent;

import java.util.ListIterator;

public interface ImmutableListIterator<E> extends ListIterator<E>, ImmutableIterator<E> {
    @Deprecated @Override void set(E e);
    @Deprecated @Override void add(E e);
}
