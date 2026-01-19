package com.ownwn.server.java.lang.replacement;

public class Arrays {

    public static Object[] copyOf(Object[] o, int newSize) {
        Object[] res = new Object[newSize];
        for (int i = 0; i < o.length; i++) {
            res[i] = o[i];
        }
        return res;
    }
}
