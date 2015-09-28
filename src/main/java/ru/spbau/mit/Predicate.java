package ru.spbau.mit;

abstract class Predicate<X> extends Function1<X, Boolean> {
    public abstract Boolean apply(X arg);

    public static final Predicate ALWAYS_TRUE = new Predicate() {
        @Override
        public Boolean apply(Object obj) {
            return true;
        }
    };

    public static final Predicate ALWAYS_FALSE = ALWAYS_TRUE.not();

    public Predicate<X> or(final Predicate<X> right) {
        final Predicate<X> left = this;

        return new Predicate<X>() {
            @Override
            public Boolean apply(X arg) {
                return (left.apply(arg) || right.apply(arg));
            }
        };
    }

    public Predicate<X> and(final Predicate<X> right) {
        final Predicate<X> left = this;

        return new Predicate<X>() {
            @Override
            public Boolean apply(X arg) {
                return (left.apply(arg) && right.apply(arg));
            }
        };
    }

    public Predicate<X> not() {
        final Predicate<X> cur = this;

        return new Predicate<X>() {
            @Override
            public Boolean apply(X arg) {
                return !cur.apply(arg);
            }
        };
    }
}