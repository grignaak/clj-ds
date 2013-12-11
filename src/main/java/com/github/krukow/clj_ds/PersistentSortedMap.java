package com.github.krukow.clj_ds;

import java.util.Comparator;


public interface PersistentSortedMap<K, V> extends PersistentMap<K, V> /* , SortedMap<K, V> */{

    @Override PersistentSortedMap<K, V> zero();

    @Override PersistentSortedMap<K, V> plus(K key, V val);

    @Override PersistentSortedMap<K, V> plusEx(K key, V val);

    @Override PersistentSortedMap<K, V> minus(K key);
    
    Comparator<K> comparator();

}
