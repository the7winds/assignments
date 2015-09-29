package ru.spbau.mit;

abstract class Function1<X, Y> { // F: X -> Y
    public abstract Y apply(X arg);

    public <Z> Function1<X, Z> compose(final Function1<? super Y, Z> outer) { // outer(this)
        final Function1<X, Y> inner = this;

        return new Function1<X, Z>() {
            @Override
            public Z apply(X arg) {
                return outer.apply(inner.apply(arg));
            }
        };
    }
}