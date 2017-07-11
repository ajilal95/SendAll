package com.aj.sendall.ui.businessservices;

import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.dto.ConnectionsAndUris;
import com.aj.sendall.db.dto.FileInfoDTO;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.network.broadcastreceiver.FileTransferGrpCreatnLstnr;
import com.aj.sendall.application.AppManager;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by ajilal on 3/5/17.
 */

@Singleton
public final class FileSendingService {
    private  ConnectionsAndUris connectionsAndUris;

    private AppManager appManager;

    @Inject
    public FileSendingService(AppManager appManager){
        this.appManager = appManager;
    }

    private ConnectionsAndUris getConnectionsAndUris(){
        if(connectionsAndUris == null){
            connectionsAndUris = new ConnectionsAndUris();
        }
        return connectionsAndUris;
    }

    public SendOperationResult send_to(Set<ConnectionViewData> receiverSet){
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
        getConnectionsAndUris().fileInfoDTOs = mediaUris;
        if(getConnectionsAndUris().connections == null || getConnectionsAndUris().connections.isEmpty()){
            return SendOperationResult.RECEIVER_EMPTY;
        } else {
            send();
            clear();
            return SendOperationResult.SENDING;
        }
    }

    private void send(){
        FileTransferGrpCreatnLstnr fileTransferGrpCreatnLstnr = new FileTransferGrpCreatnLstnr(appManager, connectionsAndUris);
        appManager.createGroupAndAdvertise(fileTransferGrpCreatnLstnr, SharedPrefConstants.CURR_STATUS_SENDING);
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
