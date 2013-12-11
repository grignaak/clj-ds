/**
 *   Copyright (c) Karl Krukow. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

package com.github.krukow.clj_lang;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.github.krukow.clj_ds.PersistentMap;
import com.github.krukow.clj_ds.TransientMap;

/*A persistent rendition of Nikolas Askitis' HAT Trie Uses path copying for
 * persistence Any errors are my own */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class PersistentHATTrie<T> extends AbstractMap<String, T> implements PersistentMap<String, T>, Iterable<Map.Entry<String, T>> {
    private static final long serialVersionUID = -7068824281866890730L;
    final HATTrieNode<T> root;
    final int count;

    public static final PersistentHATTrie EMPTY = new PersistentHATTrie(null, 0);

    public PersistentHATTrie(HATTrieNode root, int count) {
        this.root = root;
        this.count = count;
    }

    private static abstract class HATTrieNode<T> {
        protected abstract HATTrieNode<T> add(String s, int i, T t);
        protected abstract T get(String s, int j);
        protected abstract Iterator<Map.Entry<String, T>> nodeIt(String prefix);
        protected abstract String toStringWithPrefix(String prefix);
    }

    private static final class AccessNode<T> extends HATTrieNode<T> {
        private final HATTrieNode<T> children[];
        private final T emptyPtr;

        private AccessNode(HATTrieNode[] children, T emptyPtr) {
            this.children = children;
            this.emptyPtr = emptyPtr;
        }

        @Override
        public String toString() {
            return toStringWithPrefix("");
        }

        @Override
        public String toStringWithPrefix(String prefix) {
            StringBuilder sb = new StringBuilder();
            String nestedPrefix = prefix + "  ";
            sb.append(prefix);
            sb.append("(access-node\n").append(nestedPrefix);
            for (int i = 0; i < children.length; i++) {
                HATTrieNode node = children[i];
                if (node != null) {
                    sb.append((char) i).append(" -> ").append(
                            node.toStringWithPrefix(nestedPrefix)).append(";\n").append(nestedPrefix);
                }
            }
            if (emptyPtr != null) {
                sb.append("\n").append(prefix).append("**");
            }
            sb.append(prefix).append(")");
            return sb.toString();
        }

        public HATTrieNode<T> add(String s, int i, T t) {
            int length = s.length();
            if (i < length) {
                char ichar = s.charAt(i);
                HATTrieNode hatTrieNode = children[ichar];
                if (hatTrieNode != null) {
                    HATTrieNode newNode = hatTrieNode.add(s, i + 1, t);
                    if (newNode == hatTrieNode) {
                        return this;
                    }
                    HATTrieNode[] newArr = new HATTrieNode[children.length];
                    System.arraycopy(children, 0, newArr, 0, children.length);
                    newArr[ichar] = newNode;
                    return new AccessNode(newArr, emptyPtr);
                }
                ContainerNode c = new ContainerNode(PersistentTreeMap.EMPTY.plus(s.substring(i + 1), t));
                HATTrieNode[] newArr = new HATTrieNode[children.length];
                System.arraycopy(children, 0, newArr, 0, children.length);
                newArr[ichar] = c;
                return new AccessNode(newArr, emptyPtr);
            }
            if (i == length && emptyPtr == null) {
                return new AccessNode(children, s);
            }
            return this;
        }

        public T get(String s, int i) {
            if (i == s.length()) {
                return emptyPtr;
            }
            HATTrieNode<T> c = children[s.charAt(i)];
            if (c == null) {
                return null;
            }
            return c.get(s, i + 1);
        }

        private static final class AccessNodeIterator<T> implements Iterator<Map.Entry<String, T>> {
            private final HATTrieNode children[];
            private final T emptyPtr;
            private int index = -1;
            private final String prefix;
            Iterator<Map.Entry<String, T>> current = null;

            AccessNodeIterator(AccessNode<T> node, String prefix) {
                children = node.children;
                emptyPtr = node.emptyPtr;
                this.prefix = prefix;
                moveCurIfNeeded();
            }

            private void moveCurIfNeeded() {
                if (index == -1) {
                    if (emptyPtr == null) {
                        index = 0;
                    } else {
                        return;
                    }
                }

                if (current != null && current.hasNext()) return;
                while (index < children.length && children[index] == null) {
                    index += 1;
                }
                ;
                if (index == children.length) {
                    current = null;
                }
                else {
                    String prefix = new StringBuilder(this.prefix).append((char) index).toString();
                    current = children[index++].nodeIt(prefix);
                }

            }

            public boolean hasNext() {
                if (index == -1 && emptyPtr != null) {
                    return true;
                }
                while (current != null && !current.hasNext()) {
                    moveCurIfNeeded();
                }
                return current != null && current.hasNext();
            }

            @Override
            public Map.Entry<String, T> next() {
                if (index == -1 && emptyPtr != null) {
                    index = 0;
                    moveCurIfNeeded();
                    return new AbstractMap.SimpleImmutableEntry<>(prefix, emptyPtr);
                }
                return current.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        }

        @Override
        public Iterator<Map.Entry<String, T>> nodeIt(String prefix) {
            return new AccessNodeIterator(this, prefix);
        }

    }

    private static final class ContainerNode<T> extends HATTrieNode<T> {
        private PersistentTreeMap<String, T> strings;

        public ContainerNode(PersistentTreeMap<String, T> strings) {
            this.strings = strings;
        }

        public String toString() {
            return strings.toString();
        }

        public HATTrieNode<T> add(String s, int i, T t) {
            String ss = s.substring(i);
            T et = strings.get(ss);
            if (Util.equals(et, t)) {
                return this;
            }
            if (shouldBurst()) {
                return burst(s, i, t);
            }
            return new ContainerNode<T>(this.strings.plus(s.substring(i), t));
        }

        public T get(String s, int i) {
            return strings.get(s.substring(i));
        }

        private HATTrieNode burst(String s, int i, T t) {
            HATTrieNode[] children = new HATTrieNode[256];
            T empty = s.length() == i ? t : null;
            for (Iterator<Map.Entry<String, T>> iterator = strings.entrySet().iterator(); iterator.hasNext();) {
                Entry<String, T> next = iterator.next();
                String old = next.getKey();
                T value = next.getValue();
                if (empty == null && "".equals(old)) {// can only happen once
                    empty = value;
                } else {
                    char f = old.charAt(0);
                    children[f] = addToNode(children[f], old, value, 1);
                }
            }
            if (empty != t) {// i < s.length()
                char f = s.charAt(i);
                children[f] = addToNode(children[f], s, t, i + 1);
            }
            return new AccessNode(children, empty);
        }

        private static final <T> HATTrieNode addToNode(HATTrieNode hatTrieNode, String s, T v, int i) {
            if (hatTrieNode == null) {
                return new ContainerNode(PersistentTreeMap.EMPTY.plus(s.substring(i), v));
            } else {
                return hatTrieNode.add(s, i, v);
            }

        }

        private boolean shouldBurst() {
            return strings.size() == 4;
        }

        @Override
        public Iterator<Map.Entry<String, T>> nodeIt(final String prefix) {
            return new Iterator<Map.Entry<String, T>>() {
                Iterator<Map.Entry<String, T>> it = strings.entrySet().iterator();

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Map.Entry<String, T> next() {
                    Entry<String, T> next = it.next();
                    return new AbstractMap.SimpleImmutableEntry<>(prefix + next.getKey(), next.getValue());
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public String toStringWithPrefix(String prefix) {
            return prefix + toString();

        }

    }

    @Override
    public T get(Object s) {
        if (root == null || s == null || !(s instanceof String)) return null;
        return root.get((String)s, 0);
    }

    @Override
    public PersistentHATTrie<T> plus(String s, T t) {
        if (root == null) {
            return new PersistentHATTrie(new ContainerNode(PersistentTreeMap.EMPTY.plus(s, t)), 1);
        }
        HATTrieNode<T> newRoot = root.add(s, 0, t);
        if (root == newRoot) {
            return this;
        }
        return new PersistentHATTrie(newRoot, count + 1);
    }

    @Override
    public boolean containsKey(Object key) {
        return (key instanceof String) && get((String) key) != null;
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public PersistentHATTrie<T> zero() {
        return EMPTY;
    }

    public Iterator<Map.Entry<String, T>> iterator() {
        return root != null ? root.nodeIt("") : new EmptyIterator();
    }

    @Override
    public String toString() {
        if (root == null) {
            return "{}";
        }
        return root.toString();
    }

    @Override
    public PersistentMap<String, T> plusEx(String key, T val) {
        // TODO unimplemented
        throw new RuntimeException("Unimplemented: PersistentMap<String,T>.plusEx");
    }

    @Override
    public PersistentMap<String, T> minus(String key) {
        // TODO unimplemented
        throw new RuntimeException("Unimplemented: PersistentMap<String,T>.minus");
    }

    @Override
    public TransientMap<String, T> asTransient() {
        // TODO unimplemented
        throw new RuntimeException("Unimplemented: PersistentMap<String,T>.asTransient");
    }

    @Override
    public Set<java.util.Map.Entry<String, T>> entrySet() {
        // TODO unimplemented
        throw new RuntimeException("Unimplemented: AbstractMap<String,T>.entrySet");
    }
}