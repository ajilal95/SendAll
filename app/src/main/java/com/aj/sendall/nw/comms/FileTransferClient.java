package com.aj.sendall.nw.comms;

import android.os.Environment;
import android.os.StatFs;

import com.aj.sendall.events.EventRouter;
import com.aj.sendall.events.EventRouterFactory;
import com.aj.sendall.events.event.FileTransfersFinished;
import com.aj.sendall.controller.AppConsts;
import com.aj.sendall.controller.AppController;
import com.aj.sendall.db.enums.FileStatus;
import com.aj.sendall.db.model.Connections;
import com.aj.sendall.db.model.PersonalInteraction;
import com.aj.sendall.streams.StreamManager;
import com.aj.sendall.streams.StreamManagerFactory;
import com.aj.sendall.nw.comms.abstr.AbstractClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class FileTransferClient extends AbstractClient {
    private String clientStatus = AppConsts.FILE_TRANSFER_SUCCESS;
    private String ssid;

    private EventRouter eventRouter = EventRouterFactory.getInstance();

    private Connections conn;

    public FileTransferClient(String SSID, String passPhrase, int serverPort, AppController appController){
        super(SSID, passPhrase, serverPort, appController);
        this.ssid = SSID;
    }

    @Override
    protected void configureSocket(Socket socket) {

    }

    @Override
    protected void communicate() {
        try {
            //Authentication
            String thisDeviceId = appController.getThisDeviceId();
            dataOutputStream.writeUTF(thisDeviceId);

            String authResult = dataInputStream.readUTF();
            if(!AppConsts.SUCCESS.equals(authResult)){
                failed(AppConsts.FILE_TRANSFER_FAILED_AUTH_ERR);
            } else {
                String otherDeviceId = dataInputStream.readUTF();
                conn = appController.getConnectionBySSID(otherDeviceId);
                if(conn != null) {
                    //Authentication success. Now transfer files
                    if(!checkExternalMedia()){
                        dataOutputStream.writeUTF(AppConsts.FAILED);
                        failed(AppConsts.FILE_TRANSFER_FAILED_NO_EXT_MEDIA);
                    } else {
                        dataOutputStream.writeUTF(AppConsts.SUCCESS);
                        transferFiles();
                    }
                } else {
                    failed(AppConsts.FILE_TRANSFER_FAILED_AUTH_ERR);
                }
            }
        } catch (Exception e){
            failed(AppConsts.FILE_TRANSFER_FAILED_NET_IO_ERR);
            closeStreams();
            e.printStackTrace();
        }
    }

    private void transferFiles() throws Exception {
        //Read the number of files that the server has to send
        int fileCount = dataInputStream.readInt();

        //Now read the filenames one by one
        Set<String> fileNames = new HashSet<>();
        for(int i = 0; i < fileCount; i++){
            fileNames.add(dataInputStream.readUTF());
        }

        //Now read the files one by one
        for(String fileName : fileNames){
            dataOutputStream.writeUTF(fileName);
            //Check for availability of file in the server
            if(AppConsts.SUCCESS.equals(dataInputStream.readUTF())){
                long fileSize = dataInputStream.readLong();
                int mediaType = dataInputStream.readInt();
                PersonalInteraction pi = appController.getPersonalInteraction(conn.getConnectionId(), fileName, mediaType, fileSize);
                if(pi == null){
                    //No such file has been sent by the server so far
                    pi = createNewPersInter(fileName, fileSize, mediaType);
                }
                File fileToWrite;
                try {
                    fileToWrite = appController.getTempFileToWrite(conn.getConnectionId(), fileName, mediaType);
                } catch (Exception e){
                    failed(AppConsts.FILE_TRANSFER_FAILED_FILE_IO_ERR);
                    return;
                }
                pi.setFilePath(fileToWrite.getCanonicalPath());
                pi.setBytesTransfered(fileToWrite.length());
                //For enabling pause and resume
                long nextByteToRead = pi.getBytesTransfered() + 1;
                dataOutputStream.writeLong(nextByteToRead);
                long bytesRemaining = fileSize - pi.getBytesTransfered();
                if(checkSpaceAvailability(fileToWrite, bytesRemaining)){
                    transferSingleFile(fileToWrite, fileName, bytesRemaining, pi);
                } else {
                    failed(AppConsts.FILE_TRANSFER_FAILED_IN_SUFF_SPACE);
                    return;
                }
            }
        }
        dataOutputStream.writeUTF(AppConsts.SUCCESS);
    }

    private void transferSingleFile(File fileToWrite, String originalFileName, long bytesRemining, PersonalInteraction pi){
        byte[] buff = new byte[(int) Math.min(AppConsts.FILE_TRANS_BUFFER_SIZE, bytesRemining)];
        long totalBytesRead = pi.getBytesTransfered();
        StreamManager streamManager = StreamManagerFactory.getInstance(fileToWrite);
        try {
            String osMode;
            if(fileToWrite.exists()){
                osMode = "a";
            } else {
                osMode = "w";
            }
            int transferCount = 0;
            int bytesRead;
            int bytesToRead;
            FileOutputStream fos = (FileOutputStream) streamManager.getOutputStream(osMode);
            while (bytesRemining > 0){
                bytesToRead = (int) Math.min(AppConsts.FILE_TRANS_BUFFER_SIZE, bytesRemining);
                bytesRead = dataInputStream.read(buff, 0, bytesToRead);
                if(bytesRead < 0 || bytesRead != bytesToRead){
                    failed(AppConsts.FILE_TRANSFER_FAILED_NET_IO_ERR);
                    throw new IOException("Could not read data");
                }
                fos.write(buff, 0, bytesRead);
                bytesRemining -= bytesRead;
                totalBytesRead += bytesRead;
                pi.setBytesTransfered(totalBytesRead);
                transferCount++;
                //occasionally update the db
                if(transferCount == 10){
                    appController.update(pi);
                    transferCount = 0;
                }
            }
            if(fos != null){
                fos.close();
            }
            if(transferCount != 0) {
                //transfer count is set to 0 whenever db is updated
                pi.setBytesTransfered(totalBytesRead);
                appController.update(pi);
            }

            //now move the temporary file to permanent location
            File actualFile = appController.getActualFileToWrite(originalFileName, pi.getMediaType());
            if(fileToWrite.renameTo(actualFile)){
                pi.setFilePath(actualFile.getPath());
                pi.setFileStatus(FileStatus.RECEIVED);
                appController.update(pi);
            }
        } catch(IOException e){
            pi.setBytesTransfered(totalBytesRead);
            appController.update(pi);
        } finally {
            streamManager.close();
        }

    }

    private boolean checkSpaceAvailability(File fileToWrite, long bytesNeeded) throws IOException{
        String filePath = fileToWrite.getCanonicalPath();
        StatFs statFs = new StatFs(filePath.substring(0, filePath.lastIndexOf('/')));
        return statFs.getAvailableBytes() >= bytesNeeded;
    }

    private PersonalInteraction createNewPersInter(String fileName, long fileSize, int mediaType) {
        PersonalInteraction pi;
        pi = new PersonalInteraction();
        pi.setFileName(fileName);
        pi.setMediaType(mediaType);
        pi.setFileSize(fileSize);
        pi.setBytesTransfered(0L);
        pi.setFileStatus(FileStatus.RECEIVING);
        pi.setConnectionId(conn.getConnectionId());
        pi.setModifiedTime(new Date());

        appController.saveToDB(pi);
        return pi;
    }

    private boolean checkExternalMedia(){
        boolean mExternalStorageWriteable;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            mExternalStorageWriteable = false;
        } else {
            // Can't read or write
            mExternalStorageWriteable = false;
        }
        return mExternalStorageWriteable;
    }

    private void failed(String failureReason){
        clientStatus = failureReason;
    }

    @Override
    protected void finalAction(){
        FileTransfersFinished event = new FileTransfersFinished();
        event.status = clientStatus;
        event.deviceId = ssid;
        eventRouter.broadcast(event);
    }
}
