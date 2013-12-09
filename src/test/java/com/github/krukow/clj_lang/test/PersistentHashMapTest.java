/**
 * 
 */
package com.github.krukow.clj_lang.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.Test;

import com.github.krukow.clj_ds.PersistentMap;
import com.github.krukow.clj_lang.PersistentHashMap;

/**
 * @author krukow
 *
 */
public class PersistentHashMapTest {


	/**
	 * Test method for {@link com.github.krukow.clj_lang.PersistentHashMap#emptyMap()}.
	 */
	@Test
	public final void testEmptyMap() {
		PersistentHashMap<String, Integer> genMap = PersistentHashMap.emptyMap();
		assertEquals(0, genMap.size());
		PersistentHashMap<Number, Boolean> genMap2 = PersistentHashMap.emptyMap();
		assertEquals(0, genMap2.size());
		assert(genMap == (PersistentHashMap) genMap2);
	}

	
	@Test
	public final void testNullMap() {
		PersistentHashMap<String, Integer> genMap = PersistentHashMap.emptyMap();
		genMap = (PersistentHashMap<String, Integer>) genMap.plus(null, 42);
		genMap = (PersistentHashMap<String, Integer>) genMap.plus("43", 43);
		Iterator<Entry<String, Integer>> iterator = genMap.entrySet().iterator();
		assertTrue(iterator.hasNext());
		assertEquals(42, (int) genMap.get(null));
		int count=0;
		boolean nullKey = false;
		for (Map.Entry<String,Integer> e: genMap.entrySet()) {
			count+=1;
			if (e.getKey()==null) {
				nullKey = true;
				assertEquals(42, (int)e.getValue()); 
			} else {
				assertEquals(43, (int)e.getValue());
			}
			
		}
		 assertEquals(2, count);
		 assertTrue(nullKey);
	}
//
//	
//	@Test
//	public final void testIteratorFrom() {
//		final int N = 20;
//		IPersistentMap<Integer, Integer> genMap = PersistentHashMap.emptyMap();
//		for (int i=0;i<N;i++) {
//			Integer random = (int) Math.ceil(1000*Math.random());
//			while (genMap.containsKey(random)) {
//				random = (int) Math.ceil(1000*Math.random());
//			}
//			genMap = genMap.assoc(random, random);
//			
//		}
//		
//		List<Integer> l = new ArrayList<Integer>(20);
//		for (Map.Entry<Integer, Integer> e: genMap) {
//			l.add(e.getKey());
//		}
//		
//		assertEquals(20, l.size());
//		
//		
//		int index = 10;
//		int count = 0;
//		for (Iterator<Map.Entry<Integer, Integer>> iterator = genMap.iteratorFrom(l.get(index)); iterator.hasNext();) {
//			Entry<Integer, Integer> next = iterator.next();
//			assertEquals(l.get(index), next.getKey());
//			index++;
//			count++;
//		}
//		assertEquals(10, count);
//		
//		
//	}

	/**
	 * Test method for {@link com.github.krukow.clj_lang.PersistentHashMap#create(java.util.Map)}.
	 */
	@Test
	public final void testCreateMapOfQextendsKQextendsV() {
		Map<String, Integer> input = new TreeMap<String, Integer>();
		int N = 10;
		for (int i=0;i<N;i++) {
			input.put(String.valueOf(('A'+i)), i);
		}
		PersistentHashMap<String,Integer> output = PersistentHashMap.create(input);
		
		for (int i=0;i<N;i++) {
			assertEquals(i,  (int) output.get(String.valueOf(('A'+i))));
		}
		assertEquals(N, output.size());
		
		input = Collections.EMPTY_MAP;
		output = PersistentHashMap.create(input);
		assertEquals(0, output.size());
		
	}

	/**
	 * Test method for {@link com.github.krukow.clj_lang.PersistentHashMap#create(java.lang.Object[])}.
	 */
	@Test
	public final void testCreateObjectArray() {
		PersistentHashMap<Integer, Boolean> ib = PersistentHashMap.create(1,false,2,true,3,false);
		assertEquals(false, ib.get(1));
		assertEquals(true, ib.get(2));
		assertEquals(false, ib.get(3));
		
	}

	/**
	 * Test method for {@link com.github.krukow.clj_lang.PersistentHashMap#create(java.lang.Object[])}.
	 */
	@Test(expected=ClassCastException.class)
	public final void testBadInvocCreateObjectArray() {
		PersistentHashMap<Integer, Boolean> bad = PersistentHashMap.create(1,false,2,"true",3,false);
		Boolean b = bad.get(2);
	}
	

	/**
	 * NB: this methods takes a long time to run. Be patient.
	 * Test method for {@link com.github.krukow.clj_lang.PersistentHashMap#iterator()}.
	 */
	@Test
	public final void testIterator() {
		PersistentMap<Integer, Integer> dsMap = PersistentHashMap.emptyMap();
		HashSet<Integer> hs = null;
		for (int i = 0; i < 33000; i++) {
			hs = new HashSet<Integer>();
			for (Map.Entry<Integer, Integer> o : dsMap.entrySet()) {
				hs.add(o.getKey());
				assertEquals(o.getKey(), o.getValue());
			}
			assertEquals(i, hs.size());
			Integer o = new Integer(i);
			dsMap = dsMap.plus(o, o);
		}
		
	}

	@Test
	public final void testRandomIterator() {
		final int N = 33000;
		PersistentMap<Double, Double> genMap = PersistentHashMap.emptyMap();
		for (int i=0;i<N;i++) {
			double random = Math.random();
			genMap = genMap.plus(random, random);
			
		}
		HashSet<Double> hs = new HashSet<Double>();
		for (Map.Entry<Double, Double> e: genMap.entrySet()) {
			hs.add(e.getKey());
		}
		assertEquals(N, hs.size());
		
	}
	
	@Test
	public final void testRandomReverseIterator() {
		final int N = 33000;
		PersistentMap<Double, String> genMap = PersistentHashMap.emptyMap();
		for (int i=0;i<N;i++) {
			double random = Math.random();
			genMap = genMap.plus(random, ""+random);
			
		}
		List lst = new ArrayList();
		for (Map.Entry<Double, String> e: genMap.entrySet()) {
			lst.add(e.getKey());
		}
		assertEquals(33000, lst.size());
		
	}
	
	
}
