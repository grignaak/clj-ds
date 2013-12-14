package persistent;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;

import persistent.Queue.QueueBuilder;

public class QueueTest {


    @Test
    public void testCursor() {
        Queue<Integer> list = Queue.<Integer>emptyQueue()
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
        Queue<Integer> list = Queue.<Integer>emptyQueue()
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
    public void testBuilder() {
        Queue<Integer> zero = Queue.emptyQueue();
        QueueBuilder<Integer> list = Queue.newBuilder();
//        list = list.minus(); // shouldn't throw
        
        list = zero.asBuilder().plus(1);
        list = list.minus();
        assertEquals(Arrays.asList(), toList(list.build()));
        
        list = zero.asBuilder().plus(1);
        list = list.plus(2);
        list = list.minus();
        assertEquals(Arrays.asList(2), toList(list.build()));
        
        list = zero.asBuilder().plus(1);
        list = list.plus(2);
        list = list.minus();
        list = list.plus(3);
        assertEquals(Arrays.asList(2,3), toList(list.build()));
    }
    
    private static <T> List<T> toList(Iterable<T> it) {
        List<T> list = new ArrayList<>();
        for (T t : it) list.add(t);
        return list;
    }
}
