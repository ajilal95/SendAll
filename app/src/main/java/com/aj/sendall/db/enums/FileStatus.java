package com.aj.sendall.db.enums;

/**
 * Created by ajilal on 1/5/17.
 */

public enum FileStatus {
    SENDING, SENT, RECEIVING, RECEIVED;

    public static FileStatus getFileStatus(int intVal){
        switch(intVal){
            case 0 : return SENDING;
            case 1 : return SENT;
            case 2 : return RECEIVING;
            case 3 : return RECEIVED;
        }
        return null;
    }

    public int getIntVal(){
      switch (this){
          case SENDING  : return 0;
          case SENT     : return 1;
          case RECEIVING: return 2;
          case RECEIVED : return 3;
      }
        return -1;
    }
}
