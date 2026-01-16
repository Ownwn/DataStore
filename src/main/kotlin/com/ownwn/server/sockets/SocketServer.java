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
            // print out what went wrong
            ffiHelper.callFunction("perror", ADDRESS, List.of(ADDRESS) ,List.of(arena.allocateFrom("accept")));
            throw new RuntimeException("Failed to invoke accept with error (hopefully) above");
        }

        return Client.fromFileSocketDescriptor(c);

    }
}
