package persistent;

import java.util.Iterator;

public interface ImmutableIterator<E> extends Iterator<E> {
    @Deprecated @Override void remove();

}
