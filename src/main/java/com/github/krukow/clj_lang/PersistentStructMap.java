/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Dec 16, 2007 */

package com.github.krukow.clj_lang;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

public class PersistentStructMap<K,V> extends APersistentMap<K,V> {

public static class Def implements Serializable{
	final ISeq keys;
	final IPersistentMap keyslots;

	Def(ISeq keys, IPersistentMap keyslots){
		this.keys = keys;
		this.keyslots = keyslots;
	}
}

final Def def;
final Object[] vals;
final IPersistentMap<K,V> ext;


static public Def createSlotMap(ISeq keys){
	if(keys == null)
		throw new IllegalArgumentException("Must supply keys");
	int c = RT.count(keys);
	Object[] v = new Object[2*c];
	int i = 0;
	for(ISeq s = keys; s != null; s = s.next(), i++)
		{
		v[2*i] =  s.first();
		v[2*i+1] = i;
		}
	return new Def(keys, map(v));
}

static public <K,V> IPersistentMap<K,V> map(Object... init){
	if(init == null)
		return PersistentArrayMap.EMPTY;
	else if(init.length <= PersistentArrayMap.HASHTABLE_THRESHOLD)
		return PersistentArrayMap.createWithCheck(init);
	return PersistentHashMap.create(init);
}


static public <K,V> PersistentStructMap<K,V> create(Def def, ISeq keyvals){
	Object[] vals = new Object[def.keyslots.count()];
	IPersistentMap<K,V> ext = PersistentHashMap.EMPTY;
	for(; keyvals != null; keyvals = keyvals.next().next())
		{
		if(keyvals.next() == null)
			throw new IllegalArgumentException(String.format("No value supplied for key: %s", keyvals.first()));
		K k = (K) keyvals.first();
		V v = (V) RT.second(keyvals);
		Map.Entry<K,Integer> e = def.keyslots.entryAt(k);
		if(e != null)
			vals[e.getValue()] = v;
		else
			ext = ext.assoc(k, v);
		}
	return new PersistentStructMap<K,V>(def, vals, ext);
}

static public <K,V> PersistentStructMap<K,V> construct(Def def, ISeq<V> valseq){
	Object[] vals = new Object[def.keyslots.count()];
	IPersistentMap<K,V> ext = PersistentHashMap.EMPTY;
	for(int i = 0; i < vals.length && valseq != null; valseq = valseq.next(), i++)
		{
		vals[i] = valseq.first();
		}
	if(valseq != null)
		throw new IllegalArgumentException("Too many arguments to struct constructor");
	return new PersistentStructMap<K,V>(def, vals, ext);
}

//static public <K,V> IFn getAccessor(final Def def, K key){
//	Map.Entry<K,Integer> e = def.keyslots.entryAt(key);
//	if(e != null)
//		{
//		final int i = e.getValue();
//		return new AFn(){
//			public Object invoke(Object arg1) {
//				PersistentStructMap<K,V> m = (PersistentStructMap<K,V>) arg1;
//				if(m.def != def)
//					throw Util.runtimeException("Accessor/struct mismatch");
//				return m.vals[i];
//			}
//		};
//		}
//	throw new IllegalArgumentException("Not a key of struct");
//}

protected PersistentStructMap(Def def, Object[] vals, IPersistentMap<K,V> ext){
	this.ext = ext;
	this.def = def;
	this.vals = vals;
}

/**
 * Returns a new instance of PersistentStructMap using the given parameters.
 * This function is used instead of the PersistentStructMap constructor by
 * all methods that return a new PersistentStructMap.  This is done so as to
 * allow subclasses to return instances of their class from all
 * PersistentStructMap methods.
 */
protected PersistentStructMap<K,V> makeNew(Def def, Object[] vals, IPersistentMap<K,V> ext){
	return new PersistentStructMap<K,V>(def, vals, ext);
}


public boolean containsKey(Object key){
	return def.keyslots.containsKey(key) || ((Map<K,V>)ext).containsKey(key);
}

public java.util.Map.Entry<K, V> entryAt(K key){
	Map.Entry<K,Integer> e = def.keyslots.entryAt(key);
	if(e != null)
		{
		return new MapEntry<K,V>(e.getKey(), (V) vals[e.getValue()]);
		}
	return ext.entryAt(key);
}

public IPersistentMap<K,V> assoc(K key, V val){
	Map.Entry<K,Integer> e = def.keyslots.entryAt(key);
	if(e != null)
		{
		int i = e.getValue();
		Object[] newVals = vals.clone();
		newVals[i] = val;
		return makeNew(def, newVals, ext);
		}
	return makeNew(def, vals, ext.assoc(key, val));
}

public V valAt(K key){
	Integer i = (Integer) def.keyslots.valAt(key);
	if(i != null)
		{
		return (V) vals[i];
		}
	return ext.valAt(key);
}

public V valAt(K key, V notFound){
	Integer i = (Integer) def.keyslots.valAt(key);
	if(i != null)
		{
		return (V) vals[i];
		}
	return ext.valAt(key, notFound);
}

public IPersistentMap<K,V> assocEx(K key, V val) {
	if(containsKey(key))
		throw Util.runtimeException("Key already present");
	return assoc(key, val);
}

public IPersistentMap<K,V> without(K key) {
	Map.Entry e = def.keyslots.entryAt(key);
	if(e != null)
		throw Util.runtimeException("Can't remove struct key");
	IPersistentMap<K,V> newExt = ext.without(key);
	if(newExt == ext)
		return this;
	return makeNew(def, vals, newExt);
}

public Iterator<Map.Entry<K, V>> iterator(){
	return new Iterator<Map.Entry<K, V>>() {
		
		ISeq<Map.Entry<K, V>> seq = seq();

		public boolean hasNext() {
			return seq != null;
		}

		public java.util.Map.Entry<K, V> next() {
			Entry<K,V> first = seq.first();
			seq = seq.next();
			return first;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}		
	};
}

public Iterator<Map.Entry<K, V>> reverseIterator(){
	return new Iterator<Map.Entry<K, V>>() {
		
		Iterator<Map.Entry<K, V>> mapIter = ext.reverseIterator();
		Object[] keys = RT.seqToArray(def.keys);
		int index = keys.length;
		
		public boolean hasNext() {
			return mapIter.hasNext() || index > 0;
		}

		public java.util.Map.Entry<K, V> next() {
			if (mapIter.hasNext()) {
				return mapIter.next();
			}
			index -= 1;
			return new MapEntry(keys[index],vals[index]);
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}		
	};
}

public Iterator<java.util.Map.Entry<K, V>> iteratorFrom(K key) {
	Map.Entry<K,Integer> e = def.keyslots.entryAt(key);
	if(e != null) {	
		final int start = e.getValue();
		return new Iterator<java.util.Map.Entry<K, V>>() {
			int index = start;
			final Object[] keys = RT.seqToArray(def.keys);
			Iterator<Map.Entry<K, V>> extIt = ext.iterator();
			
			public boolean hasNext() {
				return index < vals.length || extIt.hasNext();
			}

			public java.util.Map.Entry<K, V> next() {
				if (index < vals.length) {
					return new MapEntry(keys[index], vals[index++]);
				}
				return extIt.next();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	} else {
		return ext.iteratorFrom(key);
	}
}

public int count(){
	return vals.length + RT.count(ext);
}

public ISeq<Map.Entry<K, V>> seq(){
	return new Seq(def.keys, vals, 0, ext);
}

public IPersistentCollection empty(){
	return construct(def, null);
}

static class Seq extends ASeq{
	final int i;
	final ISeq keys;
	final Object[] vals;
	final IPersistentMap ext;


	public Seq(ISeq keys, Object[] vals, int i, IPersistentMap ext){
		this.i = i;
		this.keys = keys;
		this.vals = vals;
		this.ext = ext;
	}

	public Object first(){
		return new MapEntry(keys.first(), vals[i]);
	}

	public ISeq next(){
		if(i + 1 < vals.length)
			return new Seq(keys.next(), vals, i + 1, ext);
		return ext.seq();
	}
}

}
