package com.aj.sendall.streams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

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

    @Override
    public boolean isDir() {
        return file != null && file.isDirectory();
    }

    @Override
    public StreamManager createDir(String dirName) throws IOException{
        if(isDir()){
            File newFile = new File(file.getCanonicalPath() + "/" + dirName);
            if(!newFile.exists()){
                newFile.mkdir();
            }
            return new FileStreamManager(newFile);
        }
        return null;
    }

    @Override
    public StreamManager createFile(String fileName) throws IOException{
        if(isDir()){
            return new FileStreamManager(new File(file.getCanonicalPath() + "/" + fileName));
        }
        return null;
    }

    @Override
    public String getHumanReadablePath() throws IOException{
        return file.getCanonicalPath();
    }

    @Override
    public String getActualPath() throws IOException {
        return file.getCanonicalPath();
    }

    @Override
    public boolean writable() {
        return file.canWrite();
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public void create() {
        file.mkdirs();
    }

    @Override
    public List<StreamManager> getListableDirs() {
        List<StreamManager> list = new ArrayList<>();
        for(File sub : file.listFiles()){
            if(!sub.getName().startsWith(".") && sub.isDirectory()){
                list.add(new FileStreamManager(sub));
            }
        }

        Collections.sort(list, new Comparator<StreamManager>() {
            @Override
            public int compare(StreamManager o1, StreamManager o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return list;
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public StreamManager getParent() {
        return new FileStreamManager(file.getParentFile());
    }

    @Override
    public boolean renameTo(StreamManager newFile) {
        try {
            return file.renameTo(new File(newFile.getActualPath()));
        } catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public long length() {
        return file.length();
    }
}
