package com.aj.sendall.ui.services;

import android.net.Uri;

import com.aj.sendall.dal.dto.ConnectionViewData;
import com.aj.sendall.dal.dto.ConnectionsAndUris;

import java.util.Set;

/**
 * Created by ajilal on 3/5/17.
 */

public final class FileSendingService {
    private static ConnectionsAndUris connectionsAndUris;

    private static ConnectionsAndUris getConnectionsAndUris(){
        if(connectionsAndUris == null){
            connectionsAndUris = new ConnectionsAndUris();
        }
        return connectionsAndUris;
    }

    public static SendOperationResult send_to(Set<ConnectionViewData> receiverSet){
        getConnectionsAndUris().connections = receiverSet;
        if(getConnectionsAndUris().mediaUris == null || getConnectionsAndUris().mediaUris.isEmpty()){
            return SendOperationResult.URI_EMPTY;
        } else {
            addCurrentToSendQueue();
            clear();
            send();
            return SendOperationResult.SENDING;
        }
    }

    public static SendOperationResult send_items(Set<Uri> mediaUris){
        getConnectionsAndUris().mediaUris = mediaUris;
        if(getConnectionsAndUris().connections == null || getConnectionsAndUris().connections.isEmpty()){
            return SendOperationResult.RECEIVER_EMPTY;
        } else {
            addCurrentToSendQueue();
            clear();
            send();
            return SendOperationResult.SENDING;
        }
    }

    private static void send(){
    }

    private static void addCurrentToSendQueue(){

    }

    public static void clear(){
        connectionsAndUris = null;
    }

    public enum SendOperationResult{
        SENDING, URI_EMPTY, RECEIVER_EMPTY
    }
}
