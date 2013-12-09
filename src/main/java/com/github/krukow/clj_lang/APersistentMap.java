/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

package com.github.krukow.clj_lang;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;

import com.github.krukow.clj_ds.PersistentMap;

@Deprecated
public abstract class APersistentMap<K, V>
extends AbstractMap<K, V>
implements PersistentMap<K, V>, Map<K, V>, Serializable {
}