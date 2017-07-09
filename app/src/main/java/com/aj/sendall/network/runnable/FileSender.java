package com.aj.sendall.network.runnable;

import android.net.Uri;

import com.aj.sendall.db.dto.FileInfoDTO;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Set;

/**
 * Created by ajilal on 2/7/17.
 */

public class FileSender implements Runnable {
    private Socket socket;
    private Set<FileInfoDTO> fileInfoDTOs;
    private DataInputStream dis;
    private DataOutputStream dos;

    public FileSender(Socket socket, Set<FileInfoDTO> fileInfoDTOs, DataInputStream dis){
        this.socket = socket;
        this.fileInfoDTOs = fileInfoDTOs;
        this.dis = dis;
    }

    @Override
    public void run() {
        try {
            dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        } catch (IOException ioe){
            ioe.printStackTrace();
        } finally {
            if(!socket.isClosed()){
                try {
                    dos.close();
                    dis.close();
                    socket.close();
                } catch (IOException ioe){
                    ioe.printStackTrace();
                }
            }

        }
    }
}
