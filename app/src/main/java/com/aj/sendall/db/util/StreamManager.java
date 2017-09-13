package com.aj.sendall.db.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

interface StreamManager {
    InputStream createInputStream() throws IOException, IllegalStateException;
    InputStream getInputStream() throws IOException, IllegalStateException;
    OutputStream createOutputStream(String mode) throws IOException;
    OutputStream getOutputStream(String mode) throws IOException;
    void closeInputStream();
    void closeOutputStream();
    void close();
    long skipIS(long pos) throws IOException;
}
