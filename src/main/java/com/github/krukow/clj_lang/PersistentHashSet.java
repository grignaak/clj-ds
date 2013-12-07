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

import java.util.Iterator;
import java.util.List;

import com.github.krukow.clj_ds.PersistentMap;
import com.github.krukow.clj_ds.PersistentSet;
import com.github.krukow.clj_ds.TransientMap;
import com.github.krukow.clj_ds.TransientSet;

public class PersistentHashSet<T> extends APersistentSet<T> implements PersistentSet<T> {

static public final PersistentHashSet EMPTY = new PersistentHashSet(PersistentHashMap.EMPTY);

@SuppressWarnings("unchecked")
static public final <T> PersistentHashSet<T> emptySet() {
	return EMPTY;
}

public static <T> PersistentHashSet<T> create(T... init){
	PersistentHashSet<T> ret = EMPTY;
	for(int i = 0; i < init.length; i++)
		{
		ret = (PersistentHashSet<T>) ret.cons(init[i]);
		}
	return ret;
}

public static <T> PersistentHashSet<T> create(Iterable<? extends T> init){
	PersistentHashSet<T> ret = EMPTY;
	for(T key : init)
		{
		ret = (PersistentHashSet<T>) ret.cons(key);
		}
	return ret;
}

static public <T> PersistentHashSet<T> create(ISeq<? extends T> items){
	PersistentHashSet<T> ret = EMPTY;
	for(; items != null; items = items.next())
		{
		ret = (PersistentHashSet<T>) ret.cons(items.first());
		}
	return ret;
}

public static <T> PersistentHashSet<T> createWithCheck(T ... init){
	PersistentHashSet<T> ret = EMPTY;
	for(int i = 0; i < init.length; i++)
		{
		ret = (PersistentHashSet<T>) ret.cons(init[i]);
		if(ret.count() != i + 1)
			throw new IllegalArgumentException("Duplicate key: " + init[i]);
		}
	return ret;
}

public static <T> PersistentHashSet<T> createWithCheck(List<? extends T> init){
	PersistentHashSet<T> ret = EMPTY;
	int i=0;
	for(T key : init)
		{
		ret = (PersistentHashSet<T>) ret.cons(key);
		if(ret.count() != i + 1)
			throw new IllegalArgumentException("Duplicate key: " + key);		
		++i;
		}
	return ret;
}

static public <T> PersistentHashSet<T> createWithCheck(ISeq<? extends T> items){
	PersistentHashSet<T> ret = EMPTY;
	for(int i=0; items != null; items = items.next(), ++i)
		{
		ret = (PersistentHashSet<T>) ret.cons(items.first());
		if(ret.count() != i + 1)
			throw new IllegalArgumentException("Duplicate key: " + items.first());
		}
	return ret;
}

PersistentHashSet(PersistentMap<T, Boolean> impl){
	super(impl);
}

public Iterator<T> iterator(){
    return impl.keySet().iterator();
}


public PersistentHashSet<T> disjoin(T key) {
	if(contains(key))
		return new PersistentHashSet<T>(impl.minus(key));
	return this;
}

public PersistentHashSet<T> cons(T o){
	if(contains(o))
		return this;
	return new PersistentHashSet<T>(impl.plus(o,Boolean.TRUE));
}

public PersistentSet<T> empty(){
	return EMPTY;	
}

public TransientHashSet<T> asTransient() {
	return new TransientHashSet<T>(((PersistentHashMap) impl).asTransient());
}

static final class TransientHashSet<T> extends ATransientSet<T> implements TransientSet<T> {
	TransientHashSet(TransientMap impl) {
		super(impl);
	}

	public PersistentHashSet<T> persistent() {
		return new PersistentHashSet<T>(impl.persist());
	}
	
	@Override
	public PersistentSet<T> persist() {
		return persistent();
	}
	
	@Override
	public TransientSet<T> plus(T val) {
		return (TransientSet<T>) conj(val);
	}
	
	@Override
	public TransientSet<T> minus(T val) {
		return (TransientSet<T>) disjoin(val);
	}
}
	
	@Override
	public PersistentSet<T> zero() {
		return (PersistentSet<T>) empty();
	}
	
	@Override
	public PersistentSet<T> plus(T val) {
		return cons(val);
	}
	
	@Override
	public PersistentSet<T> minus(T val) {
		return disjoin(val);
	}

}
