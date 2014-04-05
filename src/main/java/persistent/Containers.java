package persistent;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import persistent.AbstractBuilder.Owner;
import persistent.AbstractDictionary.AbstractDictionaryBuilder;
import persistent.Dictionary.DictionaryBuilder;
import persistent.FiniteSet.FiniteSetBuilder;
import persistent.Sequence.AbstractSequenceBuilder;
import persistent.Sequence.SequenceBuilder;
import persistent.Series.SeriesBuilder;

public class Containers {
    private static class WrappedSequenceBuilder<E> extends AbstractSequenceBuilder<E> {
    
        private final Sequence<E> result;
    
        WrappedSequenceBuilder(Owner owner, Sequence<E> sequence) {
            super(owner);
            this.result = sequence;
        }
    
        @Override
        public SequenceBuilder<E> plus(E e) {
            owner.ensureEditable();
            return new WrappedSequenceBuilder<>(owner, result.plus(e));
        }
    
        @Override
        public SequenceBuilder<E> replace(int index, E e) {
            owner.ensureEditable();
            return new WrappedSequenceBuilder<>(owner, result.replace(index, e));
        }
    
        @Override
        public SequenceBuilder<E> plusAll(Collection<? extends E> more) {
            owner.ensureEditable();
            return new WrappedSequenceBuilder<>(owner, result.plusAll(more));
        }
    
        @Override
        public SequenceBuilder<E> minus() {
            owner.ensureEditable();
            return result.isEmpty() ? this : new WrappedSequenceBuilder<>(owner, result.minus());
        }
    
        @Override
        public SequenceBuilder<E> zero() {
            owner.ensureEditable();
            return new WrappedSequenceBuilder<>(owner, result.zero());
        }
    
        @Override
        public Sequence<E> build() {
            owner.ensureEditable();
            return built(result);
        }
    }
    
    private static class WrappedDictionaryBuilder<K, V> extends AbstractDictionaryBuilder<K,V> implements DictionaryBuilder<K, V> {
        private final Dictionary<K, V> impl;

        WrappedDictionaryBuilder(Owner owner, Dictionary<K,V> impl) {
            super(owner);
            this.impl = impl;
        }

        @Override
        public DictionaryBuilder<K, V> plus(K key, V value) {
            owner.ensureEditable();
            return new WrappedDictionaryBuilder<>(owner, impl.plus(key, value));
        }

        @Override
        public DictionaryBuilder<K, V> plusIfAbsent(K key, V value) {
            owner.ensureEditable();
            return new WrappedDictionaryBuilder<>(owner, impl.plusIfAbsent(key, value));
        }

        @Override
        public DictionaryBuilder<K, V> replace(K key, V expected, V actual) {
            owner.ensureEditable();
            return new WrappedDictionaryBuilder<>(owner, impl.replace(key, expected, actual));
        }

        @Override
        public DictionaryBuilder<K, V> minus(K key) {
            owner.ensureEditable();
            return new WrappedDictionaryBuilder<>(owner, impl.minus(key));
        }

        @Override
        public DictionaryBuilder<K, V> minus(K key, V expected) {
            owner.ensureEditable();
            return new WrappedDictionaryBuilder<>(owner, impl.minus(key, expected));
        }

        @Override
        public DictionaryBuilder<K, V> zero() {
            owner.ensureEditable();
            return new WrappedDictionaryBuilder<>(owner, impl.zero());
        }

        @Override
        public Dictionary<K, V> build() {
            owner.ensureEditable();
            return owner.built(impl);
        }
    }

    private static class WrappedSeriesBuilder<E> extends AbstractSeries.AbstractSeriesBuilder<E> {
        private final Series<E> result;
    
        public WrappedSeriesBuilder(Owner owner, Series<E> series) {
            super(owner);
            this.result = series;
        }
    
        @Override
        public SeriesBuilder<E> plus(E e) {
            owner.ensureEditable();
            return new WrappedSeriesBuilder<>(owner, result.plus(e));
        }
    
        @Override
        public SeriesBuilder<E> plusAll(Collection<? extends E> more) {
            owner.ensureEditable();
            throw new RuntimeException("Unimplemented: SeriesBuilder<E>.plusAll");
        }
    
        @Override
        public SeriesBuilder<E> zero() {
            owner.ensureEditable();
            return new WrappedSeriesBuilder<>(owner, result.zero());
        }
    
        @Override
        public Series<E> build() {
            owner.ensureEditable();
            return built(result);
        }
    
        @Override
        public SeriesBuilder<E> minus() {
            owner.ensureEditable();
            return result.isEmpty() ? this : new WrappedSeriesBuilder<>(owner, result.minus());
        }
    }
    
    private static class WrappedDictionarySet<K> extends AbstractSet<K> implements FiniteSet<K> {
        private final Dictionary<K, Object> impl;
        
        public WrappedDictionarySet(Dictionary<K, Object> impl) {
            this.impl = impl;
        }

        @Override
        public Cursor<K> cursor() {
            return new KeyCursor<>(impl.cursor());
        }
        
        @Override
        public boolean contains(Object o) {
            return impl.containsKey(o);
        }

        @Override
        public FiniteSet<K> plus(K e) {
            return new WrappedDictionarySet<K>(impl.plusIfAbsent(e, Boolean.TRUE));
        }

        @Override
        public FiniteSet<K> plusAll(Collection<? extends K> more) {
            return asBuilder().plusAll(more).build();
        }

        @Override
        public FiniteSet<K> minus(K value) {
            return new WrappedDictionarySet<K>(impl.minus(value));
        }

        @Override
        public FiniteSet<K> zero() {
            return new WrappedDictionarySet<>(impl.zero());
        }

        @Override
        public FiniteSetBuilder<K> asBuilder() {
            return new WrappedDictionarySetBuilder<>(impl.asBuilder());
        }

        @Override
        public ImmutableIterator<K> iterator() {
            return new KeyIterator<>(impl.entrySet().iterator());
        }

        @Override
        public int size() {
            return impl.size();
        }
    }
    
    private static class WrappedDictionarySetBuilder<K> implements FiniteSet.FiniteSetBuilder<K> {
        private final DictionaryBuilder<K, Object> impl;
        
        public WrappedDictionarySetBuilder(DictionaryBuilder<K, Object> impl) {
            this.impl = impl;
        }

        @Override
        public FiniteSetBuilder<K> plus(K e) {
            return new WrappedDictionarySetBuilder<K>(impl.plusIfAbsent(e, Boolean.TRUE));
        }

        @Override
        public FiniteSetBuilder<K> plusAll(Collection<? extends K> more) {
            DictionaryBuilder<K, Object> current = impl;
            for (K k : more) {
                current = impl.plus(k, Boolean.TRUE);
            }
            return new WrappedDictionarySetBuilder<>(current);
        }

        @Override
        public FiniteSetBuilder<K> minus(K value) {
            return new WrappedDictionarySetBuilder<K>(impl.minus(value));
        }

        @Override
        public FiniteSetBuilder<K> zero() {
            return new WrappedDictionarySetBuilder<>(impl.zero());
        }

        @Override
        public FiniteSet<K> build() {
            return new WrappedDictionarySet<>(impl.build());
        }
    }
    
    static class IteratorCursor<E> extends AbstractCursor<E> {
        private Iterator<E> backend;
        private Cursor<E> next;
        private final E current;
        
        private IteratorCursor(E current, Iterator<E> backend) {
            synchronized (this) {
                this.backend = backend;
                this.current = current;
            }
        }
        
        static <E> Cursor<E> create(Iterator<E> backend) {
            return backend.hasNext() ? new IteratorCursor<E>(backend.next(), backend) : Containers.<E>emptyCursor();
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public synchronized Cursor<E> tail() {
            if (next == null) {
                next = create(backend);
                backend = null; // let the thing be garbage collected.
            }
            return next;
        }

        @Override
        public E head() {
            return current;
        }
        
    }

    private static final Cursor<?> EMPTY_CURSOR = new AbstractCursor<Object>() {
        @Override public boolean isDone() { return true; }
        @Override public Cursor<Object> tail() { return this; }
        @Override public Object head() { throw new NoSuchElementException("Empty cursor"); }
    };

    private Containers() {/* Utility class */}

    /**
     * Create a builder which uses the underlying series to build instead of
     * relying on it's supplied builder. This is useful when developing a
     * series type that doesn't have its own builder.
     */
    public static <E> SeriesBuilder<E> wrappedBuilder(Series<E> sequence) {
        return new WrappedSeriesBuilder<>(new Owner(), sequence);
    }
    
    /**
     * Create a builder which uses the underlying sequence to build instead of
     * relying on it's supplied builder. This is useful when developing a
     * sequence type that doesn't have its own builder.
     */
    public static <E> SequenceBuilder<E> wrappedBuilder(Sequence<E> sequence) {
        return new WrappedSequenceBuilder<>(new Owner(), sequence);
    }
    
    /**
     * Create a builder which uses the underlying dictionary to build instead of
     * relying on it's supplied builder. This is useful when developing a
     * dictionary type that doesn't have its own builder.
     */
    public static <K,V> DictionaryBuilder<K,V> wrappedBuilder(Dictionary<K,V> dict) {
        return new WrappedDictionaryBuilder<>(new Owner(), dict);
    }

    /**
     * An empty cursor.
     */
    @SuppressWarnings("unchecked")
    public static <E> Cursor<E> emptyCursor() {
        return (Cursor<E>) EMPTY_CURSOR;
    }
    
    
    /**
     * Return a set that is backed by the underlying dictionary. The dictionary
     * type <em>must</em> allow adding any value type.
     */
    @SuppressWarnings("unchecked")
    /*package*/static <K> FiniteSet<K> setFromDictionary(final Dictionary<K, ?> dictionary) {
        return new WrappedDictionarySet<K>((Dictionary<K, Object>)dictionary);
    }
}
