package com.aj.sendall.nw.comms.abstr;

import com.aj.sendall.controller.AppController;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

abstract public class AbstractServer implements Runnable {
    protected AppController appController;

    protected AbstractServer(AppController appController){
        this.appController = appController;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = appController.getRunningSocket();
        if(serverSocket != null) {
            preRun();
            while (!serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    configureSocket(socket);
                    AbstractServerConnDelegate clientConnector = getClientConnector(socket, appController);
                    new Thread(clientConnector).start();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    abstract protected void preRun();
    abstract protected void configureSocket(Socket socket);
    abstract protected AbstractServerConnDelegate getClientConnector(Socket socket, AppController appController);
}
