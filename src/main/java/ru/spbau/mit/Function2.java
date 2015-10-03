package ru.spbau.mit;

abstract class Function2<X, Y, Z> { // (X, Y) -> Z
    public abstract Z apply(X arg1, Y arg2);

    public <W> Function2<X, Y, W> compose(final Function1<? super Z, ? extends W> outer) { // outer(this)
        final Function2<X, Y, Z> inner = this;

        return new Function2<X, Y, W>() {
            @Override
            public W apply(X arg1, Y arg2) {
                return outer.apply(inner.apply(arg1, arg2));
            }
        };
    }

    public Function1<Y, Z> bind1(final X arg1) {
        final Function2<X, Y, Z> origin = this;
        
        return new Function1<Y, Z>() {
            @Override
            public Z apply(Y arg2) {
                return origin.apply(arg1, arg2);
            }
        };
    }

    public Function1<X, Z> bind2(final Y arg2) {
        final Function2<X, Y, Z> origin = this;
        
        return new Function1<X, Z>() {
            @Override
            public Z apply(X arg1) {
                return origin.apply(arg1, arg2);
            }
        };
    }

    public Function1<X, Function1<Y, Z>> curry() {
        final Function2<X, Y, Z> origin = this;

        return new Function1<X, Function1<Y, Z>>() {
            @Override
            public Function1<Y, Z> apply(X arg1) {
                return origin.bind1(arg1);
            }
        };
    }
}
