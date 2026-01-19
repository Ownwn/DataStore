package com.ownwn.server.java.lang.replacement;

import java.util.function.Function;

public abstract class Stream<T> implements Cloneable {
    private List<T> underlying;
    private List<Function<T, T>> operations;
    static <T> Stream<T> empty() {
        return null; // todo

    }


    Stream(List<T> col) {
        underlying = col; // todo mutate by someone else bad
    }

    Stream() {}

    public <R> Stream<R> map(Function<T, R> mapper) {
        return null;// todo
    }

    public List<T> toList() {
        return underlying; // todo
    }

    @Override
    protected Stream<T> clone() {
        return new Stream<>() {
            {
                operations = new ArrayList<>(operations);
            }
        };
    }
}
