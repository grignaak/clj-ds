package com.github.krukow.clj_ds;

import java.util.Map;

interface ThouShaltNotMutateThisMap<K, V> extends Map<K,V> {
    @Override @Deprecated
    public V put(K key, V value);
    
    @Override @Deprecated
    public void putAll(Map<? extends K, ? extends V> m);
    
    @Override @Deprecated
    public V remove(Object key);
    
    @Override @Deprecated
    public void clear();
}
