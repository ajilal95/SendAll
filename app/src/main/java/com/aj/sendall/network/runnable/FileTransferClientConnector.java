package com.aj.sendall.network.runnable;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.dto.ConnectionsAndUris;
import com.aj.sendall.db.dto.FileInfoDTO;
import com.aj.sendall.db.enums.FileStatus;
import com.aj.sendall.db.model.PersonalInteraction;
import com.aj.sendall.db.util.StreamUtil;
import com.aj.sendall.network.runnable.abstr.AbstractClientConnector;
import com.aj.sendall.network.utils.Constants;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;

class FileTransferClientConnector extends AbstractClientConnector {
    private ConnectionsAndUris connectionsAndUris;
    private ConnectionViewData thisConn;

    FileTransferClientConnector(Socket socket, AppManager appManager, ConnectionsAndUris connectionsAndUris){
        super(socket, null, appManager);
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
                    dataOutputStream.writeUTF(Constants.SUCCESS);
                    communicate();
                } else {
                    dataOutputStream.writeUTF(Constants.FAILED);
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
        while(!(request.equals(Constants.SUCCESS) || request.equals(Constants.FAILED))
                && fileCount > 0){
            String fileName = request;
            FileInfoDTO file = getFileInfoByFileName(fileName);
            if(file == null){
                //Failed. So read the next request and continue
                dataOutputStream.writeUTF(Constants.FAILED);
            } else {
                dataOutputStream.writeUTF(Constants.SUCCESS);
                StreamUtil streamUtil = StreamUtil.getInstance(file.uri, appManager);
                InputStream is = streamUtil.getInputStream();
                dataOutputStream.writeLong(file.size);//inform about the size of the file
                long nextBytePos = dataInputStream.readLong();//To enable resuming failed transfer
                long totalSent = nextBytePos - 1;
                long skipped = is.skip(totalSent);//resuming
                //Inform the skip status
                if(skipped == nextBytePos - 1 || nextBytePos <= 0){
                    //Skip is successful
                    dataOutputStream.writeUTF(Constants.SUCCESS);
                    PersonalInteraction pi;
                    if(nextBytePos == 0){
                        //Transfering a new file
                        pi = saveNewPersonalInteraction(file);
                    } else {
                        pi = appManager.dbUtil.getPersonalInteraction(thisConn.profileId, file.filePath, file.mediaType);
                    }
                    DataInputStream fdis = new DataInputStream(is);
                    byte[] buff = new byte[2048];
                    int bytesRead;
                    int transferCount = 0;
                    while ((bytesRead = fdis.read(buff)) > 0){
                        try {
                            dataOutputStream.write(buff, 0, bytesRead);
                        } catch (Exception e){
                            pi.setBytesTransfered(totalSent);
                            appManager.dbUtil.update(pi);
                            streamUtil.close();
                            throw e;
                        }
                        totalSent += bytesRead;
                        pi.setBytesTransfered(totalSent);
                        transferCount++;
                        //Update db occasionally
                        if(transferCount == 10) {
                            appManager.dbUtil.update(pi);
                            transferCount = 0;
                        }
                    }
                    pi.setBytesTransfered(totalSent);
                    appManager.dbUtil.update(pi);
                    streamUtil.close();
                } else {
                    dataOutputStream.writeUTF(Constants.FAILED);
                }
            }
            try {
                request = dataInputStream.readUTF();
            } catch (SocketException | SocketTimeoutException e){
                break;
            }
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

        appManager.dbUtil.save(pi);
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
        UpdateEvent updateEvent = new UpdateEvent();
        updateEvent.data.put(Constants.ACTION, Constants.CLOSE_SOCKET);
        update(updateEvent);
    }
}
