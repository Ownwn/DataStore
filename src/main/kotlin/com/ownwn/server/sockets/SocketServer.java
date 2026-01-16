package com.ownwn.server.sockets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.net.InetAddress;
import java.util.List;

import static java.lang.foreign.ValueLayout.*;

public class SocketServer { // todo split arenas to avoid memory leak over time
    private final FFIHelper ffiHelper;
    private final Arena arena;
    private int socketHandle;
    private InetAddress inetAddress;

    public SocketServer(short port, Arena arena) throws Throwable {

        ffiHelper = new FFIHelper(arena);
        this.arena = arena;

        socketHandle = (int) ffiHelper.callIntFunction("socket", JAVA_INT, List.of(2,1,0));

        if (socketHandle < 0) throw new RuntimeException("Bad socket");

        MemorySegment opt = arena.allocateFrom(JAVA_INT, 1);


        ffiHelper.callFunction("setsockopt", JAVA_INT,
                List.of(JAVA_INT, JAVA_INT, JAVA_INT, ADDRESS, JAVA_LONG),
                List.of(socketHandle, 1, 2, opt, opt.byteSize()));

        MemorySegment sockaddr_in = arena.allocate(16L, 1);

        // family 2, port 2, addr 4, zerof 8. Total struct size == 16 bytes
        sockaddr_in.set(JAVA_SHORT, 0, (short) 2);
        sockaddr_in.set(JAVA_SHORT, 2, (Short) ffiHelper.callShortFunction("htons", JAVA_SHORT, List.of(port)));
        sockaddr_in.set(JAVA_INT, 4, 0);
        sockaddr_in.set(JAVA_LONG, 8, 0);

        if ((int) ffiHelper.callFunction("bind", JAVA_INT, List.of(JAVA_INT, ADDRESS, JAVA_LONG), List.of(socketHandle, sockaddr_in, 16L)) < 0) {
            throw new RuntimeException("Failed to invoke bind");
        }


        if ((int) ffiHelper.callFunction("listen", JAVA_INT, List.of(JAVA_INT, JAVA_INT), List.of(socketHandle, 16)) < 0) {
            throw new RuntimeException("Failed to invoke listen");
        }


    }

    public InetAddress getHostInetAddress() {
        return InetAddress.ofLiteral("0.0.0.0"); // todo
    }

    public Client accept() throws Throwable {

        // struct sockaddr_storage peer;
        MemorySegment sockAddrStoragePeer = arena.allocate(128); // todo
        MemorySegment addrLength = arena.allocate(128);
        addrLength.set(JAVA_INT, 0, (int) sockAddrStoragePeer.byteSize());


        int c = (int) ffiHelper.callFunction("accept", JAVA_INT, List.of(JAVA_INT, ADDRESS, ADDRESS) ,List.of(socketHandle, sockAddrStoragePeer, addrLength));
        if (c < 0) {
            MemorySegment str = arena.allocateFrom("accept");
            ffiHelper.callFunction("perror", ADDRESS, List.of(ADDRESS) ,List.of(str));
            throw new RuntimeException("Failed to invoke accept");
        }


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

    public void close() {

    }
}
