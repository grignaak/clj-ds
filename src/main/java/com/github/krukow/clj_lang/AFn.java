/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Mar 25, 2006 4:05:37 PM */

package com.github.krukow.clj_lang;

@Deprecated
public abstract class AFn implements IFn {

@Override
public Object call() {
	return invoke();
}

@Override
public void run(){
	try
		{
		invoke();
		}
	catch(Exception e)
		{
		throw Util.sneakyThrow(e);
		}
}



@Override
public Object invoke() {
	return throwArity(0);
}

@Override
public Object invoke(Object arg1) {
	return throwArity(1);
}

@Override
public Object invoke(Object arg1, Object arg2) {
	return throwArity(2);
}

@Override
public Object invoke(Object arg1, Object arg2, Object arg3) {
	return throwArity(3);
}

public Object throwArity(int n){
	String name = getClass().getSimpleName();
	int suffix = name.lastIndexOf("__");
	throw new ArityException(n, (suffix == -1 ? name : name.substring(0, suffix)).replace('_', '-'));
}
}
