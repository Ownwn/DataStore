package com.ownwn.server.sockets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.net.InetAddress;
import java.util.List;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

public abstract class Client {
    protected FFIHelper clientFFIhelper;
    protected Arena arena;

    public void setArena(Arena arena) {
        this.arena = arena;
        clientFFIhelper = new FFIHelper(arena);
    }

    public abstract InputStream getInputStream();
    public abstract OutputStream getOutputStream();
    public abstract InetAddress getInetAddress();

    public static Client fromFileSocketDescriptor(int c) {
        return new Client() {
            @Override
            public InputStream getInputStream() {
                return new InputStream() {
                    @Override
                    public int read() {
                        // todo chunk buf to optimize allocs
                        MemorySegment buf = arena.allocate(1, 1L);

                        try {
                            int numReaded = (int) ((long) clientFFIhelper.callFunction("read", JAVA_LONG, List.of(JAVA_INT, ADDRESS, JAVA_LONG), List.of(c, buf, buf.byteSize())));
                            if (numReaded <= 0) return -1;

                            return buf.get(JAVA_BYTE, 0) & 0xFF;
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            }

            @Override
            public OutputStream getOutputStream() {
                return new OutputStream() {
                    @Override
                    public void write(int b) throws IOException {
                        MemorySegment resByte = arena.allocateFrom(JAVA_BYTE, (byte) b); // todo optimize chunk size
                        try {
                            clientFFIhelper.callFunction("write", JAVA_LONG, List.of(JAVA_INT, ADDRESS, JAVA_LONG), List.of(c, resByte, 1));
                        } catch (Throwable e) {
                            throw new IOException(e);
                        }

                    }

                    @Override
                    public void close() throws IOException {
                        super.close(); // todo close client when done! important!
                        try {
                            clientFFIhelper.callIntFunction("close", JAVA_INT, List.of(c));
                        } catch (Throwable e) {
                            throw new IOException(e);
                        }
                    }
                };
            }

            @Override
            public InetAddress getInetAddress() {
                return InetAddress.ofLiteral("123.123.123.123");
            }
        }; // todo
    }
}
