package com.github.krukow.clj_ds;

public interface PersistentSequence<E> extends PersistentCollection<E> {

    /**
     * @return A new sequence consisting of the current sequence without its
     *         first element.
     */
    PersistentSequence<E> minus();

    /**
     * @return The first element of this sequence.
     */
    E peek();
}
