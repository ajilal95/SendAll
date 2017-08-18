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
        UpdateEvent event = new UpdateEvent();
        event.source = this.getClass();
        event.data.put(Constants.ACTION, Constants.ACCEPT_CONN);
        updatableActivity.update(event);
    }

    protected void configureSocket(Socket socket){
        /*try{
            socket.setSoTimeout(60000);
        } catch (Exception e){
            e.printStackTrace();
        }*/
    }

    @Override
    protected AbstractClientConnector getClientConnector(Socket socket, AppManager appManager, Updatable updatableActivity){
        return new NewConnCreationClientConnector(socket, updatableActivity, appManager);
    }
}
