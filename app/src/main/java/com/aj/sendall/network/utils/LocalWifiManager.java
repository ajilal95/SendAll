package com.aj.sendall.network.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

import java.io.Serializable;

/**
 * Created by ajilal on 25/5/17.
 */

public class LocalWifiManager implements Serializable{
    private final WifiManager wifiManager;
    private final WifiP2pManager wifiP2pManager;
    private final WifiP2pManager.Channel channel;

    public LocalWifiManager(Context context){
        wifiManager = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(context, context.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {

            }
        });
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

    }
}
