package com.github.krukow.clj_ds;

import com.github.krukow.clj_lang.Sorted;

public interface PersistentSortedSet<E> extends PersistentSet<E>, Sorted<E> /*, SortedSet<E> */ {

    @Override PersistentSortedSet<E> zero();
	
	@Override PersistentSortedSet<E> plus(E o);

	@Override PersistentSortedSet<E> minus(E key);

}
