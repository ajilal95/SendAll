package com.aj.sendall.nw.comms;


import com.aj.sendall.controller.AppController;
import com.aj.sendall.db.dto.ConnectionsAndUris;
import com.aj.sendall.nw.comms.abstr.AbstractServerConnDelegate;
import com.aj.sendall.nw.comms.abstr.AbstractServer;

import java.net.Socket;

public class FileTransferServer extends AbstractServer{
    private static final long ACCEPT_WAIT_TIME = 30000;//30 sec
    private ConnectionsAndUris connectionsAndUris;
    private AppController appController;

    public FileTransferServer(AppController appController, ConnectionsAndUris connectionsAndUris){
        super(appController);
        this.appController = appController;
        this.connectionsAndUris = connectionsAndUris;
    }

    @Override
    protected void configureSocket(Socket socket) {
        //nothing to do
    }

    @Override
    protected AbstractServerConnDelegate getClientConnector(Socket socket, AppController appController) {
        return new FileTransferServerConnDelegate(socket, appController, connectionsAndUris);
    }

    @Override
    protected void preRun() {
        //appController.setServerAcceptWaitTimer(ACCEPT_WAIT_TIME);//30 sec
    }
}
