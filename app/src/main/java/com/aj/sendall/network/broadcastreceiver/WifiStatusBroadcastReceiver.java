package com.aj.sendall.network.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.notification.util.NotificationUtil;

import javax.inject.Inject;

public class WifiStatusBroadcastReceiver extends BroadcastReceiver {
    @Inject
    public AppManager appManager;

    public WifiStatusBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(appManager == null){
            ((AndroidApplication)context.getApplicationContext()).getDaggerInjector().inject(this);
        }
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            onWifiStateChanged(state);
        }
    }

    private void onWifiStateChanged(int state){
        appManager.setWifiP2pState(state);
        int currentAppStatus = appManager.getCurrentAppStatus();
        if (WifiP2pManager.WIFI_P2P_STATE_ENABLED == state) {
            appManager.showStatusNotification(SharedPrefConstants.CURR_STATUS_IDLE == currentAppStatus);
        } else {
            appManager.showStatusNotification(false);
        }
    }
}
