package com.aj.sendall.network.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

import com.aj.sendall.db.dto.ConnectionsAndUris;
import com.aj.sendall.db.util.DBUtil;
import com.aj.sendall.network.asych.ServiceAdvertiser;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by ajilal on 25/5/17.
 */

@Singleton
public class LocalWifiManager implements Serializable{
    private Context context;
    private DBUtil dbUtil;
    private boolean initialised = false;
    private WifiManager wifiManager;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;

    @Inject
    public LocalWifiManager(Context context, DBUtil dbUtil){
        this.context = context;
        this.dbUtil = dbUtil;
        init();
    }

    private void init() {
        if(!initialised) {
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
            channel = wifiP2pManager.initialize(context, context.getMainLooper(), new WifiP2pManager.ChannelListener() {
                @Override
                public void onChannelDisconnected() {

                }
            });
            initialised = true;
        }
    }

    public boolean isWifiEnabled(){
        return  wifiManager.isWifiEnabled();
    }

    public void enableWifi(boolean enable){
        wifiManager.setWifiEnabled(enable);
    }

    public void scanPeersAndNotifyBroadcastReceiver(WifiP2pManager.ActionListener listener){
        wifiP2pManager.discoverPeers(channel, listener);
    }

    public void stopScanning(){
        wifiP2pManager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }

    public void requestPeers(WifiP2pManager.PeerListListener listener){
        wifiP2pManager.requestPeers(channel, listener);
    }

    public void connectAndReceiveFiles(WifiP2pDevice device){
        if(dbUtil.isConnectedSendAllDevice(device.deviceName)) {
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int reason) {

                }
            });
        }
    }

    public void createGroupAndAdvertise(ConnectionsAndUris connectionsAndUris){
        wifiP2pManager.createGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
//        new ServiceAdvertiser(connectionsAndUris, this).start();
    }
}
