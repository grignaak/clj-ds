package persistent;

import java.util.Collection;
import java.util.List;

public interface Sequence<E> extends Series<E>, List<E> {
    interface SequenceBuilder<E> extends SeriesBuilder<E> {
        @Override Sequence<E> build();
        @Override SequenceBuilder<E> zero();
        @Override SequenceBuilder<E> minus();
        @Override SequenceBuilder<E> plusAll(Collection<? extends E> more);
        SequenceBuilder<E> replace(int index, E e);
        @Override SequenceBuilder<E> plus(E e);
        
    }
    
    public static abstract class AbstractSequenceBuilder<E> extends AbstractSeries.AbstractSeriesBuilder<E> implements SequenceBuilder<E> {
        AbstractSequenceBuilder(Owner owner) { super(owner); }
        
        @Override
        public SequenceBuilder<E> plusAll(Collection<? extends E> more) {
            return (SequenceBuilder<E>)super.plusAll(more);
        }
    }
    
    @Override Sequence<E> plus(E e);
    Sequence<E> replace(int index, E e);
    
    @Override Sequence<E> plusAll(Collection<? extends E> more);
    
    @Override Sequence<E> minus();
    
    @Override Sequence<E> zero();
    @Override SequenceBuilder<E> asBuilder();
    
    @Override ImmutableListIterator<E> listIterator();
    @Override ImmutableListIterator<E> listIterator(int index);
    
    @Deprecated @Override void add(int index, E element);
    @Deprecated @Override E remove(int index);
    @Deprecated @Override boolean addAll(int index, Collection<? extends E> c);
    @Deprecated @Override E set(int index, E element);
}