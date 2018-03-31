package com.aj.sendall.streams;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

class TreeUriStreamManager implements StreamManager {
    private DocumentFile df;
    private Context c;

    private InputStream is;
    private OutputStream os;

    TreeUriStreamManager(Context c, Uri treeUri){
        this.df = DocumentFile.fromTreeUri(c, treeUri);
        this.c = c;
        if(df.isDirectory()) {
            try {
                c.grantUriPermission(c.getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                c.getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } catch (SecurityException e){
                Log.e(TreeUriStreamManager.class.getSimpleName(), "Could not get permission to access : " + treeUri.toString());
            }
        }
    }

    private TreeUriStreamManager(Context c, DocumentFile df){
        this.df = df;
        this.c = c;
//        if(df.isDirectory()) {
//            try {
//                c.grantUriPermission(c.getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                c.getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            } catch (SecurityException e){
//                Log.e(TreeUriStreamManager.class.getSimpleName(), "Could not get permission to access : " + treeUri.toString());
//            }
//        }
    }

    @Override
    public InputStream createInputStream() throws IOException, IllegalStateException {
        return is = c.getContentResolver().openInputStream(df.getUri());
    }

    @Override
    public InputStream getInputStream() throws IOException, IllegalStateException {
        if(is == null){
            createInputStream();
        }
        return is;
    }

    @Override
    public OutputStream createOutputStream(String mode) throws IOException {
        if(mode == null){
            mode = "a";
        }
        return os = c.getContentResolver().openOutputStream(df.getUri(), mode);
    }

    @Override
    public OutputStream getOutputStream(String mode) throws IOException {
        if(os == null){
            createOutputStream(mode);
        }
        return os;
    }

    @Override
    public void closeInputStream() {
        if(is != null){
            try {
                is.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void closeOutputStream() {
        if(os != null){
            try{
                os.close();
            } catch(Exception e){
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
        if(is != null){
            return is.skip(pos);
        }
        return -1;
    }

    @Override
    public boolean isDir() {
        return df.isDirectory();
    }

    @Override
    public StreamManager createDir(String dirName) throws IOException {
        for(DocumentFile chld : df.listFiles()){
            if(chld.isDirectory() && chld.getName().equals(dirName)){
                return new TreeUriStreamManager(c, chld.getUri());
            }
        }
        return new TreeUriStreamManager(c, df.createDirectory(dirName));
    }

    @Override
    public StreamManager createFile(String fileName, String MIMEType) throws IOException {
        return new TreeUriStreamManager(c, df.createFile(MIMEType, fileName));
    }

    @Override
    public String getHumanReadablePath() throws IOException {
        return FileUtil.getFullPathFromTreeUri(df.getUri(), c);
    }

    @Override
    public String getActualPath() throws IOException {
        return df.getUri().toString();
    }

    @Override
    public boolean writable() {
        return df.canWrite();
    }

    @Override
    public boolean exists() {
        return df.exists();
    }

    @Override
    public void create() {
    }

    @Override
    public List<StreamManager> getListableDirs() {
        LinkedList<StreamManager> list = new LinkedList<>();
        for(DocumentFile sub : df.listFiles()){
            if(!sub.getName().startsWith(".") && sub.isDirectory()) {
                list.add(new TreeUriStreamManager(c, sub.getUri()));
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
        return df.getName();
    }

    @Override
    public StreamManager getParent() {
        throw new NullPointerException();
    }

    @Override
    public boolean renameTo(StreamManager newFile) {
        try {
            return df.renameTo(newFile.getActualPath());
        } catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public long length() {
        return df.length();
    }
}
