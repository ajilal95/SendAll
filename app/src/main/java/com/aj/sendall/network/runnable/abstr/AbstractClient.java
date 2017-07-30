package com.aj.sendall.network.runnable.abstr;

import android.net.wifi.WifiConfiguration;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.interfaces.Updatable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Created by ajilal on 29/7/17.
 */

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
        if(connectToWifi()){
            tryToOpenSocket();
            communicate();
        }
    }

    private void tryToOpenSocket(){
        try {
            socket = new Socket("192.168.49.1", port);
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

    protected void closeSocket(){
        tryToCloseSocket();
        socket = null;
    }

    private void tryToCloseSocket(){
        if(socket != null){
            try {
                socket.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private boolean connectToWifi() {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "\"" + SSID + "\"";
        wifiConfiguration.preSharedKey = "\"" + passPhrase + "\"";

        int res = appManager.wifiManager.addNetwork(wifiConfiguration);

        appManager.wifiManager.disconnect();
        appManager.wifiManager.enableNetwork(res, true);
        return appManager.wifiManager.reconnect();
    }
}
