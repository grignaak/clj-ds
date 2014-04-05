/**
 * 
 */
package persistent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.HashSet;
import java.util.ListIterator;

import org.junit.Test;

import persistent.TrieVector;

/**
 * @author krukow
 *
 */
public class TrieVectorTest {

	@Test
	public final void testEmptyVector() {
		TrieVector<Integer> vecI = TrieVector.emptyVector();
		assertEquals(0, vecI.size());
		TrieVector<String> vecS = TrieVector.emptyVector();
		assertEquals(0, vecS.size());
		assertSame(vecI, vecS);
		
		ListIterator<Integer> abc = Arrays.asList(1, 2, 3).listIterator(1);
		System.out.println(abc.previous());
		System.out.println(abc.next());
	}

	/**
	 *  * NB: this methods takes a long time to run. Be patient.
	 */
	@Test
	public final void testIterator() {
		TrieVector<Integer> vec = TrieVector.emptyVector();
		HashSet<Integer> hs = null;
		int N = 32*32*32+33;
		//Checking all states up to: N
		for (int i = 0; i < N; i++) {
			hs = new HashSet<Integer>();
			int expected = 0;
			for (Integer o : vec) {
				assert(expected == o);
				expected += 1;
				hs.add(o);
			}
			assertEquals(i,hs.size());
			Integer o = new Integer(i);
			vec = vec.plus(o);
		}	
	}
}
