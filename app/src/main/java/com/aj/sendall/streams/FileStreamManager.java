package com.aj.sendall.streams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class FileStreamManager implements StreamManager {
    private File file;

    private FileInputStream fis;
    private FileOutputStream fos;

    private String currentOSMode;

    FileStreamManager(File file){
        this.file = file;
    }

    @Override
    public InputStream createInputStream() throws IOException, IllegalStateException {
        if(fis == null){
            fis = new FileInputStream(file);
        }
        return fis;
    }

    @Override
    public InputStream getInputStream() throws IOException, IllegalStateException {
        if(fis == null){
            createInputStream();
        }
        return fis;
    }

    @Override
    public OutputStream createOutputStream(String mode) throws IOException {
        if(mode == null)
            mode = "w";

        if(fos == null){
            if("a".equalsIgnoreCase(mode)){
                fos = new FileOutputStream(file, true);
            } else {
                fos = new FileOutputStream(file);
            }
            currentOSMode = mode;
        } else {
            if(!mode.equalsIgnoreCase(currentOSMode))
                throw new IllegalStateException("OutputStream already opened in another mode");
        }
        return fos;
    }

    @Override
    public OutputStream getOutputStream(String mode) throws IOException {
        if(fos == null){
            createOutputStream(mode);
        }
        return fos;
    }

    @Override
    public void closeInputStream() {
        if(fis != null){
            try {
                fis.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void closeOutputStream() {
        if(fos != null){
            try {
                fos.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
        closeInputStream();
        closeOutputStream();
    }

    @Override
    public long skipIS(long pos) throws IOException {
        if(fis != null){
            return fis.skip(pos);
        } else {
            return 0;
        }
    }
}
