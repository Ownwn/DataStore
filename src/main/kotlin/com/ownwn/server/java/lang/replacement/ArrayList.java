package com.ownwn.server.java.lang.replacement;

import java.util.Iterator;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public final class ArrayList<T> implements List<T> {
    Object[] array;
    int currentSize = 0;

    static void main() {
        List<Integer> l = new ArrayList<>();
        java.util.List<Integer> l2 = new java.util.ArrayList<>();
        for (int i = 0; i < 10000; i++) {
//            if (l.toString().intern() != l2.toString().intern()) throw new Error(l.toString() + " " + l2.toString());
            l.add(i);
            l2.add(i);
            if (!l.equals(l2)) throw new Error("nah");
        }

        for (int h : l) {
            System.out.println(h);
        }
        l.add(1);
    }


    public ArrayList() {
        this(32);
    }

    public ArrayList(int initialCapacity) {
        array = new Object[initialCapacity];
    }

    /** shallow copy constructor */
    public ArrayList(List<T> list) {
        for (T t : list) {
            add(t);
        }
    }

    /** shallow copy constructor */
    public ArrayList(java.util.List<T> list) {
        for (T t : list) {
            add(t);
        }
    }

    @Override
    public int size() {
        return currentSize;
    }

    @Override
    public boolean isEmpty() {
        return currentSize == 0;
    }

    @Override
    public boolean contains(Object o) {
        for (int i = 0; i < currentSize; i++) {
            if (Objects.equals(array[i], o)) return true;
        }
        return false;
    }

    @Override
    public boolean add(Object o) {
        if (currentSize >= array.length) {
            array = Arrays.copyOf(array, currentSize*2);
        }

        array[currentSize++] = o;

        return true;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public T get(int index) {
        return (T) array[index];
    }

    @Override
    public T set(int index, T elem) {
        Object old = array[index];
        array[index] = elem;
        return (T) old;
    }

    @Override
    public T[] toArray() {
        T[] res = (T[]) new Object[size()];
        for (int i = 0; i < size(); i++) {
            res[i] = (T) array[i];
        }
        return res;
    }

    @Override
    public T[] toArray(Object[] arr) {
        if (arr.length < currentSize) {
            return toArray(); // supplied arr is too small, just make a new one
        }

        for (int i = 0; i < size(); i++) {
            arr[i] = (T) array[i];
        }
        return (T[]) arr;
    }

    @Override
    public Stream<T> stream() {
        return new Stream<T>(this) {
        };
    }

    @Override
    public void add(int index, Object elem) {
        // todo
    }

    @Override
    public int hashCode() {
        int res = 0;
        for (int i = 0; i < currentSize; i++) {
            Object o = array[i];
            if (o == null) i+= 12340987; // todo bad probs
            else res+= o.hashCode();
        }
        return res;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof java.util.ArrayList<?> l2)) {
            return false;
        }
        if (l2.size() != size()) return false;
        for (int i = 0; i < size(); i++) {
            if (!Objects.equals(l2.get(i), get(i))) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder("[");
        for (int i = 0; i < size(); i++) {
            res.append( get(i) == null ? "null" : get(i).toString());
            res.append(", ");
        }
        if (size() > 0)res.delete(res.length()-2, res.length());
        res.append("]");
        return res.toString();
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private int i = 0;
            @Override
            public boolean hasNext() {
                return i < ArrayList.this.size();
            }

            @Override
            public T next() {
                return (T) ArrayList.this.array[i++];
            }
        };
    }
}
