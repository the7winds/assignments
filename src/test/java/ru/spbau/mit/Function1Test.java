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
            assertEquals(Integer.valueOf(i + 1), inc.apply(i));
        }
    }

    @Test
    public void composeTest01() {
        for (int i = -100; i < 100; i++) {
            assertEquals(Integer.valueOf(i + 2), inc.compose(inc).apply(i));
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
            assertEquals(odd.apply(i), inc.compose(inc).compose(odd).apply(i));
        }
    }

    private static class A {
    }

    private static class B extends A {
    }

    private static class C extends B {
    }

    private static Function1<A, B> f1 = new Function1<A, B>() {
        @Override
        public B apply(A a) {
            return new B();
        }
    };

    private static Function1<A, C> f2 = new Function1<A, C>() {
        @Override
        public C apply(A a) {
            return new C();
        }
    };

    @Test
    public void composeTest03() {
        f1.compose(f2);
    }
}
