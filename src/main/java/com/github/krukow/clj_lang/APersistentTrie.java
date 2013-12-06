/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse private License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Mar 3, 2008 */

package com.github.krukow.clj_lang;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({ "rawtypes", "serial" })
public abstract class APersistentTrie<T> implements IPersistentTrie<T>, 
									IPersistentSet, Collection<Map.Entry<String, T>>, Set<Map.Entry<String, T>>, Serializable {
int _hash = -1;

private Object invoke(Object arg1) {
	return get((String) arg1);
}

public boolean equals(Object obj){
	if(this == obj) return true;
	if(!(obj instanceof Set))
		return false;
	Set m = (Set) obj;

	if(m.size() != count() || m.hashCode() != hashCode())
		return false;

	for(Object aM : m)
		{
		if(!contains(aM))
			return false;
		}

	return true;
}

public int hashCode(){
	if(_hash == -1)
		{
		int hash = 0;
		for (Object e : this) {
			hash +=  Util.hash(e);
			}
		this._hash = hash;
		}
	return _hash;
}

public Object[] toArray(){
	return RT.seqToArray(this);
}

public Object[] toArray(Object[] a){
    return RT.seqToPassedArray(this, a);
}

public int size(){
	return count();
}

public boolean isEmpty(){
	return count() == 0;
}


}
