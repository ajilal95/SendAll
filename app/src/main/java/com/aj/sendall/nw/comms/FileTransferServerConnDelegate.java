package com.aj.sendall.nw.comms;

import com.aj.sendall.controller.AppConsts;
import com.aj.sendall.controller.AppController;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.dto.ConnectionsAndUris;
import com.aj.sendall.db.dto.FileInfoDTO;
import com.aj.sendall.db.enums.FileStatus;
import com.aj.sendall.db.model.PersonalInteraction;
import com.aj.sendall.events.EventRouter;
import com.aj.sendall.events.EventRouterFactory;
import com.aj.sendall.events.event.FileTransferStatusEvent;
import com.aj.sendall.nw.comms.abstr.AbstractServerConnDelegate;
import com.aj.sendall.nw.protocol.FileTransferProtocol;
import com.aj.sendall.streams.StreamManager;
import com.aj.sendall.streams.StreamManagerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Date;

class FileTransferServerConnDelegate extends AbstractServerConnDelegate implements FileTransferProtocol{
    private ConnectionsAndUris connectionsAndUris;
    private ConnectionViewData thisConn;

    private String nextFile = null;
    private FileInfoDTO fileInfoDTO = null;
    private EventRouter eventRouter = EventRouterFactory.getInstance();

    FileTransferServerConnDelegate(Socket socket, AppController appController, ConnectionsAndUris connectionsAndUris){
        super(socket, appController);
        this.connectionsAndUris = connectionsAndUris;
    }
    @Override
    protected void postStreamSetup() {
        if(connectionsAndUris != null
                && connectionsAndUris.connections != null
                && connectionsAndUris.fileInfoDTOs != null) {
            try {
                new ProtocolExcecutor().excecute(this);
            } catch (Exception e) {
                closeConn();
                e.printStackTrace();
            }
        } else {
            closeConn();
        }
    }

    private PersonalInteraction saveNewPersonalInteraction(FileInfoDTO fdto){
        PersonalInteraction pi = new PersonalInteraction();
        pi.setFileName(fdto.title);
        pi.setFileSize(fdto.size);
        pi.setBytesTransfered(0L);
        pi.setFilePath(fdto.filePath);
        pi.setFileStatus(FileStatus.SENDING);
        pi.setMediaType(fdto.mediaType);
        pi.setModifiedTime(new Date());
        pi.setConnectionId(thisConn.profileId);

        appController.save(pi);
        return pi;
    }


    private FileInfoDTO getFileInfoByFileName(String fileName){
        if(fileName != null) {
            for (FileInfoDTO dto : connectionsAndUris.fileInfoDTOs) {
                if (fileName.equals(dto.title)){
                    return dto;
                }
            }
        }
        return null;
    }

    private void closeConn(){
        closeSocket();
    }

    private void throwNetIOE() throws ProtocolException{
        throw new ProtocolException(AppConsts.FILE_TRANSFER_FAILED_NET_IO_ERR);
    }

    @Override
    public void authenticate() throws ProtocolException{
        /*
         * Check whether this is an intented client.
         * This is also useful for the client to determine whether the server
         * wants to send data to the client*/
        try {
            String otherDeviceId = dataInputStream.readUTF();
            boolean validConn = false;
            for (ConnectionViewData conn : connectionsAndUris.connections) {
                if (otherDeviceId.equals(conn.uniqueId)) {
                    thisConn = conn;
                    validConn = true;
                    break;
                }
            }
            if (!validConn) {
                //failed. inform client
                dataOutputStream.writeUTF(AppConsts.FAILED);
                dataOutputStream.flush();
                throw new ProtocolException(AppConsts.FILE_TRANSFER_FAILED_AUTH_ERR);
            }
            dataOutputStream.writeUTF(AppConsts.SUCCESS);
            dataOutputStream.flush();


            dataOutputStream.writeUTF(appController.getThisDeviceId());
            dataOutputStream.flush();
            String clientAuthMsg = dataInputStream.readUTF();
            if(!AppConsts.SUCCESS.equals(clientAuthMsg)) {
                throw new ProtocolException(AppConsts.FILE_TRANSFER_FAILED_AUTH_ERR);
            }
        } catch (IOException e){
            throwNetIOE();
        }
    }

    @Override
    public void checkClientExtMedia() throws ProtocolException{
        try {
            String clientEnvSupport = dataInputStream.readUTF();
            if(!AppConsts.SUCCESS.equals(clientEnvSupport)){
                throw new ProtocolException(AppConsts.FILE_TRANSFER_FAILED_NO_EXT_MEDIA);
            }
        } catch (IOException e){
            throwNetIOE();
        }
    }

    @Override
    public void communicateFileCount() throws ProtocolException{
        /*Inform the client about the number of files to be transferred*/
        try {
            dataOutputStream.writeInt(connectionsAndUris.fileInfoDTOs.size());
            dataOutputStream.flush();
        } catch (IOException e){
            throwNetIOE();
        }
    }

    @Override
    public void communicateFileNames() throws ProtocolException{
        /*Now send the fileInfoDTO names one by one. So that the client can request one by one*/
        try {
            for(FileInfoDTO dto : connectionsAndUris.fileInfoDTOs){
                dataOutputStream.writeUTF(dto.title);
                dataOutputStream.flush();
            }
        } catch (IOException e){
            throwNetIOE();
        }
    }

    @Override
    public boolean moreFilesToTransfer() throws ProtocolException {
        try{
            nextFile = dataInputStream.readUTF();
            //AppConsts.SUCCESS means all files has been transferred
            return !AppConsts.SUCCESS.equals(nextFile);
        } catch (IOException e) {
            throwNetIOE();
        }
        return false;
    }

    @Override
    public void confirmNextFileToTransfer() throws ProtocolException{
        try {
            //handle the situation that the client is requesting an invalid fileInfoDTO
            fileInfoDTO = getFileInfoByFileName(nextFile);
            //inform client if there is no files to send
            if (fileInfoDTO == null) {
                dataOutputStream.writeUTF(AppConsts.FAILED);
                nextFile = null;
            } else {
                dataOutputStream.writeUTF(AppConsts.SUCCESS);
            }
            dataOutputStream.flush();
        } catch (IOException e){
            throwNetIOE();
        }
    }

    @Override
    public void doStorageCalculationsForNextFile() throws ProtocolException{
        try {
            if (fileInfoDTO != null) {
                //communicate the total fileInfoDTO size
                dataOutputStream.writeLong(fileInfoDTO.size);
                dataOutputStream.flush();
                //Illegal ;) . But communicate fileInfoDTO type here
                dataOutputStream.writeInt(fileInfoDTO.mediaType);
                dataOutputStream.flush();
                //Now client has to check the space availability and inform the
                //server if it is okay to send the fileInfoDTO
                String clientResponse = dataInputStream.readUTF();
                if(!AppConsts.SUCCESS.equals(clientResponse)){
                    throw new ProtocolException(clientResponse);
                }
            }
        } catch (IOException e){
            throwNetIOE();
        }
    }

    @Override
    public void transferNextFile() throws ProtocolException{
        try {
            boolean okayToSendTheFile = true;//cross check once more
            if (nextFile == null) {
                //inform client
                dataOutputStream.writeUTF(AppConsts.FAILED);
                okayToSendTheFile = false;
            } else {
                dataOutputStream.writeUTF(AppConsts.SUCCESS);
            }
            dataOutputStream.flush();

            //check the server status also
            String clientStatus = dataInputStream.readUTF();
            if(!AppConsts.SUCCESS.equals(clientStatus)){
                okayToSendTheFile = false;
            }

            if(!okayToSendTheFile){
                return;//no need to go further
            }

            //find the PersonalInteraction
            PersonalInteraction pi = appController.getPersonalInteraction(thisConn.profileId, fileInfoDTO.title, fileInfoDTO.mediaType, fileInfoDTO.size);
            if(pi == null){
                pi = saveNewPersonalInteraction(fileInfoDTO);
            }

            //To update ui
            FileTransferStatusEvent event = new FileTransferStatusEvent();
            event.connectionId = pi.getConnectionId();
            event.fileName = nextFile;

            //communicate bytes send so far(for resuming transfer)
            long bytesTransferred = dataInputStream.readLong();
            StreamManager streamManager = StreamManagerFactory.getInstance(appController.getApplicationContext(), fileInfoDTO);
            InputStream is = streamManager.getInputStream();
            long bytesSkipped = is.skip(bytesTransferred);

            if(bytesSkipped == bytesTransferred) {
                if(bytesTransferred < fileInfoDTO.size){
                    pi.setFileStatus(FileStatus.SENDING);
                    appController.update(pi);
                }
                DataInputStream fdis = new DataInputStream(is);
                byte[] buff = new byte[(int) Math.min(AppConsts.FILE_TRANS_BUFFER_SIZE, fileInfoDTO.size)];
                int bytesRead;
//                int ack;
//                all :
                while ((bytesRead = fdis.read(buff)) > 0) {
                    try {
                        dataOutputStream.write(buff, 0, bytesRead);
//                        dataOutputStream.flush();
//                        ack = dataInputStream.readInt();//an acknowledgement from client that data received
//                        switch(ack){
//                            case CONTINUE_TRANSFER :
//                                break;
//                            case STOP_TRANSFER :
//                                break all;
//                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                    bytesTransferred += bytesRead;
                    event.totalTransferred = bytesTransferred;
                    eventRouter.broadcast(event);
                }
                dataOutputStream.flush();
            }
            streamManager.close();
            pi.setBytesTransfered(bytesTransferred);
            pi.setFileStatus(FileStatus.SENT);
            appController.update(pi);
            event.totalTransferred = FileTransferStatusEvent.COMPLETED;
            eventRouter.broadcast(event);
        } catch (IOException e){
            throwNetIOE();
        }
    }
}
