package com.aj.sendall.ui.utils;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.dto.ConnectionsAndUris;
import com.aj.sendall.db.dto.FileInfoDTO;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.network.services.FileTransferServerService;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public final class FileTransferUIUtil {
    private  ConnectionsAndUris connectionsAndUris;

    private AppManager appManager;

    @Inject
    FileTransferUIUtil(AppManager appManager){
        this.appManager = appManager;
    }

    private ConnectionsAndUris getConnectionsAndUris(){
        if(connectionsAndUris == null){
            connectionsAndUris = new ConnectionsAndUris();
        }
        return connectionsAndUris;
    }

    public SendOperationResult send_to(Set<ConnectionViewData> receiverSet){
        if(!isOkayToSend()){
            return SendOperationResult.BUSY;
        }

        getConnectionsAndUris().connections = receiverSet;
        if(getConnectionsAndUris().fileInfoDTOs == null || getConnectionsAndUris().fileInfoDTOs.isEmpty()){
            return SendOperationResult.URI_EMPTY;
        } else {
            send();
            clear();
            return SendOperationResult.SENDING;
        }
    }

    public SendOperationResult send_items(Set<FileInfoDTO> mediaUris){
        if(!isOkayToSend()){
            return SendOperationResult.BUSY;
        }
        getConnectionsAndUris().fileInfoDTOs = mediaUris;
        if(getConnectionsAndUris().connections == null || getConnectionsAndUris().connections.isEmpty()){
            return SendOperationResult.RECEIVER_EMPTY;
        } else {
            send();
            clear();
            return SendOperationResult.SENDING;
        }
    }

    private boolean isOkayToSend(){
        return appManager.getCurrentAppStatus() == SharedPrefConstants.CURR_STATUS_IDLE;
    }

    private void send(){
        FileTransferServerService.start(appManager.context, getConnectionsAndUris());
    }

//    private static void addCurrentToSendQueue(){
//
//    }

    private void clear(){
        connectionsAndUris = null;
    }

    public enum SendOperationResult{
        SENDING, URI_EMPTY, RECEIVER_EMPTY, BUSY
    }
}
