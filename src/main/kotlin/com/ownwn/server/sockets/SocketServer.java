package com.ownwn.server.sockets;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;
import static java.lang.foreign.ValueLayout.JAVA_SHORT;

public class SocketServer { // todo split arenas to avoid memory leak over time
    private final FFIHelper ffiHelper;
    private final Arena arena;
    private int socketHandle;

    public SocketServer(Arena arena) throws Throwable {

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
        sockaddr_in.set(JAVA_SHORT, 2, ((Integer) ffiHelper.callIntFunction("htons", JAVA_INT, List.of(8081))).shortValue());
        sockaddr_in.set(JAVA_INT, 4, 0);
        sockaddr_in.set(JAVA_LONG, 8, 0);

        if ((int) ffiHelper.callFunction("bind", JAVA_INT, List.of(JAVA_INT, ADDRESS, JAVA_LONG), List.of(socketHandle, sockaddr_in, 16L)) < 0) {
            throw new RuntimeException("Failed to invoke bind");
        }


        if ((int) ffiHelper.callFunction("listen", JAVA_INT, List.of(JAVA_INT, JAVA_INT), List.of(socketHandle, 16)) < 0) {
            throw new RuntimeException("Failed to invoke listen");
        }
    }

    public Client accept() throws Throwable {
        int c = (int) ffiHelper.callIntFunction("accept", JAVA_INT, List.of(socketHandle, 0, 0));
        if (c < 0) {
            System.err.println("Failed to invoke accept");
        }

        MemorySegment buf = arena.allocate(1000, 1L);

        int numReaded = (int) ((long) ffiHelper.callFunction("read", JAVA_LONG, List.of(JAVA_INT, ADDRESS, JAVA_LONG), List.of(c, buf, buf.byteSize())));
        if (numReaded > 0) {
            System.out.println("msg from client: " + buf.getString(0, StandardCharsets.UTF_8));
        }


        MemorySegment resString = arena.allocateFrom("cool res foobar");
        ffiHelper.callFunction("write", JAVA_LONG, List.of(JAVA_INT, ADDRESS, JAVA_LONG), List.of(c, resString, resString.byteSize()));

        ffiHelper.callIntFunction("close", JAVA_INT, List.of(c));
        return new Client(); // todo
    }

    public void close() {

    }
}
