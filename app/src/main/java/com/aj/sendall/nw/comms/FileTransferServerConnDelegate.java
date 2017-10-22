package com.aj.sendall.nw.comms;

import com.aj.sendall.controller.AppConsts;
import com.aj.sendall.controller.AppController;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.dto.ConnectionsAndUris;
import com.aj.sendall.db.dto.FileInfoDTO;
import com.aj.sendall.db.enums.FileStatus;
import com.aj.sendall.db.model.PersonalInteraction;
import com.aj.sendall.streams.StreamManager;
import com.aj.sendall.streams.StreamManagerFactory;
import com.aj.sendall.nw.comms.abstr.AbstractServerConnDelegate;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;

class FileTransferServerConnDelegate extends AbstractServerConnDelegate {
    private ConnectionsAndUris connectionsAndUris;
    private ConnectionViewData thisConn;

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
                /*
                * Check whether this is an intented client.
                * This is also useful for the client to determine whether the server
                * wants to send data to the client*/
                String otherDeviceId = dataInputStream.readUTF();
                boolean validConn = false;
                for (ConnectionViewData conn : connectionsAndUris.connections) {
                    if(otherDeviceId.equals(conn.uniqueId)){
                        thisConn = conn;
                        validConn = true;
                        break;
                    }
                }

                if(validConn){
                    dataOutputStream.writeUTF(AppConsts.SUCCESS);
                    communicate();
                } else {
                    dataOutputStream.writeUTF(AppConsts.FAILED);
                    closeConn();
                }

            } catch (Exception e) {
                closeConn();
                e.printStackTrace();
            }
        } else {
            closeConn();
        }
    }

    private void communicate() throws Exception{
        /*Inform the client about the number of files to be transferred*/
        dataOutputStream.writeInt(connectionsAndUris.fileInfoDTOs.size());

        /*Now send the file names one by one. So that the client can request one by one*/
        for(FileInfoDTO dto : connectionsAndUris.fileInfoDTOs){
            dataOutputStream.writeUTF(dto.title);
        }

        /*Now send the files one by one as per the request from the client*/
        String request = dataInputStream.readUTF();//The fileName or the terminate request
        int fileCount = connectionsAndUris.fileInfoDTOs.size();
        while(!(request.equals(AppConsts.SUCCESS) || request.equals(AppConsts.FAILED))
                && fileCount > 0){
            String fileName = request;
            FileInfoDTO file = getFileInfoByFileName(fileName);
            if(file == null){
                //Failed. So read the next request and continue
                dataOutputStream.writeUTF(AppConsts.FAILED);
            } else {
                dataOutputStream.writeUTF(AppConsts.SUCCESS);
                //Now send this device id for the other device to confirm
                dataOutputStream.writeUTF(appController.getThisDeviceId());
                String result = dataInputStream.readUTF();
                if(!AppConsts.SUCCESS.equals(result)){
                    //Auth failed
                    closeConn();
                    return;
                }
                StreamManager streamManager = StreamManagerFactory.getInstance(file.uri, appController);
                InputStream is = streamManager.getInputStream();
                dataOutputStream.writeLong(file.size);//inform about the size of the file
                dataOutputStream.writeInt(file.mediaType);
                long nextBytePos = dataInputStream.readLong();//To enable resuming failed transfer
                long totalSent = nextBytePos - 1;
                long skipped = is.skip(totalSent);//resuming
                //Inform the skip status
                if(skipped == totalSent || nextBytePos <= 0){
                    //Skip is successful
                    dataOutputStream.writeUTF(AppConsts.SUCCESS);
                    PersonalInteraction pi;
                    if(nextBytePos == 0){
                        //Transferring a new file
                        pi = saveNewPersonalInteraction(file);
                    } else {
                        pi = appController.getPersonalInteraction(thisConn.profileId, file.title, file.mediaType, file.size);
                    }
                    DataInputStream fdis = new DataInputStream(is);
                    byte[] buff = new byte[(int) Math.min(AppConsts.FILE_TRANS_BUFFER_SIZE, file.size)];
                    int bytesRead;
                    int transferCount = 0;
                    while ((bytesRead = fdis.read(buff)) > 0){
                        try {
                            dataOutputStream.write(buff, 0, bytesRead);
                        } catch (Exception e){
                            pi.setBytesTransfered(totalSent);
                            appController.update(pi);
                            streamManager.close();
                            throw e;
                        }
                        totalSent += bytesRead;
                        pi.setBytesTransfered(totalSent);
                        transferCount++;
                        //Update db occasionally
                        if(transferCount == 10) {
                            appController.update(pi);
                            transferCount = 0;
                        }
                    }
                    if(transferCount != 0) {
                        //transfer count is set to 0 whenever db is updated
                        pi.setBytesTransfered(totalSent);
                        pi.setFileStatus(FileStatus.SENT);
                        appController.update(pi);
                    }
                } else {
                    dataOutputStream.writeUTF(AppConsts.FAILED);
                }
                streamManager.close();
            }
            try {
                //Read the next filename
                request = dataInputStream.readUTF();
            } catch (SocketException | SocketTimeoutException e){
                break;
            }

            fileCount--;
        }

    }

    private PersonalInteraction saveNewPersonalInteraction(FileInfoDTO fdto){
        PersonalInteraction pi = new PersonalInteraction();
        pi.setFileName(fdto.title);
        pi.setFileSize(fdto.size);
        pi.setBytesTransfered(0L);
        pi.setFileUri(fdto.uri.toString());//TODO check the correctness ot toString() of URI
        pi.setFilePath(fdto.filePath);
        pi.setFileStatus(FileStatus.SENDING);
        pi.setMediaType(fdto.mediaType);
        pi.setModifiedTime(new Date());
        pi.setConnectionId(thisConn.profileId);

        appController.saveToDB(pi);
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
}