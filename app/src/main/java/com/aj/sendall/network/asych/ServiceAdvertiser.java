package com.aj.sendall.network.asych;

import android.net.Uri;

import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.dto.ConnectionsAndUris;
import com.aj.sendall.network.utils.LocalWifiManager;

import java.net.ServerSocket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ajilal on 12/6/17.
 */

public class ServiceAdvertiser extends Thread {
    public static final String RECORD_KEY_PORT = "_key__port";
    public static final String RECORD_KEY_RECEIVERS = "_key__receivers";

    private Collection<ConnectionViewData> receivers;
    private Object[] uris;
    private static final Object SYNCH_OBJECT = new Object();
    private LocalWifiManager localWifiManager;

    public ServiceAdvertiser(ConnectionsAndUris connectionsAndUris, LocalWifiManager localWifiManager){
        this.receivers = connectionsAndUris.connections;
        this.uris = connectionsAndUris.mediaUris.toArray();
        this.localWifiManager = localWifiManager;
    }

    @Override
    public void run(){
        synchronized (SYNCH_OBJECT){
            try {
                ServerSocket server = new ServerSocket(0);
                int port = server.getLocalPort();
                Map<String, String> record = new HashMap<>();
                record.put(RECORD_KEY_PORT, String.valueOf(port));
                StringBuilder receiversStringBuilder = new StringBuilder();
                for(ConnectionViewData receiver : receivers){
                    //The service advertises a part of the unique id of the receivers
                    //The client must return with the
                    receiversStringBuilder.append(receiver.uniqueId.substring(0, 10));
                }
                record.put(RECORD_KEY_RECEIVERS, receiversStringBuilder.toString());
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

}
