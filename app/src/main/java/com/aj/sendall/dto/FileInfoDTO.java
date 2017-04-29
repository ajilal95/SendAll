package com.aj.sendall.dto;

import android.net.Uri;

/**
 * Created by ajilal on 26/4/17.
 */

public class FileInfoDTO {
    public boolean isSelected = false;
    public Uri uri = null;
    public int id;
    public String title;
    public long size;
    public int albumId;

    public String getSizeString(){
        String sizeStringFinal;
        if(size < 1024l){
            sizeStringFinal = size + " Bytes";
        } else if(size < (1024 * 1024)){
            sizeStringFinal = (size / 1024) + " KB";
        } else if(size < (1024 * 1024 * 1024)){
            String sizeString = ((float) size) / (1024 * 1024) + "";
            int indexOfDecimalPoint = sizeString.indexOf('.');
            sizeString = sizeString.substring(0,
                    indexOfDecimalPoint >= 0 ?
                            Math.min(indexOfDecimalPoint + 3, sizeString.length()) :
                            sizeString.length());
            sizeStringFinal = sizeString + " MB";
        } else {
            String sizeString = ((float) size) / (1024 * 1024 * 1024) + "";
            int indexOfDecimalPoint = sizeString.indexOf('.');
            sizeString = sizeString.substring(0,
                    indexOfDecimalPoint >= 0 ?
                            Math.min(indexOfDecimalPoint + 3, sizeString.length()) :
                            sizeString.length());
            sizeStringFinal = sizeString + " GB";
        }
        return sizeStringFinal;
    }
}
