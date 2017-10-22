package com.aj.sendall.nw.comms;

import com.aj.sendall.controller.AppController;
import com.aj.sendall.nw.comms.abstr.AbstractServer;
import com.aj.sendall.nw.comms.abstr.AbstractServerConnDelegate;

import java.net.Socket;

public class NewConnCreationServer extends AbstractServer {

    public NewConnCreationServer(AppController appController){
        super(appController);
    }

    @Override
    public void preRun() {
    }

    protected void configureSocket(Socket socket){
    }

    @Override
    protected AbstractServerConnDelegate getClientConnector(Socket socket, AppController appController){
        return new NewConnCreationServerConnDelegate(socket, appController);
    }
}
