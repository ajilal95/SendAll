package com.aj.sendall.network.runnable;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.runnable.abstr.AbstractClientConnector;
import com.aj.sendall.network.runnable.abstr.AbstractServer;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.interfaces.Updatable;

import java.net.ServerSocket;
import java.net.Socket;

public class NewConnCreationServer extends AbstractServer {

    public NewConnCreationServer(ServerSocket serverSocket, AppManager appManager, Updatable updatableActivity){
        super(serverSocket, appManager, updatableActivity);
    }

    @Override
    public void preRun() {
        //Auto close the socket after 45 seconds
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(45000L);
                    NewConnCreationServer.super.closeServer();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        UpdateEvent event = new UpdateEvent();
        event.source = this.getClass();
        event.data.put(Constants.ACTION, Constants.ACCEPT_CONN);
        updatableActivity.update(event);
    }

    @Override
    protected AbstractClientConnector getClientConnector(Socket socket, AppManager appManager, Updatable updatableActivity){
        return new NewConnCreationClientConnector(socket, updatableActivity, appManager);
    }
}
