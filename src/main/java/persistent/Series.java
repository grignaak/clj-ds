package persistent;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

import persistent.Series.SeriesBuilder;

public abstract class Series<E> extends Container<E> {
    public static abstract class SeriesBuilder<E> extends ContainerBuilder<E> {
        SeriesBuilder(AtomicReference<Thread> owner) { super(owner); }
        
        /** {@inheritDoc} */
        @Override public abstract SeriesBuilder<E> plus(E e);
        
        /** {@inheritDoc} */
        @Override
        public SeriesBuilder<E> plusAll(Collection<? extends E> more) {
            return (SeriesBuilder<E>) super.plusAll(more);
        }
        
        /** {@inheritDoc} */
        @Override public abstract SeriesBuilder<E> zero();
        
        /** {@inheritDoc} */
        @Override public abstract Series<E> build();

        /**
         * Create or return a builder minus the next element in the series.
         * Unlike a series, if the builder is empty no exception is thrown but
         * an empty builder is returned.
         */
        public abstract SeriesBuilder<E> minus();
    }
    
    Series() {/* If you see me, I know you're implemented correctly. */ }
    
    /**
     * Create or return a Series which minus the element which is returned
     * by {@link #peek()}
     * 
     * @throws IllegalStateException
     *             if the series is {@link #isEmpty() empty}.
     */
    public abstract Series<E> minus();
    
    /**
     * Return the next element in the series.
     * 
     * @throws NoSuchElementException
     *             if the series is {@link #isEmpty() empty}.
     */
    public abstract E peek();
    
    @Override
    public Cursor<E> cursor() {
        return new SeriesCursor<>(this);
    }
    
    
    /** {@inheritDoc} */
    @Override public abstract Series<E> plus(E e);
    
    /** {@inheritDoc} */
    @Override
    public Series<E> plusAll(Collection<? extends E> more) {
        return (Series<E>) super.plusAll(more);
    }
    
    /** {@inheritDoc} */
    @Override public abstract Series<E> zero();
    
    /** {@inheritDoc} */
    @Override public abstract SeriesBuilder<E> asBuilder();
}


class SeriesCursor<E> extends Cursor<E> {
    private final Series<E> impl;
    
    SeriesCursor(Series<E> impl) {
        this.impl = impl;
    }

    @Override
    public boolean isDone() {
        return impl.isEmpty();
    }

    @Override
    public Cursor<E> tail() {
        if (isDone())
            return emptyCursor();
        return new SeriesCursor<>(impl.minus());
    }

    @Override
    public E head() {
        return impl.peek();
    }
}



class WrappedSeriesBuilder<E> extends Series.SeriesBuilder<E> {
    private final Series<E> result;

    WrappedSeriesBuilder(AtomicReference<Thread> owner, Series<E> series) {
        super(owner);
        this.result = series;
    }
    
    @Override
    public SeriesBuilder<E> plus(E e) {
        ensureEditable();
        return new WrappedSeriesBuilder<>(owner, result.plus(e));
    }

    @Override
    public SeriesBuilder<E> plusAll(Collection<? extends E> more) {
        ensureEditable();
        throw new RuntimeException("Unimplemented: SeriesBuilder<E>.plusAll");
    }

    @Override
    public SeriesBuilder<E> zero() {
        ensureEditable();
        return new WrappedSeriesBuilder<>(owner, result.zero());
    }

    @Override
    public Series<E> build() {
        ensureEditable();
        return built(result);
    }

    @Override
    public SeriesBuilder<E> minus() {
        ensureEditable();
        return result.isEmpty() ? this : new WrappedSeriesBuilder<>(owner, result.minus());
    }
}


class SeriesIterator<E> extends ImmutableIterator<E> {
    private Series<E> current;
    
    SeriesIterator(Series<E> start) {
        current = start;
    }
    @Override
    public boolean hasNext() {
        return !current.isEmpty();
    }

    @Override
    public E next() {
        if (current.isEmpty())
            throw new NoSuchElementException();
        
        E head = current.peek();
        current = current.minus();
        return head;
    }
    
}