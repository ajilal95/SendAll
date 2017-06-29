package com.aj.sendall.network.utils;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.network.services.ToggleReceiverService;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by ajilal on 29/6/17.
 */

@Singleton
public class NetworkUtil {
    public LocalWifiManager localWifiManager;
    public SharedPrefUtil sharedPrefUtil;

    @Inject
    public NetworkUtil(LocalWifiManager localWifiManager, SharedPrefUtil sharedPrefUtil){
        this.localWifiManager = localWifiManager;
        this.sharedPrefUtil = sharedPrefUtil;
    }


    public void startP2pServiceDiscovery(){
        if(sharedPrefUtil.getCurrentAppStatus() == SharedPrefConstants.CURR_STATUS_IDLE) {
            sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_RECEIVABLE);
            sharedPrefUtil.commit();
            WifiP2pManager.DnsSdTxtRecordListener txtRecordListener = new WifiP2pManager.DnsSdTxtRecordListener() {
                @Override
                public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
                    if(Constants.P2P_SERVICE_FULL_DOMAIN_NAME.equals(fullDomainName)) {
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

    public void stopP2pServiceDiscovery(){
        localWifiManager.stopP2pServiceDiscovery();
    }
}
