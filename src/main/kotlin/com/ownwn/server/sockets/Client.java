package com.ownwn.server.sockets;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

public interface Client {
    public InputStream getInputStream();
    public OutputStream getOutputStream();
    public InetAddress getInetAddress();
}
