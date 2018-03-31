package com.aj.sendall.streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface StreamManager {
    InputStream createInputStream() throws IOException, IllegalStateException;
    InputStream getInputStream() throws IOException, IllegalStateException;
    OutputStream createOutputStream(String mode) throws IOException;
    OutputStream getOutputStream(String mode) throws IOException;
    void closeInputStream();
    void closeOutputStream();
    void close();
    long skipIS(long pos) throws IOException;
    boolean isDir();
    StreamManager createDir(String dirName) throws IOException;
    StreamManager createFile(String fileName, String MIMEType) throws IOException;
    String getHumanReadablePath() throws IOException;
    String getActualPath() throws IOException;
    boolean writable();
    boolean exists();
    void create();
    List<StreamManager> getListableDirs();
    String getName();
    StreamManager getParent();
    boolean renameTo(StreamManager newManager);
    long length();
}
