package persistent;

import java.util.Collection;
import java.util.NoSuchElementException;

public abstract class AbstractSeries<E> extends AbstractContainer<E> implements Series<E> {
    public static abstract class AbstractSeriesBuilder<E> extends AbstractContainer.AbstractContainerBuilder<E> implements Series.SeriesBuilder<E> {
        public AbstractSeriesBuilder(Owner owner) {
            super(owner);
        }

        @Override
        public Series.SeriesBuilder<E> plusAll(Collection<? extends E> more) {
            return (Series.SeriesBuilder<E>) super.plusAll(more);
        }
    }

    public static class SeriesCursor<E> extends AbstractCursor<E> {
        private final Series<E> impl;

        public SeriesCursor(Series<E> impl) {
            this.impl = impl;
        }

        @Override
        public boolean isDone() {
            return impl.isEmpty();
        }

        @Override
        public Cursor<E> tail() {
            if (isDone())
                return Containers.emptyCursor();
            return new SeriesCursor<>(impl.minus());
        }

        @Override
        public E head() {
            return impl.peek();
        }
    }

    public static class SeriesIterator<E> extends AbstractImmutableIterator<E> {
        private Series<E> current;

        public SeriesIterator(Series<E> start) {
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

    @Override
    public Cursor<E> cursor() {
        return new SeriesCursor<>(this);
    }

    /** {@inheritDoc} */
    @Override
    public Series<E> plusAll(Collection<? extends E> more) {
        return (Series<E>) super.plusAll(more);
    }

}