package persistent;

import java.util.Map;

public interface Dictionary<K, V> extends Map<K,V>, Traversable<Map.Entry<K,V>> {

    public interface DictionaryBuilder<K, V> {
        DictionaryBuilder<K, V> plus(K key, V value);
        DictionaryBuilder<K, V> plusIfAbsent(K key, V value);
        
        DictionaryBuilder<K, V> replace(K key, V expected, V actual);
        
        DictionaryBuilder<K, V> minus(K key);
        DictionaryBuilder<K, V> minus(K key, V expected);

        DictionaryBuilder<K, V> zero();
        
        Dictionary<K,V> build();
    }
    
    
    // TODO plusAll
    Dictionary<K, V> plus(K key, V value);
    Dictionary<K, V> plusIfAbsent(K key, V value);

    Dictionary<K, V> replace(K key, V expected, V actual);
    
    // TODO minusAll/retainAll
    Dictionary<K, V> minus(K key);
    Dictionary<K, V> minus(K key, V expected);
    
    Dictionary<K, V> zero();
    
    DictionaryBuilder<K,V> asBuilder();
    
    @Deprecated @Override V put(K key, V value);
    @Deprecated @Override void putAll(Map<? extends K, ? extends V> m);
    @Deprecated @Override V remove(Object key);
    @Deprecated @Override void clear();
    
    @Override FiniteSet<Entry<K, V>> entrySet();
    @Override FiniteSet<K> keySet();
    @Override Container<V> values();
}
