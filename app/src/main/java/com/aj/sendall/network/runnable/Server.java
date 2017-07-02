package com.aj.sendall.network.runnable;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.db.contentprovidutil.ContentProviderUtil;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.dto.ConnectionsAndUris;
import com.aj.sendall.db.dto.FileInfoDTO;
import com.aj.sendall.db.sharedprefs.SharedPrefUtil;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

/**
 * Created by ajilal on 28/6/17.
 */

public class Server implements Runnable{
    private ServerSocket serverSocket;
    private int port;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private ConnectionsAndUris connectionsAndUris;

    @Inject
    public SharedPrefUtil sharedPrefUtil;
    @Inject
    public Handler handler;
    @Inject
    public ContentProviderUtil contentProviderUtil;

    public Server(ServerSocket serverSocket
                    , int port
                    , WifiP2pManager wifiP2pManager
                    , WifiP2pManager.Channel channel
                    , ConnectionsAndUris connectionsAndUris
                    , Context context){
        this.serverSocket = serverSocket;
        this.port = port;
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.connectionsAndUris = connectionsAndUris;
        ((AndroidApplication)context.getApplicationContext()).getDaggerInjector().inject(this);
    }

    @Override
    public void run() {
        //TODO the magic
        //protocol
        // client sends its id
        // matches with the connections in connectionsAndUris
        // if valid then start new file sending and manage server accordingly.


        Set<FileInfoDTO> fileInfoDTOs = connectionsAndUris.fileInfoDTOs;
        Set<ConnectionViewData> connectionsSet = connectionsAndUris.connections;

        if(fileInfoDTOs != null && !fileInfoDTOs.isEmpty()) {
            //To stop the server after 1 minute automatically
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!serverSocket.isClosed()) {
                        try {
                            serverSocket.close();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                }
            }, 60000);

            //monitor to shutdown sending if no sending is ongoing
            Set<Socket> activeSocketsReference = new HashSet<>();
            handler.postDelayed(new SendingMonitor(handler, activeSocketsReference, sharedPrefUtil),  30000);

            while (!connectionsSet.isEmpty()) {
                try {
                    Socket socket = serverSocket.accept();
                    DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                    String uid = dis.readUTF();

                    if (verifyAndRemoveConnectionBasedOnUID(uid, connectionsSet)) {
                        //connection is verified
                        activeSocketsReference.add(socket);
                        handler.post(new Sender(socket, fileInfoDTOs, dis));
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    if (serverSocket.isClosed()) {
                        break;
                    }
                }
            }
        }

        try {
            if(!serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    private boolean verifyAndRemoveConnectionBasedOnUID(String uid, Set<ConnectionViewData> connectionViewDatas){
        ConnectionViewData selectedVData = null;

        for(ConnectionViewData data : connectionViewDatas){
            if(data.uniqueId.equals(uid)){
                selectedVData = data;
                break;
            }
        }

        if(selectedVData != null){
            connectionViewDatas.remove(selectedVData);
            return true;
        }

        return false;
    }

}
