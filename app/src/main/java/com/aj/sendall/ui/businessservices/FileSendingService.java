package com.aj.sendall.ui.businessservices;

import android.net.Uri;

import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.dto.ConnectionsAndUris;
import com.aj.sendall.network.utils.LocalWifiManager;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by ajilal on 3/5/17.
 */

@Singleton
public final class FileSendingService {
    private  ConnectionsAndUris connectionsAndUris;

    private LocalWifiManager localWifiManager;

    @Inject
    public FileSendingService(LocalWifiManager localWifiManager){
        this.localWifiManager = localWifiManager;
    }

    private ConnectionsAndUris getConnectionsAndUris(){
        if(connectionsAndUris == null){
            connectionsAndUris = new ConnectionsAndUris();
        }
        return connectionsAndUris;
    }

    public SendOperationResult send_to(Set<ConnectionViewData> receiverSet){
        getConnectionsAndUris().connections = receiverSet;
        if(getConnectionsAndUris().mediaUris == null || getConnectionsAndUris().mediaUris.isEmpty()){
            return SendOperationResult.URI_EMPTY;
        } else {
//            addCurrentToSendQueue();
            send();
            clear();
            return SendOperationResult.SENDING;
        }
    }

    public SendOperationResult send_items(Set<Uri> mediaUris){
        getConnectionsAndUris().mediaUris = mediaUris;
        if(getConnectionsAndUris().connections == null || getConnectionsAndUris().connections.isEmpty()){
            return SendOperationResult.RECEIVER_EMPTY;
        } else {
//            addCurrentToSendQueue();
            send();
            clear();
            return SendOperationResult.SENDING;
        }
    }

    private void send(){
        localWifiManager.createGroupAndAdvertise(connectionsAndUris);
    }

//    private static void addCurrentToSendQueue(){
//
//    }

    private void clear(){
        connectionsAndUris = null;
    }

    public enum SendOperationResult{
        SENDING, URI_EMPTY, RECEIVER_EMPTY
    }
}
