package persistent;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Comparator;

final class DefaultComparator<T extends Comparable<T>> implements Comparator<T>, Serializable {
    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static public final Comparator<? extends Comparable<?>> DEFAULT_COMPARATOR = new DefaultComparator();
    
    public int compare(T o1, T o2) {
        return o1.compareTo(o2);
    }

    private Object readResolve() throws ObjectStreamException {
        // ensures that we aren't hanging onto a new default comparator for
        // every
        // sorted set, etc., we deserialize
        return DefaultComparator.DEFAULT_COMPARATOR;
    }
}