package ru.spbau.mit;

import org.junit.Test;
import static org.junit.Assert.*;

public class Function2Test {
    private static Function2<Integer, Integer, Integer> sum =
            new Function2<Integer, Integer, Integer> () {
                public Integer apply(Integer a, Integer b) {
                    return a + b;
                }
            };

    private static Function1<Integer, Integer> inc =
            new Function1<Integer, Integer> () {
                public Integer apply(Integer a) {
                    return a + 1;
                }
            };

    @Test
    public void alpplyTest() {
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                assertTrue(i + j == sum.apply(i, j));
            }
        }
    }

    @Test
    public void composeTest() {
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                assertTrue(i + j + 1 == sum.compose(inc).apply(j, i));
            }
        }
    }

    @Test
    public void bind1Test() {
        Function1<Integer, Integer> add5 = sum.bind1(5);

        for (int i = 0; i < 100; i++) {
            assertTrue(i + 5 == add5.apply(i));
        }
    }

    @Test
    public void bind2Test() {
        Function1<Integer, Integer> add5 = sum.bind2(5);

        for (int i = 0; i < 100; i++) {
            assertTrue(i + 5 == add5.apply(i));
        }
    }

    @Test
    public void curryTest() {
        Function1<Integer, Function1<Integer, Integer>> f1 = sum.curry();

        for (int i = 0; i < 100; i++) {
            assertTrue(2 * i == f1.apply(i).apply(i));
        }
    }
}