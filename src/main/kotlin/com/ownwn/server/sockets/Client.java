package com.ownwn.server.sockets;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

public class Client {
    public InputStream getInputStream() {
        throw new RuntimeException("NYI");
    }
    public OutputStream getOutputStream() {
        throw new RuntimeException("NYI");
    }

    public InetAddress getInetAddress() {
        throw new RuntimeException("NYI");
    }
}
