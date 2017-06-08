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
    }

    private void createLocalWifiManager(){
        if(localWifiManager == null) {
            localWifiManager = new LocalWifiManager(this);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        createLocalWifiManager();
        boolean isRecActive = SharedPrefUtil.getCurrentReceivingStatus(this);

        if(isRecActive){
            stopBroadcastReceiver();
            SharedPrefUtil.setCurrentReceivingState(this, false, true);
        } else {
            startBroadcastReceiver();
            SharedPrefUtil.setCurrentReceivingState(this, true, true);
        }

        NotificationUtil.showToggleReceivingNotification(this);
    }

    private void startBroadcastReceiver(){
        createBroadcastReceiver();
        wasWifiEnabled = localWifiManager.isWifiEnabled();//to restore wifi state on stopping the app
        if(!wasWifiEnabled){
            localWifiManager.enableWifi(true);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        registerReceiver(wifiStatusBroadcastReceiver, intentFilter);
        wifiStatusBroadcastReceiver.overrideAndScan();
    }

    private void stopBroadcastReceiver(){
        if(wifiStatusBroadcastReceiver != null){
            wifiStatusBroadcastReceiver.overrideAndStopScan();
            unregisterReceiver(wifiStatusBroadcastReceiver);
            if(!wasWifiEnabled){
                //restoring wifi state
                localWifiManager.enableWifi(false);
            }
            wifiStatusBroadcastReceiver = null;
        }
    }

    private void createBroadcastReceiver(){
        stopBroadcastReceiver();
        wifiStatusBroadcastReceiver = new WifiStatusBroadcastReceiver(localWifiManager);
    }

}
