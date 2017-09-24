package com.aj.sendall.network.runnable;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.runnable.abstr.AbstractClientConnector;
import com.aj.sendall.network.runnable.abstr.AbstractServer;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.network.monitor.Updatable;

import java.net.ServerSocket;
import java.net.Socket;

public class NewConnCreationServer extends AbstractServer {

    public NewConnCreationServer(AppManager appManager, Updatable updatableActivity){
        super(appManager, updatableActivity);
    }

    @Override
    public void preRun() {
        UpdateEvent event = new UpdateEvent();
        event.source = this.getClass();
        event.action = Constants.ACCEPT_CONN;
        updatableActivity.update(event);
    }

    protected void configureSocket(Socket socket){
    }

    @Override
    protected AbstractClientConnector getClientConnector(Socket socket, AppManager appManager, Updatable updatableActivity){
        return new NewConnCreationClientConnector(socket, updatableActivity, appManager);
    }
}
