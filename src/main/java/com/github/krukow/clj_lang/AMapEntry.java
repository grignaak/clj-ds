/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Mar 1, 2008 */

package com.github.krukow.clj_lang;

import java.util.Map;

@Deprecated
public abstract class AMapEntry<K, V> extends APersistentVector implements Map.Entry<K, V> {

    @Override
    public Object nth(int i) {
        if (i == 0)
            return getKey();
        else if (i == 1)
            return getValue();
        else
            throw new IndexOutOfBoundsException();
    }

    private IPersistentVector asVector() {
        return LazilyPersistentVector.createOwning(getKey(), getValue());
    }

    @Override
    public IPersistentVector assocN(int i, Object val) {
        return asVector().assocN(i, val);
    }

    @Override
    public int count() {
        return 2;
    }

    @Override
    public IPersistentVector cons(Object o) {
        return asVector().cons(o);
    }

    @Override
    public IPersistentCollection empty() {
        return null;
    }

    @Override
    public IPersistentStack pop() {
        return LazilyPersistentVector.createOwning(getKey());
    }

    @Override
    public Object setValue(Object value) {
        throw new UnsupportedOperationException();
    }

}
