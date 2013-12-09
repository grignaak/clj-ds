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

import java.util.AbstractSet;
import java.util.Iterator;

import com.github.krukow.clj_ds.PersistentMap;
import com.github.krukow.clj_ds.PersistentSet;
import com.github.krukow.clj_ds.TransientMap;
import com.github.krukow.clj_ds.TransientSet;

public class APersistentSet<T> extends AbstractSet<T> implements PersistentSet<T> {

    protected final PersistentMap<T, Boolean> impl;

    protected APersistentSet(PersistentMap<T, Boolean> impl) {
        this.impl = impl;
    }

    @Override
    public boolean contains(Object key) {
        return impl.containsKey(key);
    }

    @Override
    public Iterator<T> iterator() {
        return impl.keySet().iterator();
    }
    
    @Override
    public int size() {
        return impl.size();
    }

    @Override
    public TransientSet<T> asTransient() {
        return new ATransientSet<>(impl.asTransient());
    }

    @Override
    public PersistentSet<T> zero() {
        return new APersistentSet<>(impl.zero());
    }

    @Override
    public PersistentSet<T> plus(T val) {
        return new APersistentSet<>(impl.plus(val, Boolean.TRUE));
    }

    @Override
    public PersistentSet<T> minus(T val) {
        return new APersistentSet<>(impl.minus(val));
    }
    
    private static class ATransientSet<T> extends AbstractSet<T> implements TransientSet<T> {

        private final TransientMap<T, Boolean> impl;

        private ATransientSet(TransientMap<T, Boolean> impl) {
            this.impl = impl;
        }
        @Override
        public TransientSet<T> plus(T val) {
            return new ATransientSet<>(impl.plus(val, Boolean.TRUE));
        }

        @Override
        public TransientSet<T> minus(T val) {
            return new ATransientSet<>(impl.minus(val));
        }

        @Override
        public PersistentSet<T> persist() {
            return new APersistentSet<>(impl.persist());
        }

        @Override
        public Iterator<T> iterator() {
            return impl.keySet().iterator();
        }

        @Override
        public int size() {
            return impl.size();
        }
    }
}
