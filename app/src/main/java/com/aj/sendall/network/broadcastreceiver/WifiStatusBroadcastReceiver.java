package com.aj.sendall.network.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.network.utils.LocalWifiManager;
import com.aj.sendall.notification.util.NotificationUtil;

import javax.inject.Inject;

public class WifiStatusBroadcastReceiver extends BroadcastReceiver {
    @Inject
    public LocalWifiManager localWifiManager;
    @Inject
    NotificationUtil notificationUtil;
    @Inject
    SharedPrefUtil sharedPrefUtil;

    private PeersAvailableActionListener peersAvailableActionListener = new PeersAvailableActionListener();
    private PeerScanActionListener peerScanActionListener = new PeerScanActionListener();
    public boolean broadCastReceiverActive = false;
    public final Object sync = new Object();

    public WifiStatusBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(localWifiManager == null){
            ((AndroidApplication)context.getApplicationContext()).getDaggerInjector().inject(this);
        }
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            onWifiStateChanged(state);
        } /*else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
        }*/
    }

    private void onWifiStateChanged(int state){
        localWifiManager.setWifiP2pState(state);
        int currentAppStatus = sharedPrefUtil.getCurrentAppStatus();

        if(WifiP2pManager.WIFI_P2P_STATE_ENABLED == state) {
            broadCastReceiverActive = true;
            notificationUtil.showToggleReceivingNotification();
            //Start receiving automatically only if the current status of the app was set to receivable in the
            //last operation of the app. avoid receiving if status is IDLE or SENDING.
            if(sharedPrefUtil.isAutoscanOnWifiEnabled()) {
                localWifiManager.scanPeersAndNotifyBroadcastReceiver(peerScanActionListener);
            }
        } else {
            broadCastReceiverActive = false;
            notificationUtil.removeToggleNotification();
            sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
            if(currentAppStatus == SharedPrefConstants.CURR_STATUS_SENDING){
                stopSending();
            } else if(currentAppStatus == SharedPrefConstants.CURR_STATUS_RECEIVABLE) {
                localWifiManager.stopScanning();
            }
        }
    }

    private void stopSending(){
        sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
        sharedPrefUtil.commit();
    }

    public class PeerScanActionListener implements WifiP2pManager.ActionListener{
        int failureCount = 0;
        @Override
        public void onSuccess() {
            failureCount = 0;
            localWifiManager.requestPeers(peersAvailableActionListener);
        }

        @Override
        public void onFailure(int reason) {
            if(failureCount == 10){
                return;
            } else{
                failureCount++;
            }
            if(WifiP2pManager.BUSY == reason) {
                try {
                    Thread.sleep(2000);
                    localWifiManager.scanPeersAndNotifyBroadcastReceiver(this);
                }catch (Exception e){
                    e.printStackTrace();
                }
            } else if(WifiP2pManager.ERROR == reason){
                try {
                    Thread.sleep(3000);
                    localWifiManager.scanPeersAndNotifyBroadcastReceiver(this);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public class PeersAvailableActionListener implements WifiP2pManager.PeerListListener{
        private WifiP2pDeviceList unservicedPeers;
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            unservicedPeers = peers;
            synchronized (sync) {
                if (broadCastReceiverActive) {
                    WifiP2pDeviceList localDeviceList = unservicedPeers;//for avoiding synchronization issues
                    if(localDeviceList != null) {
                        for (WifiP2pDevice wifiP2pDevice : localDeviceList.getDeviceList()) {
                            if(broadCastReceiverActive) {
                                localWifiManager.connectAndReceiveFiles(wifiP2pDevice);
                            } else {
                                break;
                            }
                        }

                        //Checking if the unserviced device list has not been changed
                        if(localDeviceList.equals(unservicedPeers)){
                            unservicedPeers = null;
                        }
                    }
                    if(broadCastReceiverActive){
                        localWifiManager.scanPeersAndNotifyBroadcastReceiver(peerScanActionListener);
                    }

                }
            }
        }
    }
}
