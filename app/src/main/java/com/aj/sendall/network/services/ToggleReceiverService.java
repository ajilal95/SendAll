package com.aj.sendall.network.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;

import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.network.broadcastreceiver.WifiStatusBroadcastReceiver;
import com.aj.sendall.network.utils.LocalWifiManager;
import com.aj.sendall.notification.util.NotificationUtil;

public class ToggleReceiverService extends IntentService {
    private LocalWifiManager localWifiManager;
    private WifiStatusBroadcastReceiver wifiStatusBroadcastReceiver;
    private boolean wasWifiEnabled;

    public ToggleReceiverService() {
        super("ToggleReceiverService");
        localWifiManager = new LocalWifiManager(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean isRecActive = SharedPrefUtil.getCurrentReceivingStatus(this);

        if(isRecActive){
            stopBroadcastReceiver();
            if(!wasWifiEnabled){
                //restoring wifi state
                localWifiManager.enableWifi(false);
            }
            SharedPrefUtil.setCurrentReceivingState(this, false, true);
        } else {
            startBroadcastReceiver();
            wasWifiEnabled = localWifiManager.isWifiEnabled();//to restore wifi state
            if(!wasWifiEnabled){
                localWifiManager.enableWifi(true);
            }
            localWifiManager.searchPeersAndNotifyBroadcastReceiver(new PeerDiscoveryActionListener());

            SharedPrefUtil.setCurrentReceivingState(this, true, true);
        }

        NotificationUtil.showToggleReceivingNotification(this);
    }

    private void startBroadcastReceiver(){
        createBroadcastReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        registerReceiver(wifiStatusBroadcastReceiver, intentFilter);
    }

    private void stopBroadcastReceiver(){
        if(wifiStatusBroadcastReceiver != null){
            unregisterReceiver(wifiStatusBroadcastReceiver);
            wifiStatusBroadcastReceiver = null;
        }
    }

    private void createBroadcastReceiver(){
        stopBroadcastReceiver();
        wifiStatusBroadcastReceiver = new WifiStatusBroadcastReceiver(localWifiManager);
    }

    public class PeerDiscoveryActionListener implements WifiP2pManager.ActionListener{
        private int noOfFailedAttempts = 0;
        @Override
        public void onSuccess() {
            noOfFailedAttempts = 0;
        }

        @Override
        public void onFailure(int reason) {
            noOfFailedAttempts++;
            if(noOfFailedAttempts >= 3){
                //TODO stop listening
            }
            try{
                Thread.sleep(5000);
                localWifiManager.searchPeersAndNotifyBroadcastReceiver(this);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
