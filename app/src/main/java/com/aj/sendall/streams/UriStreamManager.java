package com.aj.sendall.streams;

import android.net.Uri;

import com.aj.sendall.controller.AppController;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class UriStreamManager implements StreamManager{
    private Uri uri;
    private AppController appController;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private String currentOStreamMode;

    UriStreamManager(Uri uri, AppController appController){
        this.uri = uri;
        this.appController = appController;
    }

    @Override
    public InputStream createInputStream() throws IOException {
        if(inputStream == null){
            inputStream = appController.getApplicationContext().getContentResolver().openInputStream(uri);
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
                outputStream = appController.getApplicationContext().getContentResolver().openOutputStream(uri, mode);
            } else {
                outputStream = appController.getApplicationContext().getContentResolver().openOutputStream(uri);
            }
            currentOStreamMode = mode;
        } else {
            if((mode == null && currentOStreamMode != null) || (mode != null && currentOStreamMode == null))
                throw new IllegalStateException("OutputStream already opened in another mode");
            else if(mode != null && !mode.equalsIgnoreCase(currentOStreamMode))
                throw new IllegalStateException("OutputStream already opened in another mode");
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
