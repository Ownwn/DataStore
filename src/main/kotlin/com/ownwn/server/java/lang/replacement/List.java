package com.ownwn.server.java.lang.replacement;

public interface List<T> extends Iterable<T> {

    int size();

    boolean isEmpty();

    boolean contains(Object o);

    boolean add(T t);

    boolean remove(Object o);

    int hashCode();

    T get(int index);

    T set(int index, T elem);

    void add(int index, T elem);

    T[] toArray();

    T[] toArray(Object[] arr);

    Stream<T> stream();
}
