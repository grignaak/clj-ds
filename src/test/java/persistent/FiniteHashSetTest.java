/**
 * 
 */
package persistent;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;

import org.junit.Test;

/**
 * @author krukow
 * 
 */
public class FiniteHashSetTest {

    @Test
    public final void testEmptyVector() {
        FiniteSet<Integer> vecI = Containers.emptyHashSet();
        assertEquals(0, vecI.size());
        FiniteSet<String> vecS = Containers.emptyHashSet();
        assertEquals(0, vecS.size());
        assertEquals(vecI, vecS);
    }

    /**
     * * NB: this methods takes a long time to run. Be patient.
     */
    @Test
    public final void testIterator() {
        FiniteSet<Integer> dsSet = Containers.emptyHashSet();
        HashSet<Integer> hs = null;
        for (int i = 0; i < 20000; i++) {
            hs = new HashSet<Integer>();
            for (Integer o : dsSet) {
                hs.add(o);
            }
            assertEquals(i, hs.size());
            Integer o = new Integer(i);
            dsSet = (FiniteSet<Integer>) dsSet.plus(o);

        }

    }

}
