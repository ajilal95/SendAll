package com.aj.sendall.network.runnable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Created by ajilal on 10/7/17.
 */

class NewConnCreationSender implements Runnable {
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    NewConnCreationSender(Socket socket){
        this.socket = socket;
    }
    @Override
    public void run() {
        try {
            dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        } catch(Exception e){
            e.printStackTrace();
            if(!socket.isClosed()) {
                try {
                    socket.close();
                } catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }
}
