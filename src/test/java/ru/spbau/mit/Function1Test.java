package ru.spbau.mit;

import org.junit.Test;
import static org.junit.Assert.*;

public class Function1Test {
    private static Function1<Integer, Integer> inc = new Function1<Integer, Integer>() {
        @Override
        public Integer apply(Integer arg) {
            return arg + 1;
        }
    };

    @Test
    public void applyTest01() {
        for (int i = -100; i < 100; i++) {
            assertTrue(i + 1 == inc.apply(i));
        }
    }

    @Test
    public void composeTest01() {
        for (int i = -100; i < 100; i++) {
            assertTrue(i + 2 == inc.compose(inc).apply(i));
        }
    }

    private Function1<Integer, Boolean> odd = new Function1<Integer, Boolean>() {
        @Override
        public Boolean apply(Integer arg) {
            return arg % 2 == 0;
        }
    };

    @Test
    public void applyTest02() {
        for (int i = -100; i < 100; i++) {
            assertTrue(odd.apply(2 * i));
            assertFalse(odd.apply(2 * i + 1));
        }
    }

    @Test
    public void composeTest02() {
        for (int i = -100; i < 100; i++) {
            assertTrue(odd.apply(i) == inc.compose(inc).compose(odd).apply(i));
        }
    }
}