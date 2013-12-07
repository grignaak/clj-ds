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

import com.github.krukow.clj_ds.TransientMap;
import com.github.krukow.clj_ds.TransientSet;


public abstract class ATransientSet<T> implements TransientSet<T> {
	TransientMap<T, Boolean> impl;

	ATransientSet(TransientMap<T, Boolean> impl) {
		this.impl = impl;
	}
	
	public int count() {
		return impl.size();
	}

	public TransientSet<T> conj(T val) {
		TransientMap m = impl.plus(val, Boolean.TRUE);
		if (m != impl) this.impl = m;
		return this;
	}

	public boolean contains(T key) {
		return impl.containsKey(key);
	}

	public TransientSet<T> disjoin(T key)  {
		TransientMap m = impl.minus(key);
		if (m != impl) this.impl = m;
		return this;
	}
}
