package com.aj.sendall.nw.protocol;

public interface FileTransferProtocol {
    int PAUSE_TRANSFER = 0;
    int CONTINUE_TRANSFER = 1;
    int STOP_TRANSFER = -1;

    void authenticate() throws ProtocolException;
    void checkClientExtMedia() throws ProtocolException;
    void communicateFileCount() throws ProtocolException;
    void communicateFileNames() throws ProtocolException;
    boolean moreFilesToTransfer() throws ProtocolException;
    void confirmNextFileToTransfer() throws ProtocolException;
    void doStorageCalculationsForNextFile() throws ProtocolException;
    void transferNextFile() throws ProtocolException;

    class ProtocolExcecutor {
        public void excecute(FileTransferProtocol ftp) throws ProtocolException{
            ftp.authenticate();
            ftp.checkClientExtMedia();
            ftp.communicateFileCount();
            ftp.communicateFileNames();
            while (ftp.moreFilesToTransfer()){
                ftp.confirmNextFileToTransfer();
                ftp.doStorageCalculationsForNextFile();
                ftp.transferNextFile();
            }
        }
    }
    class ProtocolException extends Exception{
        private String status;

        public ProtocolException(String status){
            this.status = status;
        }

        public String getStatus(){
            return status;
        }
    }
}
