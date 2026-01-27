package com.ownwn.server.java.lang.replacement.stream;

import java.io.IOException;

public final class ByteArrayInputStream implements InputStream {
    private int i = 0;
    private final byte[] body;
    public ByteArrayInputStream(byte[] body) {
        this.body = body;
    }
    @Override
    public int read() throws IOException {
        int res = body[i];
        if (res != 0) i++;

        return res;
    }

    @Override
    public void close() throws IOException {

    }
}
