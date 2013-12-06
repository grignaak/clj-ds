/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Jun 19, 2006 */

package com.github.krukow.clj_lang;

import java.lang.reflect.Array;

public class ArraySeq extends ASeq implements IndexedSeq {
public final Object array;
final int i;
final Object[] oa;
final Class ct;
//ISeq _rest;

static public ArraySeq create(){
	return null;
}

static public ArraySeq create(Object... array){
	if(array == null || array.length == 0)
		return null;
	return new ArraySeq(array, 0);
}

ArraySeq(Object array, int i){
	this.array = array;
	this.ct = array.getClass().getComponentType();
	this.i = i;
	this.oa = (Object[]) (array instanceof Object[] ? array : null);
//    this._rest = this;
}

public Object first(){
	if(oa != null)
		return oa[i];
	return Reflector.prepRet(ct, Array.get(array, i));
}

public ISeq next(){
	if(oa != null)
		{
		if(i + 1 < oa.length)
			return new ArraySeq(array, i + 1);
		}
	else
		{
		if(i + 1 < Array.getLength(array))
			return new ArraySeq(array, i + 1);
		}
	return null;
}

public int count(){
	if(oa != null)
		return oa.length - i;
	return Array.getLength(array) - i;
}

public int index(){
	return i;
}

public int lastIndexOf(Object o) {
	if (oa != null) {
		if (o == null) {
			for (int j = oa.length - 1 ; j >= i; j--)
				if (oa[j] == null) return j - i;
		} else {
			for (int j = oa.length - 1 ; j >= i; j--)
				if (o.equals(oa[j])) return j - i;
		}
	} else {
		if (o == null) {
			for (int j = Array.getLength(array) - 1 ; j >= i; j--)
				if (Reflector.prepRet(ct, Array.get(array, j)) == null) return j - i;
		} else {
			for (int j = Array.getLength(array) - 1 ; j >= i; j--)
				if (o.equals(Reflector.prepRet(ct, Array.get(array, j)))) return j - i;
		}
	}
	return -1;
}

}
