package com.aj.sendall.network.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.network.services.ToggleReceiverService;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.notification.util.NotificationUtil;

import javax.inject.Inject;

public class WifiStatusBroadcastReceiver extends BroadcastReceiver {
    @Inject
    public AppManager appManager;
    public NotificationUtil notificationUtil;
    public SharedPrefUtil sharedPrefUtil;

    public WifiStatusBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(appManager == null){
            ((AndroidApplication)context.getApplicationContext()).getDaggerInjector().inject(this);
            notificationUtil = appManager.notificationUtil;
            sharedPrefUtil = appManager.sharedPrefUtil;
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
        appManager.setWifiP2pState(state);
        int currentAppStatus = sharedPrefUtil.getCurrentAppStatus();
        if(SharedPrefConstants.CURR_STATUS_STOPPING_ALL != currentAppStatus) {
            if (WifiP2pManager.WIFI_P2P_STATE_ENABLED == state) {
                if(SharedPrefConstants.CURR_STATUS_IDLE == currentAppStatus) {
                    if (sharedPrefUtil.isAutoScanOnWifiEnabled()) {
                        ToggleReceiverService.startP2pServiceDiscovery(appManager);
                    }
                    notificationUtil.showToggleReceivingNotification();
                }
            } else {
                notificationUtil.removeToggleNotification();
                if (currentAppStatus == SharedPrefConstants.CURR_STATUS_SENDING
                        || currentAppStatus == SharedPrefConstants.CURR_STATUS_RECEIVABLE
                        || currentAppStatus == SharedPrefConstants.CURR_STATUS_CEATING_CONNECTION) {
                    appManager.stopAllWifiOps();
                }
            }
        } else {
            sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
            sharedPrefUtil.commit();
            notificationUtil.showToggleReceivingNotification();
        }
    }
}
