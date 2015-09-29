package ru.spbau.mit;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

abstract class Collections {
    public static <X, Y> Collection<Y> map(Function1<? super X, ? extends Y> f,
                                           Iterable<X> collection, Collection<Y> result) {
        for (X e : collection) {
            result.add(f.apply(e));
        }

        return result;
	}

    public static <X> Iterable<X> filter(Predicate<? super X> pred,
                                         Iterable<X> collection, Collection<X> result) {

        for (X e : collection) {
            if (pred.apply(e)) {
                result.add(e);
            }
        }

        return result;
    }

    public static <X> Iterable<X> takeWhile(Predicate<? super X> pred,
                                            Iterable<X> collection, Collection<X> result) {
        for (X e : collection) {
            if (!pred.apply(e)) {
                break;
            }
            result.add(e);
        }

        return result;
    }

    public static <X> Iterable<X> takeUnless(Predicate<? super X> pred,
                                             Iterable<X> collection, Collection<X> result) {
        for (X e : collection) {
            if (pred.apply(e)) {
                break;
            }
            result.add(e);
        }

        return result;
    }

    public static <X, Y> Y foldl(Function2<Y, X, Y> f, Y init, Iterable<X> collection) {
        Y result = init;

        for (X element : collection) {
            result = f.apply(result, element);
        }

        return result;
    }

    public static <X, Y> Y foldr(Function2<X, Y, Y> f, Y init, Iterable<X> collection) {
        Y result = init;
        LinkedList<X> lst = new LinkedList<X>();

        for (X element : collection) {
            lst.addLast(element);
        }

        for (Iterator<X> i = lst.descendingIterator(); i.hasNext();){
            result = f.apply(i.next(), result);
        }

        return result;
    }
}