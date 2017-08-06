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
    protected Updatable updatableActivity;
    protected AppManager appManager;
    private Socket socket;
    protected DataInputStream dataInputStream;
    protected DataOutputStream dataOutputStream;

    public AbstractClient(String SSID, String passPhrase, int serverPort, Updatable updatableActivity, AppManager appManager){
        this.SSID = SSID;
        this.passPhrase = passPhrase;
        this.updatableActivity = updatableActivity;
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
    }

    private void tryToOpenSocket(InetAddress serverAdd){
        try {
            socket = new Socket(serverAdd, port);
            dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        } catch (Exception e){
            e.printStackTrace();
            UpdateEvent event = new UpdateEvent();
            event.source = this.getClass();
            event.data.put(Constants.ACTION, Constants.FAILED);
            updatableActivity.update(event);
        }
    }

    protected abstract void communicate();

    private void tryToCloseSocket(){
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
