package com.aj.sendall.nw.comms.abstr;

import com.aj.sendall.events.EventRouter;
import com.aj.sendall.events.EventRouterFactory;
import com.aj.sendall.events.event.SocketClosing;
import com.aj.sendall.controller.AppController;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;


public abstract class AbstractClient implements Runnable {
    private int port;
    private String SSID;
    private String passPhrase;
    protected AppController appController;
    private Socket socket;
    protected DataInputStream dataInputStream;
    protected DataOutputStream dataOutputStream;
    private int connAttemptsRemining = 5;//Try 5 times to connect
    private boolean active = true;//to make sure that reconnection is not tried after user closes the connection
    private EventRouter eventRouter = EventRouterFactory.getInstance();
    private SocketCloseListener socketCloseListener = new SocketCloseListener();

    public AbstractClient(String SSID, String passPhrase, int serverPort, AppController appController){
        this.SSID = SSID;
        this.passPhrase = passPhrase;
        this.appController = appController;
        this.port = serverPort;
    }

    @Override
    public void run() {
        InetAddress serverAdd = appController.connectAndGetAddressOf(this.SSID, this.passPhrase);
        if(serverAdd != null){
            if(tryToOpenSocket(serverAdd)) {
                communicate();
                tryToCloseSocket();
            }
        }
        finalAction();
    }

    abstract protected void configureSocket(Socket socket);
    abstract protected void finalAction();

    private boolean tryToOpenSocket(InetAddress serverAdd){
        try {
            if(appController.isClient()) {
                eventRouter.unsubscribe(SocketClosing.class, socketCloseListener);//un-subscribe old listener(if any)
                eventRouter.subscribe(SocketClosing.class, socketCloseListener);//subscribe new listener
                socket = appController.createClientSocket(serverAdd, port);
                configureSocket(socket);
                dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                return true;
            }
        } catch (Exception e){
            e.printStackTrace();
            //retries
            connAttemptsRemining--;
            if(active && connAttemptsRemining > 0){
                try {
                    Thread.sleep(1000);//wait 1 second
                } catch(InterruptedException ie){
                    //ignored
                }
                tryToOpenSocket(serverAdd);
            }
        }
        return false;
    }

    protected abstract void communicate();

    private void tryToCloseSocket(){
        active = false;
        if(socket != null){
            try {
                socket.close();
            } catch (Exception e){
                e.printStackTrace();
            }
            socket = null;
        }
    }

    protected void closeStreams(){
        try{
            dataInputStream.close();
        } catch (Exception e){
            e.printStackTrace();
        }

        try{
            dataOutputStream.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private class SocketCloseListener implements EventRouter.Receiver<SocketClosing>{
        @Override
        public void receive(SocketClosing event) {
            if (event.closingSocket == socket) {
                closeStreams();
            }
            eventRouter.unsubscribe(SocketClosing.class, this);
        }
    }
}
