package com.aj.sendall.network.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.network.utils.LocalWifiManager;
import com.aj.sendall.notification.util.NotificationUtil;

import java.util.Map;

import javax.inject.Inject;

public class ToggleReceiverService extends IntentService {
    @Inject
    public LocalWifiManager localWifiManager;
    public NotificationUtil notificationUtil;
    public SharedPrefUtil sharedPrefUtil;

    public ToggleReceiverService() {
        super("ToggleReceiverService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((AndroidApplication)getApplication()).getDaggerInjector().inject(this);
        notificationUtil = localWifiManager.notificationUtil;
        sharedPrefUtil = localWifiManager.sharedPrefUtil;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int currentAppStatus = sharedPrefUtil.getCurrentAppStatus();

        notificationUtil.removeToggleNotification();
        if(currentAppStatus == SharedPrefConstants.CURR_STATUS_IDLE){
            sharedPrefUtil.setAutoScanOnWifiEnabled(true);
            sharedPrefUtil.commit();
            startP2pServiceDiscovery(localWifiManager);
            notificationUtil.showToggleReceivingNotification();
        } else if (currentAppStatus == SharedPrefConstants.CURR_STATUS_RECEIVABLE){
            sharedPrefUtil.setAutoScanOnWifiEnabled(false);
            sharedPrefUtil.commit();
            stopP2pServiceDiscovery(localWifiManager);
            // The notification update and status change are done
            // from the ActionListener of WifiP2pManager.stopServiceRequest()
        }

    }

    public static void startP2pServiceDiscovery(LocalWifiManager localWifiManager){
        SharedPrefUtil sharedPrefUtil = localWifiManager.sharedPrefUtil;

        if(sharedPrefUtil.getCurrentAppStatus() == SharedPrefConstants.CURR_STATUS_IDLE) {
            sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_RECEIVABLE);
            sharedPrefUtil.commit();
            WifiP2pManager.DnsSdTxtRecordListener txtRecordListener = new WifiP2pManager.DnsSdTxtRecordListener() {
                @Override
                public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
                    if(Constants.P2P_SERVICE_FULL_DOMAIN_NAME.equals(fullDomainName)) {
                        //TODO check the Constants.ADV_KEY_GROUP_PURPOSE
                        Log.d(ToggleReceiverService.class.getSimpleName(), "Inside WifiP2pManager.DnsSdTxtRecordListener");
                        Log.d(ToggleReceiverService.class.getSimpleName(), fullDomainName);
                        Log.d(ToggleReceiverService.class.getSimpleName(), txtRecordMap.toString());
                        Log.d(ToggleReceiverService.class.getSimpleName(), srcDevice.isGroupOwner() + "");
                    }
                }
            };

            WifiP2pManager.DnsSdServiceResponseListener serviceResponseListener = new WifiP2pManager.DnsSdServiceResponseListener() {
                @Override
                public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                    Log.d(ToggleReceiverService.class.getSimpleName(), "Inside WifiP2pManager.DnsSdServiceResponseListener");
                    Log.d(ToggleReceiverService.class.getSimpleName(), instanceName);
                    Log.d(ToggleReceiverService.class.getSimpleName(), registrationType);
                    Log.d(ToggleReceiverService.class.getSimpleName(), srcDevice.deviceName);
                }
            };

            localWifiManager.startP2pServiceDiscovery(txtRecordListener, serviceResponseListener);
        }
    }

    public static void stopP2pServiceDiscovery(LocalWifiManager localWifiManager){
        localWifiManager.stopP2pServiceDiscovery();
    }
}
