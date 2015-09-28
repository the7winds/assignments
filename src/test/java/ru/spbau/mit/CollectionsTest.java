package ru.spbau.mit;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

public class CollectionsTest {
    private static Predicate<Integer> odd = new Predicate<Integer>() {
        @Override
        public Boolean apply(Integer arg) {
            return arg % 2 == 0;
        }
    };

    @Test
    public void testMap() {
        final int size = 10;

        ArrayList<Integer> li = new ArrayList<Integer>();

        for (int i = 0; i < size; ++i) {
            li.add(i);
        }

        ArrayList<Boolean> lb = new ArrayList<Boolean>();

        Collections.map(odd, li, lb);

        for (int i = 0; i < size; ++i) {
            assertTrue(odd.apply(i) == lb.get(i));
        }
    }

    @Test
    public void testFilter() {
        final int size = 10;

        ArrayList<Integer> li1 = new ArrayList<Integer>();

        for (int i = 0; i < size; ++i) {
            li1.add(i);
        }

        ArrayList<Integer> li2 = new ArrayList<Integer>();

        Collections.filter(odd, li1, li2);

        for (Integer aLi2 : li2) {
            assertTrue(odd.apply(aLi2));
        }
    }

    @Test
    public void takeUnless() {
        final int size = 10;

        ArrayList<Integer> li1 = new ArrayList<Integer>();

        for (int i = 0; i < size; ++i) {
            li1.add(1);
        }

        for (int i = 0; i < size; ++i) {
            li1.add(2);
        }

        ArrayList<Integer> li2 = new ArrayList<Integer>();

        Collections.takeUnless(odd, li1, li2);

        assertTrue(li2.size() == size);
    }

    @Test
    public void takeWhile() {
        final int size = 10;

        ArrayList<Integer> li1 = new ArrayList<Integer>();

        for (int i = 0; i < size; ++i) {
            li1.add(1);
        }

        for (int i = 0; i < size; ++i) {
            li1.add(2);
        }

        ArrayList<Integer> li2 = new ArrayList<Integer>();

        Collections.takeWhile(odd.not(), li1, li2);

        assertTrue(li2.size() == size);
    }

    private static Function2<Integer, Integer, Integer> sub =
            new Function2<Integer, Integer, Integer> () {
                public Integer apply(Integer a, Integer b) {
                    return a - b;
                }
            };

    @Test
    public void foldl() {
        final int size = 10;

        ArrayList<Integer> li = new ArrayList<Integer>();

        for (int i = 0; i < size; ++i) {
            li.add(i);
        }

        assertTrue(-45 == Collections.foldl(sub, 0, li));
    }

    @Test
    public void foldr() {
        final int size = 10;

        ArrayList<Integer> li = new ArrayList<Integer>();

        for (int i = 0; i < size; ++i) {
            li.add(i);
        }

        assertTrue(4 == Collections.foldr(sub, 9, li));
    }


}
