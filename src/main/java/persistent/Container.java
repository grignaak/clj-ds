package persistent;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A persistent, immutable container of elements of type E.
 * <p>
 * A persistent container is a data structure that always preserves the previous
 * version of itself when it is modified. (A persistent data structure is
 * <em>not</em> one that is committed to <em>persistent storage</em>. Because
 * most operations make only small changes to the container, many
 * implementations share structure with the prior version. This often reduces
 * memory overhead and runtime for copying the structure.
 * <p>
 * Containers are thread safe, but their iterators may not be.
 * 
 * <h3>Builders</h3>
 * 
 * Container implementations provide builders which employ the same
 * structure-sharing and runtime characteristics as the container itself. The
 * builder <em>may</em> update the data structure in-place as long as prior
 * versions of the structure is left intact. If a container does not provide an
 * optimized builder, it typically provides a builder that is merely a wrapper
 * around a Container.
 * <p>
 * A builder is <em>not</em> thread-safe and disallows access to any thread
 * except the thread which created it. A builder implements {@link Collection}
 * methods to support external code querying it, but it should only be used to
 * locally build up a container to avoid leaking the builder to another thread.
 * <p>
 * Once a builder builds its Container, it may not be used anymore.
 */
public abstract class Container<E> extends AbstractCollection<E> implements Collection<E> {
    /**
     * A builder for a Container, which employs the same structure sharing and
     * runtime qualities as the container it builds.
     * 
     * <p>
     * A builder <em>may</em> update the data structure in-place as long as
     * prior versions of the structure is left intact. If not, it is typically
     * merely a wrapper around around a Container.
     * <p>
     * A builder is <em>not</em> thread-safe and disallows access to any thread
     * except the thread which created it. A builder implements
     * {@link Collection} methods to support external code querying it, but it
     * should only be used to locally build up a container to avoid leaking the
     * builder to another thread.
     * <p>
     * Once a builder builds its Container, it may not be used anymore. Results
     * are undefined if more than one method on a builder is called.
     */
    public static abstract class ContainerBuilder<E> {
        protected final AtomicReference<Thread> owner;
        
        ContainerBuilder(AtomicReference<Thread> owner) {
            this.owner = owner;
        }
        
        protected void ensureEditable() {
            final Thread thread = owner.get();
            if (thread == Thread.currentThread())
                return;
            if (thread != null)
                throw new IllegalAccessError("Builder used by non-owner thread");
            throw new IllegalAccessError("Builder used after persistent! call");
        }
        
        protected <Cont extends Container<E>> Cont built(Cont result) {
            owner.set(null);
            return result;
        }
        
        /**
         * Create or return a builder with the element added to it. (Note: some
         * set-like builder may not allow duplicates, and the returned builder
         * may be equivalent to the prior builder.)
         */
        public abstract ContainerBuilder<E> plus(E e);
        
        /**
         * Create or return a builder with the elements added to it. (Note: some
         * set-like builder may not allow duplicates, and the returned builder
         * may be equivalent to the prior builder.)
         */
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
        public abstract ContainerBuilder<E> zero();
        
        /**
         * Build the container. Once called, this builder may no longer be used.
         */
        public abstract Container<E> build();
    }
    
    public Container() {/* Don't want anyone faking immutability */}
    
    /**
     * Create or return a container with the element added to it. (Note: some
     * set-like containers may not allow duplicates, and the returned container
     * may be equivalent to the prior container.)
     */
    public abstract Container<E> plus(E e);
    
    /**
     * Create or return container with the elements added to it. (Note: some
     * set-like containers may not allow duplicates, and the returned container
     * may be equivalent to the prior container.)
     */
    public Container<E> plusAll(Collection<? extends E> more) {
        return asBuilder().plusAll(more).build();
    }
    
    /**
     * Create or return a container without any elements, but with the same
     * runtime characteristics as the this one.
     */
    public abstract Container<E> zero();
    
    /**
     * Create a builder for a container of this type.
     */
    public abstract ContainerBuilder<E> asBuilder();
    
    /**
     * Create or return a persistent cursor that traverses this container.
     */
    public abstract Cursor<E> cursor();
    
    
    @Deprecated @Override public final boolean add(E e) { throw mutate(); }
    @Deprecated @Override public final boolean addAll(Collection<? extends E> c) { throw mutate(); }
    @Deprecated @Override public final void clear() { throw mutate(); }
    @Deprecated @Override public final boolean remove(Object o) { throw mutate(); }
    @Deprecated @Override public final boolean removeAll(Collection<?> c) { throw mutate(); }
    @Deprecated @Override public final boolean retainAll(Collection<?> c) { throw mutate(); }
    @Override
    public abstract ImmutableIterator<E> iterator();
    
    static final RuntimeException mutate() {
        throw new UnsupportedOperationException("Mutator method not allowed on an immutable collection");
    }
    
    static final AtomicReference<Thread> currentThread() { return new AtomicReference<>(Thread.currentThread()); }
}