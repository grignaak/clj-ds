package com.github.krukow.clj_ds;



public interface PersistentSortedSet<E> extends PersistentSet<E> /*, SortedSet<E> */ {

    @Override
    PersistentSortedSet<E> zero();

    @Override
    PersistentSortedSet<E> plus(E o);

    @Override
    PersistentSortedSet<E> minus(E key);

}
