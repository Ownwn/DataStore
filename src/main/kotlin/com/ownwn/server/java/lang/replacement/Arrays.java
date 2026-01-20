package com.ownwn.server.java.lang.replacement;

public class Arrays {

    public static Object[] copyOf(Object[] o, int newSize) {
        Object[] res = new Object[newSize];
        for (int i = 0; i < o.length; i++) {
            res[i] = o[i];
        }
        return res;
    }

    public static byte[] copyOfRange(byte[] original, int from, int to) {
        int range = to-from;
        byte[] res = new byte[range];
        for (int i = 0; i < range; i++) {
            res[i] = original[from + i];
        }
        return res;
    }
}
