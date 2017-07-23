package com.aj.sendall.network.runnable;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;

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

/**
 * Created by ajilal on 20/7/17.
 */

public class NewConnCreationClient implements Runnable, Updatable {
    private static final String GRP_OWNER_IP = "192.168.49.1";
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
    }

    @Override
    public void run() {
        if(connectToWifi()){
            appManager.wifiP2pManager.requestGroupInfo(appManager.channel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if(!group.isGroupOwner()){
                        tryToOpenSocket();
                        communicate();
                    }
                }
            });
        }
    }

    private void tryToOpenSocket(){
        try {
            socket = new Socket(GRP_OWNER_IP, port);
        } catch (Exception e){
            UpdateEvent event = new UpdateEvent();
            event.source = this.getClass();
            event.data.put(Constants.ACTION, Constants.CONN_FAILED);
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

                String otherUserName = dis.readUTF();
                String otherDeviceId = dis.readUTF();

                Connections conn = new Connections();
                conn.setConnectionName(otherUserName);
                conn.setSSID(otherDeviceId);
                conn.setLastContaced(new Date());

                appManager.dbUtil.saveConnection(conn);
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
