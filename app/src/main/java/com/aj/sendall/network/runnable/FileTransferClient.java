package com.aj.sendall.network.runnable;

import android.os.Environment;
import android.os.StatFs;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.db.enums.FileStatus;
import com.aj.sendall.db.model.Connections;
import com.aj.sendall.db.model.PersonalInteraction;
import com.aj.sendall.db.util.StreamUtil;
import com.aj.sendall.network.runnable.abstr.AbstractClient;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.interfaces.Updatable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class FileTransferClient extends AbstractClient {
    public static final String FAILED_AUTH_ERR = "authentication_failure";
    public static final String FAILED_NO_EXT_MEDIA = "ext_media_not_available";
    public static final String FAILED_NET_IO_ERR = "communication_error";
    public static final String FAILED_FILE_IO_ERR = "file_error";
    public static final String FAILED_IN_SUFF_SPACE = "insufficient_space";

    private Connections conn;

    public FileTransferClient(String SSID, String passPhrase, int serverPort, Updatable updatable, AppManager appManager){
        super(SSID, passPhrase, serverPort, updatable, appManager);
    }

    @Override
    protected void configureSocket(Socket socket) {

    }

    @Override
    protected void communicate() {
        try {
            //Authentication
            String thisDeviceId = appManager.sharedPrefUtil.getThisDeviceId();
            dataOutputStream.writeUTF(thisDeviceId);

            String authResult = dataInputStream.readUTF();
            if(!Constants.SUCCESS.equals(authResult)){
                failed(FAILED_AUTH_ERR);
            } else {
                String otherDeviceId = dataInputStream.readUTF();
                conn = appManager.dbUtil.getConnectionBySSID(otherDeviceId);
                if(conn != null) {
                    //Authentication success. Now transfer files
                    if(!checkExternalMedia()){
                        dataOutputStream.writeUTF(Constants.FAILED);
                        failed(FAILED_NO_EXT_MEDIA);
                    } else {
                        dataOutputStream.writeUTF(Constants.SUCCESS);
                        transferFiles();
                    }
                } else {
                    failed(null);//At this stage there is no need for updating the ui regarding the failure
                }
            }
        } catch (Exception e){
            failed(FAILED_NET_IO_ERR);
            e.printStackTrace();
        }
        tryToCloseSocket();
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
            if(Constants.SUCCESS.equals(dataInputStream.readUTF())){
                long fileSize = dataInputStream.readLong();
                int mediaType = dataInputStream.readInt();
                PersonalInteraction pi = appManager.dbUtil.getPersonalInteraction(conn.getConnectionId(), fileName, mediaType, fileSize);
                if(pi == null){
                    //No such file has been sent by the server so far
                    pi = createNewPersInter(fileName, fileSize, mediaType);
                }
                File fileToWrite;
                try {
                    fileToWrite = appManager.getTempFileToWrite(fileName);
                } catch (Exception e){
                    failed(FAILED_FILE_IO_ERR);
                    return;
                }
                pi.setFilePath(fileToWrite.getCanonicalPath());
                //For enabling pause and resume
                long nextByteToRead = pi.getBytesTransfered() + 1;
                dataOutputStream.writeLong(nextByteToRead);
                long bytesRemaining = fileSize - pi.getBytesTransfered();
                if(checkSpaceAvailability(fileToWrite, bytesRemaining)){
                    transferSingleFile(fileToWrite, fileName, bytesRemaining, pi);
                } else {
                    failed(FAILED_IN_SUFF_SPACE);
                }
            }
        }
        dataOutputStream.writeUTF(Constants.SUCCESS);
    }

    private void transferSingleFile(File fileToWrite, String originalFileName, long bytesRemining, PersonalInteraction pi){
        byte[] buff = new byte[(int) Math.min(Constants.FILE_TRANS_BUFFER_SIZE, bytesRemining)];
        long totalBytesRead = pi.getBytesTransfered();
        StreamUtil streamUtil = StreamUtil.getInstance(fileToWrite);
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
            FileOutputStream fos = (FileOutputStream) streamUtil.getOutputStream(osMode);
            while (bytesRemining > 0){
                bytesToRead = (int) Math.min(Constants.FILE_TRANS_BUFFER_SIZE, bytesRemining);
                bytesRead = dataInputStream.read(buff, 0, bytesToRead);
                if(bytesRead < 0 || bytesRead != bytesToRead){
                    failed(FAILED_NET_IO_ERR);
                    throw new IOException("Could not read data");
                }
                fos.write(buff, 0, bytesRead);
                bytesRemining -= bytesRead;
                totalBytesRead += bytesRead;
                pi.setBytesTransfered(totalBytesRead);
                transferCount++;
                //occasionally update the db
                if(transferCount == 10){
                    appManager.dbUtil.update(pi);
                    transferCount = 0;
                }
            }
            if(fos != null){
                fos.close();
            }
            if(transferCount != 0) {
                //transfer count is set to 0 whenever db is updated
                pi.setBytesTransfered(totalBytesRead);
                appManager.dbUtil.update(pi);
            }

            //now move the temporary file to permanent location
            File actualFile = appManager.getActualFileToWrite(originalFileName, pi.getMediaType());
            if(fileToWrite.renameTo(actualFile)){
                pi.setFilePath(actualFile.getPath());
                pi.setFileStatus(FileStatus.RECEIVED);
                appManager.dbUtil.update(pi);
            }
        } catch(IOException e){
            pi.setBytesTransfered(totalBytesRead);
            appManager.dbUtil.update(pi);
        } finally {
            streamUtil.close();
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

        appManager.dbUtil.save(pi);
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

    private void failed(String action){
        tryToCloseSocket();
        if(updatable != null) {
            UpdateEvent event = new UpdateEvent();
            event.source = this.getClass();
            event.data.put(Constants.ACTION, action);
            updatable.update(event);
        }
    }
}
