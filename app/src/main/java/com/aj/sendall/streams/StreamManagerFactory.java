package com.aj.sendall.streams;

import android.net.Uri;

import com.aj.sendall.controller.AppController;

import java.io.File;

public class StreamManagerFactory{

    public static StreamManager getInstance(Uri uri, AppController appController){
        return new UriStreamManager(uri, appController);
    }

    public static StreamManager getInstance(File file){
        return new FileStreamManager(file);
    }
}