package com.aj.sendall.network.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.network.broadcastreceiver.WifiStatusBroadcastReceiver;
import com.aj.sendall.network.utils.LocalWifiManager;
import com.aj.sendall.notification.util.NotificationUtil;

import javax.inject.Inject;

public class ToggleReceiverService extends IntentService {
    @Inject
    public LocalWifiManager localWifiManager;
    @Inject
    public NotificationUtil notificationUtil;
    @Inject
    public SharedPrefUtil sharedPrefUtil;

    public ToggleReceiverService() {
        super("ToggleReceiverService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((AndroidApplication)getApplication()).getDaggerInjector().inject(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int currentAppStatus = sharedPrefUtil.getCurrentAppStatus();

        if(currentAppStatus == SharedPrefConstants.CURR_STATUS_IDLE){
            sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_RECEIVABLE);
            sharedPrefUtil.setAutoscanOnWifiEnabled(true);
            sharedPrefUtil.commit();
        } else if (currentAppStatus == SharedPrefConstants.CURR_STATUS_RECEIVABLE){
            sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
            sharedPrefUtil.setAutoscanOnWifiEnabled(false);
            sharedPrefUtil.commit();
        } else {
            //App is in sending state
            //if wifi is enabled then the application might be in the middle of receiving or sending something
            //else just override and change the state. The state must be the residue of the last operation.
            if(!localWifiManager.isWifiEnabled()){
                sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                sharedPrefUtil.commit();
            }
        }

        notificationUtil.showToggleReceivingNotification();
    }
}
