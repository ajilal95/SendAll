package com.aj.sendall.nw.sockets;

public class SocketSystemFactory {
    private static final SocketSystem ss = new SocketSystemImpl();

    public static SocketSystem getInstance(){
        return ss;
    }
}
