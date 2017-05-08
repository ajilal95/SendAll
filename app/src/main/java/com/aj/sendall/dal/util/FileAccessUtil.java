package com.aj.sendall.dal.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by ajilal on 14/4/17.
 */

public final class FileAccessUtil {
    private static String getExternalMediaState(){
        return Environment.getExternalStorageState();
    }

    public static boolean isMediaReadable(){
        return isMediaWritable()
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(getExternalMediaState());
    }

    public static boolean isMediaWritable(){
        return  Environment.MEDIA_MOUNTED.equals(getExternalMediaState());
    }

    private static File getRootFile(boolean toWrite){
        File root = null;
        if((toWrite && isMediaWritable())
                || (!toWrite && isMediaReadable())) {
            root = Environment.getExternalStorageDirectory();
        }
        return root;
    }
}
