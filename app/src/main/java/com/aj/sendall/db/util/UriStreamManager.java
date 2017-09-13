package com.aj.sendall.db.util;

import android.net.Uri;

import com.aj.sendall.application.AppManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class UriStreamManager implements StreamManager{
    private Uri uri;
    private AppManager appManager;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private String currentOStreamMode = null;

    public UriStreamManager(Uri uri, AppManager appManager){
        this.uri = uri;
        this.appManager = appManager;
    }

    @Override
    public InputStream createInputStream() throws IOException {
        if(inputStream == null){
            inputStream = appManager.context.getContentResolver().openInputStream(uri);
        }
        return inputStream;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if(inputStream == null){
            createInputStream();
        }
        return inputStream;
    }


    @Override
    public OutputStream createOutputStream(String mode) throws IOException, IllegalStateException {
        if(outputStream == null){
            if(mode != null) {
                outputStream = appManager.context.getContentResolver().openOutputStream(uri, mode);
            } else {
                outputStream = appManager.context.getContentResolver().openOutputStream(uri);
            }
        } else {
            if(mode != currentOStreamMode && !mode.equals(currentOStreamMode))
                throw new IllegalStateException("OutputStream alredy opened in another mode");
        }
        return outputStream;
    }

    public OutputStream getOutputStream(String mode) throws IOException, IllegalStateException {
        if(outputStream == null){
            createOutputStream(mode);
        }
        return outputStream;
    }

    @Override
    public long skipIS(long pos) throws IOException{
        return inputStream.skip(pos);
    }

    @Override
    public void closeInputStream() {
        if(inputStream != null){
            try {
                inputStream.close();
            } catch (IOException e){
                e.printStackTrace();
            }
            inputStream = null;
        }
    }

    @Override
    public void closeOutputStream() {
        if(outputStream != null){
            try {
                outputStream.close();
            } catch (IOException e){
                e.printStackTrace();
            }
            outputStream = null;
        }
    }

    @Override
    public void close() {
        closeInputStream();
        closeOutputStream();
    }
}
