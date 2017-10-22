package com.aj.sendall.nw.sockets;

import com.aj.sendall.events.EventRouter;
import com.aj.sendall.events.EventRouterFactory;
import com.aj.sendall.events.event.ClientModeStarted;
import com.aj.sendall.events.event.ClientModeStopped;
import com.aj.sendall.events.event.CloseAllSocketsCommand;
import com.aj.sendall.events.event.ServerAcceptedConn;
import com.aj.sendall.events.event.ServerListeningForNewConn;
import com.aj.sendall.events.event.ServerModeStarted;
import com.aj.sendall.events.event.ServerModeStopped;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

class SocketSystemImpl implements SocketSystem{
    private EventRouter eventRouter = EventRouterFactory.getInstance();

    private boolean serverRunning = false;
    private boolean clientRunning = true;
    private ServerIdleTimeMonitor serverIdleTimeMonitor;

    private static final Object lock = new Object();


    private ServerSocket serverSocket = null;

    SocketSystemImpl(){
        subscribeEvents();
    }

    private void subscribeEvents(){
        eventRouter.subscribe(ServerListeningForNewConn.class, new EventRouter.Receiver<ServerListeningForNewConn>() {
            @Override
            public void receive(ServerListeningForNewConn event) {
                startIdleTimeMonitor();
            }
        });
        eventRouter.subscribe(ServerAcceptedConn.class, new EventRouter.Receiver<ServerAcceptedConn>() {
            @Override
            public void receive(ServerAcceptedConn event) {
                stopIdleTimeMonitor();
            }
        });
    }

    public void setServerAcceptWaitTimer(long maxWaitTime){
        stopIdleTimeMonitor();
        if(maxWaitTime > 0) {
            serverIdleTimeMonitor = new ServerIdleTimeMonitor(serverSocket, maxWaitTime);
        }
    }

    private void startIdleTimeMonitor(){
        if(serverIdleTimeMonitor != null){
            serverIdleTimeMonitor.start();
        }
    }

    private void stopIdleTimeMonitor(){
        if(serverIdleTimeMonitor != null){
            serverIdleTimeMonitor.notifyServerAccepted();
            serverIdleTimeMonitor = null;
        }
    }

    public ServerSocket startServerMode(int port) throws IOException, IllegalStateException{
        synchronized (lock) {
            if(!idle()) {
                throwModeException();
            }
            serverSocket = new ServerSocketProxy(port);
            serverRunning = true;
            eventRouter.broadcast(new ServerModeStarted());
            return serverSocket;
        }
    }

    public int getServerPortNo(){
        if(serverSocket != null){
            return serverSocket.getLocalPort();
        }
        return -1;
    }

    public void stopServerMode(){
        synchronized (lock) {
            if (inServerMode()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                serverRunning = false;
                eventRouter.broadcast(new ServerModeStopped());
                serverSocket = null;
            }
        }
    }

    public ServerSocket getCurrentServerSocket(){
        return serverSocket;
    }

    public boolean inClientMode(){
        return clientRunning;
    }

    public boolean inServerMode(){
        return serverRunning;
    }

    public boolean idle(){
        return !serverRunning && !clientRunning;
    }

    public boolean startClientMode() throws IllegalStateException{
        synchronized (lock) {
            if (idle()) {
                clientRunning = true;
                eventRouter.broadcast(new ClientModeStarted());
                return true;
            } else {
                throwModeException();
            }
            return false;
        }
    }

    public void stopClientMode(){
        synchronized (lock) {
            if(inClientMode()) {
                clientRunning = false;
                stopAllClients();
                eventRouter.broadcast(new ClientModeStopped());
            }
        }
    }

    public void setIdle(){
        stopServerMode();
        stopClientMode();
    }

    private void stopAllClients(){
        eventRouter.broadcast(new CloseAllSocketsCommand());
    }

    public Socket createClientSocket(InetAddress add, int port) throws IOException, IllegalStateException{
        if(!inClientMode()){
            return throwModeException();
        }
        return new SocketProxy(new Socket(add, port));
    }

    private Socket throwModeException() throws IllegalStateException{
        throw new IllegalStateException("System running in " + (inServerMode() ? "server" : "client") + " mode");
    }

    private class ServerIdleTimeMonitor extends Thread{
        private long maxIdleTime;
        private ServerSocket serverSocket;
        private Thread idleThread;

        ServerIdleTimeMonitor(ServerSocket serverSocket, long maxIdleTime){
            this.serverSocket = serverSocket;
            this.maxIdleTime = maxIdleTime;
        }

        @Override
        public void run() {
            try {
                this.idleThread = Thread.currentThread();
                Thread.sleep(maxIdleTime);
                serverSocket.close();
            } catch (InterruptedException | IOException e){
                e.printStackTrace();
            }
        }

        void notifyServerAccepted(){
            try {
                idleThread.interrupt();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
