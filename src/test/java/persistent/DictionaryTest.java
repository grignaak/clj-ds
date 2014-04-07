/**
 * 
 */
package persistent;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author krukow
 * 
 */
@RunWith(Parameterized.class)
public class DictionaryTest {

    @Parameter
    public Dictionary<?, ?> zero;
    
    @Parameters
    public static List<Object[]> parameters() {
        return Arrays.asList(new Object[][] {
                {HashDictionary.emptyDictionary()},
                {TreeDictionary.emptyDictionary(new ComparatorWithNull<Integer>())}
        });
    }
    
    public static class ComparatorWithNull<T extends Comparable<T>> implements Comparator<T> {
        @Override
        @SuppressWarnings("unchecked")
        public int compare(T o1, T o2) {
            if (o1 != null && o2 != null)
                return ((Comparator<T>)DefaultComparator.DEFAULT_COMPARATOR).compare(o1, o2);
            if (o1 == null)
                return (o2 == null) ? 0 : -1;
            return 1;
        }
        
    }
    
    @SuppressWarnings("unchecked")
    private <K> Dictionary<K,Integer> zero(Class<K> keyType) {
        return (Dictionary<K,Integer>)zero;
    }
    
    /**
     * Test method for
     * {@link com.github.krukow.clj_lang.HashDictionary#emptyDictionary()}.
     */
    @Test
    public final void testEmptyMap() {
        HashDictionary<String, Integer> genMap = HashDictionary.emptyDictionary();
        assertEquals(0, genMap.size());
        HashDictionary<Number, Boolean> genMap2 = HashDictionary.emptyDictionary();
        assertEquals(0, genMap2.size());
        assertSame(genMap, genMap2);
    }

    @Test
    public final void testNullMap() {
        Dictionary<String, Integer> genMap = zero(String.class)
            .plus(null, 42)
            .plus("43", 43);
        Iterator<Entry<String, Integer>> iterator = genMap.entrySet().iterator();
        assertTrue(iterator.hasNext());
        assertEquals(42, (int) genMap.get(null));
        int count = 0;
        boolean nullKey = false;
        for (Map.Entry<String, Integer> e : genMap.entrySet()) {
            count += 1;
            if (e.getKey() == null) {
                nullKey = true;
                assertEquals(42, (int) e.getValue());
            } else {
                assertEquals(43, (int) e.getValue());
            }

        }
        assertEquals(2, count);
        assertTrue(nullKey);
    }

    /**
     * Test method for
     * {@link com.github.krukow.clj_lang.HashDictionary#create(java.util.Map)}
     * .
     */
    @Test
    public final void testCreateMapOfQextendsKQextendsV() {
        Map<String, Integer> input = new TreeMap<String, Integer>();
        int N = 10;
        for (int i = 0; i < N; i++) {
            input.put(String.valueOf(('A' + i)), i);
        }
        HashDictionary<String, Integer> output = HashDictionary.create(input);

        for (int i = 0; i < N; i++) {
            assertEquals(i, (int) output.get(String.valueOf(('A' + i))));
        }
        assertEquals(N, output.size());

        input = Collections.EMPTY_MAP;
        output = HashDictionary.create(input);
        assertEquals(0, output.size());

    }

    /**
     * Test method for
     * {@link com.github.krukow.clj_lang.HashDictionary#create(java.lang.Object[])}
     * .
     */
    @Test
    public final void testCreateObjectArray() {
        Dictionary<Integer, Integer> ib = zero(Integer.class).asBuilder()
                .plus(1, 1000)
                .plus(2, 2000)
                .plus(3, 3000)
                .build();
        assertEquals((Integer)1000, ib.get(1));
        assertEquals((Integer)2000, ib.get(2));
        assertEquals((Integer)3000, ib.get(3));
    }

    /**
     * NB: this methods takes a long time to run. Be patient. Test method for
     * {@link com.github.krukow.clj_lang.HashDictionary#iterator()}.
     */
    @Test
    @Ignore
    public final void testIterator() {
        Dictionary<Integer, Integer> dsMap = zero(Integer.class);
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
    public final void testIteratorWithoutHasNext() {
        Dictionary<Integer, Integer> dsMap = zero(Integer.class);
        for (int i = 0; i < 1000; i++) {
            dsMap = dsMap.plus(i, i);
        }
        Iterator<?> it = dsMap.iterator();
        for (int i = 0; i < 1000; i++) {
            try {
                assertNotNull(it.next());
            } catch (Throwable e) {
                System.out.println(i);
                throw e;
            }
        }
    }
    
    @Test
    public final void testReplace() {
        Integer origValue = 1001;
        Integer newValue = 3000;
        
        Dictionary<Integer, Integer> m = zero(Integer.class)
                .plus(1, origValue)
                .replace(1, 0, newValue);
        assertEquals(origValue, m.get(1));
        
        m = zero(Integer.class).asBuilder()
                .plus(1, origValue)
                .replace(1, 0, newValue)
                .build();
        assertEquals(origValue, m.get(1));
        
        m = zero(Integer.class)
                .plus(null, origValue)
                .replace(null, 0, newValue);
        assertEquals(origValue, m.get(null));
        
        m = zero(Integer.class).asBuilder()
                .plus(null, origValue)
                .replace(null, 0, newValue)
                .build();
        assertEquals(origValue, m.get(null));
        
        m = zero(Integer.class)
                .plus(1, origValue)
                .replace(1, origValue, newValue);
        assertEquals(newValue, m.get(1));
        
        m = zero(Integer.class).asBuilder()
                .plus(1, origValue)
                .replace(1, origValue, newValue)
                .build();
        assertEquals(newValue, m.get(1));

        m = zero(Integer.class)
                .plus(null, origValue)
                .replace(null, origValue, newValue);
        assertEquals(newValue, m.get(null));
        
        m = zero(Integer.class).asBuilder()
                .plus(null, origValue)
                .replace(null, origValue, newValue)
                .build();
        assertEquals(newValue, m.get(null));
    }
    
    @Test
    public final void testPlusIfAbsent() {
        Integer origValue = 1001;
        Integer newValue = 3000;
        
        Dictionary<Integer, Integer> m = zero(Integer.class)
                .plus(1, origValue)
                .plusIfAbsent(1, newValue);
        assertEquals(origValue, m.get(1));
        
        m = zero(Integer.class).asBuilder()
                .plus(1, origValue)
                .plusIfAbsent(1, newValue)
                .build();
        assertEquals(origValue, m.get(1));
        
        m = zero(Integer.class)
                .plus(null, origValue)
                .plusIfAbsent(null, newValue);
        assertEquals(origValue, m.get(null));
        
        m = zero(Integer.class).asBuilder()
                .plus(null, origValue)
                .plusIfAbsent(null, newValue)
                .build();
        assertEquals(origValue, m.get(null));
        
        m = zero(Integer.class)
                .plusIfAbsent(1, newValue);
        assertEquals(newValue, m.get(1));
        
        m = zero(Integer.class).asBuilder()
                .plusIfAbsent(1, newValue)
                .build();
        assertEquals(newValue, m.get(1));
        
        m = zero(Integer.class)
                .plusIfAbsent(null, newValue);
        assertEquals(newValue, m.get(null));
        
        m = zero(Integer.class).asBuilder()
                .plusIfAbsent(null, newValue)
                .build();
        assertEquals(newValue, m.get(null));
    }
    
    @Test
    public final void testMinusIfExists_singular() {
        Integer origValue = 1001;
        Integer newValue = 3000;
        
        Dictionary<Integer, Integer> m = zero(Integer.class)
                .plus(1, origValue)
                .minus(1, newValue);
        assertEquals(origValue, m.get(1));
        assertFalse(m.isEmpty());
        
        m = zero(Integer.class).asBuilder()
                .plus(1, origValue)
                .minus(1, newValue)
                .build();
        assertEquals(origValue, m.get(1));
        assertFalse(m.isEmpty());
        
        m = zero(Integer.class)
                .plus(null, origValue)
                .minus(null, newValue);
        assertEquals(origValue, m.get(null));
        assertFalse(m.isEmpty());
        
        m = zero(Integer.class).asBuilder()
                .plus(null, origValue)
                .minus(null, newValue)
                .build();
        assertEquals(origValue, m.get(null));
        assertFalse(m.isEmpty());
        
        m = zero(Integer.class)
                .plus(1, origValue)
                .minus(1, origValue);
        assertTrue(m.isEmpty());
        assertEquals(null, m.get(1));
        
        m = zero(Integer.class).asBuilder()
                .plus(1, origValue)
                .minus(1, origValue)
                .build();
        assertEquals(null, m.get(1));
        assertTrue(m.isEmpty());
        
        m = zero(Integer.class)
                .plus(null, origValue)
                .minus(null, origValue);
        assertTrue(m.isEmpty());
        assertEquals(null, m.get(null));
        
        m = zero(Integer.class).asBuilder()
                .plus(null, origValue)
                .minus(null, origValue)
                .build();
        assertEquals(null, m.get(null));
        assertTrue(m.isEmpty());
    }
    
    @Test
    public final void testMinusIfExists_multiple() {
        Integer origValue = 1001;
        Integer newValue = 3000;
        
        Dictionary<Integer, Integer> m = zero(Integer.class)
                .plus(2, 2)
                .plus(1, origValue)
                .minus(1, newValue);
        assertEquals(origValue, m.get(1));
        assertEquals(2, m.size());
        
        m = zero(Integer.class).asBuilder()
                .plus(2, 2)
                .plus(1, origValue)
                .minus(1, newValue)
                .build();
        assertEquals(origValue, m.get(1));
        assertEquals(2, m.size());
        

        m = zero(Integer.class)
                .plus(2, 2)
                .plus(null, origValue)
                .minus(null, newValue);
        assertEquals(origValue, m.get(null));
        assertEquals(2, m.size());
        
        m = zero(Integer.class).asBuilder()
                .plus(2, 2)
                .plus(null, origValue)
                .minus(null, newValue)
                .build();
        assertEquals(origValue, m.get(null));
        assertEquals(2, m.size());
        
        m = zero(Integer.class)
                .plus(2, 2)
                .plus(1, origValue)
                .minus(1, origValue);
        assertEquals(null, m.get(1));
        assertEquals(1, m.size());
        
        m = zero(Integer.class).asBuilder()
                .plus(2, 2)
                .plus(1, origValue)
                .minus(1, origValue)
                .build();
        assertEquals(null, m.get(1));
        assertEquals(1, m.size());
        
        m = zero(Integer.class)
                .plus(2, 2)
                .plus(null, origValue)
                .minus(null, origValue);
        assertEquals(null, m.get(null));
        assertEquals(1, m.size());
        
        m = zero(Integer.class).asBuilder()
                .plus(2, 2)
                .plus(null, origValue)
                .minus(null, origValue)
                .build();
        assertEquals(null, m.get(null));
        assertEquals(1, m.size());
    }
    

    @Test
    public final void testRandomIterator() {
        final int N = 33000;
        Dictionary<Double, Integer> genMap = zero(Double.class);
        for (int i = 0; i < N; i++) {
            double random = Math.random();
            genMap = genMap.plus(random, (int)random);

        }
        HashSet<Double> hs = new HashSet<Double>();
        for (Map.Entry<Double, Integer> e : genMap.entrySet()) {
            hs.add(e.getKey());
        }
        assertEquals(N, hs.size());

    }
}
