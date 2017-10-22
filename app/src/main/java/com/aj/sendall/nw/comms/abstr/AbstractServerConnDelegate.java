package com.aj.sendall.nw.comms.abstr;

import com.aj.sendall.events.EventRouter;
import com.aj.sendall.events.EventRouterFactory;
import com.aj.sendall.events.event.SocketClosing;
import com.aj.sendall.controller.AppController;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;


abstract public class AbstractServerConnDelegate implements Runnable {
    private Socket socket;
    protected DataInputStream dataInputStream;
    protected DataOutputStream dataOutputStream;
    protected AppController appController;
    protected final EventRouter eventRouter = EventRouterFactory.getInstance();

    public AbstractServerConnDelegate(Socket socket, AppController appController){
        this.socket = socket;
        this.appController = appController;
    }

    @Override
    public void run() {
        try {
            dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            eventRouter.subscribe(SocketClosing.class, new EventRouter.Receiver<SocketClosing>() {
                @Override
                public void receive(SocketClosing event) {
                    closeStreams();
                    eventRouter.unsubscribe(SocketClosing.class, this);
                }
            });
            postStreamSetup();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    abstract protected void postStreamSetup();

    private void closeStreams(){
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

    protected void closeSocket(){
        try{
            socket.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
