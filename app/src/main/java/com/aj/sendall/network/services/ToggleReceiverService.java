package com.aj.sendall.network.services;

import android.app.IntentService;
import android.content.Intent;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.network.utils.NetworkUtil;
import com.aj.sendall.notification.util.NotificationUtil;

import javax.inject.Inject;

public class ToggleReceiverService extends IntentService {
    @Inject
    public NotificationUtil notificationUtil;
    @Inject
    public SharedPrefUtil sharedPrefUtil;
    @Inject
    public NetworkUtil networkUtil;

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

        notificationUtil.removeToggleNotification();
        if(currentAppStatus == SharedPrefConstants.CURR_STATUS_IDLE){
            sharedPrefUtil.setAutoScanOnWifiEnabled(true);
            sharedPrefUtil.commit();
            networkUtil.startP2pServiceDiscovery();
            notificationUtil.showToggleReceivingNotification();
        } else if (currentAppStatus == SharedPrefConstants.CURR_STATUS_RECEIVABLE){
            sharedPrefUtil.setAutoScanOnWifiEnabled(false);
            sharedPrefUtil.commit();
            networkUtil.stopP2pServiceDiscovery();
            //The notification update and status change are done from the ActionListener of WifiP2pManager.clearServiceRequests()
        }

    }
}
