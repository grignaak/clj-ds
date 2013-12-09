package com.github.krukow.clj_ds;

import java.util.Collection;

public interface ThouShaltNotMutateThisCollection<T> extends Collection<T> {
    @Override @Deprecated public boolean add(T e);
    @Override @Deprecated public boolean addAll(Collection<? extends T> c);
    @Override @Deprecated public boolean remove(Object o);
    @Override @Deprecated public boolean removeAll(Collection<?> c);
    @Override @Deprecated public void clear();
    
}
