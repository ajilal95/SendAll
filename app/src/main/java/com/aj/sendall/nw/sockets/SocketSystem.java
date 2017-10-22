package com.aj.sendall.nw.sockets;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public interface SocketSystem {
    ServerSocket startServerMode(int port) throws IOException, IllegalStateException;
    ServerSocket getCurrentServerSocket();
    void setServerAcceptWaitTimer(long maxWaitTime);
    int getServerPortNo();
    void stopServerMode();

    boolean startClientMode() throws IllegalStateException;
    void stopClientMode();
    Socket createClientSocket(InetAddress add, int port) throws IOException, IllegalStateException;

    boolean inServerMode();
    boolean inClientMode();
    boolean idle();
    void setIdle();
}
