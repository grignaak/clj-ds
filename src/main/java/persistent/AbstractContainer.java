package persistent;

import java.util.AbstractCollection;
import java.util.Collection;


public abstract class AbstractContainer<E> extends AbstractCollection<E> implements Collection<E>, Container<E> {

    public static abstract class AbstractContainerBuilder<E> extends AbstractBuilder implements ContainerBuilder<E> {
        public AbstractContainerBuilder(Owner owner) {
            super(owner);
        }
        
        protected <Cont extends Container<E>> Cont built(Cont result) {
            return owner.built(result);
        }
        
        @Override
        public abstract ContainerBuilder<E> plus(E e);
        
        @Override
        public ContainerBuilder<E> plusAll(Collection<? extends E> more) {
            ContainerBuilder<E> plussed = this;
            for (E item : more) {
                plussed = plussed.plus(item);
            }
            return plussed;
        }
        
        /**
         * Create or return a builder without any elements, but with the same
         * runtime characteristics as the this one.
         */
        @Override
        public abstract ContainerBuilder<E> zero();
        
        /**
         * Build the container. Once called, this builder may no longer be used.
         */
        @Override
        public abstract Container<E> build();
    }
    
    public static class WrappedContainerBuilder<E> extends AbstractContainer.AbstractContainerBuilder<E> {
        private final Container<E> impl;
        
        public WrappedContainerBuilder(Owner owner, Container<E> impl) {
            super(owner);
            this.impl = impl;
        }
        
        @Override
        public ContainerBuilder<E> plus(E e) {
            owner.ensureEditable();
            return new WrappedContainerBuilder<>(owner, impl.plus(e));
        }
    
        @Override
        public ContainerBuilder<E> zero() {
            owner.ensureEditable();
            return new WrappedContainerBuilder<>(owner, impl.zero());
        }
    
        @Override
        public Container<E> build() {
            owner.ensureEditable();
            return built(impl);
        }
    }

    @Override
    public Container<E> plusAll(Collection<? extends E> more) {
        return asBuilder().plusAll(more).build();
    }

    @Deprecated @Override public final boolean add(E e) { throw mutate(); }

    @Deprecated @Override public final boolean addAll(Collection<? extends E> c) { throw mutate(); }
    @Deprecated @Override public final void clear() { throw mutate(); }
    @Deprecated @Override public final boolean remove(Object o) { throw mutate(); }
    @Deprecated @Override public final boolean removeAll(Collection<?> c) { throw mutate(); }
    @Deprecated @Override public final boolean retainAll(Collection<?> c) { throw mutate(); }
    
    static final UnsupportedOperationException mutate() {
        throw new UnsupportedOperationException("Mutator method not allowed on an immutable collection");
    }
}

