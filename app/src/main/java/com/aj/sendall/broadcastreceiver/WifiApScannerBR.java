package com.aj.sendall.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.aj.sendall.events.EventRouter;
import com.aj.sendall.events.EventRouterFactory;
import com.aj.sendall.events.event.SendallNetsAvailable;
import com.aj.sendall.sharedprefs.SharedPrefUtil;

import java.util.ArrayList;
import java.util.List;


public class WifiApScannerBR extends BroadcastReceiver {
    private EventRouter eventRouter = EventRouterFactory.getInstance();
    private SharedPrefUtil sharedPrefUtil;

    public WifiApScannerBR(SharedPrefUtil sharedPrefUtil){
        this.sharedPrefUtil = sharedPrefUtil;
    }

    public void onReceive(Context context, Intent intent){
        String action = intent.getAction();

        if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)){
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if(wifiManager != null){
                List<ScanResult> allScanResults = wifiManager.getScanResults();
                List<String> filteredResults = new ArrayList<>();
                if(allScanResults != null) {
                    for (ScanResult scanResult : allScanResults){
                        if(sharedPrefUtil.isOurNetwork(scanResult)){
                            filteredResults.add(scanResult.SSID);
                        }
                    }
                }

                if(!filteredResults.isEmpty()) {
                    SendallNetsAvailable e = new SendallNetsAvailable();
                    e.availableSSIDs = filteredResults;
                    eventRouter.broadcast(e);
                }
            }
        }
    }
}
