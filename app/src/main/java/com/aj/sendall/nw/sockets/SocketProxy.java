package com.aj.sendall.nw.sockets;

import com.aj.sendall.events.EventRouter;
import com.aj.sendall.events.EventRouterFactory;
import com.aj.sendall.events.event.CloseAllSocketsCommand;
import com.aj.sendall.events.event.SocketClosing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

class SocketProxy extends Socket {
    private EventRouter eventRouter = EventRouterFactory.getInstance();
    private Socket s;

    SocketProxy(Socket socket){
        this.s = socket;
        eventRouter.subscribe(CloseAllSocketsCommand.class, new EventRouter.Receiver<CloseAllSocketsCommand>() {
            @Override
            public void receive(CloseAllSocketsCommand event) {
                eventRouter.unsubscribe(CloseAllSocketsCommand.class, this);
                try {
                    close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        s.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    @Override
    public boolean isOutputShutdown() {
        return s.isOutputShutdown();
    }

    @Override
    public boolean isInputShutdown() {
        return s.isInputShutdown();
    }

    @Override
    public boolean isClosed() {
        return s.isClosed();
    }

    @Override
    public boolean isBound() {
        return s.isBound();
    }

    @Override
    public boolean isConnected() {
        return s.isConnected();
    }

    @Override
    public String toString() {
        return s.toString();
    }

    @Override
    public void shutdownOutput() throws IOException {
        s.shutdownOutput();
    }

    @Override
    public void shutdownInput() throws IOException {
        s.shutdownInput();
    }

    @Override
    public synchronized void close() throws IOException {
        SocketClosing event = new SocketClosing();
        event.closingSocket = this;
        eventRouter.broadcast(event);
        s.close();
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        return s.getReuseAddress();
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        s.setReuseAddress(on);
    }

    @Override
    public int getTrafficClass() throws SocketException {
        return s.getTrafficClass();
    }

    @Override
    public void setTrafficClass(int tc) throws SocketException {
        s.setTrafficClass(tc);
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return s.getKeepAlive();
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        s.setKeepAlive(on);
    }

    @Override
    public synchronized int getReceiveBufferSize() throws SocketException {
        return s.getReceiveBufferSize();
    }

    @Override
    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        s.setReceiveBufferSize(size);
    }

    @Override
    public synchronized int getSendBufferSize() throws SocketException {
        return s.getSendBufferSize();
    }

    @Override
    public synchronized void setSendBufferSize(int size) throws SocketException {
        s.setSendBufferSize(size);
    }

    @Override
    public synchronized int getSoTimeout() throws SocketException {
        return s.getSoTimeout();
    }

    @Override
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        s.setSoTimeout(timeout);
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        return s.getOOBInline();
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException {
        s.setOOBInline(on);
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        s.sendUrgentData(data);
    }

    @Override
    public int getSoLinger() throws SocketException {
        return s.getSoLinger();
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        s.setSoLinger(on, linger);
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return s.getTcpNoDelay();
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        s.setTcpNoDelay(on);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return s.getOutputStream();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return s.getInputStream();
    }

    @Override
    public SocketChannel getChannel() {
        return s.getChannel();
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return s.getLocalSocketAddress();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return s.getRemoteSocketAddress();
    }

    @Override
    public int getLocalPort() {
        return s.getLocalPort();
    }

    @Override
    public int getPort() {
        return s.getPort();
    }

    @Override
    public InetAddress getLocalAddress() {
        return s.getLocalAddress();
    }

    @Override
    public InetAddress getInetAddress() {
        return s.getInetAddress();
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        s.bind(bindpoint);
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        s.connect(endpoint, timeout);
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        s.connect(endpoint);
    }
}
