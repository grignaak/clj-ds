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

package persistent;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Stack;

import persistent.Containers.IteratorCursor;

/**
 * Persistent Red Black Tree Note that instances of this class are constant
 * values i.e. add/remove etc return new values
 * <p/>
 * See Okasaki, Kahrs, Larsen et al
 */

public class TreeDictionary<K, V> extends AbstractDictionary<K, V> {
    private static final TreeDictionary<? extends Comparable<?>, ?> EMPTY = new TreeDictionary<>(DefaultComparator.DEFAULT_COMPARATOR);

    private final Comparator<K> comp;
    private final Node<K,V> tree;
    private final int _count;

    public static <K extends Comparable<K>, V> TreeDictionary<K, V> create(Map<? extends K, ? extends V> other) {
        TreeDictionary<K, V> ret = TreeDictionary.<K,V>emptyDictionary();
        for (Map.Entry<? extends K, ? extends V> o : other.entrySet())
        {
            ret = ret.plus(o.getKey(), o.getValue());
        }
        return ret;
    }
    
    @SuppressWarnings("unchecked")
    public static <K extends Comparable<K>, V> TreeDictionary<K, V> emptyDictionary() {
        return (TreeDictionary<K, V>) EMPTY;
    }
    
    public static <K, V> TreeDictionary<K, V> emptyDictionary(Comparator<K> comparator) {
        return new TreeDictionary<K, V>(comparator);
    }

    private TreeDictionary(Comparator<K> comp) {
        this(comp, null, 0);
    }

    private TreeDictionary(Comparator<K> comp, Node<K,V> tree, int _count) {
        this.comp = comp;
        this.tree = tree;
        this._count = _count;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(Object key) {
        return entryAt((K) key) != null;
    }

    @Override
    public TreeDictionary<K, V> plus(K key, V val) {
        Box<Node<K,V>> found = new Box<>(null);
        Node<K,V> t = add(tree, key, val, found);
        if (t == null)   // null == already contains key
        {
            Node<K,V> foundNode = (Node<K,V>) found.val;
            if (foundNode.getValue() == val)
                /* note only get same collection on
                 * identity of val, not equals() */
                return this;
            return new TreeDictionary<K, V>(comp, replace(tree, key, val), _count);
        }
        return new TreeDictionary<K, V>(comp, t.blacken(), _count + 1);
    }

    @Override
    public Dictionary<K, V> plusIfAbsent(K key, V val) {
        Box<Node<K,V>> found = new Box<>(null);
        Node<K,V> t = add(tree, key, val, found);
        if (t == null)   // null == already contains key
        {
            return this;
        }
        return new TreeDictionary<K, V>(comp, t.blacken(), _count + 1);
    }

    @Override
    public Dictionary<K, V> replace(K key, V expected, V actual) {
        Box<Node<K,V>> found = new Box<>(null);
        Node<K,V> t = add(tree, key, actual, found);
        if (t == null)   // null == already contains key
        {
            Node<K,V> foundNode = (Node<K,V>) found.val;
            Object foundValue = foundNode.getValue();
            if (foundValue != expected || foundValue == actual)
                /* note: only get same collection on identity of val, not
                 * equals() */
                return this;
            return new TreeDictionary<K, V>(comp, replace(tree, key, actual), _count);
        }
        
        return this;
    }

    public TreeDictionary<K, V> minus(K key) {
        Box<Node<K,V>> found = new Box<>(null);
        Node<K,V> t = remove(tree, key, found);
        if (t == null)
        {
            if (found.val == null) // null == doesn't contain key
                return this;
            // empty
            return new TreeDictionary<K, V>(comp);
        }
        return new TreeDictionary<K, V>(comp, t.blacken(), _count - 1);
    }
    
    @Override
    public TreeDictionary<K, V> minus(K key, V expected) {
        Box<Node<K,V>> found = new Box<>(null);
        Node<K,V> t = remove(tree, key, found);
        if (t == null) // either not found, or empty tree
        {
            if (found.val == null || found.val.getValue() != expected) // null == doesn't contain key
                return this;
            // empty
            return new TreeDictionary<K, V>(comp);
        }
        
        if (found.val.getValue() == expected)
            return new TreeDictionary<K, V>(comp, t.blacken(), _count - 1);
        else
            return this;
    }


    public TreeDictionary<K,V> zero() {
        return new TreeDictionary<>(comp);
    }

    public Comparator<K> comparator() {
        return comp;
    }

    public K firstKey() {
        Map.Entry<K,V> t = firstEntry();
        return t != null ? t.getKey() : null;
    }

    public Map.Entry<K,V> firstEntry() {
        Node<K,V> t = tree;
        if (t != null)
        {
            while (t.left() != null)
                t = t.left();
        }
        return t;
    }

    public K lastKey() {
        Map.Entry<K,V> t = lastEntry();
        return t != null ? t.getKey() : null;
    }

    public Map.Entry<K, V> lastEntry() {
        Node<K,V> t = tree;
        if (t != null)
        {
            while (t.right() != null)
                t = t.right();
        }
        return t;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        Node<K,V> n = entryAt((K)key);
        return ((n != null) ? (V) n.getValue() : null);
    }

    @Override
    public int size() {
        return _count;
    }

    private Node<K,V> entryAt(K key) {
        Node<K,V> t = tree;
        while (t != null)
        {
            int c = comp.compare(key, (K) t.key);
            if (c == 0)
                return t;
            else if (c < 0)
                t = t.left();
            else
                t = t.right();
        }
        return t;
    }

    private Node<K,V> add(Node<K,V> t, K key, V val, Box<Node<K,V>> found) {
        if (t == null)
        {
            if (val == null)
                return new Red<>(key);
            return new RedVal<>(key, val);
        }
        int c = comp.compare((K) key, (K) t.key);
        if (c == 0)
        {
            found.val = t;
            return null;
        }
        Node<K,V> ins = c < 0 ? add(t.left(), key, val, found) : add(t.right(), key, val, found);
        if (ins == null) // found below
            return null;
        if (c < 0)
            return t.addLeft(ins);
        return t.addRight(ins);
    }

    /**
     * Found returned is old node that contained the value
     * 
     * @return null if not found, otherwise the new tree
     */
    private Node<K,V> remove(Node<K,V> t, K key, Box<Node<K,V>> found) {
        if (t == null)
            return null; // not found indicator
        int c = comp.compare(key, t.key);
        if (c == 0)
        {
            found.val = t;
            return append(t.left(), t.right());
        }
        Node<K,V> del = c < 0 ? remove(t.left(), key, found) : remove(t.right(), key, found);
        if (del == null && found.val == null) // not found below
            return null;
        if (c < 0)
        {
            if (t.left() instanceof Black)
                return balanceLeftDel(t.key, t.getValue(), del, t.right());
            else
                return red(t.key, t.getValue(), del, t.right());
        }
        if (t.right() instanceof Black)
            return balanceRightDel(t.key, t.getValue(), t.left(), del);
        return red(t.key, t.getValue(), t.left(), del);
        // return t.removeLeft(del);
        // return t.removeRight(del);
    }

    private static <K,V> Node<K,V> append(Node<K,V> left, Node<K,V> right) {
        if (left == null)
            return right;
        else if (right == null)
            return left;
        else if (left instanceof Red)
        {
            if (right instanceof Red)
            {
                Node<K,V> app = append(left.right(), right.left());
                if (app instanceof Red)
                    return red(app.key, app.getValue(),
                            red(left.key, left.getValue(), left.left(), app.left()),
                            red(right.key, right.getValue(), app.right(), right.right()));
                else
                    return red(left.key, left.getValue(), left.left(), red(right.key, right.getValue(), app, right.right()));
            }
            else
                return red(left.key, left.getValue(), left.left(), append(left.right(), right));
        }
        else if (right instanceof Red)
            return red(right.key, right.getValue(), append(left, right.left()), right.right());
        else // black/black
        {
            Node<K,V> app = append(left.right(), right.left());
            if (app instanceof Red)
                return red(app.key, app.getValue(),
                        black(left.key, left.getValue(), left.left(), app.left()),
                        black(right.key, right.getValue(), app.right(), right.right()));
            else
                return balanceLeftDel(left.key, left.getValue(), left.left(),
                        black(right.key, right.getValue(), app, right.right()));
        }
    }

    private static <K,V> Node<K,V> balanceLeftDel(K key, V val, Node<K,V> del, Node<K,V> right) {
        if (del instanceof Red)
            return red(key, val, del.blacken(), right);
        else if (right instanceof Black)
            return rightBalance(key, val, del, right.redden());
        else if (right instanceof Red && right.left() instanceof Black)
            return red(right.left().key, right.left().getValue(),
                    black(key, val, del, right.left().left()),
                    rightBalance(right.key, right.getValue(), right.left().right(), right.right().redden()));
        else
            throw new UnsupportedOperationException("Invariant violation");
    }

    private static <K,V> Node<K,V> balanceRightDel(K key, V val, Node<K,V> left, Node<K,V> del) {
        if (del instanceof Red)
            return red(key, val, left, del.blacken());
        else if (left instanceof Black)
            return leftBalance(key, val, left.redden(), del);
        else if (left instanceof Red && left.right() instanceof Black)
            return red(left.right().key, left.right().getValue(),
                    leftBalance(left.key, left.getValue(), left.left().redden(), left.right().left()),
                    black(key, val, left.right().right(), del));
        else
            throw new UnsupportedOperationException("Invariant violation");
    }

    private static <K,V> Node<K,V> leftBalance(K key, V val, Node<K,V> ins, Node<K,V> right) {
        if (ins instanceof Red && ins.left() instanceof Red)
            return red(ins.key, ins.getValue(), ins.left().blacken(), black(key, val, ins.right(), right));
        else if (ins instanceof Red && ins.right() instanceof Red)
            return red(ins.right().key, ins.right().getValue(),
                    black(ins.key, ins.getValue(), ins.left(), ins.right().left()),
                    black(key, val, ins.right().right(), right));
        else
            return black(key, val, ins, right);
    }

    private static <K,V> Node<K,V> rightBalance(K key, V val, Node<K,V> left, Node<K,V> ins) {
        if (ins instanceof Red && ins.right() instanceof Red)
            return red(ins.key, ins.getValue(), black(key, val, left, ins.left()), ins.right().blacken());
        else if (ins instanceof Red && ins.left() instanceof Red)
            return red(ins.left().key, ins.left().getValue(),
                    black(key, val, left, ins.left().left()),
                    black(ins.key, ins.getValue(), ins.left().right(), ins.right()));
        else
            return black(key, val, left, ins);
    }

    private Node<K,V> replace(Node<K,V> t, K key, V val) {
        int c = comp.compare(key, t.key);
        return t.replace(t.key,
                c == 0 ? val : t.getValue(),
                c < 0 ? replace(t.left(), key, val) : t.left(),
                c > 0 ? replace(t.right(), key, val) : t.right());
    }

    private static <K,V> Red<K,V> red(K key, V val, Node<K,V> left, Node<K,V> right) {
        if (left == null && right == null)
        {
            if (val == null)
                return new Red<>(key);
            return new RedVal<>(key, val);
        }
        if (val == null)
            return new RedBranch<>(key, left, right);
        return new RedBranchVal<>(key, val, left, right);
    }

    private static <K,V> Black<K,V> black(K key, V val, Node<K,V> left, Node<K,V> right) {
        if (left == null && right == null)
        {
            if (val == null)
                return new Black<>(key);
            return new BlackVal<>(key, val);
        }
        if (val == null)
            return new BlackBranch<>(key, left, right);
        return new BlackBranchVal<>(key, val, left, right);
    }

    private static abstract class Node<K,V> implements Map.Entry<K,V> {
        final K key;

        Node(K key) {
            this.key = key;
        }

        public final K getKey() {
            return key;
        }

        public V getValue() {
            return null;
        }

        public final V setValue(V value) { throw new UnsupportedOperationException(); }

        Node<K,V> left() {
            return null;
        }

        Node<K,V> right() {
            return null;
        }

        abstract Node<K,V> addLeft(Node<K,V> ins);

        abstract Node<K,V> addRight(Node<K,V> ins);

        abstract Node<K,V> removeLeft(Node<K,V> del);

        abstract Node<K,V> removeRight(Node<K,V> del);

        abstract Node<K,V> blacken();

        abstract Node<K,V> redden();

        Node<K,V> balanceLeft(Node<K,V> parent) {
            return black(parent.key, parent.getValue(), this, parent.right());
        }

        Node<K,V> balanceRight(Node<K,V> parent) {
            return black(parent.key, parent.getValue(), parent.left(), this);
        }

        abstract Node<K,V> replace(K key, V val, Node<K,V> left, Node<K,V> right);
    }

    private static class Black<K,V> extends Node<K,V> {
        public Black(K key) {
            super(key);
        }

        Node<K,V> addLeft(Node<K,V> ins) {
            return ins.balanceLeft(this);
        }

        Node<K,V> addRight(Node<K,V> ins) {
            return ins.balanceRight(this);
        }

        Node<K,V> removeLeft(Node<K,V> del) {
            return balanceLeftDel(key, getValue(), del, right());
        }

        Node<K,V> removeRight(Node<K,V> del) {
            return balanceRightDel(key, getValue(), left(), del);
        }

        Node<K,V> blacken() {
            return this;
        }

        Node<K,V> redden() {
            return new Red<>(key);
        }

        Node<K,V> replace(K key, V val, Node<K,V> left, Node<K,V> right) {
            return black(key, val, left, right);
        }

    }

    private static class BlackVal<K,V> extends Black<K,V> {
        final V val;

        public BlackVal(K key, V val) {
            super(key);
            this.val = val;
        }

        public V getValue() {
            return val;
        }

        Node<K,V> redden() {
            return new RedVal<>(key, val);
        }

    }

    private static class BlackBranch<K,V> extends Black<K,V> {
        final Node<K,V> left;

        final Node<K,V> right;

        public BlackBranch(K key, Node<K,V> left, Node<K,V> right) {
            super(key);
            this.left = left;
            this.right = right;
        }

        public Node<K,V> left() {
            return left;
        }

        public Node<K,V> right() {
            return right;
        }

        Node<K,V> redden() {
            return new RedBranch<>(key, left, right);
        }

    }

    private static class BlackBranchVal<K,V> extends BlackBranch<K,V> {
        final V val;

        public BlackBranchVal(K key, V val, Node<K,V> left, Node<K,V> right) {
            super(key, left, right);
            this.val = val;
        }

        public V getValue() {
            return val;
        }

        Node<K,V> redden() {
            return new RedBranchVal<>(key, val, left, right);
        }

    }

    private static class Red<K,V> extends Node<K,V> {
        public Red(K key) {
            super(key);
        }

        Node<K,V> addLeft(Node<K,V> ins) {
            return red(key, getValue(), ins, right());
        }

        Node<K,V> addRight(Node<K,V> ins) {
            return red(key, getValue(), left(), ins);
        }

        Node<K,V> removeLeft(Node<K,V> del) {
            return red(key, getValue(), del, right());
        }

        Node<K,V> removeRight(Node<K,V> del) {
            return red(key, getValue(), left(), del);
        }

        Node<K,V> blacken() {
            return new Black<>(key);
        }

        Node<K,V> redden() {
            throw new UnsupportedOperationException("Invariant violation");
        }

        Node<K,V> replace(K key, V val, Node<K,V> left, Node<K,V> right) {
            return red(key, val, left, right);
        }

    }

    private static class RedVal<K,V> extends Red<K,V> {
        final V val;

        public RedVal(K key, V val) {
            super(key);
            this.val = val;
        }

        public V getValue() {
            return val;
        }

        Node<K,V> blacken() {
            return new BlackVal<>(key, val);
        }

    }

    private static class RedBranch<K,V> extends Red<K,V> {
        final Node<K,V> left;

        final Node<K,V> right;

        public RedBranch(K key, Node<K,V> left, Node<K,V> right) {
            super(key);
            this.left = left;
            this.right = right;
        }

        public Node<K,V> left() {
            return left;
        }

        public Node<K,V> right() {
            return right;
        }

        Node<K,V> balanceLeft(Node<K,V> parent) {
            if (left instanceof Red)
                return red(key, getValue(), left.blacken(), black(parent.key, parent.getValue(), right, parent.right()));
            else if (right instanceof Red)
                return red(right.key, right.getValue(), black(key, getValue(), left, right.left()),
                        black(parent.key, parent.getValue(), right.right(), parent.right()));
            else
                return super.balanceLeft(parent);

        }

        Node<K,V> balanceRight(Node<K,V> parent) {
            if (right instanceof Red)
                return red(key, getValue(), black(parent.key, parent.getValue(), parent.left(), left), right.blacken());
            else if (left instanceof Red)
                return red(left.key, left.getValue(), black(parent.key, parent.getValue(), parent.left(), left.left()),
                        black(key, getValue(), left.right(), right));
            else
                return super.balanceRight(parent);
        }

        Node<K,V> blacken() {
            return new BlackBranch<>(key, left, right);
        }

    }

    private static class RedBranchVal<K,V> extends RedBranch<K,V> {
        final V val;

        public RedBranchVal(K key, V val, Node<K,V> left, Node<K,V> right) {
            super(key, left, right);
            this.val = val;
        }

        public V getValue() {
            return val;
        }

        Node<K,V> blacken() {
            return new BlackBranchVal<>(key, val, left, right);
        }
    }

    static private class NodeIterator<K,V> extends AbstractImmutableIterator<Map.Entry<K,V>> {
        private Stack<Node<K,V>> stack = new Stack<>();
        private final boolean asc;

        private NodeIterator(Node<K,V> t, boolean asc) {
            this.asc = asc;
            push(t);
        }

        private void push(Node<K,V> root) {
            for (Node<K,V> t = root; t != null; t = asc ? t.left() : t.right()) {
                stack.push(t);
            }
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public Map.Entry<K,V> next() {
            Node<K,V> t = stack.pop();
            push(asc ? t.right() : t.left());
            return t;
        }
    }
    
    
    class EntrySet extends AbstractSet<Map.Entry<K,V>> implements FiniteSet<Map.Entry<K,V>> {
        @Override
        public Cursor<Map.Entry<K, V>> cursor() {
            return IteratorCursor.create(iterator());
        }

        @Override
        public FiniteSet<Map.Entry<K, V>> plus(Map.Entry<K, V> e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public FiniteSet<Map.Entry<K, V>> plusAll(Collection<? extends Map.Entry<K, V>> more) {
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

        @Override
        public ImmutableIterator<Map.Entry<K, V>> iterator() {
            return new NodeIterator<>(tree, true);
        }

        @Override
        public int size() {
            return _count;
        }
    }
    
    private transient EntrySet entrySet;
    @Override
    public FiniteSet<Entry<K, V>> entrySet() {
        this.entrySet = new EntrySet();
        return entrySet;
    }

    @Override
    public DictionaryBuilder<K, V> asBuilder() {
        return Containers.wrappedBuilder(this);
    }
}
