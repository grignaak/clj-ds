package com.github.krukow.clj_lang.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import com.github.krukow.clj_lang.PersistentArrayMap;

public class PersistentNonHashMapsTest {

/*

PersistentArrayMap.java
PersistentStructMap.java
PersistentTreeMap.java

*/
	@Test
	public final void testArrayMap() {
		PersistentArrayMap<String, Integer> am = PersistentArrayMap.createWithCheck(new Object[]{
				"1",1,"2",2,"3",3,"4",4,"5",5});
		List<Map.Entry<String, Integer>> l = new ArrayList<Map.Entry<String,Integer>>();
		int i=1;
		for (Map.Entry<String, Integer> e:am) {
			l.add(e);
			assertEquals(i, (int) e.getValue());
			assertEquals(i+"", e.getKey());
			i += 1;
		}
		
		i=3;
		for (Iterator<Entry<String, Integer>> it = am.iteratorFrom("3");it.hasNext();) {
			System.out.println(l.get(i));
			assertEquals(l.get(i++), it.next());
		}
		assertEquals(5, i);
		
		for(Iterator<Entry<String, Integer>> rit = am.reverseIterator();rit.hasNext();) {
			assertEquals(l.get(--i), rit.next());
		}
		assertEquals(0,i);
		
	}
}
