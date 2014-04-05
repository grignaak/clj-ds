/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

package persistent;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import persistent.AbstractBuilder.Owner;
import persistent.Containers.IteratorCursor;

/*A persistent rendition of Phil Bagwell's Hash Array Mapped Trie
 * 
 * Uses path copying for persistence HashCollision leaves vs. extended hashing
 * Node polymorphism vs. conditionals No sub-tree pools or root-resizing Any
 * errors are my own */

public class HashDictionary<K, V> extends AbstractDictionary<K, V> implements Dictionary<K, V> {

    private static final class Flag {
        boolean val = false;
    }
    
    private final int count;
    private final INode root;
    private final boolean hasNull;
    private final V nullValue;

    final private static HashDictionary<?,?> EMPTY = new HashDictionary<>(0, null, false, null);
    final private static Object NOT_FOUND = new Object();

    @SuppressWarnings("unchecked")
    final public static <K, V> HashDictionary<K, V> emptyDictionary() {
        return (HashDictionary<K, V>) EMPTY;
    }
    
    public final static <K,V> HashDictionaryBuilder<K,V> newBuilder() {
        return HashDictionary.<K,V>emptyDictionary().asBuilder();
    }
    
    static public <K, V> HashDictionary<K, V> create(Map<? extends K, ? extends V> other) {
        HashDictionaryBuilder<K, V> ret = newBuilder();
        for (Map.Entry<? extends K, ? extends V> e : other.entrySet()) {
            ret = ret.plus(e.getKey(), e.getValue());
        }

        return ret.build();
    }

//    /* @param init {key1,val1,key2,val2,...} */
//    @SuppressWarnings("unchecked")
//    public static <K, V> HashDictionary<K, V> create(Object... init) {
//        DictionaryBuilder<K, V> ret = EMPTY.asTransient();
//        for (int i = 0; i < init.length; i += 2)
//        {
//            K k = (K) init[i];
//            V v = (V) init[i + 1];
//            ret = ret.plus(k, v);
//        }
//        return (HashDictionary<K, V>) ret.build();
//    }
//
//    public static <K, V> HashDictionary<K, V> createWithCheck(Object... init) {
//        DictionaryBuilder<K, V> ret = EMPTY.asTransient();
//        for (int i = 0; i < init.length; i += 2)
//        {
//            ret = ret.plus((K) init[i], (V) init[i + 1]);
//            if (ret.size() != i / 2 + 1)
//                throw new IllegalArgumentException("Duplicate key: " + init[i]);
//        }
//        return (HashDictionary<K, V>) ret.build();
//    }

    HashDictionary(int count, INode root, boolean hasNull, V nullValue) {
        this.count = count;
        this.root = root;
        this.hasNull = hasNull;
        this.nullValue = nullValue;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null)
            return hasNull;
        return (root != null) ? root.find(0, Objects.hashCode(key), key, NOT_FOUND) != NOT_FOUND : false;
    }

    @Override
    public HashDictionary<K, V> plus(K key, V val) {
        if (key == null) {
            if (hasNull && val == nullValue)
                return this;
            return new HashDictionary<K, V>(hasNull ? count : count + 1, root, true, val);
        }
        Flag addedLeaf = new Flag();
        INode newroot = (root == null ? BitmapIndexedNode.EMPTY : root)
                .assoc(0, Objects.hashCode(key), key, val, addedLeaf);
        if (newroot == root)
            return this;
        return new HashDictionary<K, V>(addedLeaf.val ? count : count + 1, newroot, hasNull, nullValue);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        if (key == null)
            return hasNull ? nullValue : null;
        return (V) (root != null ? root.find(0, Objects.hashCode(key), key, null) : null);
    }

    @Override
    public HashDictionary<K, V> plusIfAbsent(K key, V val) {
        if (containsKey(key))
            throw new IllegalStateException("Key already present");
        return plus(key, val);
    }
    
    @Override
    public Dictionary<K, V> replace(K key, V expected, V actual) {
        // TODO unimplemented
        throw new RuntimeException("Unimplemented: HashDictionary.replace");
    }

    public HashDictionary<K, V> minus(K key) {
        if (key == null)
            return hasNull ? new HashDictionary<K, V>(count - 1, root, false, null) : this;
        if (root == null)
            return this;
        INode newroot = root.without(0, Objects.hashCode(key), key);
        if (newroot == root)
            return this;
        return new HashDictionary<K, V>(count - 1, newroot, hasNull, nullValue);
    }
    
    @Override
    public Dictionary<K, V> minus(K key, V expected) {
        // TODO unimplemented
        throw new RuntimeException("Unimplemented: HashDictionary.minus");
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public HashDictionary<K,V> zero() {
        return emptyDictionary();
    }

    private static int mask(int hash, int shift) {
        // return ((hash << shift) >>> 27);// & 0x01f;
        return (hash >>> shift) & 0x01f;
    }

    @Override
    public HashDictionaryBuilder<K,V> asBuilder() {
        return new HashDictionaryBuilder<K,V>(this);
    }

    private static class Iter<K,V> implements ImmutableIterator<Map.Entry<K, V>> {
        private final Iterator<Map.Entry<K, V>> i;
        private final V nullValue;
        private boolean nullReady = true;

        private Iter(Iterator<Entry<K, V>> s, V nullValue) {
            this.i = s;
            this.nullValue = nullValue;
        }

        @Override
        public boolean hasNext() {
            return nullReady || i.hasNext();
        }

        @Override
        public Map.Entry<K, V> next() {
            if (nullReady) {
                nullReady = false;
                return new AbstractMap.SimpleImmutableEntry<>(null, nullValue);
            }
            return i.next();
        }

        @Override @Deprecated
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class HashDictionaryBuilder<K, V> extends AbstractDictionaryBuilder<K, V> implements DictionaryBuilder<K, V> {
        private INode root;
        private int count;
        private boolean hasNull;
        private V nullValue;
        private final Flag leafFlag = new Flag();

        private HashDictionaryBuilder(HashDictionary<K, V> m) {
            this(m.root, m.count, m.hasNull, m.nullValue);
        }

        private HashDictionaryBuilder(INode root, int count, boolean hasNull, V nullValue) {
            super(new Owner());
            this.root = root;
            this.count = count;
            this.hasNull = hasNull;
            this.nullValue = nullValue;
        }

        @Override
        public HashDictionaryBuilder<K, V> plus(K key, V val) {
            owner.ensureEditable();
            if (key == null) {
                if (this.nullValue != val)
                    this.nullValue = (V) val;
                if (!hasNull) {
                    this.count++;
                    this.hasNull = true;
                }
                return this;
            }
            // Box leafFlag = new Box(null);
            leafFlag.val = false;
            INode n = (root == null ? BitmapIndexedNode.EMPTY : root)
                    .assoc(owner, 0, Objects.hashCode(key), key, val, leafFlag);
            if (n != this.root)
                this.root = n;
            if (leafFlag.val) this.count++;
            return this;
        }
        
        @Override
        public HashDictionaryBuilder<K, V> plusIfAbsent(K key, V value) {
            owner.ensureEditable();
            // TODO unimplemented
            throw new RuntimeException("Unimplemented: HashDictionaryBuilder.plusIfAbsent");
        }
        
        @Override
        public HashDictionaryBuilder<K, V> replace(K key, V expected, V actual) {
            owner.ensureEditable();
            // TODO unimplemented
            throw new RuntimeException("Unimplemented: HashDictionaryBuilder.replace");
        }

        @Override
        public HashDictionaryBuilder<K, V> minus(K key) {
            owner.ensureEditable();
            if (key == null) {
                if (!hasNull) return this;
                hasNull = false;
                nullValue = null;
                this.count--;
                return this;
            }
            if (root == null) return this;

            leafFlag.val = false;
            INode n = root.without(owner, 0, Objects.hashCode(key), key, leafFlag);
            if (n != root)
                this.root = n;
            if (leafFlag.val) this.count--;
            return this;
        }
        
        @Override
        public HashDictionaryBuilder<K, V> minus(K key, V expected) {
            // TODO unimplemented
            throw new RuntimeException("Unimplemented: HashDictionaryBuilder.minus");
        }
        
        @Override
        public HashDictionaryBuilder<K, V> zero() {
            owner.ensureEditable();
            // TODO unimplemented
            throw new RuntimeException("Unimplemented: HashDictionaryBuilder.zero");
        }

        @Override
        public HashDictionary<K, V> build() {
            owner.ensureEditable();
            return owner.built(new HashDictionary<K, V>(count, root, hasNull, nullValue));
        }
    }

    private static interface INode {
        INode assoc(int shift, int hash, Object key, Object val, Flag addedLeaf);

        ImmutableIterator<?> nodeIt(boolean reverse);

        ImmutableIterator<?> nodeItFrom(int shift, int hash, Object key);

        INode without(int shift, int hash, Object key);

        Map.Entry<?,?> find(int shift, int hash, Object key);

        Object find(int shift, int hash, Object key, Object notFound);

//        ISeq nodeSeq();

        INode assoc(Owner owner, int shift, int hash, Object key, Object val, Flag addedLeaf);

        INode without(Owner edit, int shift, int hash, Object key, Flag removedLeaf);
    }

    private final static class ArrayNode implements INode {
        int count;
        final INode[] array;
        final Owner edit;

        ArrayNode(Owner edit, int count, INode[] array) {
            this.array = array;
            this.edit = edit;
            this.count = count;
        }

        public ImmutableIterator<?> nodeItFrom(int shift, int hash, Object key) {
            return new ArrayNodeIterator(this, shift, hash, key);
        }

        private static class ArrayNodeIterator implements ImmutableIterator<Object> {
            int index;
            Iterator<?> current;
            INode[] array;
            int shift, hash;
            Object key;

            public ArrayNodeIterator(ArrayNode an) {
                array = an.array;
                moveCurIfNeeded();
            }

            public ArrayNodeIterator(ArrayNode an, int shift, int hash, Object key) {
                array = an.array;
                this.shift = shift;
                this.hash = hash;
                this.key = key;
                moveCurTo();
            }

            private void moveCurTo() {
                index = mask(hash, shift);
                INode node = array[index];
                if (node == null)
                    return;
                current = node.nodeItFrom(shift + 5, hash, key);
                index += 1;

            }

            public boolean hasNext() {
                while (current != null && !current.hasNext()) {
                    moveCurIfNeeded();
                }
                return current != null && current.hasNext();
            }

            private void moveCurIfNeeded() {
                if (current != null && current.hasNext()) return;
                while (index < array.length && array[index] == null) {
                    index += 1;
                }
                ;
                current = (index == array.length) ? null : array[index++].nodeIt(false);
            }

            @Override
            public Object next() {
                return current.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        }

        private static final class ReverseArrayNodeIterator implements ImmutableIterator<Object> {
            int index;
            Iterator<?> current;
            INode[] array;

            public ReverseArrayNodeIterator(ArrayNode an) {
                this.array = an.array;
                this.index = array.length - 1;
                moveCurIfNeeded();
            }

            private void moveCurIfNeeded() {
                if (current != null && current.hasNext()) return;
                while (index >= 0 && array[index] == null) {
                    index -= 1;
                }
                ;
                current = (index < 0) ? null : array[index--].nodeIt(true);
            }

            public boolean hasNext() {
                while (current != null && !current.hasNext()) {
                    moveCurIfNeeded();
                }
                return current != null && current.hasNext();
            }

            @Override
            public Object next() {
                return current.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

        public ImmutableIterator<?> nodeIt(boolean reverse) {
            return reverse ? new ReverseArrayNodeIterator(this) : new ArrayNodeIterator(this);
        }

        public INode assoc(int shift, int hash, Object key, Object val, Flag addedLeaf) {
            int idx = mask(hash, shift);
            INode node = array[idx];
            if (node == null)
                return new ArrayNode(null, count + 1, cloneAndSet(array, idx,
                        BitmapIndexedNode.EMPTY.assoc(shift + 5, hash, key, val, addedLeaf)));
            INode n = node.assoc(shift + 5, hash, key, val, addedLeaf);
            if (n == node)
                return this;
            return new ArrayNode(null, count, cloneAndSet(array, idx, n));
        }

        public INode without(int shift, int hash, Object key) {
            int idx = mask(hash, shift);
            INode node = array[idx];
            if (node == null)
                return this;
            INode n = node.without(shift + 5, hash, key);
            if (n == node)
                return this;
            if (n == null) {
                if (count <= 8) // shrink
                    return pack(null, idx);
                return new ArrayNode(null, count - 1, cloneAndSet(array, idx, n));
            } else
                return new ArrayNode(null, count, cloneAndSet(array, idx, n));
        }

        public Map.Entry<?,?> find(int shift, int hash, Object key) {
            int idx = mask(hash, shift);
            INode node = array[idx];
            if (node == null)
                return null;
            return node.find(shift + 5, hash, key);
        }

        public Object find(int shift, int hash, Object key, Object notFound) {
            int idx = mask(hash, shift);
            INode node = array[idx];
            if (node == null)
                return notFound;
            return node.find(shift + 5, hash, key, notFound);
        }

        private ArrayNode ensureEditable(Owner edit) {
            if (this.edit == edit)
                return this;
            return new ArrayNode(edit, count, this.array.clone());
        }

        private ArrayNode editAndSet(Owner edit, int i, INode n) {
            ArrayNode editable = ensureEditable(edit);
            editable.array[i] = n;
            return editable;
        }

        private INode pack(Owner edit, int idx) {
            Object[] newArray = new Object[2 * (count - 1)];
            int j = 1;
            int bitmap = 0;
            for (int i = 0; i < idx; i++)
                if (array[i] != null) {
                    newArray[j] = array[i];
                    bitmap |= 1 << i;
                    j += 2;
                }
            for (int i = idx + 1; i < array.length; i++)
                if (array[i] != null) {
                    newArray[j] = array[i];
                    bitmap |= 1 << i;
                    j += 2;
                }
            return new BitmapIndexedNode(edit, bitmap, newArray);
        }

        public INode assoc(Owner edit, int shift, int hash, Object key, Object val, Flag addedLeaf) {
            int idx = mask(hash, shift);
            INode node = array[idx];
            if (node == null) {
                ArrayNode editable = editAndSet(edit, idx,
                        BitmapIndexedNode.EMPTY.assoc(edit, shift + 5, hash, key, val, addedLeaf));
                editable.count++;
                return editable;
            }
            INode n = node.assoc(edit, shift + 5, hash, key, val, addedLeaf);
            if (n == node)
                return this;
            return editAndSet(edit, idx, n);
        }

        public INode without(Owner edit, int shift, int hash, Object key, Flag removedLeaf) {
            int idx = mask(hash, shift);
            INode node = array[idx];
            if (node == null)
                return this;
            INode n = node.without(edit, shift + 5, hash, key, removedLeaf);
            if (n == node)
                return this;
            if (n == null) {
                if (count <= 8) // shrink
                    return pack(edit, idx);
                ArrayNode editable = editAndSet(edit, idx, n);
                editable.count--;
                return editable;
            }
            return editAndSet(edit, idx, n);
        }
    }

    private final static class BitmapIndexedNode implements INode {
        static final BitmapIndexedNode EMPTY = new BitmapIndexedNode(null, 0, new Object[0]);

        int bitmap;
        Object[] array;
        final Owner edit;

        final int index(int bit) {
            return Integer.bitCount(bitmap & (bit - 1));
        }

        BitmapIndexedNode(Owner edit, int bitmap, Object[] array) {
            this.bitmap = bitmap;
            this.array = array;
            this.edit = edit;
        }

        public ImmutableIterator<?> nodeItFrom(int shift, int hash, Object key) {
            return new BitmapIndexedNodeIterator(this, shift, hash, key);
        }

        public ImmutableIterator<?> nodeIt(boolean reverse) {
            return reverse ? new ReverseBitmapIndexedNodeIterator(this) : new BitmapIndexedNodeIterator(this);
        }

        private static class BitmapIndexedNodeIterator implements ImmutableIterator<Object> {
            BitmapIndexedNode node;

            int index;
            int N;
            Iterator<?> current;

            public BitmapIndexedNodeIterator(BitmapIndexedNode node) {
                this.node = node;
                N = node.array.length;
                moveCurIfNeeded();
            }

            public BitmapIndexedNodeIterator(BitmapIndexedNode bitmapIndexedNode,
                    int shift, int hash, Object key) {
                this.node = bitmapIndexedNode;
                N = node.array.length;
                moveCurTo(shift, hash, key);
            }

            private void moveCurTo(int shift, int hash, Object key) {
                int bit = bitpos(hash, shift);
                if ((node.bitmap & bit) == 0)
                    return;
                index = 2 * node.index(bit);
                Object keyOrNull = node.array[index];
                Object valOrNode = node.array[index + 1];
                if (keyOrNull == null) {
                    index += 2;
                    INode val = ((INode) valOrNode);
                    if (val != null) {
                        Iterator<?> nodeIt = val.nodeItFrom(shift + 5, hash, key);
                        if (nodeIt.hasNext()) {
                            current = nodeIt;
                            return;
                        }
                    }
                } else {
                    if (Objects.equals(key, keyOrNull)) {
                        return;// OK index points to key
                    } else {
                        throw new IllegalArgumentException("Key not found: " + key);
                    }

                }

            }

            public boolean hasNext() {
                moveCurIfNeeded();
                if (current == null && index >= N) {
                    return false;
                }
                return true;
            }

            // current != null => current.hasNext or index points to a valid key
            private void moveCurIfNeeded() {
                if (current != null && current.hasNext()) return;
                current = null;
                while (index < N) {
                    Object keyOrNull = node.array[index];
                    Object valOrNode = node.array[index + 1];
                    if (keyOrNull == null) {
                        index += 2;
                        INode val = ((INode) valOrNode);
                        if (val != null) {
                            Iterator<?> nodeIt = val.nodeIt(false);
                            if (nodeIt.hasNext()) {
                                current = nodeIt;
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                }
            }

            @Override
            public Object next() {
                if (current != null) {
                    return current.next();
                } else {
                    Object keyOrNull = node.array[index++];
                    Object valOrNode = node.array[index++];
                    return new AbstractMap.SimpleImmutableEntry<>(keyOrNull, valOrNode);
                }

            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        }

        static final class ReverseBitmapIndexedNodeIterator implements ImmutableIterator<Object> {
            BitmapIndexedNode node;

            int index;
            Iterator<?> current;

            public ReverseBitmapIndexedNodeIterator(BitmapIndexedNode node) {
                this.node = node;
                index = node.array.length - 1;
                moveCurIfNeeded();
            }

            public boolean hasNext() {
                moveCurIfNeeded();
                if (current == null && index < 0) {
                    return false;
                }
                return true;
            }

            // current != null => current.hasNext or index points to a valid key
            private void moveCurIfNeeded() {
                if (current != null && current.hasNext()) return;
                current = null;
                while (index >= 0) {
                    Object valOrNode = node.array[index];
                    Object keyOrNull = node.array[index - 1];
                    if (keyOrNull == null) {
                        index -= 2;
                        INode val = ((INode) valOrNode);
                        if (val != null) {
                            Iterator<?> nodeIt = val.nodeIt(true);
                            if (nodeIt.hasNext()) {
                                current = nodeIt;
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object next() {
                if (current != null) {
                    return current.next();
                } else {
                    Object valOrNode = node.array[index--];
                    Object keyOrNull = node.array[index--];
                    return new AbstractMap.SimpleImmutableEntry<>(keyOrNull, valOrNode);
                }

            }
        }

        public INode assoc(int shift, int hash, Object key, Object val, Flag addedLeaf) {
            int bit = bitpos(hash, shift);
            int idx = index(bit);
            if ((bitmap & bit) != 0) {
                Object keyOrNull = array[2 * idx];
                Object valOrNode = array[2 * idx + 1];
                if (keyOrNull == null) {
                    INode n = ((INode) valOrNode).assoc(shift + 5, hash, key, val, addedLeaf);
                    if (n == valOrNode)
                        return this;
                    return new BitmapIndexedNode(null, bitmap, cloneAndSet(array, 2 * idx + 1, n));
                }
                if (Objects.equals(key, keyOrNull)) {
                    if (val == valOrNode)
                        return this;
                    return new BitmapIndexedNode(null, bitmap, cloneAndSet(array, 2 * idx + 1, val));
                }
                addedLeaf.val = true;
                return new BitmapIndexedNode(null, bitmap,
                        cloneAndSet(array,
                                2 * idx, null,
                                2 * idx + 1, createNode(shift + 5, keyOrNull, valOrNode, hash, key, val)));
            } else {
                int n = Integer.bitCount(bitmap);
                if (n >= 16) {
                    INode[] nodes = new INode[32];
                    int jdx = mask(hash, shift);
                    nodes[jdx] = EMPTY.assoc(shift + 5, hash, key, val, addedLeaf);
                    int j = 0;
                    for (int i = 0; i < 32; i++)
                        if (((bitmap >>> i) & 1) != 0) {
                            if (array[j] == null)
                                nodes[i] = (INode) array[j + 1];
                            else
                                nodes[i] = EMPTY.assoc(shift + 5, Objects.hashCode(array[j]), array[j], array[j + 1], addedLeaf);
                            j += 2;
                        }
                    return new ArrayNode(null, n + 1, nodes);
                } else {
                    Object[] newArray = new Object[2 * (n + 1)];
                    System.arraycopy(array, 0, newArray, 0, 2 * idx);
                    newArray[2 * idx] = key;
                    addedLeaf.val = true;
                    newArray[2 * idx + 1] = val;
                    System.arraycopy(array, 2 * idx, newArray, 2 * (idx + 1), 2 * (n - idx));
                    return new BitmapIndexedNode(null, bitmap | bit, newArray);
                }
            }
        }

        public INode without(int shift, int hash, Object key) {
            int bit = bitpos(hash, shift);
            if ((bitmap & bit) == 0)
                return this;
            int idx = index(bit);
            Object keyOrNull = array[2 * idx];
            Object valOrNode = array[2 * idx + 1];
            if (keyOrNull == null) {
                INode n = ((INode) valOrNode).without(shift + 5, hash, key);
                if (n == valOrNode)
                    return this;
                if (n != null)
                    return new BitmapIndexedNode(null, bitmap, cloneAndSet(array, 2 * idx + 1, n));
                if (bitmap == bit)
                    return null;
                return new BitmapIndexedNode(null, bitmap ^ bit, removePair(array, idx));
            }
            if (Objects.equals(key, keyOrNull))
                // TODO: collapse
                return new BitmapIndexedNode(null, bitmap ^ bit, removePair(array, idx));
            return this;
        }

        public Map.Entry<?,?> find(int shift, int hash, Object key) {
            int bit = bitpos(hash, shift);
            if ((bitmap & bit) == 0)
                return null;
            int idx = index(bit);
            Object keyOrNull = array[2 * idx];
            Object valOrNode = array[2 * idx + 1];
            if (keyOrNull == null)
                return ((INode) valOrNode).find(shift + 5, hash, key);
            if (Objects.equals(key, keyOrNull))
                return new AbstractMap.SimpleImmutableEntry<>(keyOrNull, valOrNode);
            return null;
        }

        public Object find(int shift, int hash, Object key, Object notFound) {
            int bit = bitpos(hash, shift);
            if ((bitmap & bit) == 0)
                return notFound;
            int idx = index(bit);
            Object keyOrNull = array[2 * idx];
            Object valOrNode = array[2 * idx + 1];
            if (keyOrNull == null)
                return ((INode) valOrNode).find(shift + 5, hash, key, notFound);
            if (Objects.equals(key, keyOrNull))
                return valOrNode;
            return notFound;
        }

        private BitmapIndexedNode ensureEditable(Owner edit) {
            if (this.edit == edit)
                return this;
            int n = Integer.bitCount(bitmap);
            // make room for next assoc
            Object[] newArray = new Object[n >= 0 ? 2 * (n + 1) : 4];
            System.arraycopy(array, 0, newArray, 0, 2 * n);
            return new BitmapIndexedNode(edit, bitmap, newArray);
        }

        private BitmapIndexedNode editAndSet(Owner edit, int i, Object a) {
            BitmapIndexedNode editable = ensureEditable(edit);
            editable.array[i] = a;
            return editable;
        }

        private BitmapIndexedNode editAndSet(Owner edit, int i, Object a, int j, Object b) {
            BitmapIndexedNode editable = ensureEditable(edit);
            editable.array[i] = a;
            editable.array[j] = b;
            return editable;
        }

        private BitmapIndexedNode editAndRemovePair(Owner edit, int bit, int i) {
            if (bitmap == bit)
                return null;
            BitmapIndexedNode editable = ensureEditable(edit);
            editable.bitmap ^= bit;
            System.arraycopy(editable.array, 2 * (i + 1), editable.array, 2 * i, editable.array.length - 2 * (i + 1));
            editable.array[editable.array.length - 2] = null;
            editable.array[editable.array.length - 1] = null;
            return editable;
        }

        public INode assoc(Owner edit, int shift, int hash, Object key, Object val, Flag addedLeaf) {
            int bit = bitpos(hash, shift);
            int idx = index(bit);
            if ((bitmap & bit) != 0) {
                Object keyOrNull = array[2 * idx];
                Object valOrNode = array[2 * idx + 1];
                if (keyOrNull == null) {
                    INode n = ((INode) valOrNode).assoc(edit, shift + 5, hash, key, val, addedLeaf);
                    if (n == valOrNode)
                        return this;
                    return editAndSet(edit, 2 * idx + 1, n);
                }
                if (Objects.equals(key, keyOrNull)) {
                    if (val == valOrNode)
                        return this;
                    return editAndSet(edit, 2 * idx + 1, val);
                }
                addedLeaf.val = true;
                return editAndSet(edit, 2 * idx, null, 2 * idx + 1,
                        createNode(edit, shift + 5, keyOrNull, valOrNode, hash, key, val));
            } else {
                int n = Integer.bitCount(bitmap);
                if (n * 2 < array.length) {
                    addedLeaf.val = true;
                    BitmapIndexedNode editable = ensureEditable(edit);
                    System.arraycopy(editable.array, 2 * idx, editable.array, 2 * (idx + 1), 2 * (n - idx));
                    editable.array[2 * idx] = key;
                    editable.array[2 * idx + 1] = val;
                    editable.bitmap |= bit;
                    return editable;
                }
                if (n >= 16) {
                    INode[] nodes = new INode[32];
                    int jdx = mask(hash, shift);
                    nodes[jdx] = EMPTY.assoc(edit, shift + 5, hash, key, val, addedLeaf);
                    int j = 0;
                    for (int i = 0; i < 32; i++)
                        if (((bitmap >>> i) & 1) != 0) {
                            if (array[j] == null)
                                nodes[i] = (INode) array[j + 1];
                            else
                                nodes[i] = EMPTY.assoc(edit, shift + 5, Objects.hashCode(array[j]), array[j], array[j + 1],
                                        addedLeaf);
                            j += 2;
                        }
                    return new ArrayNode(edit, n + 1, nodes);
                } else {
                    Object[] newArray = new Object[2 * (n + 4)];
                    System.arraycopy(array, 0, newArray, 0, 2 * idx);
                    newArray[2 * idx] = key;
                    addedLeaf.val = true;
                    newArray[2 * idx + 1] = val;
                    System.arraycopy(array, 2 * idx, newArray, 2 * (idx + 1), 2 * (n - idx));
                    BitmapIndexedNode editable = ensureEditable(edit);
                    editable.array = newArray;
                    editable.bitmap |= bit;
                    return editable;
                }
            }
        }

        public INode without(Owner edit, int shift, int hash, Object key, Flag removedLeaf) {
            int bit = bitpos(hash, shift);
            if ((bitmap & bit) == 0)
                return this;
            int idx = index(bit);
            Object keyOrNull = array[2 * idx];
            Object valOrNode = array[2 * idx + 1];
            if (keyOrNull == null) {
                INode n = ((INode) valOrNode).without(edit, shift + 5, hash, key, removedLeaf);
                if (n == valOrNode)
                    return this;
                if (n != null)
                    return editAndSet(edit, 2 * idx + 1, n);
                if (bitmap == bit)
                    return null;
                return editAndRemovePair(edit, bit, idx);
            }
            if (Objects.equals(key, keyOrNull)) {
                removedLeaf.val = true;
                // TODO: collapse
                return editAndRemovePair(edit, bit, idx);
            }
            return this;
        }
    }

    private final static class HashCollisionNode implements INode {

        final int hash;
        int count;
        Object[] array;
        final Owner edit;

        HashCollisionNode(Owner edit, int hash, int count, Object... array) {
            this.edit = edit;
            this.hash = hash;
            this.count = count;
            this.array = array;
        }

        static final class HashCollisionNodeIterator implements ImmutableIterator<Object> {
            Object[] array;
            int index;
            int count;

            public HashCollisionNodeIterator(HashCollisionNode node) {

                this.array = node.array;
                this.count = node.count;
            }

            public HashCollisionNodeIterator(HashCollisionNode hashCollisionNode,
                    int shift, int hash, Object key) {
                this.array = hashCollisionNode.array;
                this.count = hashCollisionNode.count;
                int idx = hashCollisionNode.findIndex(key);
                index = idx == -1 ? count * 2 : idx;

            }

            public boolean hasNext() {
                return index < count * 2;
            }

            public Object next() {
                Object k = array[index++];
                Object v = array[index++];
                return new AbstractMap.SimpleImmutableEntry<>(k, v);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        }

        static final class ReverseHashCollisionNodeIterator implements ImmutableIterator<Object> {
            Object[] array;
            int index;
            int count;

            public ReverseHashCollisionNodeIterator(HashCollisionNode node) {
                this.array = node.array;
                this.count = node.count;
                this.index = count * 2 - 1;
            }

            public boolean hasNext() {
                return index >= 0;
            }

            public Object next() {
                Object v = array[index--];
                Object k = array[index--];
                return new AbstractMap.SimpleImmutableEntry<>(k, v);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        }

        public ImmutableIterator<?> nodeItFrom(int shift, int hash, Object key) {
            return new HashCollisionNodeIterator(this, shift, hash, key);
        }

        public ImmutableIterator<?> nodeIt(boolean reverse) {
            return reverse ? new ReverseHashCollisionNodeIterator(this) : new HashCollisionNodeIterator(this);
        }

        public INode assoc(int shift, int hash, Object key, Object val, Flag addedLeaf) {
            if (hash == this.hash) {
                int idx = findIndex(key);
                if (idx != -1) {
                    if (array[idx + 1] == val)
                        return this;
                    return new HashCollisionNode(null, hash, count, cloneAndSet(array, idx + 1, val));
                }
                Object[] newArray = new Object[array.length + 2];
                System.arraycopy(array, 0, newArray, 0, array.length);
                newArray[array.length] = key;
                newArray[array.length + 1] = val;
                addedLeaf.val = true;
                return new HashCollisionNode(edit, hash, count + 1, newArray);
            }
            // nest it in a bitmap node
            return new BitmapIndexedNode(null, bitpos(this.hash, shift), new Object[] { null, this })
                    .assoc(shift, hash, key, val, addedLeaf);
        }

        public INode without(int shift, int hash, Object key) {
            int idx = findIndex(key);
            if (idx == -1)
                return this;
            if (count == 1)
                return null;
            return new HashCollisionNode(null, hash, count - 1, removePair(array, idx / 2));
        }

        public Map.Entry<?,?> find(int shift, int hash, Object key) {
            int idx = findIndex(key);
            if (idx < 0)
                return null;
            if (Objects.equals(key, array[idx]))
                return new AbstractMap.SimpleImmutableEntry<>(array[idx], array[idx + 1]);
            return null;
        }

        public Object find(int shift, int hash, Object key, Object notFound) {
            int idx = findIndex(key);
            if (idx < 0)
                return notFound;
            if (Objects.equals(key, array[idx]))
                return array[idx + 1];
            return notFound;
        }

        public int findIndex(Object key) {
            for (int i = 0; i < 2 * count; i += 2)
            {
                if (Objects.equals(key, array[i]))
                    return i;
            }
            return -1;
        }

        private HashCollisionNode ensureEditable(Owner edit) {
            if (this.edit == edit)
                return this;
            Object[] newArray = new Object[2 * (count + 1)]; // make room for
                                                             // next assoc
            System.arraycopy(array, 0, newArray, 0, 2 * count);
            return new HashCollisionNode(edit, hash, count, newArray);
        }

        private HashCollisionNode ensureEditable(Owner edit, int count, Object[] array) {
            if (this.edit == edit) {
                this.array = array;
                this.count = count;
                return this;
            }
            return new HashCollisionNode(edit, hash, count, array);
        }

        private HashCollisionNode editAndSet(Owner edit, int i, Object a) {
            HashCollisionNode editable = ensureEditable(edit);
            editable.array[i] = a;
            return editable;
        }

        private HashCollisionNode editAndSet(Owner edit, int i, Object a, int j, Object b) {
            HashCollisionNode editable = ensureEditable(edit);
            editable.array[i] = a;
            editable.array[j] = b;
            return editable;
        }

        public INode assoc(Owner edit, int shift, int hash, Object key, Object val, Flag addedLeaf) {
            if (hash == this.hash) {
                int idx = findIndex(key);
                if (idx != -1) {
                    if (array[idx + 1] == val)
                        return this;
                    return editAndSet(edit, idx + 1, val);
                }
                if (array.length > 2 * count) {
                    addedLeaf.val = true;
                    HashCollisionNode editable = editAndSet(edit, 2 * count, key, 2 * count + 1, val);
                    editable.count++;
                    return editable;
                }
                Object[] newArray = new Object[array.length + 2];
                System.arraycopy(array, 0, newArray, 0, array.length);
                newArray[array.length] = key;
                newArray[array.length + 1] = val;
                addedLeaf.val = true;
                return ensureEditable(edit, count + 1, newArray);
            }
            // nest it in a bitmap node
            return new BitmapIndexedNode(edit, bitpos(this.hash, shift), new Object[] { null, this, null, null })
                    .assoc(edit, shift, hash, key, val, addedLeaf);
        }

        public INode without(Owner edit, int shift, int hash, Object key, Flag removedLeaf) {
            int idx = findIndex(key);
            if (idx == -1)
                return this;
            removedLeaf.val = true;
            if (count == 1)
                return null;
            HashCollisionNode editable = ensureEditable(edit);
            editable.array[idx] = editable.array[2 * count - 2];
            editable.array[idx + 1] = editable.array[2 * count - 1];
            editable.array[2 * count - 2] = editable.array[2 * count - 1] = null;
            editable.count--;
            return editable;
        }
    }

    private static INode[] cloneAndSet(INode[] array, int i, INode a) {
        INode[] clone = array.clone();
        clone[i] = a;
        return clone;
    }

    private static Object[] cloneAndSet(Object[] array, int i, Object a) {
        Object[] clone = array.clone();
        clone[i] = a;
        return clone;
    }

    private static Object[] cloneAndSet(Object[] array, int i, Object a, int j, Object b) {
        Object[] clone = array.clone();
        clone[i] = a;
        clone[j] = b;
        return clone;
    }

    private static Object[] removePair(Object[] array, int i) {
        Object[] newArray = new Object[array.length - 2];
        System.arraycopy(array, 0, newArray, 0, 2 * i);
        System.arraycopy(array, 2 * (i + 1), newArray, 2 * i, newArray.length - 2 * i);
        return newArray;
    }

    private static INode createNode(int shift, Object key1, Object val1, int key2hash, Object key2, Object val2) {
        int key1hash = Objects.hashCode(key1);
        if (key1hash == key2hash)
            return new HashCollisionNode(null, key1hash, 2, new Object[] { key1, val1, key2, val2 });
        Flag _ = new Flag();
        Owner edit = new Owner();
        return BitmapIndexedNode.EMPTY
                .assoc(edit, shift, key1hash, key1, val1, _)
                .assoc(edit, shift, key2hash, key2, val2, _);
    }

    private static INode createNode(Owner edit, int shift, Object key1, Object val1, int key2hash,
            Object key2, Object val2) {
        int key1hash = Objects.hashCode(key1);
        if (key1hash == key2hash)
            return new HashCollisionNode(null, key1hash, 2, new Object[] { key1, val1, key2, val2 });
        Flag _ = new Flag();
        return BitmapIndexedNode.EMPTY
                .assoc(edit, shift, key1hash, key1, val1, _)
                .assoc(edit, shift, key2hash, key2, val2, _);
    }

    private static int bitpos(int hash, int shift) {
        return 1 << mask(hash, shift);
    }

    class EntrySet extends AbstractSet<Map.Entry<K, V>> implements FiniteSet<Map.Entry<K, V>> {
        @Override
        public ImmutableIterator<Map.Entry<K, V>> iterator() {
            @SuppressWarnings("unchecked")
            final ImmutableIterator<Map.Entry<K, V>> s =
                root != null ? (ImmutableIterator<Map.Entry<K, V>>)root.nodeIt(false) : new EmptyIterator<Map.Entry<K, V>>();
            return hasNull ? new Iter<K,V>(s, nullValue) : s;
        }
        
        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry<?, ?>))
                return false;
            Map.Entry<?,?> entry = (Map.Entry<?, ?>)o;
            Object key = entry.getKey();
            V value = get(key);
            
            return (value != null || containsKey(key) && Objects.equals(value, entry.getValue()));
        }
        
        @Override
        public int size() {
            return count;
        }

        @Override
        public Cursor<Map.Entry<K, V>> cursor() {
            return IteratorCursor.create(iterator());
        }

        @Override
        public FiniteSet<Map.Entry<K, V>> plus(Map.Entry<K, V> e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FiniteSet<Map.Entry<K, V>> plusAll(Collection<? extends java.util.Map.Entry<K, V>> more) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FiniteSet<Map.Entry<K, V>> minus(Map.Entry<K, V> value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FiniteSet<Map.Entry<K, V>> zero() {
            throw new UnsupportedOperationException();
        }

        @Override
        public FiniteSet.FiniteSetBuilder<Map.Entry<K, V>> asBuilder() {
            throw new UnsupportedOperationException();
        }
    };
    
    private EntrySet entrySet;
    @Override
    public FiniteSet<Map.Entry<K, V>> entrySet() {
        if (entrySet == null);
            this.entrySet = new EntrySet();
        return this.entrySet;
    }
}
