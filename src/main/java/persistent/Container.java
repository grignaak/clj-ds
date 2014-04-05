package persistent;

import java.util.Collection;

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
public interface Container<E> extends Collection<E>, Traversable<E> {

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
    interface ContainerBuilder<E> {

        /**
         * Build the container. Once called, this builder may no longer be used.
         */
        Container<E> build();

        /**
         * Create or return a builder without any elements, but with the same
         * runtime characteristics as the this one.
         */
        ContainerBuilder<E> zero();


        /**
         * Create or return a builder with the elements added to it. (Note: some
         * set-like builder may not allow duplicates, and the returned builder
         * may be equivalent to the prior builder.)
         */
        ContainerBuilder<E> plusAll(Collection<? extends E> more);

        /**
         * Create or return a builder with the element added to it. (Note: some
         * set-like builder may not allow duplicates, and the returned builder
         * may be equivalent to the prior builder.)
         */
        ContainerBuilder<E> plus(E e);
        
    }
    
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
    public abstract Container<E> plusAll(Collection<? extends E> more);

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

    @Override public abstract ImmutableIterator<E> iterator();
    
    @Deprecated @Override public abstract boolean add(E e);
    @Deprecated @Override public abstract boolean addAll(Collection<? extends E> c);
    @Deprecated @Override public abstract void clear();
    @Deprecated @Override public abstract boolean remove(Object o);
    @Deprecated @Override public abstract boolean removeAll(Collection<?> c);
    @Deprecated @Override public abstract boolean retainAll(Collection<?> c);
}
