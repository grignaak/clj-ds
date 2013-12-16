/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Jul 5, 2007 */

package persistent;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

import persistent.AbstractBuilder.Owner;

/**
 * A random-access, persistent, thread-safe sequence. By default elements are
 * added and removed from the tail of the sequence in O(1) time. Access at an
 * arbitrary index is O(1).
 */
public class TrieVector<T> extends AbstractList<T> implements Sequence<T>, RandomAccess {

    private class VectorIterator extends AbstractImmutableListIterator<T> {
        private final int end;
        private final int start;
        private int i;
        private int base;
        private Object[] array;

        private VectorIterator(int jump, int start, int end) {
            this.end = end;
            this.start = start;
            i = jump;
            base = i - (i & 0x01f);
            int size = size();
            array = (i < size) ? arrayFor(i) : (size != 0) ? arrayFor(i-1) : null;
        }
        private VectorIterator(int start, int end) {
            this(start, start, end);
        }

        public boolean hasNext() {
            return i < end;
        }

        @Override
        public int nextIndex() {
            return i;
        }

        @SuppressWarnings("unchecked")
        public T next() {
            if (i >= end)
                throw new NoSuchElementException();
            if (i - base == 32) {
                array = arrayFor(i);
                base += 32;
            }
            return (T) array[i++ & 0x01f];
        }

        @Override
        public boolean hasPrevious() {
            return i > start;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T previous() {
            if (i <= start)
                throw new NoSuchElementException();
            if (i - base == 0) {
                array = arrayFor(i-1);
                base -= 32;
            }
            i--;
            return (T) array[i & 0x01f];
        }

        @Override
        public int previousIndex() {
            return i - 1;
        }
    }
    
    private static class Node implements Serializable {
        private static final long serialVersionUID = 1L;
        private transient final Owner edit;
        final Object[] array;

        Node(Owner edit, Object[] array) {
            this.edit = edit;
            this.array = array;
        }

        Node(Owner edit) {
            this.edit = edit;
            this.array = new Object[32];
        }
    }

    private final static Owner NOEDIT = Owner.none();
    private final static Node EMPTY_NODE = new Node(NOEDIT, new Object[32]);

    private final int cnt;
    private final int shift;
    private final Node root;
    private final Object[] tail;

    public final static TrieVector<?> EMPTY = new TrieVector<>(0, 5, EMPTY_NODE, new Object[] {});

    @SuppressWarnings("unchecked")
    static public <T> TrieVector<T> emptyVector() {
        return (TrieVector<T>) EMPTY;
    }

    static public <T> Sequence<T> create(Iterable<? extends T> items) {
        TrieVectorBuilder<T> ret = TrieVector.<T>emptyVector().asBuilder();
        for (T item : items)
            ret = ret.plus(item);
        return ret.build();
    }

    @SuppressWarnings("unchecked")
    static public <T> TrieVector<T> create(T... items) {
        TrieVectorBuilder<T> ret = TrieVector.<T>emptyVector().asBuilder();
        for (T item : items)
            ret = ret.plus(item);
        return ret.build();
    }
    

    static private Sequence<Object> createOwning(Object... items) {
        if (items.length == 0)
            return emptyVector();
        else if (items.length <= 32)
            return new TrieVector<>(items.length, 5, TrieVector.EMPTY_NODE, items);
        return TrieVector.create(items);
    }

    @SuppressWarnings("unchecked")
    static public <T> TrieVector<T> create(Collection<T> coll) {
        if (coll.size() <= 32)
            return (TrieVector<T>) createOwning(coll.toArray());
        return TrieVector.create(coll);
    }


    private TrieVector(int cnt, int shift, Node root, Object[] tail) {
        this.cnt = cnt;
        this.shift = shift;
        this.root = root;
        this.tail = tail;
    }

    @Override
    public TrieVectorBuilder<T> asBuilder() {
        return asBuilder(new Owner());
    }

    private final int tailoff() {
        if (cnt < 32)
            return 0;
        return ((cnt - 1) >>> 5) << 5;
    }

    private Object[] arrayFor(int i) {
        if (i >= 0 && i < cnt)
        {
            if (i >= tailoff())
                return tail;
            Node node = root;
            for (int level = shift; level > 0; level -= 5)
                node = (Node) node.array[(i >>> level) & 0x01f];
            return node.array;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int i) {
        Object[] node = arrayFor(i);
        return (T) node[i & 0x01f];
    }

    @Override
    public T peek() {
        return get(0);
    }

    @Override
    public TrieVector<T> replace(int i, T val) {
        if (i >= 0 && i < cnt)
        {
            if (i >= tailoff())
            {
                Object[] newTail = new Object[tail.length];
                System.arraycopy(tail, 0, newTail, 0, tail.length);
                newTail[i & 0x01f] = val;

                return new TrieVector<T>(cnt, shift, root, newTail);
            }

            return new TrieVector<T>(cnt, shift, doAssoc(shift, root, i, val), tail);
        }
        if (i == cnt)
            return plus(val);
        throw new IndexOutOfBoundsException();
    }

    private static Node doAssoc(int level, Node node, int i, Object val) {
        Node ret = new Node(node.edit, node.array.clone());
        if (level == 0)
        {
            ret.array[i & 0x01f] = val;
        }
        else
        {
            int subidx = (i >>> level) & 0x01f;
            ret.array[subidx] = doAssoc(level - 5, (Node) node.array[subidx], i, val);
        }
        return ret;
    }

    @Override
    public int size() {
        return cnt;
    }

    @Override
    public TrieVector<T> plus(T val) {
        // room in tail?
        // if(tail.length < 32)
        if (cnt - tailoff() < 32)
        {
            Object[] newTail = new Object[tail.length + 1];
            System.arraycopy(tail, 0, newTail, 0, tail.length);
            newTail[tail.length] = val;
            return new TrieVector<T>(cnt + 1, shift, root, newTail);
        }
        // full tail, push into tree
        Node newroot;
        Node tailnode = new Node(root.edit, tail);
        int newshift = shift;
        // overflow root?
        if ((cnt >>> 5) > (1 << shift))
        {
            newroot = new Node(root.edit);
            newroot.array[0] = root;
            newroot.array[1] = newPath(root.edit, shift, tailnode);
            newshift += 5;
        }
        else
            newroot = pushTail(shift, root, tailnode);
        return new TrieVector<T>(cnt + 1, newshift, newroot, new Object[] { val });
    }

    private Node pushTail(int level, Node parent, Node tailnode) {
        // if parent is leaf, insert node,
        // else does it map to an existing child? -> nodeToInsert = pushNode one
        // more level
        // else alloc new path
        // return nodeToInsert placed in copy of parent
        int subidx = ((cnt - 1) >>> level) & 0x01f;
        Node ret = new Node(parent.edit, parent.array.clone());
        Node nodeToInsert;
        if (level == 5)
        {
            nodeToInsert = tailnode;
        }
        else
        {
            Node child = (Node) parent.array[subidx];
            nodeToInsert = (child != null) ?
                    pushTail(level - 5, child, tailnode)
                    : newPath(root.edit, level - 5, tailnode);
        }
        ret.array[subidx] = nodeToInsert;
        return ret;
    }

    private static Node newPath(Owner edit, int level, Node node) {
        if (level == 0)
            return node;
        Node ret = new Node(edit);
        ret.array[0] = newPath(edit, level - 5, node);
        return ret;
    }

    private ImmutableListIterator<T> rangedIterator(final int start, final int end) {
        return new VectorIterator(start, end);
    }

    public ImmutableIterator<T> iterator() {
        return rangedIterator(0, size());
    }

    private static final class ChunkedCursor<T> extends AbstractCursor<T> {

        private final TrieVector<T> vec;
        private final Object[] node;
        private final int i;
        private final int offset;
        

        private ChunkedCursor(TrieVector<T> vec, int i, int offset) {
            this.vec = vec;
            this.i = i;
            this.offset = offset;
            this.node = (i < vec.size()) ? vec.arrayFor(i) : null;
        }

        private ChunkedCursor(TrieVector<T> vec, Object[] node, int i, int offset) {
            this.vec = vec;
            this.node = node;
            this.i = i;
            this.offset = offset;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T head() {
            return (T) node[offset];
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public Cursor<T> tail() {
            if (offset + 1 < node.length)
                return new ChunkedCursor<T>(vec, node, i, offset + 1);
            else if (i + node.length < vec.cnt)
                return new ChunkedCursor<T>(vec, i + node.length, 0);
            else
                return Containers.emptyCursor();
        }
    }

    @Override
    public TrieVector<T> zero() {
        return emptyVector();
    }

    @Override
    public TrieVector<T> minus() {
        if (cnt == 0)
            throw new IllegalStateException("Can't pop empty vector");
        if (cnt == 1)
            return emptyVector();
        // if(tail.length > 1)
        if (cnt - tailoff() > 1)
        {
            Object[] newTail = new Object[tail.length - 1];
            System.arraycopy(tail, 0, newTail, 0, newTail.length);
            return new TrieVector<T>(cnt - 1, shift, root, newTail);
        }
        Object[] newtail = arrayFor(cnt - 2);

        Node newroot = popTail(shift, root);
        int newshift = shift;
        if (newroot == null)
        {
            newroot = EMPTY_NODE;
        }
        if (shift > 5 && newroot.array[1] == null)
        {
            newroot = (Node) newroot.array[0];
            newshift -= 5;
        }
        return new TrieVector<T>(cnt - 1, newshift, newroot, newtail);
    }

    private Node popTail(int level, Node node) {
        int subidx = ((cnt - 2) >>> level) & 0x01f;
        if (level > 5)
        {
            Node newchild = popTail(level - 5, (Node) node.array[subidx]);
            if (newchild == null && subidx == 0)
                return null;
            else
            {
                Node ret = new Node(root.edit, node.array.clone());
                ret.array[subidx] = newchild;
                return ret;
            }
        }
        else if (subidx == 0)
            return null;
        else
        {
            Node ret = new Node(root.edit, node.array.clone());
            ret.array[subidx] = null;
            return ret;
        }
    }

    public static final class TrieVectorBuilder<T> extends AbstractSequenceBuilder<T> {
        private int cnt;
        private int shift;
        private Node root;
        private Object[] tail;

        private TrieVectorBuilder(int cnt, int shift, Node root, Object[] tail) {
            super(root.edit);
            this.cnt = cnt;
            this.shift = shift;
            this.root = root;
            this.tail = tail;
        }

        private TrieVectorBuilder(Owner owner, TrieVector<? extends T> v) {
            this(v.cnt, v.shift, editableRoot(v.root, owner), editableTail(v.tail));
        }

        private Node ensureEditable(Node node) {
            if (node.edit == root.edit)
                return node;
            return new Node(root.edit, node.array.clone());
        }

        private static Node editableRoot(Node node, Owner owner) {
            return new Node(owner, node.array.clone());
        }

        @Override
        public TrieVector<T> build() {
            owner.ensureEditable();
            Object[] trimmedTail = new Object[cnt - tailoff()];
            System.arraycopy(tail, 0, trimmedTail, 0, trimmedTail.length);
            return built(new TrieVector<T>(cnt, shift, root, trimmedTail));
        }

        private static Object[] editableTail(Object[] tl) {
            Object[] ret = new Object[32];
            System.arraycopy(tl, 0, ret, 0, tl.length);
            return ret;
        }
        
        @Override
        public TrieVectorBuilder<T> plusAll(Collection<? extends T> more) {
            return (TrieVectorBuilder<T>) super.plusAll(more);
        }

        @Override
        public TrieVectorBuilder<T> plus(T val) {
            owner.ensureEditable();
            int i = cnt;
            // room in tail?
            if (i - tailoff() < 32)
            {
                tail[i & 0x01f] = val;
                ++cnt;
                return this;
            }
            // full tail, push into tree
            Node newroot;
            Node tailnode = new Node(root.edit, tail);
            tail = new Object[32];
            tail[0] = val;
            int newshift = shift;
            // overflow root?
            if ((cnt >>> 5) > (1 << shift))
            {
                newroot = new Node(root.edit);
                newroot.array[0] = root;
                newroot.array[1] = newPath(root.edit, shift, tailnode);
                newshift += 5;
            }
            else
                newroot = pushTail(shift, root, tailnode);
            root = newroot;
            shift = newshift;
            ++cnt;
            return this;
        }

        private Node pushTail(int level, Node origParent, Node tailnode) {
            // if parent is leaf, insert node,
            // else does it map to an existing child? -> nodeToInsert = pushNode
            // one more level
            // else alloc new path
            // return nodeToInsert placed in parent
            Node parent = ensureEditable(origParent);
            int subidx = ((cnt - 1) >>> level) & 0x01f;
            Node ret = parent;
            Node nodeToInsert;
            if (level == 5)
            {
                nodeToInsert = tailnode;
            }
            else
            {
                Node child = (Node) parent.array[subidx];
                nodeToInsert = (child != null) ?
                        pushTail(level - 5, child, tailnode)
                        : newPath(root.edit, level - 5, tailnode);
            }
            ret.array[subidx] = nodeToInsert;
            return ret;
        }

        final private int tailoff() {
            if (cnt < 32)
                return 0;
            return ((cnt - 1) >>> 5) << 5;
        }

        private Object[] editableArrayFor(int i) {
            if (i >= 0 && i < cnt)
            {
                if (i >= tailoff())
                    return tail;
                Node node = root;
                for (int level = shift; level > 0; level -= 5)
                    node = ensureEditable((Node) node.array[(i >>> level) & 0x01f]);
                return node.array;
            }
            throw new IndexOutOfBoundsException();
        }

        @Override
        public TrieVectorBuilder<T> replace(int i, T val) {
            owner.ensureEditable();
            if (i >= 0 && i < cnt)
            {
                if (i >= tailoff())
                {
                    tail[i & 0x01f] = val;
                    return this;
                }

                root = doAssoc(shift, root, i, val);
                return this;
            }
            if (i == cnt)
                return plus(val);
            throw new IndexOutOfBoundsException();
        }

        private Node doAssoc(int level, Node origNode, int i, Object val) {
            Node node = ensureEditable(origNode);
            Node ret = node;
            if (level == 0)
            {
                ret.array[i & 0x01f] = val;
            }
            else
            {
                int subidx = (i >>> level) & 0x01f;
                ret.array[subidx] = doAssoc(level - 5, (Node) node.array[subidx], i, val);
            }
            return ret;
        }

        public TrieVectorBuilder<T> minus() {
            owner.ensureEditable();
            if (cnt == 0)
                return this;
            if (cnt == 1)
            {
                cnt = 0;
                return this;
            }
            int i = cnt - 1;
            // pop in tail?
            if ((i & 0x01f) > 0)
            {
                --cnt;
                return this;
            }

            Object[] newtail = editableArrayFor(cnt - 2);

            Node newroot = popTail(shift, root);
            int newshift = shift;
            if (newroot == null)
            {
                newroot = new Node(root.edit);
            }
            if (shift > 5 && newroot.array[1] == null)
            {
                newroot = ensureEditable((Node) newroot.array[0]);
                newshift -= 5;
            }
            root = newroot;
            shift = newshift;
            --cnt;
            tail = newtail;
            return this;
        }

        private Node popTail(int level, Node origNode) {
            Node node = ensureEditable(origNode);
            int subidx = ((cnt - 2) >>> level) & 0x01f;
            if (level > 5)
            {
                Node newchild = popTail(level - 5, (Node) node.array[subidx]);
                if (newchild == null && subidx == 0)
                    return null;
                else
                {
                    Node ret = node;
                    ret.array[subidx] = newchild;
                    return ret;
                }
            }
            else if (subidx == 0)
                return null;
            else
            {
                Node ret = node;
                ret.array[subidx] = null;
                return ret;
            }
        }

        @Override
        public TrieVectorBuilder<T> zero() {
            return TrieVector.<T>emptyVector().asBuilder();
        }
    }

    public Cursor<T> cursor() {
        return cnt != 0 ? new ChunkedCursor<>(this, 0, 0) : Containers.<T>emptyCursor();
    }
    
    @Override
    public ImmutableListIterator<T> listIterator() {
        return listIterator(0);
    }
    
    @Override
    public ImmutableListIterator<T> listIterator(int index) {
//        rangeCheckInclusive(index, cnt); TODO
        return new VectorIterator(index, 0, size());
    }

    private TrieVectorBuilder<T> asBuilder(Owner owner) {
        return new TrieVectorBuilder<T>(owner, this);
    }

    @Override
    public TrieVector<T> plusAll(Collection<? extends T> more) {
        return asBuilder().plusAll(more).build();
    }
}
