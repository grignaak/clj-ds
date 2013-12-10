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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public abstract class ASeq<T> implements ISeq<T>, Sequential, List<T>, Serializable {
transient int _hash = -1;

public String toString(){
	return RT.printString(this);
}

@Override
public IPersistentCollection<T> empty() {
	return null;
}



protected ASeq(){
}

public boolean equiv(Object obj){

	if(!(obj instanceof Sequential || obj instanceof List))
		return false;
	ISeq ms = RT.seq(obj);
	for(ISeq s = seq(); s != null; s = s.next(), ms = ms.next())
		{
		if(ms == null || !Util.equals(s.first(), ms.first()))
			return false;
		}
	return ms == null;

}

public boolean equals(Object obj){
	if(this == obj) return true;
	if(!(obj instanceof Sequential || obj instanceof List))
		return false;
	ISeq ms = RT.seq(obj);
	for(ISeq s = seq(); s != null; s = s.next(), ms = ms.next())
		{
		if(ms == null || !Util.equals(s.first(), ms.first()))
			return false;
		}
	return ms == null;

}

public int hashCode(){
	if(_hash == -1)
		{
		int hash = 1;
		for(ISeq s = seq(); s != null; s = s.next())
			{
			hash = 31 * hash + (s.first() == null ? 0 : s.first().hashCode());
			}
		this._hash = hash;
		}
	return _hash;
}

public int count(){
	int i = 1;
	for(ISeq s = next(); s != null; s = s.next(), i++)
		if(s instanceof Counted)
			return i + s.count();
	return i;
}

final public ISeq<T> seq(){
	return this;
}

public ISeq<T> cons(T o){
	return new Cons<T>(o, this);
}

public ISeq<T> more(){
    ISeq<T> s = next();
    if(s == null)
        return (ISeq<T>) PersistentConsList.emptyList();
    return s;
}

//final public ISeq rest(){
//    Seqable m = more();
//    if(m == null)
//        return null;
//    return m.seq();
//}

// java.util.Collection implementation

public Object[] toArray(){
	return RT.seqToArray(this);
}

public boolean add(T o){
	throw new UnsupportedOperationException();
}

public boolean remove(Object o){
	throw new UnsupportedOperationException();
}

public boolean addAll(Collection c){
	throw new UnsupportedOperationException();
}

public void clear(){
	throw new UnsupportedOperationException();
}

public boolean retainAll(Collection c){
	throw new UnsupportedOperationException();
}

public boolean removeAll(Collection c){
	throw new UnsupportedOperationException();
}

public boolean containsAll(Collection c){
	for(Object o : c)
		{
		if(!contains(o))
			return false;
		}
	return true;
}

public Object[] toArray(Object[] a){
	return RT.seqToPassedArray(this, a);
}

public int size(){
	return count();
}

public boolean isEmpty(){
	return seq() == null;
}

public boolean contains(Object o){
	for(ISeq s = seq(); s != null; s = s.next())
		{
		if(Util.equals(s.first(), o))
			return true;
		}
	return false;
}


public Iterator iterator(){
	return new SeqIterator(this);
}



//////////// List stuff /////////////////
private List<T> reify(){
	return Collections.unmodifiableList(new ArrayList<T>(this));
}

public List<T> subList(int fromIndex, int toIndex){
	return reify().subList(fromIndex, toIndex);
}

public T set(int index, T element){
	throw new UnsupportedOperationException();
}

public T remove(int index){
	throw new UnsupportedOperationException();
}

public int indexOf(Object o){
	ISeq s = seq();
	for(int i = 0; s != null; s = s.next(), i++)
		{
		if(Util.equals(s.first(), o))
			return i;
		}
	return -1;
}

public int lastIndexOf(Object o){
	return reify().lastIndexOf(o);
}

public ListIterator<T> listIterator(){
	return reify().listIterator();
}

public ListIterator<T> listIterator(int index){
	return reify().listIterator(index);
}

public T get(int index){
	return (T) RT.nth(this, index);
}

public void add(int index, T element){
	throw new UnsupportedOperationException();
}

public boolean addAll(int index, Collection<? extends T> c){
	throw new UnsupportedOperationException();
}

}
