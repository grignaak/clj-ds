package persistent;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.junit.Test;

public class VectorTest {

    @Test
    public void testReplace() {
        Sequence<Integer> list = TrieVector.<Integer>emptyVector()
                .plus(1).plus(2).plus(3).plus(4).plus(5);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
        
        assertEquals(Arrays.asList(1, 2, 10, 4, 5), list.replace(2, 10));
        
        assertEquals(Arrays.asList(10, 2, 3, 4, 5), list.replace(0, 10));
        
        assertEquals(Arrays.asList(1, 2, 3, 4, 10), list.replace(4, 10));
        
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6), list.replace(5, 6));
    }

    @Test
    public void testCursor() {
        Sequence<Integer> list = TrieVector.<Integer>emptyVector()
                .plus(1).plus(2).plus(3).plus(4).plus(5);
        {
            Cursor<Integer> cursor = list.cursor();
            for (int i = 1; i <= 5; i++) {
                assertFalse(cursor.isDone());
                assertEquals(i, (int)cursor.head());
                cursor = cursor.tail();
            }
            assertTrue(cursor.isDone());
            
            /* Cursors don't fail when going passed the end */
            cursor = cursor.tail();
            assertTrue(cursor.isDone());
        }
        
        {
            Iterator<Integer> it = list.cursor().iterator();
            for (int i = 1; i <= 5; i++) {
                assertTrue(it.hasNext());
                assertEquals(i, (int)it.next());
            }
            assertFalse(it.hasNext());
            try {
                it.next();
                fail("Should have thrown");
            } catch (NoSuchElementException e) {
                // good
            }
        }
    }
    
    @Test
    public void testIterator() {
        Sequence<Integer> list = TrieVector.<Integer>emptyVector()
                .plus(1).plus(2).plus(3).plus(4).plus(5);
        
        Iterator<Integer> it = list.iterator();
        for (int i = 1; i <= 5; i++) {
            assertTrue(it.hasNext());
            assertEquals(i, (int)it.next());
        }
        assertFalse(it.hasNext());
        try {
            it.next();
            fail("Should have thrown");
        } catch (NoSuchElementException e) {
            // good
        }
    }
    

    @Test
    public void testListIterator() {
        int size = 130; // a little more than a 32-divisible vector
        Sequence<Integer> list = TrieVector.<Integer>emptyVector();
        for (int i = 1; i <= size; i++) {
            list = list.plus(i);
        }

        for (int start = 0; start <= size; start++) {
            ListIterator<Integer> it = list.listIterator(start);
            assertEquals(start, it.nextIndex());
            for (int i = start; i < size; i++) {
                assertEquals(i, it.nextIndex());
                assertTrue(it.hasNext());
                assertEquals(i+1, (int)it.next());
            }
            assertFalse(it.hasNext());
            try {
                it.next();
                fail("Should have thrown");
            } catch (NoSuchElementException e) {
                // good
            }
        }
        
        for (int end = 0; end <= size; end++) {
            ListIterator<Integer> it = list.listIterator(end);
            assertEquals(end-1, it.previousIndex());
            for (int i = end; i > 0; i--) {
                assertEquals(i-1, it.previousIndex());
                assertTrue(it.hasPrevious());
                assertEquals(i, (int)it.previous());
            }
            assertFalse(it.hasPrevious());
            try {
                it.previous();
                fail("Should have thrown");
            } catch (NoSuchElementException e) {
                // good
            }
        }
    }
}
