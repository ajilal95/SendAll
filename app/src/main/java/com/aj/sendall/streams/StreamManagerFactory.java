package com.aj.sendall.streams;

import android.net.Uri;

import com.aj.sendall.controller.AppController;
import com.aj.sendall.db.dto.FileInfoDTO;

import java.io.File;

public class StreamManagerFactory{

    public static StreamManager getInstance(FileInfoDTO dto, AppController appController){
        if(dto.uri != null) {
            return new UriStreamManager(dto.uri, appController);
        } else {
            return getInstance(new File(dto.filePath.endsWith(dto.title) ? dto.filePath : dto.filePath + '/' + dto.title));
        }
    }

    public static StreamManager getInstance(File file){
        return new FileStreamManager(file);
    }
}