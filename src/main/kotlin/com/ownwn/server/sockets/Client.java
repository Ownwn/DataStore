package com.ownwn.server.sockets;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.foreign.Arena;
import java.net.InetAddress;

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
}
