/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Apr 19, 2008 */

package com.github.krukow.clj_lang;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Comparator;

public class Util {

    static final class DefaultComparator<T extends Comparable<T>> implements Comparator<T>, Serializable {
        private static final long serialVersionUID = 1L;
        
        public int compare(T o1, T o2) {
            return o1.compareTo(o2);
        }
    
        private Object readResolve() throws ObjectStreamException {
            // ensures that we aren't hanging onto a new default comparator for
            // every
            // sorted set, etc., we deserialize
            return Util.DEFAULT_COMPARATOR;
        }
    }

    @Deprecated
    static public RuntimeException runtimeException(String s) {
        return new RuntimeException(s);
    }

    @Deprecated
    static public RuntimeException runtimeException(String s, Throwable e) {
        return new RuntimeException(s, e);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    static public final Comparator<? extends Comparable<?>> DEFAULT_COMPARATOR = new DefaultComparator();
}