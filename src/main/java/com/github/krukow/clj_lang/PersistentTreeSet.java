/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Mar 3, 2008 */

package com.github.krukow.clj_lang;

import java.util.Comparator;

import com.github.krukow.clj_ds.Dictionary;
import com.github.krukow.clj_ds.PersistentSortedSet;
import com.github.krukow.clj_ds.TransientSet;

//public class PersistentTreeSet<T> extends APersistentSet<T> implements PersistentSortedSet<T> {
//    private static final PersistentTreeSet<?> EMPTY = new PersistentTreeSet<>(PersistentTreeMap.EMPTY);
//
//
//    public static <T> PersistentTreeSet<T> empty() {
//        return (PersistentTreeSet<T>) EMPTY;
//    }
//
//    static public <T> PersistentTreeSet<T> create(Iterable<? extends T> items) {
//        PersistentTreeSet<T> ret = empty();
//        for (T item : items) {
//            ret = ret.plus(item);
//        }
//        return ret;
//    }
//
//    static public <T> PersistentTreeSet<T> create(Comparator<T> comp, Iterable<? extends T> items) {
//        PersistentTreeSet<T> ret = new PersistentTreeSet<T>(new PersistentTreeMap(comp));
//        for (T item : items) {
//            ret = ret.plus(item);
//        }
//        return ret;
//    }
//
//    private PersistentTreeSet(PersistentMap<T, Boolean> impl) {
//        super(impl);
//    }
//
//    @Override
//    public PersistentTreeSet<T> minus(T key) {
//        if (contains(key))
//            return new PersistentTreeSet<T>(impl.minus(key));
//        return this;
//    }
//
//    @Override
//    public PersistentTreeSet<T> plus(T o) {
//        if (contains(o))
//            return this;
//        return new PersistentTreeSet<T>(impl.plus(o, Boolean.TRUE));
//    }
//
//    public PersistentTreeSet<T> zero() {
//        return new PersistentTreeSet<T>((PersistentTreeMap) impl.zero());
//    }
//
//    public Comparator<T> comparator() {
//        return ((PersistentTreeMap<T, ?>) impl).comparator();
//    }
//    
//    @Override
//    public TransientSet<T> asTransient() {
//        throw new UnsupportedOperationException();
//    }
//}
