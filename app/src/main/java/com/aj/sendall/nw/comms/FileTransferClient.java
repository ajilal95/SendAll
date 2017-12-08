package com.aj.sendall.nw.comms;

import android.os.Environment;
import android.os.StatFs;

import com.aj.sendall.events.EventRouter;
import com.aj.sendall.events.EventRouterFactory;
import com.aj.sendall.events.event.FileTransferStatusEvent;
import com.aj.sendall.events.event.FileTransfersFinished;
import com.aj.sendall.controller.AppConsts;
import com.aj.sendall.controller.AppController;
import com.aj.sendall.db.enums.FileStatus;
import com.aj.sendall.db.model.Connections;
import com.aj.sendall.db.model.PersonalInteraction;
import com.aj.sendall.nw.protocol.FileTransferProtocol;
import com.aj.sendall.streams.StreamManager;
import com.aj.sendall.streams.StreamManagerFactory;
import com.aj.sendall.nw.comms.abstr.AbstractClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;

public class FileTransferClient extends AbstractClient implements FileTransferProtocol{
    private String clientStatus = AppConsts.FILE_TRANSFER_SUCCESS;
    private String ssid;

    private EventRouter eventRouter = EventRouterFactory.getInstance();

    private Connections conn;

    private int fileCount = 0;
    private LinkedList<String> fileNames = new LinkedList<>();
    private String nextFile = null;
    private long nextFileSize = 0;
    private int nextMediaType = 0;
    private File fileToWrite;
    private long bytesReadForNextFile = 0;

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
            new ProtocolExcecutor().excecute(this);
        } catch (Exception e){
            failed(AppConsts.FILE_TRANSFER_FAILED_NET_IO_ERR);
            closeStreams();
            e.printStackTrace();
        }
    }

    private boolean checkSpaceAvailability(File fileToWrite, long bytesNeeded) throws IOException{
        String filePath = fileToWrite.getCanonicalPath();
        StatFs statFs = new StatFs(filePath.substring(0, filePath.lastIndexOf('/')));
        long available = statFs.getAvailableBytes();
        return available >= bytesNeeded;
    }

    private PersonalInteraction createNewPersInter(String fileName, long fileSize, int mediaType, String filePath, long bytesTransfered) {
        PersonalInteraction pi = new PersonalInteraction();
        pi.setFileName(fileName);
        pi.setMediaType(mediaType);
        pi.setFileSize(fileSize);
        pi.setBytesTransfered(bytesTransfered);
        pi.setFilePath(filePath);
        pi.setFileStatus(FileStatus.RECEIVING);
        pi.setConnectionId(conn.getConnectionId());
        pi.setModifiedTime(new Date());

        appController.save(pi);
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

    private void throwNetIOE() throws ProtocolException{
        throw new ProtocolException(AppConsts.FILE_TRANSFER_FAILED_NET_IO_ERR);
    }

    @Override
    public void authenticate() throws ProtocolException{
        //Authentication
        try {
            String thisDeviceId = appController.getThisDeviceId();
            dataOutputStream.writeUTF(thisDeviceId);
            dataOutputStream.flush();
            String authResult = dataInputStream.readUTF();
            if (!AppConsts.SUCCESS.equals(authResult)) {
                throw new ProtocolException(AppConsts.FILE_TRANSFER_FAILED_AUTH_ERR);
            }

            String otherDeviceId = dataInputStream.readUTF();
            conn = appController.getConnectionBySSID(otherDeviceId);
            if (conn == null) {
                //failed. inform server
                dataOutputStream.writeUTF(AppConsts.FAILED);
                dataOutputStream.flush();
                throw new ProtocolException(AppConsts.FILE_TRANSFER_FAILED_AUTH_ERR);
            }
            //Authentication success.
            dataOutputStream.writeUTF(AppConsts.SUCCESS);
            dataOutputStream.flush();
        } catch (IOException e){
            throw new ProtocolException(AppConsts.FILE_TRANSFER_FAILED_NET_IO_ERR);
        }
    }

    @Override
    public void checkClientExtMedia() throws ProtocolException{
        try {
            if(!checkExternalMedia()){
                dataOutputStream.writeUTF(AppConsts.FAILED);
                dataOutputStream.flush();
                throw new ProtocolException(AppConsts.FILE_TRANSFER_FAILED_NO_EXT_MEDIA);
            }
            dataOutputStream.writeUTF(AppConsts.SUCCESS);
            dataOutputStream.flush();
        } catch (IOException e){
            throwNetIOE();
        }
    }

    @Override
    public void communicateFileCount() throws ProtocolException{
        //Read the number of files that the server has to send
        try {
            fileCount = dataInputStream.readInt();
        } catch (IOException e){
            throwNetIOE();
        }
    }

    @Override
    public void communicateFileNames() throws ProtocolException{
        //Now read the filenames one by one
        try {
            for(int i = 0; i < fileCount; i++){
                fileNames.add(dataInputStream.readUTF());
            }
        } catch (IOException e){
            throwNetIOE();
        }
    }

    @Override
    public boolean moreFilesToTransfer() throws ProtocolException{
        try {
            nextFile = fileNames.pollFirst();//take the first file and remove it from the list
            if (nextFile != null) {
                //send the file name to the sender
                dataOutputStream.writeUTF(nextFile);
                dataOutputStream.flush();
                return true;
            } else {
                //list is empty means all files has been transferred
                dataOutputStream.writeUTF(AppConsts.SUCCESS);
                dataOutputStream.flush();
                return false;
            }
        } catch (IOException e){
            throwNetIOE();
        }
        return false;
    }

    @Override
    public void confirmNextFileToTransfer() throws ProtocolException{
        //Server must have checked the file name validity
        try{
            String serverResponse = dataInputStream.readUTF();
            if(!AppConsts.SUCCESS.equals(serverResponse)){
                nextFile = null;
            }
        } catch (IOException e){
            throwNetIOE();
        }
    }

    @Override
    public void doStorageCalculationsForNextFile() throws ProtocolException{
        try {
            //communicate the total file size
            nextFileSize = dataInputStream.readLong();
            //Illegal ;) . But communicate file type here
            nextMediaType = dataInputStream.readInt();
            //Now client has to check the space availability and inform the
            //server if it is okay to send the file
            try {
                fileToWrite = appController.getTempFileToWrite(conn.getConnectionId(), nextFile, nextMediaType);
            } catch (Exception e){
                e.printStackTrace();
            }
            if(fileToWrite == null){
                //failed. inform server and return.(AppConsts.FILE_TRANSFER_FAILED_FILE_IO_ERR);
                nextFile = null;
                dataOutputStream.writeUTF(AppConsts.FILE_TRANSFER_FAILED_FILE_IO_ERR);
                dataOutputStream.flush();
                throw new ProtocolException(AppConsts.FILE_TRANSFER_FAILED_FILE_IO_ERR);
            }

            bytesReadForNextFile = fileToWrite.length();
            long bytesRemining = nextFileSize - bytesReadForNextFile;
            if(!checkSpaceAvailability(fileToWrite, bytesRemining)){
                //insufficient space. inform server (AppConsts.FILE_TRANSFER_FAILED_IN_SUFF_SPACE);
                nextFile = null;
                dataOutputStream.writeUTF(AppConsts.FILE_TRANSFER_FAILED_IN_SUFF_SPACE);
                dataOutputStream.flush();
                throw new ProtocolException(AppConsts.FILE_TRANSFER_FAILED_IN_SUFF_SPACE);
            }
            //All the checks passed. It is okay to send the file
            dataOutputStream.writeUTF(AppConsts.SUCCESS);
            dataOutputStream.flush();
        } catch (IOException e){
            throwNetIOE();
        }
    }

    @Override
    public void transferNextFile() throws ProtocolException{
        try {
            boolean okayToSendTheFile = true;//cross check once more
            if (nextFile == null) {
                //inform server
                dataOutputStream.writeUTF(AppConsts.FAILED);
                okayToSendTheFile = false;
            } else {
                dataOutputStream.writeUTF(AppConsts.SUCCESS);
            }
            dataOutputStream.flush();

            //check the server status also
            String serverStatus = dataInputStream.readUTF();
            if(!AppConsts.SUCCESS.equals(serverStatus)){
                okayToSendTheFile = false;
            }

            if(!okayToSendTheFile){
                return;//no need to go further
            }

            //find the PersonalInteraction
            PersonalInteraction pi = appController.getPersonalInteraction(conn.getConnectionId(), nextFile, nextMediaType, nextFileSize);
            if(pi == null){
                pi = createNewPersInter(nextFile, nextFileSize, nextMediaType, fileToWrite.getCanonicalPath(), bytesReadForNextFile);
            }

            //communicate bytes send so far(for resuming transfer)
            dataOutputStream.writeLong(bytesReadForNextFile);
            dataOutputStream.flush();
            String osMode = "a";
            StreamManager streamManager = StreamManagerFactory.getInstance(fileToWrite);
            FileOutputStream fos = (FileOutputStream) streamManager.getOutputStream(osMode);

            //to update ui
            FileTransferStatusEvent event = new FileTransferStatusEvent();
            event.connectionId = pi.getConnectionId();
            event.fileName = nextFile;

            long bytesRemaining = nextFileSize - bytesReadForNextFile;
            if(bytesRemaining > 0L){
                pi.setFileStatus(FileStatus.RECEIVING);
                appController.update(pi);
            }
            int bytesToRead;
            int bytesToReadRemaining;
            int bytesRead;
            int readRetryCount = 0;
            byte[] buff = new byte[(int) Math.min(AppConsts.FILE_TRANS_BUFFER_SIZE, bytesRemaining)];
            all :
            while (bytesRemaining > 0){
                bytesToRead = (int) Math.min(AppConsts.FILE_TRANS_BUFFER_SIZE, bytesRemaining);
                bytesToReadRemaining = bytesToRead;
                while(bytesToReadRemaining > 0) {
                    try {
                        bytesRead = dataInputStream.read(buff, 0, bytesToRead);
                        if(bytesRead < 0){
                            throw new IOException("Read -1 bytes");
                        }
                        readRetryCount = 0;
                    } catch (Exception e) {
                        if(++readRetryCount > 5) {
                            dataOutputStream.writeInt(STOP_TRANSFER);
                            dataOutputStream.flush();
                            break all;
                        }
                        continue;
                    }
                    try {
                        fos.write(buff, 0, bytesRead);
                    } catch (IOException e) {
                        dataOutputStream.writeInt(STOP_TRANSFER);
                        dataOutputStream.flush();
                        break all;
                    }
                    bytesToReadRemaining -= bytesRead;
                    bytesRemaining -= bytesRead;
                    bytesReadForNextFile += bytesRead;
                    event.totalTransferred = bytesReadForNextFile;
                    eventRouter.broadcast(event);
                }
                dataOutputStream.writeInt(CONTINUE_TRANSFER);
                dataOutputStream.flush();
            }
            fos.flush();
            streamManager.close();
            pi.setBytesTransfered(bytesReadForNextFile);
            appController.update(pi);

            //now move the temporary file to permanent location if the file reading is complete
            if(bytesRemaining == 0) {
                File actualFile = appController.getActualFileToWrite(nextFile, pi.getMediaType());
                if(fileToWrite.renameTo(actualFile)){
                    pi.setFilePath(actualFile.getPath());
                    pi.setFileStatus(FileStatus.RECEIVED);
                    appController.update(pi);
                }
            }
            event.totalTransferred = FileTransferStatusEvent.COMPLETED;
            eventRouter.broadcast(event);
        } catch (IOException e){
            throwNetIOE();
        }
    }
}
