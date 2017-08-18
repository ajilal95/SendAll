package com.aj.sendall.application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.db.util.DBUtil;
import com.aj.sendall.network.broadcastreceiver.SendallNetWifiScanBroadcastReceiver;
import com.aj.sendall.notification.util.NotificationUtil;
import com.aj.sendall.ui.interfaces.Updatable;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AppManager implements Serializable{
    private static final String WAIT_FOR_CURRENT_OP_TOAST = "Wait for the current operation to finish";

    public Context context;
    public DBUtil dbUtil;
    private boolean initialised = false;
    private WifiManager wifiManager;
    public WifiP2pManager wifiP2pManager;
    public WifiP2pManager.Channel channel;
    public SharedPrefUtil sharedPrefUtil;
    private int wifiP2pState;
    public NotificationUtil notificationUtil;
//    private ContentProviderUtil contentProviderUtil;

    private ServiceListenerRepeater serviceListenerRepeater = null;
    private SendallNetWifiScanBroadcastReceiver sendallNetWifiScanBroadcastReceiver = null;
    private WifiApControl wifiApControl = null;
    private WifiManager.WifiLock wifiLock = null;

    @Inject
    public AppManager(Context context,
                      DBUtil dbUtil,
                      SharedPrefUtil sharedPrefUtil,
                      NotificationUtil notificationUtil/*,
                      ContentProviderUtil contentProviderUtil*/){
        this.context = context;
        this.dbUtil = dbUtil;
        this.sharedPrefUtil = sharedPrefUtil;
        this.notificationUtil = notificationUtil;
//        this.contentProviderUtil = contentProviderUtil;
        init(context);
    }

    private void init(Context context) {
        if(!initialised) {
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
            channel = wifiP2pManager.initialize(context, context.getMainLooper(), new WifiP2pManager.ChannelListener() {
                @Override
                public void onChannelDisconnected() {

                }
            });
            initialised = true;

            enableWifi(false);
            wifiP2pState = WifiP2pManager.WIFI_P2P_STATE_DISABLED;
        }
    }

    public boolean isWifiEnabled(){
        return  wifiP2pState == WifiP2pManager.WIFI_P2P_STATE_ENABLED;
    }

    private void enableWifi(boolean enable){
        if(!enable && isWifiLocked()){
            releaseWifiLock();
        }
        boolean success = wifiManager.setWifiEnabled(enable);
        if(!success){
            Toast.makeText(context, "Please turn " + (enable ? "on" : "off") +" wifi", Toast.LENGTH_SHORT).show();
        } else {
            //wait for the operation to take effect
            if(enable){
                while(!wifiManager.isWifiEnabled()){}
            } else {
                while(wifiManager.isWifiEnabled()){}
            }
        }
    }

    /*Method to set current wifi status from wifi broadcast receiver*/
    public void setWifiP2pState(int currentWifiStatus){
        this.wifiP2pState = currentWifiStatus;
    }

    private void aquireWifiLock(){
        if(wifiLock == null){
            wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, this.getClass().getSimpleName());
        }
        wifiLock.acquire();
    }

    private void releaseWifiLock(){
        if(wifiLock != null){
            wifiLock.release();
            wifiLock = null;
        }
    }

    private boolean isWifiLocked(){
        return wifiLock != null && wifiLock.isHeld();
    }

    public void initConnection(int newAppStatus){
        //first of all, change the app status
        int appStatus = sharedPrefUtil.getCurrentAppStatus();
        if(appStatus == SharedPrefConstants.CURR_STATUS_IDLE) {
            sharedPrefUtil.setCurrentAppStatus(newAppStatus);
            sharedPrefUtil.commit();
            enableWifi(false);

            WifiConfiguration wifiConfiguration = getWifiConfiguration(sharedPrefUtil.getThisDeviceId() + '_' + sharedPrefUtil.getUserName(), sharedPrefUtil.getDefaultWifiPass(), true);

            stopHotspot();
            wifiApControl = WifiApControl.getInstance(context);
            if(wifiApControl != null) {
                wifiApControl.setEnabled(wifiConfiguration, true);
            }
        } else {
            Toast.makeText(context, WAIT_FOR_CURRENT_OP_TOAST, Toast.LENGTH_SHORT).show();
        }
    }

    private void stopHotspot(){
        if(wifiApControl != null){
            wifiApControl.disable();
        }
        wifiApControl = null;
    }

    public void startScanningWifi(Updatable updatable, int newAppStatus){
        int appStatus = sharedPrefUtil.getCurrentAppStatus();
        if(appStatus == SharedPrefConstants.CURR_STATUS_IDLE) {
            sharedPrefUtil.setCurrentAppStatus(newAppStatus);
            sharedPrefUtil.commit();
            enableWifi(true);

            stopWifiScanning();

            sendallNetWifiScanBroadcastReceiver = new SendallNetWifiScanBroadcastReceiver(updatable, sharedPrefUtil);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            context.registerReceiver(sendallNetWifiScanBroadcastReceiver, intentFilter);
            wifiManager.disconnect();
            wifiManager.startScan();
        } else {
            Toast.makeText(context, WAIT_FOR_CURRENT_OP_TOAST, Toast.LENGTH_SHORT).show();
        }
    }

    public void stopWifiScanning(){
        if(sendallNetWifiScanBroadcastReceiver != null){
            context.unregisterReceiver(sendallNetWifiScanBroadcastReceiver);
        }
        sendallNetWifiScanBroadcastReceiver = null;
    }

    public void stopHotspotAndScanning(){
        releaseWifiLock();
        stopWifiScanning();
        stopHotspot();
        enableWifi(false);

        sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
        sharedPrefUtil.commit();
    }

    @NonNull
    private WifiConfiguration getWifiConfiguration(String ssid, String pass, boolean create) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        if(!create){
            ssid = "\"" + ssid + "\"";
        }
        wifiConfiguration.SSID = ssid;
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        return wifiConfiguration;
    }

    public class BroadcastReceiverAutoUnregister implements Runnable{
        private final Object syncObj = new Object();
        private boolean unregistered = false;
        private Context context;
        private BroadcastReceiver broadcastReceiver;
        private long lifetime;

        public BroadcastReceiverAutoUnregister(Context context, BroadcastReceiver broadcastReceiver, long lifetime){
            this.context = context;
            this.broadcastReceiver = broadcastReceiver;
            this.lifetime = lifetime;
        }

        @Override
        public void run() {
            synchronized (syncObj) {
                if (!unregistered) {
                    unregistered = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            context.unregisterReceiver(broadcastReceiver);
                        }
                    }, lifetime);
                }
            }
        }

        public void unregNow(){
            synchronized (syncObj) {
                if (!unregistered) {
                    unregistered = true;
                    context.unregisterReceiver(broadcastReceiver);
                }
            }
        }
    }

    /*public Map<String, String> getAllActiveSendallNets(){
//        wifiManager.startScan();
        Map<String, String> ssidToPass = new HashMap<>();
        List<ScanResult> scanResults = wifiManager.getScanResults();
        if(scanResults != null){
            for(ScanResult scanResult : scanResults){
                if(sharedPrefUtil.isOurNetwork(scanResult)){
                    ssidToPass.put(scanResult.SSID, sharedPrefUtil.getDefaultWifiPass());
                }
            }
        }
        return ssidToPass;
    }*/

    public void startP2pServiceDiscovery(final WifiP2pManager.DnsSdTxtRecordListener textListener, final WifiP2pManager.DnsSdServiceResponseListener serviceResponseListener){
        startP2pServiceDiscovery(textListener, serviceResponseListener, true);
    }

    private void startP2pServiceDiscovery(final WifiP2pManager.DnsSdTxtRecordListener textListener, final WifiP2pManager.DnsSdServiceResponseListener serviceResponseListener, boolean newRequest){
        enableWifi(true);
        if(newRequest){
            //this is a new request to start service discovery. So stop old repeater and start new repeater
            if(this.serviceListenerRepeater != null){
                this.serviceListenerRepeater.setInactive();
            }
            this.serviceListenerRepeater = new ServiceListenerRepeater(textListener, serviceResponseListener);
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                wifiP2pManager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        clearedLocalServices();
                    }

                    @Override
                    public void onFailure(int reason) {
                        clearedLocalServices();
                    }

                    private void clearedLocalServices(){
                        wifiP2pManager.clearServiceRequests(channel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                clearedServiceRequests();
                            }

                            @Override
                            public void onFailure(int reason) {
                                clearedServiceRequests();
                            }

                            private void clearedServiceRequests(){
                                wifiP2pManager.setDnsSdResponseListeners(channel, serviceResponseListener, textListener);

                                final WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
                                wifiP2pManager.addServiceRequest(channel, serviceRequest, new WifiP2pManager.ActionListener() {
                                    int failureCount = 0;
                                    @Override
                                    public void onSuccess() {
                                        Log.d(AppManager.class.getSimpleName(), "Added service request");
                                        if(isWifiEnabled()) {
                                            notificationUtil.showToggleReceivingNotification();
                                        }
                                        serviceRequestAdded();
                                    }

                                    @Override
                                    public void onFailure(int reason) {
                                        Log.d(AppManager.class.getSimpleName(), "Adding Service request failed");
                                        failureCount++;
                                        if(isWifiEnabled() && WifiP2pManager.BUSY == reason && failureCount < 5) {
                                            //retry after 2 seconds
                                            final WifiP2pManager.ActionListener thisListener = this;
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    wifiP2pManager.addServiceRequest(channel, serviceRequest, thisListener);
                                                }
                                            }, 2000);
                                        } else {
                                            Log.d(AppManager.class.getSimpleName(), "Removed service request");
                                            Toast.makeText(context, "Scanning failed", Toast.LENGTH_SHORT).show();
                                            sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                                            sharedPrefUtil.commit();
                                            if(isWifiEnabled()) {
                                                notificationUtil.showToggleReceivingNotification();
                                            }
                                            serviceRequestAdded();
                                        }
                                    }

                                    private void serviceRequestAdded(){
                                        wifiP2pManager.discoverServices(channel, new WifiP2pManager.ActionListener() {
                                            int failureCount = 0;

                                            @Override
                                            public void onSuccess() {
                                                Log.d(AppManager.class.getSimpleName(), "Started service discovery");
                                                if(isWifiEnabled()) {
                                                    notificationUtil.showToggleReceivingNotification();
                                                }
                                                new Handler().postDelayed(AppManager.this.serviceListenerRepeater, 2000);
                                            }

                                            @Override
                                            public void onFailure(int reason) {
                                                Log.d(AppManager.class.getSimpleName(), "Failed starting service discovery");
                                                failureCount++;
                                                if(isWifiEnabled() && WifiP2pManager.BUSY == reason && failureCount < 5) {
                                                    //retry after 2 seconds
                                                    final WifiP2pManager.ActionListener thisListener = this;
                                                    handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            wifiP2pManager.discoverServices(channel, thisListener);
                                                        }
                                                    }, 2000);
                                                } else {
                                                    Log.d(AppManager.class.getSimpleName(), "Aborted service discovery");
                                                    Toast.makeText(context, "Something's not right. Please turn on wifi", Toast.LENGTH_SHORT).show();
                                                    sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                                                    sharedPrefUtil.commit();
                                                    if(isWifiEnabled()) {
                                                        notificationUtil.showToggleReceivingNotification();
                                                    }
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }, 1000);

    }

    public void stopP2pServiceDiscovery(){
        //set the old repeaters if any
        if(this.serviceListenerRepeater != null){
            this.serviceListenerRepeater.setInactive();
        }

        wifiP2pManager.clearServiceRequests(channel, new WifiP2pManager.ActionListener() {
            Handler handler = new Handler();
            int failureCount = 0;
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
                Log.d(AppManager.class.getSimpleName(), "Failed stopping service discovery");
                failureCount++;
                if(isWifiEnabled() && WifiP2pManager.BUSY == reason && failureCount < 2) {
                    //retry after 0.5 seconds
                    final WifiP2pManager.ActionListener thisListener = this;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            wifiP2pManager.clearServiceRequests(channel, thisListener);
                        }
                    }, 500);
                } else {
                    Log.d(AppManager.class.getSimpleName(), "Stopped service discovery");
                }
            }
        });
    }

    public void stopP2pServiceAdv(){
        wifiP2pManager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
            Handler handler = new Handler();
            int failureCount = 0;
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
                Log.d(AppManager.class.getSimpleName(), "Failed stopping service discovery");
                failureCount++;
                if(isWifiEnabled() && WifiP2pManager.BUSY == reason && failureCount < 2) {
                    //retry after 0.5 seconds
                    final WifiP2pManager.ActionListener thisListener = this;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            wifiP2pManager.clearServiceRequests(channel, thisListener);
                        }
                    }, 500);
                } else {
                    Log.d(AppManager.class.getSimpleName(), "Stopped service discovery");
                }
            }
        });
    }

    /*public void stopAllWifiOps(){
        //Stop the request repeater if any{
        if(serviceListenerRepeater != null){
            serviceListenerRepeater.setInactive();
        }
        final Handler handler = new Handler();
        sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_STOPPING_ALL);
        sharedPrefUtil.commit();
        enableWifi(true);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                wifiP2pManager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        removedLocalServices();
                    }

                    @Override
                    public void onFailure(int reason) {
                        removedLocalServices();
                    }

                    private void removedLocalServices(){
                        wifiP2pManager.clearServiceRequests(channel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                clearedServiceRequests();
                            }

                            @Override
                            public void onFailure(int reason) {
                                clearedServiceRequests();
                            }

                            private void clearedServiceRequests(){
                                wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        groupRemoved();
                                    }

                                    @Override
                                    public void onFailure(int reason) {
                                        groupRemoved();
                                    }

                                    private void groupRemoved(){
                                        if(isWifiEnabled()) {
                                            sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_STOPPING_ALL);
                                            sharedPrefUtil.commit();
                                            enableWifi(false);
                                        } else {
                                            sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                                            sharedPrefUtil.commit();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }, 1000);
    }*/

    private class ServiceListenerRepeater implements Runnable{
        int requestCount = 20;
        private WifiP2pManager.DnsSdTxtRecordListener textListener;
        private WifiP2pManager.DnsSdServiceResponseListener serviceResponseListener;
        private boolean active = true;

        ServiceListenerRepeater(WifiP2pManager.DnsSdTxtRecordListener textListener, WifiP2pManager.DnsSdServiceResponseListener serviceResponseListener){
            this.textListener = textListener;
            this.serviceResponseListener = serviceResponseListener;
        }

        @Override
        public void run() {
            if(active) {
                requestCount--;
                if (requestCount > 0) {
                    startP2pServiceDiscovery(textListener, serviceResponseListener, false);
                } else {
                    stopP2pServiceDiscovery();
                }
            }
        }

        void setInactive(){
            active = false;
        }
    }

    public InetAddress connectAndGetAddressOf(String SSID, String PASS) {
        WifiConfiguration wifiConfiguration = getWifiConfiguration(SSID, PASS, false);
        if(!isWifiEnabled()){
            enableWifi(true);
        }
        aquireWifiLock();
        int res = wifiManager.addNetwork(wifiConfiguration);
        wifiManager.disconnect();
        try {
            Thread.sleep(3 * 1000);//time to disconnect
        } catch(Exception e){
            e.printStackTrace();
        }
        wifiManager.enableNetwork(res, true);
        wifiManager.reconnect();

        try {
            Thread.sleep(3 * 1000);//time to reconnect
        } catch(Exception e){
            e.printStackTrace();
        }
        int serverAddress = wifiManager.getDhcpInfo().serverAddress;
        return intToInetAddress(serverAddress);
    }

    private InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = {(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /*private WifiConfiguration getWifiConfig(String SSID, String PASS){
        Collection<ScanResult> scanResults = wifiManager.getScanResults();
        if(scanResults != null){
            for(ScanResult res : scanResults){
                if(res.SSID.equals(SSID)){
                    return getWifiConfig(res, PASS);
                }
            }
        }
        return null;
    }*/


    /*private WifiConfiguration getWifiConfig(ScanResult scanRes, String PASS) {
        WifiConfiguration conf = null;
        try {

            Log.i("rht", "Item clicked, SSID " + scanRes.SSID + " Security : " + scanRes.capabilities);

            conf = new WifiConfiguration();
            conf.SSID = "\"" + scanRes.SSID + "\"";
            conf.status = WifiConfiguration.Status.ENABLED;
            conf.priority = 40;

            if (scanRes.capabilities.toUpperCase().contains("WEP")) {
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.wepKeys[0] = "\""+ PASS +"\"";

            } else if (scanRes.capabilities.toUpperCase().contains("WPA")) {
                conf.preSharedKey = "\"" + PASS + "\"";
            } else {
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return conf;
    }*/
}
