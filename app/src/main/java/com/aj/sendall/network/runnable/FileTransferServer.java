package com.aj.sendall.network.runnable;


import com.aj.sendall.application.AppManager;
import com.aj.sendall.db.dto.ConnectionsAndUris;
import com.aj.sendall.network.runnable.abstr.AbstractClientConnector;
import com.aj.sendall.network.runnable.abstr.AbstractServer;
import com.aj.sendall.network.monitor.Updatable;

import java.net.Socket;

public class FileTransferServer extends AbstractServer{
    private static final long ACCEPT_WAIT_TIME = 30000;//30 sec
    private ConnectionsAndUris connectionsAndUris;

    public FileTransferServer(AppManager appManager, ConnectionsAndUris connectionsAndUris){
        super(appManager, null);
        this.connectionsAndUris = connectionsAndUris;
    }

    @Override
    protected void configureSocket(Socket socket) {
        //nothing to do
    }

    @Override
    protected AbstractClientConnector getClientConnector(Socket socket, AppManager appManager, Updatable updatableActivity) {
        return new FileTransferClientConnector(socket, appManager, connectionsAndUris);
    }

    @Override
    protected void preRun() {
        socketSystem.setServerAcceptWaitTimer(ACCEPT_WAIT_TIME);//30 sec
    }
}
