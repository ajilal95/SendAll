package com.aj.sendall.network.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

import com.aj.sendall.network.utils.LocalWifiManager;

public class WifiStatusBroadcastReceiver extends BroadcastReceiver {
    private LocalWifiManager localWifiManager;
    private PeersAvailableActionListener peersAvailableActionListener = new PeersAvailableActionListener();
    private PeerScanActionListener peerScanActionListener = new PeerScanActionListener();
    public boolean broadCastReceiverActive = false;
    public final Object synch = new Object();
    public boolean tryingConnection = false;

    public WifiStatusBroadcastReceiver(LocalWifiManager localWifiManager) {
        this.localWifiManager = localWifiManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            onWifiStateChanged(state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            localWifiManager.requestPeers(peersAvailableActionListener);
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

    /* Method to scan for connections manually in case
    * the application started when the wifi was active*/
    public void overrideAndScan(){
        startScanning();
    }

    public void overrideAndStopScan(){
        stopScanning();
    }

    private void onWifiStateChanged(int state){
        if(WifiP2pManager.WIFI_P2P_STATE_ENABLED == state) {
            startScanning();
        } else {
            stopScanning();
        }
    }

    private void startScanning() {
        broadCastReceiverActive = true;
        localWifiManager.scanPeersAndNotifyBroadcastReceiver(peerScanActionListener);
    }

    private void stopScanning(){
        broadCastReceiverActive = false;
        localWifiManager.stopScanning();
    }

    public class PeerScanActionListener implements WifiP2pManager.ActionListener{
        @Override
        public void onSuccess() {
            localWifiManager.requestPeers(peersAvailableActionListener);
        }

        @Override
        public void onFailure(int reason) {

        }
    }

    public class PeersAvailableActionListener implements WifiP2pManager.PeerListListener{
        private WifiP2pDeviceList unservicedPeers;
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            unservicedPeers = peers;
            synchronized (synch) {
                for (WifiP2pDevice wifiP2pDevice : unservicedPeers.getDeviceList()) {
                    localWifiManager.connectAndReceiveFiles(wifiP2pDevice);
                }
                if(peers.equals(unservicedPeers)){
                    unservicedPeers = null;
                }
                if (broadCastReceiverActive) {
                    localWifiManager.scanPeersAndNotifyBroadcastReceiver(new PeerScanActionListener());
                }
            }
        }
    }
}
