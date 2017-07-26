package com.aj.sendall.network.runnable;

import android.net.wifi.WifiConfiguration;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.db.model.Connections;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.interfaces.Updatable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;


public class NewConnCreationClient implements Runnable, Updatable {
    private String grpOwnerIp = "192.168.49.1";
    private int port;
    private String SSID;
    private String passPhrase;
    private Updatable activity;
    private AppManager appManager;
    private Socket socket;

    public NewConnCreationClient(String SSID, String passPhrase, int serverPort, Updatable activity, AppManager appManager){
        this.SSID = SSID;
        this.passPhrase = passPhrase;
        this.activity = activity;
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
            socket = new Socket(grpOwnerIp, port);
        } catch (Exception e){
            e.printStackTrace();
            UpdateEvent event = new UpdateEvent();
            event.source = this.getClass();
            event.data.put(Constants.ACTION, Constants.FAILED);
            activity.update(event);
        }
    }

    private void communicate(){
        if(socket != null){
            try {
                InputStream is = socket.getInputStream();
                DataInputStream dis = new DataInputStream(new BufferedInputStream(is));

                OutputStream os = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(os));

                String thisUserName = appManager.sharedPrefUtil.getUserName();
                String thisDeviceId = appManager.sharedPrefUtil.getThisDeviceId();

                dos.writeUTF(thisUserName);
                dos.writeUTF(thisDeviceId);
                dos.flush();

                String otherUserName = dis.readUTF();
                String otherDeviceId = dis.readUTF();

                Connections conn = new Connections();
                conn.setConnectionName(otherUserName);
                conn.setSSID(otherDeviceId);
                conn.setLastContaced(new Date());

                appManager.dbUtil.saveConnection(conn);

                UpdateEvent event = new UpdateEvent();
                event.source = this.getClass();
                event.data.put(Constants.ACTION, Constants.SUCCESS);
                activity.update(event);
            }catch (Exception e){
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

    private void tryToCloseSocket(){
        if(socket != null){
            try {
                socket.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public boolean connectToWifi() {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "\"" + SSID + "\"";
        wifiConfiguration.preSharedKey = "\"" + passPhrase + "\"";

        int res = appManager.wifiManager.addNetwork(wifiConfiguration);

        appManager.wifiManager.disconnect();
        appManager.wifiManager.enableNetwork(res, true);
        return appManager.wifiManager.reconnect();
    }
}
