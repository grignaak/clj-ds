/**
a *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich May 20, 2006 */

package com.github.krukow.clj_lang;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import persistent.Box;

import com.github.krukow.clj_ds.PersistentSortedMap;
import com.github.krukow.clj_ds.TransientMap;

/**
 * Persistent Red Black Tree Note that instances of this class are constant
 * values i.e. add/remove etc return new values
 * <p/>
 * See Okasaki, Kahrs, Larsen et al
 */

//public class PersistentTreeMap<K, V> extends AbstractMap<K, V> implements PersistentSortedMap<K, V> {
//
//    private final Comparator<K> comp;
//    private final Node tree;
//    private final int _count;
//
//    final static public PersistentTreeMap EMPTY = new PersistentTreeMap();
//
//    public static <K, V> PersistentTreeMap<K, V> create(Map<? extends K, ? extends V> other) {
//        PersistentTreeMap<K, V> ret = EMPTY;
//        for (Map.Entry<? extends K, ? extends V> o : other.entrySet())
//        {
//            ret = ret.plus(o.getKey(), o.getValue());
//        }
//        return ret;
//    }
//
//    public PersistentTreeMap() {
//        this(Util.DEFAULT_COMPARATOR);
//    }
//
//    public PersistentTreeMap(Comparator<K> comp) {
//        this.comp = comp;
//        tree = null;
//        _count = 0;
//    }
//
//    private PersistentTreeMap(Comparator<K> comp, Node tree, int _count) {
//        this.comp = comp;
//        this.tree = tree;
//        this._count = _count;
//    }
//
//    @Override
//    public boolean containsKey(Object key) {
//        return entryAt((K) key) != null;
//    }
//
//    @Override
//    public PersistentTreeMap<K, V> plusEx(K key, V val) {
//        Box found = new Box(null);
//        Node t = add(tree, key, val, found);
//        if (t == null)   // null == already contains key
//        {
//            throw Util.runtimeException("Key already present");
//        }
//        return new PersistentTreeMap<K, V>(comp, t.blacken(), _count + 1);
//    }
//
//    @Override
//    public PersistentTreeMap<K, V> plus(K key, V val) {
//        Box found = new Box(null);
//        Node t = add(tree, key, val, found);
//        if (t == null)   // null == already contains key
//        {
//            Node foundNode = (Node) found.val;
//            if (foundNode.val() == val)  // note only get same collection on
//                                        // identity of val, not equals()
//                return this;
//            return new PersistentTreeMap<K, V>(comp, replace(tree, key, val), _count);
//        }
//        return new PersistentTreeMap<K, V>(comp, t.blacken(), _count + 1);
//    }
//
//    public PersistentTreeMap<K, V> minus(K key) {
//        Box found = new Box(null);
//        Node t = remove(tree, key, found);
//        if (t == null)
//        {
//            if (found.val == null) // null == doesn't contain key
//                return this;
//            // empty
//            return new PersistentTreeMap<K, V>(comp);
//        }
//        return new PersistentTreeMap<K, V>(comp, t.blacken(), _count - 1);
//    }
//
//    public PersistentTreeMap zero() {
//        return new PersistentTreeMap(comp);
//    }
//
//    public Comparator<K> comparator() {
//        return comp;
//    }
//
//    public K firstKey() {
//        Map.Entry<K,V> t = firstEntry();
//        return t != null ? t.getKey() : null;
//    }
//
//    private Map.Entry<K,V> firstEntry() {
//        Node t = tree;
//        if (t != null)
//        {
//            while (t.left() != null)
//                t = t.left();
//        }
//        return t;
//    }
//
//    public K lastKey() {
//        Map.Entry<K,V> t = lastEntry();
//        return t != null ? t.getKey() : null;
//    }
//
//    private Map.Entry<K, V> lastEntry() {
//        Node t = tree;
//        if (t != null)
//        {
//            while (t.right() != null)
//                t = t.right();
//        }
//        return t;
//    }
//
//    @Override
//    public V get(Object key) {
//        Node n = entryAt((K)key);
//        return ((n != null) ? (V) n.val() : null);
//    }
//
//    @Override
//    public int size() {
//        return _count;
//    }
//
//    private  Node entryAt(K key) {
//        Node t = tree;
//        while (t != null)
//        {
//            int c = doCompare(key, (K) t.key);
//            if (c == 0)
//                return t;
//            else if (c < 0)
//                t = t.left();
//            else
//                t = t.right();
//        }
//        return t;
//    }
//
//    private int doCompare(K k1, K k2) {
//        // if(comp != null)
//        return comp.compare(k1, k2);
//        // return ((Comparable) k1).compareTo(k2);
//    }
//
//    private Node add(Node t, Object key, Object val, Box found) {
//        if (t == null)
//        {
//            if (val == null)
//                return new Red(key);
//            return new RedVal(key, val);
//        }
//        int c = doCompare((K) key, (K) t.key);
//        if (c == 0)
//        {
//            found.val = t;
//            return null;
//        }
//        Node ins = c < 0 ? add(t.left(), key, val, found) : add(t.right(), key, val, found);
//        if (ins == null) // found below
//            return null;
//        if (c < 0)
//            return t.addLeft(ins);
//        return t.addRight(ins);
//    }
//
//    private Node remove(Node t, K key, Box found) {
//        if (t == null)
//            return null; // not found indicator
//        int c = doCompare(key, (K) t.key);
//        if (c == 0)
//        {
//            found.val = t;
//            return append(t.left(), t.right());
//        }
//        Node del = c < 0 ? remove(t.left(), key, found) : remove(t.right(), key, found);
//        if (del == null && found.val == null) // not found below
//            return null;
//        if (c < 0)
//        {
//            if (t.left() instanceof Black)
//                return balanceLeftDel(t.key, t.val(), del, t.right());
//            else
//                return red(t.key, t.val(), del, t.right());
//        }
//        if (t.right() instanceof Black)
//            return balanceRightDel(t.key, t.val(), t.left(), del);
//        return red(t.key, t.val(), t.left(), del);
//        // return t.removeLeft(del);
//        // return t.removeRight(del);
//    }
//
//    private static Node append(Node left, Node right) {
//        if (left == null)
//            return right;
//        else if (right == null)
//            return left;
//        else if (left instanceof Red)
//        {
//            if (right instanceof Red)
//            {
//                Node app = append(left.right(), right.left());
//                if (app instanceof Red)
//                    return red(app.key, app.val(),
//                            red(left.key, left.val(), left.left(), app.left()),
//                            red(right.key, right.val(), app.right(), right.right()));
//                else
//                    return red(left.key, left.val(), left.left(), red(right.key, right.val(), app, right.right()));
//            }
//            else
//                return red(left.key, left.val(), left.left(), append(left.right(), right));
//        }
//        else if (right instanceof Red)
//            return red(right.key, right.val(), append(left, right.left()), right.right());
//        else // black/black
//        {
//            Node app = append(left.right(), right.left());
//            if (app instanceof Red)
//                return red(app.key, app.val(),
//                        black(left.key, left.val(), left.left(), app.left()),
//                        black(right.key, right.val(), app.right(), right.right()));
//            else
//                return balanceLeftDel(left.key, left.val(), left.left(),
//                        black(right.key, right.val(), app, right.right()));
//        }
//    }
//
//    private static Node balanceLeftDel(Object key, Object val, Node del, Node right) {
//        if (del instanceof Red)
//            return red(key, val, del.blacken(), right);
//        else if (right instanceof Black)
//            return rightBalance(key, val, del, right.redden());
//        else if (right instanceof Red && right.left() instanceof Black)
//            return red(right.left().key, right.left().val(),
//                    black(key, val, del, right.left().left()),
//                    rightBalance(right.key, right.val(), right.left().right(), right.right().redden()));
//        else
//            throw new UnsupportedOperationException("Invariant violation");
//    }
//
//    private static Node balanceRightDel(Object key, Object val, Node left, Node del) {
//        if (del instanceof Red)
//            return red(key, val, left, del.blacken());
//        else if (left instanceof Black)
//            return leftBalance(key, val, left.redden(), del);
//        else if (left instanceof Red && left.right() instanceof Black)
//            return red(left.right().key, left.right().val(),
//                    leftBalance(left.key, left.val(), left.left().redden(), left.right().left()),
//                    black(key, val, left.right().right(), del));
//        else
//            throw new UnsupportedOperationException("Invariant violation");
//    }
//
//    private static Node leftBalance(Object key, Object val, Node ins, Node right) {
//        if (ins instanceof Red && ins.left() instanceof Red)
//            return red(ins.key, ins.val(), ins.left().blacken(), black(key, val, ins.right(), right));
//        else if (ins instanceof Red && ins.right() instanceof Red)
//            return red(ins.right().key, ins.right().val(),
//                    black(ins.key, ins.val(), ins.left(), ins.right().left()),
//                    black(key, val, ins.right().right(), right));
//        else
//            return black(key, val, ins, right);
//    }
//
//    private static Node rightBalance(Object key, Object val, Node left, Node ins) {
//        if (ins instanceof Red && ins.right() instanceof Red)
//            return red(ins.key, ins.val(), black(key, val, left, ins.left()), ins.right().blacken());
//        else if (ins instanceof Red && ins.left() instanceof Red)
//            return red(ins.left().key, ins.left().val(),
//                    black(key, val, left, ins.left().left()),
//                    black(ins.key, ins.val(), ins.left().right(), ins.right()));
//        else
//            return black(key, val, left, ins);
//    }
//
//    private Node replace(Node t, K key, Object val) {
//        int c = doCompare(key, (K) t.key);
//        return t.replace(t.key,
//                c == 0 ? val : t.val(),
//                c < 0 ? replace(t.left(), key, val) : t.left(),
//                c > 0 ? replace(t.right(), key, val) : t.right());
//    }
//
//    private static Red red(Object key, Object val, Node left, Node right) {
//        if (left == null && right == null)
//        {
//            if (val == null)
//                return new Red(key);
//            return new RedVal(key, val);
//        }
//        if (val == null)
//            return new RedBranch(key, left, right);
//        return new RedBranchVal(key, val, left, right);
//    }
//
//    private static Black black(Object key, Object val, Node left, Node right) {
//        if (left == null && right == null)
//        {
//            if (val == null)
//                return new Black(key);
//            return new BlackVal(key, val);
//        }
//        if (val == null)
//            return new BlackBranch(key, left, right);
//        return new BlackBranchVal(key, val, left, right);
//    }
//
//    private static abstract class Node implements Map.Entry {
//        final Object key;
//
//        Node(Object key) {
//            this.key = key;
//        }
//
//        public Object key() {
//            return key;
//        }
//
//        public Object val() {
//            return null;
//        }
//
//        public Object getKey() {
//            return key();
//        }
//
//        public Object getValue() {
//            return val();
//        }
//
//        public Object setValue(Object value) {
//            throw new UnsupportedOperationException();
//        }
//
//        Node left() {
//            return null;
//        }
//
//        Node right() {
//            return null;
//        }
//
//        abstract Node addLeft(Node ins);
//
//        abstract Node addRight(Node ins);
//
//        abstract Node removeLeft(Node del);
//
//        abstract Node removeRight(Node del);
//
//        abstract Node blacken();
//
//        abstract Node redden();
//
//        Node balanceLeft(Node parent) {
//            return black(parent.key, parent.val(), this, parent.right());
//        }
//
//        Node balanceRight(Node parent) {
//            return black(parent.key, parent.val(), parent.left(), this);
//        }
//
//        abstract Node replace(Object key, Object val, Node left, Node right);
//    }
//
//    private static class Black extends Node {
//        public Black(Object key) {
//            super(key);
//        }
//
//        Node addLeft(Node ins) {
//            return ins.balanceLeft(this);
//        }
//
//        Node addRight(Node ins) {
//            return ins.balanceRight(this);
//        }
//
//        Node removeLeft(Node del) {
//            return balanceLeftDel(key, val(), del, right());
//        }
//
//        Node removeRight(Node del) {
//            return balanceRightDel(key, val(), left(), del);
//        }
//
//        Node blacken() {
//            return this;
//        }
//
//        Node redden() {
//            return new Red(key);
//        }
//
//        Node replace(Object key, Object val, Node left, Node right) {
//            return black(key, val, left, right);
//        }
//
//    }
//
//    private static class BlackVal extends Black {
//        final Object val;
//
//        public BlackVal(Object key, Object val) {
//            super(key);
//            this.val = val;
//        }
//
//        public Object val() {
//            return val;
//        }
//
//        Node redden() {
//            return new RedVal(key, val);
//        }
//
//    }
//
//    private static class BlackBranch extends Black {
//        final Node left;
//
//        final Node right;
//
//        public BlackBranch(Object key, Node left, Node right) {
//            super(key);
//            this.left = left;
//            this.right = right;
//        }
//
//        public Node left() {
//            return left;
//        }
//
//        public Node right() {
//            return right;
//        }
//
//        Node redden() {
//            return new RedBranch(key, left, right);
//        }
//
//    }
//
//    private static class BlackBranchVal extends BlackBranch {
//        final Object val;
//
//        public BlackBranchVal(Object key, Object val, Node left, Node right) {
//            super(key, left, right);
//            this.val = val;
//        }
//
//        public Object val() {
//            return val;
//        }
//
//        Node redden() {
//            return new RedBranchVal(key, val, left, right);
//        }
//
//    }
//
//    private static class Red extends Node {
//        public Red(Object key) {
//            super(key);
//        }
//
//        Node addLeft(Node ins) {
//            return red(key, val(), ins, right());
//        }
//
//        Node addRight(Node ins) {
//            return red(key, val(), left(), ins);
//        }
//
//        Node removeLeft(Node del) {
//            return red(key, val(), del, right());
//        }
//
//        Node removeRight(Node del) {
//            return red(key, val(), left(), del);
//        }
//
//        Node blacken() {
//            return new Black(key);
//        }
//
//        Node redden() {
//            throw new UnsupportedOperationException("Invariant violation");
//        }
//
//        Node replace(Object key, Object val, Node left, Node right) {
//            return red(key, val, left, right);
//        }
//
//    }
//
//    private static class RedVal extends Red {
//        final Object val;
//
//        public RedVal(Object key, Object val) {
//            super(key);
//            this.val = val;
//        }
//
//        public Object val() {
//            return val;
//        }
//
//        Node blacken() {
//            return new BlackVal(key, val);
//        }
//
//    }
//
//    private static class RedBranch extends Red {
//        final Node left;
//
//        final Node right;
//
//        public RedBranch(Object key, Node left, Node right) {
//            super(key);
//            this.left = left;
//            this.right = right;
//        }
//
//        public Node left() {
//            return left;
//        }
//
//        public Node right() {
//            return right;
//        }
//
//        Node balanceLeft(Node parent) {
//            if (left instanceof Red)
//                return red(key, val(), left.blacken(), black(parent.key, parent.val(), right, parent.right()));
//            else if (right instanceof Red)
//                return red(right.key, right.val(), black(key, val(), left, right.left()),
//                        black(parent.key, parent.val(), right.right(), parent.right()));
//            else
//                return super.balanceLeft(parent);
//
//        }
//
//        Node balanceRight(Node parent) {
//            if (right instanceof Red)
//                return red(key, val(), black(parent.key, parent.val(), parent.left(), left), right.blacken());
//            else if (left instanceof Red)
//                return red(left.key, left.val(), black(parent.key, parent.val(), parent.left(), left.left()),
//                        black(key, val(), left.right(), right));
//            else
//                return super.balanceRight(parent);
//        }
//
//        Node blacken() {
//            return new BlackBranch(key, left, right);
//        }
//
//    }
//
//    private static class RedBranchVal extends RedBranch {
//        final Object val;
//
//        public RedBranchVal(Object key, Object val, Node left, Node right) {
//            super(key, left, right);
//            this.val = val;
//        }
//
//        public Object val() {
//            return val;
//        }
//
//        Node blacken() {
//            return new BlackBranchVal(key, val, left, right);
//        }
//    }
//
//    static private class NodeIterator implements Iterator {
//        private Stack stack = new Stack();
//        private boolean asc;
//
//        private NodeIterator(Node t, boolean asc) {
//            this.asc = asc;
//            push(t);
//        }
//
//        private void push(Node t) {
//            while (t != null)
//            {
//                stack.push(t);
//                t = asc ? t.left() : t.right();
//            }
//        }
//
//        @Override
//        public boolean hasNext() {
//            return !stack.isEmpty();
//        }
//
//        @Override
//        public Object next() {
//            Node t = (Node) stack.pop();
//            push(asc ? t.right() : t.left());
//            return t;
//        }
//
//        @Override
//        public void remove() {
//            throw new UnsupportedOperationException();
//        }
//    }
//    
//    @Override
//    public Set<Entry<K, V>> entrySet() {
//        return new AbstractSet<Map.Entry<K, V>>() {
//            @Override
//            public Iterator<Map.Entry<K, V>> iterator() {
//                return new NodeIterator(tree, true);
//            }
//
//            @Override
//            public int size() {
//                return _count;
//            }
//        };
//    }
//
//    @Override
//    public TransientMap<K, V> asTransient() {
//        return SimpleTransientMap.wrap(this);
//    }
//}
