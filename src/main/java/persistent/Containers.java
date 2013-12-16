package persistent;

import java.util.Collection;
import java.util.NoSuchElementException;

import persistent.AbstractBuilder.Owner;
import persistent.AbstractDictionary.AbstractDictionaryBuilder;
import persistent.Dictionary.DictionaryBuilder;
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

    private static final Cursor<?> EMPTY = new AbstractCursor<Object>() {
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

    @SuppressWarnings("unchecked")
    public static <E> Cursor<E> emptyCursor() {
        return (Cursor<E>) EMPTY;
    }
}
