package persistent;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.concurrent.atomic.AtomicReference;

import persistent.Sequence.SequenceBuilder;

public abstract class Sequence<E> extends Series<E> implements List<E> {
    public static abstract class SequenceBuilder<E> extends SeriesBuilder<E> {
        SequenceBuilder(AtomicReference<Thread> owner) { super(owner); }
        
        @Override public abstract SequenceBuilder<E> plus(E e);
        public abstract SequenceBuilder<E> replace(int index, E e);
        
        @Override
        public SequenceBuilder<E> plusAll(Collection<? extends E> more) {
            return (SequenceBuilder<E>)super.plusAll(more);
        }

        @Override public abstract SequenceBuilder<E> minus();
    
        @Override public abstract SequenceBuilder<E> zero();
        @Override public abstract Sequence<E> build();
    }
    
    Sequence() {/* Immutable? I guarantee it */}

    @Override public abstract Sequence<E> plus(E e);
    public abstract Sequence<E> replace(int index, E e);
    
    @Override
    public Sequence<E> plusAll(Collection<? extends E> more) {
        return (Sequence<E>) super.plusAll(more);
    }
    
    @Override public abstract Sequence<E> minus();
    
    @Override public abstract Sequence<E> zero();
    @Override public abstract SequenceBuilder<E> asBuilder();
    
    @Override
    public ImmutableListIterator<E> listIterator() {
        return listIterator(0);
    }
    
    @Override public abstract ImmutableListIterator<E> listIterator(int index);
    
    @Override
    public int indexOf(Object o) {
        ListIterator<E> it = listIterator();
        if (o==null) {
            while (it.hasNext())
                if (it.next()==null)
                    return it.previousIndex();
        } else {
            while (it.hasNext())
                if (o.equals(it.next()))
                    return it.previousIndex();
        }
        return -1;
    }
    
    @Override
    public int lastIndexOf(Object o) {
        ListIterator<E> it = listIterator(size());
        if (o==null) {
            while (it.hasPrevious())
                if (it.previous()==null)
                    return it.nextIndex();
        } else {
            while (it.hasPrevious())
                if (o.equals(it.previous()))
                    return it.nextIndex();
        }
        return -1;
    }
    
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return (this instanceof RandomAccess) ?
                new RandomAccessSubSequence<>(this, fromIndex, toIndex) :
                new SubSequence<>(this, fromIndex, toIndex);
    }
    

    protected static void rangeCheck(int index, int size) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index, size));
    }

    protected static void rangeCheckInclusive(int index, int size) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index, size));
    }

    private static String outOfBoundsMsg(int index, int size) {
        return "Index: "+index+", Size: "+size;
    }
    
    @Deprecated @Override public final void add(int index, E element) { throw mutate(); }
    @Deprecated @Override public final E remove(int index) { throw mutate(); }
    @Deprecated @Override public final boolean addAll(int index, Collection<? extends E> c) { throw mutate(); }
    @Deprecated @Override public final E set(int index, E element) { throw mutate(); }
}




class WrappedSequenceBuilder<E> extends SequenceBuilder<E> {

    private final Sequence<E> result;
    
    WrappedSequenceBuilder(AtomicReference<Thread> owner, Sequence<E> sequence) {
        super(owner);
        this.result = sequence;
    }
    
    @Override
    public SequenceBuilder<E> plus(E e) {
        ensureEditable();
        return new WrappedSequenceBuilder<>(owner, result.plus(e));
    }

    @Override
    public SequenceBuilder<E> replace(int index, E e) {
        ensureEditable();
        return new WrappedSequenceBuilder<>(owner, result.replace(index, e));
    }

    @Override
    public SequenceBuilder<E> plusAll(Collection<? extends E> more) {
        ensureEditable();
        return new WrappedSequenceBuilder<>(owner, result.plusAll(more));
    }

    @Override
    public SequenceBuilder<E> minus() {
        ensureEditable();
        return result.isEmpty() ? this : new WrappedSequenceBuilder<>(owner, result.minus());
    }

    @Override
    public SequenceBuilder<E> zero() {
        ensureEditable();
        return new WrappedSequenceBuilder<>(owner, result.zero());
    }

    @Override
    public Sequence<E> build() {
        ensureEditable();
        return built(result);
    }
    
}





class SubSequence<E> extends AbstractList<E> {
    protected final List<E> backingList;
    protected final int offset;
    protected int size;

    SubSequence(List<E> list, int fromIndex, int toIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > list.size())
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                               ") > toIndex(" + toIndex + ")");
        backingList = list;
        offset = fromIndex;
        size = toIndex - fromIndex;
    }

    public E get(int index) {
        Sequence.rangeCheck(index, size);
        return backingList.get(index+offset);
    }

    public int size() {
        return size;
    }

    public Iterator<E> iterator() {
        return listIterator();
    }

    public ListIterator<E> listIterator(final int index) {
        Sequence.rangeCheckInclusive(index, size);

        return new ImmutableListIterator<E>() {
            private final ListIterator<E> i = backingList.listIterator(index+offset);

            public boolean hasNext() {
                return nextIndex() < size;
            }

            public E next() {
                if (hasNext())
                    return i.next();
                else
                    throw new NoSuchElementException();
            }

            public boolean hasPrevious() {
                return previousIndex() >= 0;
            }

            public E previous() {
                if (hasPrevious())
                    return i.previous();
                else
                    throw new NoSuchElementException();
            }

            public int nextIndex() {
                return i.nextIndex() - offset;
            }

            public int previousIndex() {
                return i.previousIndex() - offset;
            }
        };
    }

    public List<E> subList(int fromIndex, int toIndex) {
        return new SubSequence<>(this, fromIndex, toIndex);
    }
}




class RandomAccessSubSequence<E> extends SubSequence<E> implements RandomAccess {
    RandomAccessSubSequence(List<E> list, int fromIndex, int toIndex) {
        super(list, fromIndex, toIndex);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        return new RandomAccessSubSequence<>(this, fromIndex, toIndex);
    }
}