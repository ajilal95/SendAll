package com.aj.sendall.network.runnable.abstr;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.interfaces.Updatable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;


public abstract class AbstractClient implements Runnable, Updatable {
    private int port;
    private String SSID;
    private String passPhrase;
    protected Updatable updatable;
    protected AppManager appManager;
    private Socket socket;
    protected DataInputStream dataInputStream;
    protected DataOutputStream dataOutputStream;
    private int connAttemptsRemining = 5;//Try 5 times to connect
    private boolean active = true;//to make sure that reconnection is not tried after user closes the connection

    public AbstractClient(String SSID, String passPhrase, int serverPort, Updatable updatable, AppManager appManager){
        this.SSID = SSID;
        this.passPhrase = passPhrase;
        this.updatable = updatable;
        this.appManager = appManager;
        this.port = serverPort;
    }

    @Override
    public void run() {
        InetAddress serverAdd = appManager.connectAndGetAddressOf(this.SSID, this.passPhrase);
        if(serverAdd != null){
            tryToOpenSocket(serverAdd);
            communicate();
        }
        tryToCloseSocket();
    }

    abstract protected void configureSocket(Socket socket);

    private void tryToOpenSocket(InetAddress serverAdd){
        try {
            socket = new Socket(serverAdd, port);
            configureSocket(socket);
            dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
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
    }

    protected abstract void communicate();

    protected void tryToCloseSocket(){
        active = false;
        if(socket != null){
            try {
                socket.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void update(UpdateEvent updateEvent) {
        if(Constants.CLOSE_SOCKET.equals(updateEvent.data.get(Constants.ACTION))){
            tryToCloseSocket();
            socket = null;
        }
    }
}
