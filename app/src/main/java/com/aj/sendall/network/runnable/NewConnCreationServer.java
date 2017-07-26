package com.aj.sendall.network.runnable;

import android.os.Handler;

import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.application.AppManager;
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
        new Handler().postDelayed(super.closeServer, 45000);
    }

    @Override
    protected AbstractClientConnector getClientConnector(Socket socket, AppManager appManager, Updatable updatableActivity){
        return new NewConnCreationClientConnector(socket, updatableActivity, appManager);
    }

    @Override
    public void update(UpdateEvent updateEvent) {
        if(Constants.CLOSE_SOCKET.equals(updateEvent.data.get(Constants.ACTION))){
            try {
                closeServer();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
