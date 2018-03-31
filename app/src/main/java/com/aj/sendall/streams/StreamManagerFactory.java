package com.aj.sendall.streams;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.aj.sendall.db.dto.FileInfoDTO;
import com.aj.sendall.utils.ThisDevice;

import java.io.File;

public class StreamManagerFactory{

    public static StreamManager getInstance(Context c, FileInfoDTO dto){
        String filePath = dto.filePath.endsWith(dto.title) ? dto.filePath : dto.filePath + '/' + dto.title;
        return getInstance(c, filePath);
    }

    public static StreamManager getInstance(Context c, Uri treeUri){
        return new TreeUriStreamManager(c, treeUri);
    }

    public static StreamManager getInstance(Context c, String path){

        if(ThisDevice.canUseTreeUri()){
            try {
                //Use SAF
                return getInstance(c, Uri.parse(path));
            } catch (Exception e){
                Log.w(StreamManagerFactory.class.getSimpleName(), "Tried and failed to parse to uri : " + path);
                //in case path is not a tree uri
                return getInstance(new File(path), c);
            }
        } else {
            return getInstance(new File(path), c);
        }
    }

    public static StreamManager getInstance(File file, Context context){
        return new FileStreamManager(file, context);
    }
}