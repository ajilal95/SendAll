package com.aj.sendall.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;

import com.aj.sendall.application.ThisApplication;
import com.aj.sendall.controller.AppController;

import javax.inject.Inject;

public class WifiStatusBroadcastReceiver extends BroadcastReceiver {
    @Inject
    public AppController appController;

    public WifiStatusBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(appController == null){
            ((ThisApplication)context.getApplicationContext()).getDaggerInjector().inject(this);
        }
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            onWifiStateChanged(state);
        }
    }

    private void onWifiStateChanged(int state){
        appController.setWifiP2pState(state);
    }
}
