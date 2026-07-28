package com.ownwn.server.java.lang.replacement.stream;

public interface OutputStream {

    void write(int b);

    void close();

    default void write(byte[] bytes) {
        write(bytes, bytes.length);
    }

    void write(byte[] bytes, int len);
}
