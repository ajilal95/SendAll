package com.aj.sendall.network.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;

import com.aj.sendall.network.utils.LocalWifiManager;

public class WifiStatusBroadcastReceiver extends BroadcastReceiver {
    LocalWifiManager localWifiManager;

    public WifiStatusBroadcastReceiver(LocalWifiManager localWifiManager) {
        this.localWifiManager = localWifiManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            boolean isWifiEnabled = (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED);
            onWifiEnabled(isWifiEnabled);

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            NetworkInfo networkState = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if(networkState.isConnected())
            {
                //activity.setServerStatus("Connection Status: Connected");
            }
            else
            {
                //activity.setServerStatus("Connection Status: Disconnected");
                //manager.cancelConnect(channel, null);

            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }

    private void onWifiEnabled(boolean isWifiEnabled){

    }

    private void onPeersChanged(){

    }
}
