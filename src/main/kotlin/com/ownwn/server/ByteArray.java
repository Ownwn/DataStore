package com.ownwn.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;

public class ByteArray {
    private byte[] buf = new byte[32];
    int freePos = 0;

    public void add(byte b) {
        if (freePos >= buf.length) {
            byte[] newBuf = new byte[buf.length*2];
            System.arraycopy(buf, 0, newBuf, 0, buf.length);
            buf = newBuf;
        }

        buf[freePos++] = b;
    }

    private ByteArray() {

    }

    public byte[] getInternalArray() {
        return buf;
    }

    public static ByteArray fromInputStream(InputStream body) throws IOException {
        ByteArray array = new ByteArray();
        int b;
        try {
            while ((b = body.read()) != -1) {
                array.add((byte) b);
            }
        } catch (SocketTimeoutException ignored) {

        }

        return array;
    }
}
