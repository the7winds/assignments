package ru.spbau.mit;

import org.junit.Test;
import static org.junit.Assert.*;

public class PredicateTest {
    private static Predicate<Integer> grtr5 = new Predicate<Integer>() {
        @Override
        public Boolean apply(Integer arg) {
            return arg > 5;
        }
    };

    @Test
    public void applyTest() {
        for (int i = 6; i < 1000; ++i) {
            assertTrue(grtr5.apply(i));
        }

        for (int i = 5; i > -1000; --i) {
            assertFalse(grtr5.apply(i));
        }
    }

    @Test
    public void notTest() {
        for (int i = 6; i < 1000; ++i) {
            assertFalse(grtr5.not().apply(i));
        }

        for (int i = 5; i > -1000; --i) {
            assertTrue(grtr5.not().apply(i));
        }
    }

    @Test
    public void orTest() {
        for (int i = -1000; i < 1000; ++i) {
            assertTrue(grtr5.or(grtr5.not()).apply(i));
        }
    }

    @Test
    public void andTest() {
        for (int i = -1000; i < 1000; ++i) {
            assertFalse(grtr5.and(grtr5.not()).apply(i));
        }
    }
}
