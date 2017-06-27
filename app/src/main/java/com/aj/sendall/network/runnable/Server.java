package com.aj.sendall.network.runnable;

import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;

import com.aj.sendall.db.dto.ConnectionsAndUris;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.db.sharedprefs.SharedPrefUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Set;

/**
 * Created by ajilal on 28/6/17.
 */

public class Server implements Runnable{
    private ServerSocket serverSocket;
    private int port;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private SharedPrefUtil sharedPrefUtil;
    private ConnectionsAndUris connectionsAndUris;
    private Handler handler;

    public Server(ServerSocket serverSocket
                    , int port
                    , WifiP2pManager wifiP2pManager
                    , WifiP2pManager.Channel channel
                    , SharedPrefUtil sharedPrefUtil
                    , ConnectionsAndUris connectionsAndUris
                    , Handler handler){
        this.serverSocket = serverSocket;
        this.port = port;
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.sharedPrefUtil = sharedPrefUtil;
        this.connectionsAndUris = connectionsAndUris;
        this.handler = handler;
    }

    @Override
    public void run() {
        //TODO the magic
        //protocol
        // client sends its id
        // matches with the connections in connectionsAndUris
        // if valid then start new file sending and manage server accordingly.
        try {
            serverSocket.close();
        } catch (IOException ioe){
            ioe.printStackTrace();
        } finally {
            sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
            sharedPrefUtil.commit();
        }

    }
}
