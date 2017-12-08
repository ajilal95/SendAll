package com.aj.sendall.streams;

import com.aj.sendall.db.dto.FileInfoDTO;

import java.io.File;

public class StreamManagerFactory{

    public static StreamManager getInstance(FileInfoDTO dto){
        return getInstance(new File(dto.filePath.endsWith(dto.title) ? dto.filePath : dto.filePath + '/' + dto.title));
    }

    public static StreamManager getInstance(File file){
        return new FileStreamManager(file);
    }
}