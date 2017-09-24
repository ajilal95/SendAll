package com.aj.sendall.network.runnable.abstr;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.network.monitor.Updatable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;


abstract public class AbstractClientConnector implements Runnable, Updatable {
    private Socket socket;
    protected DataInputStream dataInputStream;
    protected DataOutputStream dataOutputStream;
    protected Updatable updatableActivity;
    protected AppManager appManager;

    public AbstractClientConnector(Socket socket, Updatable updatableActivity, AppManager appManager){
        this.socket = socket;
        this.appManager = appManager;
        this.updatableActivity = updatableActivity;
    }

    @Override
    public void run() {
        try {
            dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            postStreamSetup();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    abstract protected void postStreamSetup();

    void closeStreams(){
        try {
            dataInputStream.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        try {
            dataOutputStream.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public final void update(UpdateEvent updateEvent){
        if(Constants.CLOSE_SOCKET.equals(updateEvent.action)){
            try{
                socket.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        //for child class specific updates
        onUpdate(updateEvent);
    }

    protected void onUpdate(UpdateEvent updateEvent){
        //Child classes must override this method for implementing child class specific updates
    }
}
