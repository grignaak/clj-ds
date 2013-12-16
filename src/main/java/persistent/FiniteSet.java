package persistent;

import java.util.Collection;
import java.util.Set;

public interface FiniteSet<E> extends Container<E>, Set<E> {
    public interface FiniteSetBuilder<E> extends Container.ContainerBuilder<E> {

        @Override FiniteSetBuilder<E> plus(E e);
        
        @Override
        FiniteSetBuilder<E> plusAll(Collection<? extends E> more);
        
        // TODO future implementation
//        public abstract FiniteSetBuilder<E> difference(Collection<? extends E> minus);
//        public abstract FiniteSetBuilder<E> symmetricDifference(Collection<? extends E> minus);
//        public abstract FiniteSetBuilder<E> intersection(Collection<? extends E> other);
        
        FiniteSetBuilder<E> minus(E value);
        @Override FiniteSetBuilder<E> zero();
        
        @Override FiniteSet<E> build();
    }
    
    @Override public abstract FiniteSet<E> plus(E e);
    
    @Override
    FiniteSet<E> plusAll(Collection<? extends E> more);
    
    // TODO future implementation
//    public abstract FiniteSet<E> difference(Collection<? extends E> minus);
//    public abstract FiniteSet<E> symmetricDifference(Collection<? extends E> minus);
//    public abstract FiniteSet<E> intersection(Collection<? extends E> other);
    
    FiniteSet<E> minus(E value);
    @Override FiniteSet<E> zero();
    @Override FiniteSetBuilder<E> asBuilder();
}