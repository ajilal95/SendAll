package com.aj.sendall.network.runnable.abstr;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.monitor.SocketSystem;
import com.aj.sendall.network.monitor.Updatable;
import com.aj.sendall.network.utils.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

abstract public class AbstractServer implements Runnable, Updatable {
    private Set<AbstractClientConnector> clientConnectors = new HashSet<>();
    private ServerSocket serverSocket;
    protected AppManager appManager;
    protected Updatable updatableActivity;
    protected SocketSystem socketSystem = SocketSystem.getInstance();

    protected AbstractServer(AppManager appManager, Updatable updatableActivity){
        this.updatableActivity = updatableActivity;
        this.appManager = appManager;
    }

    @Override
    public void run() {
        serverSocket = socketSystem.getCurrentServerSocket();
        if(serverSocket != null) {
            preRun();
            while (!serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    configureSocket(socket);
                    AbstractClientConnector clientConnector = getClientConnector(socket, appManager, updatableActivity);
                    socketSystem.addSocketCloseListener(socket, new ClientConnectorCloseListener(clientConnector));
                    clientConnectors.add(clientConnector);
                    new Thread(clientConnector).start();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    abstract protected void preRun();
    abstract protected void configureSocket(Socket socket);
    abstract protected AbstractClientConnector getClientConnector(Socket socket, AppManager appManager, Updatable updatableActivity);

    @Override
    public void update(UpdateEvent updateEvent) {
    }

    private class ClientConnectorCloseListener implements Updatable{
        private AbstractClientConnector acc;

        private ClientConnectorCloseListener(AbstractClientConnector acc){
            this.acc = acc;
        }

        @Override
        public void update(UpdateEvent updateEvent) {
            acc.closeStreams();
        }
    }
}
