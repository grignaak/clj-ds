package persistent;

import java.util.Map;

public abstract class Dictionary<K, V> implements Map<K, V> {
    
    public static abstract class DictionaryBuilder<K, V> {
        DictionaryBuilder() {/* Blessed implementations only */}
        public abstract DictionaryBuilder<K, V> plus(K key, V value);
        public abstract DictionaryBuilder<K, V> plusIfAbsent(K key, V value);
        
        public abstract DictionaryBuilder<K, V> replace(K key, V expected, V actual);
        
        public abstract DictionaryBuilder<K, V> minus(K key);
        public abstract DictionaryBuilder<K, V> minus(K key, V expected);

        public abstract DictionaryBuilder<K, V> zero();
        
        public abstract Dictionary<K,V> build();
    }
    
    Dictionary() {/* Can't fake immutability */}
    
    public abstract Dictionary<K, V> plus(K key, V value);
    public abstract Dictionary<K, V> plusIfAbsent(K key, V value);

    public abstract Dictionary<K, V> replace(K key, V expected, V actual);
    
    public abstract Dictionary<K, V> minus(K key);
    public abstract Dictionary<K, V> minus(K key, V expected);
    
    public abstract Dictionary<K, V> zero();
    
    public abstract DictionaryBuilder<K,V> asBuilder();
    
    @Deprecated @Override public final V put(K key, V value) { throw Container.mutate(); }
    @Deprecated @Override public final void putAll(Map<? extends K, ? extends V> m)  { throw Container.mutate(); }
    @Deprecated @Override public final V remove(Object key)  { throw Container.mutate(); }
    
    @Override public abstract FiniteSet<Entry<K, V>> entrySet();
    @Override public abstract FiniteSet<K> keySet();
    @Override public abstract Container<V> values();
}