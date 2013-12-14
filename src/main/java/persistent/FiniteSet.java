package persistent;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public abstract class FiniteSet<E> extends Container<E> implements Set<E> {
    public static abstract class FiniteSetBuilder<E> extends ContainerBuilder<E> {
        FiniteSetBuilder(AtomicReference<Thread> owner) { super(owner); }
        @Override public abstract FiniteSetBuilder<E> plus(E e);
        @Override public abstract FiniteSetBuilder<E> plusAll(Collection<? extends E> more);
        
        public abstract FiniteSetBuilder<E> difference(Collection<? extends E> minus);
        public abstract FiniteSetBuilder<E> symmetricDifference(Collection<? extends E> minus);
        public abstract FiniteSetBuilder<E> intersection(Collection<? extends E> other);
        
        public abstract FiniteSetBuilder<E> minus(E value);
        @Override public abstract FiniteSetBuilder<E> zero();
        
        @Override public abstract FiniteSet<E> build();
    }
    
    
    FiniteSet() {/* masterlock */}
    
    @Override public abstract FiniteSet<E> plus(E e);
    @Override public abstract FiniteSet<E> plusAll(Collection<? extends E> more);
    
    public abstract FiniteSet<E> difference(Collection<? extends E> minus);
    public abstract FiniteSet<E> symmetricDifference(Collection<? extends E> minus);
    public abstract FiniteSet<E> intersection(Collection<? extends E> other);
    
    public abstract FiniteSet<E> minus(E value);
    @Override public abstract FiniteSet<E> zero();
    
    @Override public abstract FiniteSetBuilder<E> asBuilder();
}