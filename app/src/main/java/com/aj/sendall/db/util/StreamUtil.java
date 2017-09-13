package com.aj.sendall.db.util;

import android.net.Uri;

import com.aj.sendall.application.AppManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtil implements StreamManager{
    StreamManager streamManger;

    private StreamUtil(Uri uri, AppManager appManager){
        this.streamManger = new UriStreamManager(uri, appManager);
    }

    public static StreamUtil getInstance(Uri uri, AppManager appManager){
        return new StreamUtil(uri, appManager);
    }

    @Override
    public InputStream createInputStream() throws IOException {
        return streamManger.createInputStream();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return streamManger.getInputStream();
    }

    @Override
    public OutputStream createOutputStream(String mode) throws IOException {
        return streamManger.createOutputStream(mode);
    }

    @Override
    public OutputStream getOutputStream(String mode) throws IOException {
        return streamManger.getOutputStream(mode);
    }

    @Override
    public void closeInputStream() {
        streamManger.closeInputStream();
    }

    @Override
    public void closeOutputStream() {
        streamManger.closeOutputStream();
    }

    @Override
    public void close() {
        streamManger.close();
    }

    @Override
    public long skipIS(long pos) throws IOException{
        return streamManger.skipIS(pos);
    }
}