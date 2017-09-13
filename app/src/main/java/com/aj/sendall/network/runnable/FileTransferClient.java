package com.aj.sendall.network.runnable;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.runnable.abstr.AbstractClient;
import com.aj.sendall.ui.interfaces.Updatable;

import java.net.Socket;

public class FileTransferClient extends AbstractClient {

    public FileTransferClient(String SSID, String passPhrase, int serverPort, Updatable updatableActivity, AppManager appManager){
        super(SSID, passPhrase, serverPort, updatableActivity, appManager);
    }

    @Override
    protected void configureSocket(Socket socket) {

    }

    @Override
    protected void communicate() {

    }
}
