package com.aj.sendall.network.runnable;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Parcelable;
import android.widget.Toast;

import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.ui.interfaces.Updatable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class NewConnCreationServer implements Runnable, Updatable {
    public static final String UPDATE_CONST_SERVER = "server";
    private ServerSocket serverSocket;
    private Handler handler;
    private Context context;
    private SharedPrefUtil sharedPrefUtil;
    private Updatable updatableActivity;
    private Set<NewConnCreationClientConnector> clientConnectors = new HashSet<>();

    public NewConnCreationServer(ServerSocket serverSocket
            , AppManager appManager
            , Updatable updatableActivity){
        this.serverSocket = serverSocket;
        this.context = appManager.context;
        this.handler = appManager.handler;
        this.updatableActivity = updatableActivity;
        this.sharedPrefUtil = appManager.sharedPrefUtil;
    }
    @Override
    public void run() {
        Updatable.UpdateEvent update = new Updatable.UpdateEvent();
        update.source = this.getClass();
        update.data.put(UPDATE_CONST_SERVER, this);
        updatableActivity.update(update);

        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                NewConnCreationClientConnector clientConnector = new NewConnCreationClientConnector(socket, updatableActivity, sharedPrefUtil);
                clientConnectors.add(clientConnector);
                handler.post(clientConnector);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    @Override
    public void update(UpdateEvent updateEvent) {
        if(Constants.CLOSE_SOCKET.equals(updateEvent.data.get(Constants.ACTION))){
            try {
                if(!clientConnectors.isEmpty()){
                    for(NewConnCreationClientConnector clientConnector : clientConnectors){
                        clientConnector.update(updateEvent);
                    }
                }
                serverSocket.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
