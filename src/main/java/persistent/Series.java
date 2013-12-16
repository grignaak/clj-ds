package persistent;

import java.util.Collection;
import java.util.NoSuchElementException;

public interface Series<E> extends Container<E> {

    public interface SeriesBuilder<E> extends ContainerBuilder<E> {
        /**
         * Create or return a builder minus the next element in the series.
         * Unlike a series, if the builder is empty no exception is thrown but
         * an empty builder is returned.
         */
        SeriesBuilder<E> minus();
        
        /** {@inheritDoc} */
        @Override Series<E> build();
        
        /** {@inheritDoc} */
        @Override SeriesBuilder<E> zero();
        
        /** {@inheritDoc} */
        @Override SeriesBuilder<E> plusAll(Collection<? extends E> more);
    
        
        /** {@inheritDoc} */
        @Override SeriesBuilder<E> plus(E e);
        
    }

    /**
     * Create or return a Series which minus the element which is returned
     * by {@link #peek()}
     * 
     * @throws IllegalStateException
     *             if the series is {@link #isEmpty() empty}.
     */
    Series<E> minus();

    /**
     * Return the next element in the series.
     * 
     * @throws NoSuchElementException
     *             if the series is {@link #isEmpty() empty}.
     */
    E peek();

    @Override Cursor<E> cursor();

    /** {@inheritDoc} */
    @Override Series<E> plus(E e);

    /** {@inheritDoc} */
    @Override Series<E> plusAll(Collection<? extends E> more);

    /** {@inheritDoc} */
    @Override Series<E> zero();

    /** {@inheritDoc} */
    @Override SeriesBuilder<E> asBuilder();

}
