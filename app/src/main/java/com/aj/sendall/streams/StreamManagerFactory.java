package com.aj.sendall.streams;

import android.content.Context;
import android.net.Uri;

import com.aj.sendall.db.dto.FileInfoDTO;

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
        if(path.startsWith("/tree/")){
            return getInstance(c, Uri.parse(path));
        } else {
            return getInstance(new File(path));
        }
    }

    public static StreamManager getInstance(File file){
        return new FileStreamManager(file);
    }
}