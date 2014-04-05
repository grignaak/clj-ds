package persistent;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import persistent.AbstractBuilder.Owner;

public abstract class AbstractDictionary<K, V> extends AbstractMap<K,V> implements Dictionary<K, V> {
    
    public static abstract class AbstractDictionaryBuilder<K, V> extends AbstractBuilder implements DictionaryBuilder<K,V> {
        
        public AbstractDictionaryBuilder(Owner owner) {
            super(owner);
        }
    }
    
    @Deprecated @Override public final V put(K key, V value) { throw AbstractContainer.mutate(); }
    @Deprecated @Override public final void putAll(Map<? extends K, ? extends V> m)  { throw AbstractContainer.mutate(); }
    @Deprecated @Override public final V remove(Object key)  { throw AbstractContainer.mutate(); }
    @Deprecated @Override public void clear() { throw AbstractContainer.mutate(); }
    
    @Override
    public Container<V> values() {
        return new DefaultValueContainer<V>(entrySet());
    }
    
    @Override()
    public abstract FiniteSet<K> keySet();
    
    @Override
    public boolean containsKey(Object key) {
        return keySet().contains(key);
    }
    
    @Override
    public ImmutableIterator<Map.Entry<K, V>> iterator() {
        return entrySet().iterator();
    }
    
    @Override
    public Cursor<Entry<K, V>> cursor() {
        return entrySet().cursor();
    }
}


class DefaultValueContainer<V> extends AbstractContainer<V> {
    private final FiniteSet<? extends Map.Entry<?, V>> entries;
    
    DefaultValueContainer(FiniteSet<? extends Map.Entry<?,V>> entries) {
        this.entries = entries;
    }

    @Override
    public Container<V> plus(V e) {
        return new Cons<V>(e, this);
    }

    @Override
    public Container<V> zero() {
        return TrieVector.emptyVector();
    }

    @Override
    public persistent.Container.ContainerBuilder<V> asBuilder() {
        return new WrappedContainerBuilder<V>(new Owner(), this);
    }

    @Override
    public Cursor<V> cursor() {
        return new ValueCursor<>(entries.cursor());
    }

    @Override
    public ImmutableIterator<V> iterator() {
        return new ValueIterator<V>(entries.iterator());
    }

    @Override
    public int size() {
        return entries.size();
    }
}





class ValueCursor<V> extends AbstractCursor<V> {
    private final Cursor<? extends Entry<?, V>> entries;
    
    ValueCursor(Cursor<? extends Entry<?, V>> entries) {
        this.entries = entries;
    }
    
    @Override
    public boolean isDone() {
        return entries.isDone();
    }

    @Override
    public Cursor<V> tail() {
        return new ValueCursor<V>(entries.tail());
    }

    @Override
    public V head() {
        return entries.head().getValue();
    }
}

class ValueIterator<V> extends AbstractImmutableIterator<V> {
    private final Iterator<? extends Map.Entry<?,V>> iterator;
    
    ValueIterator(Iterator<? extends Map.Entry<?,V>> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public V next() {
        return iterator.next().getValue();
    }
}






class KeyCursor<K> extends AbstractCursor<K> {
    private final Cursor<? extends Entry<K, ?>> entries;
    
    KeyCursor(Cursor<? extends Entry<K, ?>> entries) {
        this.entries = entries;
    }
    
    @Override
    public boolean isDone() {
        return entries.isDone();
    }

    @Override
    public Cursor<K> tail() {
        return new KeyCursor<K>(entries.tail());
    }

    @Override
    public K head() {
        return entries.head().getKey();
    }
}

class KeyIterator<K> extends AbstractImmutableIterator<K> {
    private final Iterator<? extends Map.Entry<K,?>> iterator;
    
    KeyIterator(Iterator<? extends Map.Entry<K,?>> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public K next() {
        return iterator.next().getKey();
    }
}