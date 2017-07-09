package com.aj.sendall.network.runnable;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.db.dto.ConnectionsAndUris;
import com.aj.sendall.network.utils.LocalWifiManager;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by ajilal on 10/7/17.
 */

public class NewConnCreationServer implements Runnable {
    private ServerSocket serverSocket;
    private int port;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private Handler handler;
    private Context context;

    public NewConnCreationServer(ServerSocket serverSocket
            , int port
            , LocalWifiManager localWifiManager){
        this.serverSocket = serverSocket;
        this.port = port;
        this.wifiP2pManager = localWifiManager.wifiP2pManager;
        this.channel = localWifiManager.channel;
        this.context = localWifiManager.context;
        this.handler = localWifiManager.handler;
//        ((AndroidApplication)context.getApplicationContext()).getDaggerInjector().inject(this);
    }
    @Override
    public void run() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                handler.post(new NewConnCreationSender(socket));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
