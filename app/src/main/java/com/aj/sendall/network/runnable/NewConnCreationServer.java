package com.aj.sendall.network.runnable;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;

import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.ui.interfaces.Updatable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by ajilal on 10/7/17.
 */

public class NewConnCreationServer implements Runnable, Updatable {
    public static final String UPDATE_CONST_SERVER = "server";
    private ServerSocket serverSocket;
    private int port;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private Handler handler;
    private Context context;
    private SharedPrefUtil sharedPrefUtil;
    private Updatable updatableActivity;

    public NewConnCreationServer(ServerSocket serverSocket
            , int port
            , AppManager appManager
            , Updatable updatableActivity){
        this.serverSocket = serverSocket;
        this.port = port;
        this.wifiP2pManager = appManager.wifiP2pManager;
        this.channel = appManager.channel;
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
                handler.post(new NewConnCreationClientConnector(socket, updatableActivity, sharedPrefUtil));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    @Override
    public void update(UpdateEvent updateEvent) {
        if(Constants.CLOSE_SOCKET.equals(updateEvent.data.get(Constants.ACTION))){
            try {
                serverSocket.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
