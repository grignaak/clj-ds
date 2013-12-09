package com.github.krukow.clj_lang.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import com.github.krukow.clj_ds.PersistentMap;
import com.github.krukow.clj_lang.PersistentArrayMap;

public class PersistentNonHashMapsTest {

/*

PersistentArrayMap.java
PersistentStructMap.java
PersistentTreeMap.java

*/
	@Test
	public final void testArrayMap() {
		PersistentMap<String, Integer> am = PersistentArrayMap.empty()
		        .asTransient()
		        .plus("1", 1)
		        .plus("2", 2)
		        .plus("3", 3)
		        .plus("4", 4)
		        .plus("5", 5)
		        .persist();
		List<Map.Entry<String, Integer>> l = new ArrayList<Map.Entry<String,Integer>>();
		int i=1;
		for (Map.Entry<String, Integer> e:am.entrySet()) {
			l.add(e);
			assertEquals(i, (int) e.getValue());
			assertEquals(i+"", e.getKey());
			i += 1;
		}
		
		assertEquals(5, i);
		
	}
}
