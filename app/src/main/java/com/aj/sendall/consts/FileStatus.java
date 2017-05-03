package com.aj.sendall.consts;

/**
 * Created by ajilal on 1/5/17.
 */

public enum FileStatus {
    SENDING, SENT, RECEIVING, RECEIVED;

    public static FileStatus getFileStatus(int ordinal){
        for(FileStatus status : FileStatus.values()){
            if(status.ordinal() == ordinal){
                return status;
            }
        }
        return null;
    }
}
