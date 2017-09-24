package com.aj.sendall.network.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.network.monitor.Updatable;

import java.util.ArrayList;
import java.util.List;


public class SendallNetWifiScanBroadcastReceiver extends BroadcastReceiver {
    public static String UPDATE_EXTRA_RESULT = "UPDATE_EXTRA_RESULT";
    private Updatable updatable;
    private SharedPrefUtil sharedPrefUtil;

    public SendallNetWifiScanBroadcastReceiver(Updatable updatable, SharedPrefUtil sharedPrefUtil){
        this.updatable = updatable;
        this.sharedPrefUtil = sharedPrefUtil;
    }

    public void onReceive(Context context, Intent intent){
        String action = intent.getAction();

        if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)){
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if(wifiManager != null){
                List<ScanResult> allScanResults = wifiManager.getScanResults();
                List<ScanResult> filteredResults = new ArrayList<>();
                if(allScanResults != null) {
                    for (ScanResult scanResult : allScanResults){
                        if(sharedPrefUtil.isOurNetwork(scanResult)){
                            filteredResults.add(scanResult);
                        }
                    }
                }

                if(!filteredResults.isEmpty()) {
                    Updatable.UpdateEvent event = new Updatable.UpdateEvent();
                    event.source = this.getClass();
                    event.putExtra(UPDATE_EXTRA_RESULT, filteredResults);
                    if (updatable != null) {
                        updatable.update(event);
                    }
                }
            }
        }
    }
}
