/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

/* rich Mar 25, 2006 3:54:03 PM */

package com.github.krukow.clj_lang;

import java.util.concurrent.Callable;

public interface IFn extends Callable, Runnable{

public Object invoke() ;

public Object invoke(Object arg1) ;

public Object invoke(Object arg1, Object arg2) ;

public Object invoke(Object arg1, Object arg2, Object arg3) ;
}
