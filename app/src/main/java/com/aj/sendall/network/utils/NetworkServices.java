package com.aj.sendall.network.utils;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;

import com.aj.sendall.network.broadcastreceiver.WifiStatusBroadcastReceiver;

/**
 * Created by ajilal on 6/5/17.
 */

public class NetworkServices {
    public static WifiStatusBroadcastReceiver broadcastReceiver;

    public static boolean enableWIFIBroadcastReceiver(Context context){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        if(broadcastReceiver != null) {
            disableWIFIBroadcastReceiver(context);
        }
        try {
            broadcastReceiver = new WifiStatusBroadcastReceiver();
            context.registerReceiver(broadcastReceiver, intentFilter);
        } catch (Exception e){
            broadcastReceiver = null;
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean disableWIFIBroadcastReceiver(Context context){
        if(broadcastReceiver != null){
            try {
                context.unregisterReceiver(broadcastReceiver);
                broadcastReceiver = null;
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
