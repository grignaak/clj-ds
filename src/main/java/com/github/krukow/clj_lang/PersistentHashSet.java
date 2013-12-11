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

import com.github.krukow.clj_ds.PersistentMap;
import com.github.krukow.clj_ds.PersistentSet;

public class PersistentHashSet<T> extends APersistentSet<T> implements PersistentSet<T> {

    static private final PersistentHashSet EMPTY = new PersistentHashSet(PersistentHashMap.emptyMap());

    @SuppressWarnings("unchecked")
    static public final <T> PersistentHashSet<T> emptySet() {
        return EMPTY;
    }

    public static <T> PersistentSet<T> create(T... init) {
        PersistentSet<T> ret = EMPTY;
        for (int i = 0; i < init.length; i++)
        {
            ret = ret.plus(init[i]);
        }
        return ret;
    }

    public static <T> PersistentSet<T> create(Iterable<? extends T> init) {
        PersistentSet<T> ret = EMPTY;
        for (T key : init)
        {
            ret = ret.plus(key);
        }
        return ret;
    }

    private PersistentHashSet(PersistentMap<T, Boolean> impl) {
        super(impl);
    }
}
