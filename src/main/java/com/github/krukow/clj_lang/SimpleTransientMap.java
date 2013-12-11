package com.github.krukow.clj_lang;

import java.util.AbstractMap;
import java.util.Set;

import com.github.krukow.clj_ds.PersistentMap;
import com.github.krukow.clj_ds.TransientMap;

public class SimpleTransientMap<K, V> extends AbstractMap<K, V> implements TransientMap<K, V> {
    protected final PersistentMap<K,V> impl;
    
    private SimpleTransientMap(PersistentMap<K, V> impl) {
        this.impl = impl;
    }
    
    public static <K, V> SimpleTransientMap<K, V> wrap(PersistentMap<K, V> impl) {
        return new SimpleTransientMap<>(impl);
    }
    
    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return impl.entrySet();
    }

    @Override
    public TransientMap<K, V> plus(K key, V val) {
        return wrap(impl.plus(key, val));
    }

    @Override
    public TransientMap<K, V> minus(K key) {
        return wrap(impl.minus(key));
    }

    @Override
    public PersistentMap<K, V> persist() {
        return impl;
    }
}
