package com.aj.sendall.network.runnable.abstr;

import android.os.Handler;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.interfaces.Updatable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

abstract public class AbstractServer implements Runnable, Updatable {
    private Set<AbstractClientConnector> clientConnectors = new HashSet<>();
    private final ServerSocket serverSocket;
    protected AppManager appManager;
    protected Updatable updatableActivity;
    protected CloseServer closeServer = new CloseServer();

    protected AbstractServer(ServerSocket serverSocket, AppManager appManager, Updatable updatableActivity){
        this.serverSocket = serverSocket;
        this.updatableActivity = updatableActivity;
        this.appManager = appManager;
    }

    @Override
    public void run() {
        if(serverSocket != null) {
            while (!serverSocket.isClosed()) {
                try {
                    preRun();
                    Socket socket = serverSocket.accept();
                    AbstractClientConnector clientConnector = getClientConnector(socket, appManager, updatableActivity);
                    clientConnectors.add(clientConnector);
                    new Thread(clientConnector).start();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    abstract protected void preRun();

    protected void closeServer() {
        UpdateEvent updateEvent = new UpdateEvent();
        updateEvent.source = this.getClass();
        updateEvent.data.put(Constants.ACTION, Constants.CLOSE_SOCKET);
        if(!clientConnectors.isEmpty()){
            for(AbstractClientConnector clientConnector : clientConnectors){
                clientConnector.update(updateEvent);
            }
            clientConnectors.clear();
        }
        closeServer.run();
    }

    abstract protected AbstractClientConnector getClientConnector(Socket socket, AppManager appManager, Updatable updatableActivity);


    private class CloseServer implements Runnable{
        public void run(){
            if(serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
