package com.aj.sendall.network.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.broadcastreceiver.SendallNetWifiScanBroadcastReceiver;
import com.aj.sendall.network.monitor.Updatable;
import com.aj.sendall.network.runnable.FileTransferClient;
import com.aj.sendall.network.runnable.NewConnCreationClientConnector;
import com.aj.sendall.network.runnable.NewConnCreationServer;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.notification.util.NotificationUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class ToggleReceiverService extends IntentService {
    public static final String ACTION = "action";
    public static final String ACTION_START = "action-start";
    public static final String ACTION_STOP = "action-stop";

    @Inject
    public AppManager appManager;

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
        int currentAppStatus = appManager.getCurrentAppStatus();

        appManager.showStatusNotification(false);
        String action = intent.getStringExtra(ACTION);
        if(currentAppStatus == SharedPrefConstants.CURR_STATUS_IDLE){
            if(ACTION_START.equals(action)){
                appManager.startScanningWifi(new FileReceiverStarter(appManager), SharedPrefConstants.CURR_STATUS_TRANSFERRING);
            } else if(ACTION_STOP.equals(action)){
                //Stop all possible services
                NewConnCreationServerService.stop(this);
                NewConnCreationClientService.stop(this);
                FileTransferServerService.stop(this);
                FileTransferClientService.stop(this);
            }
        }
        appManager.showStatusNotification(true);
    }

    public static class FileReceiverStarter implements Updatable {
        private List<String> contactedSSIDs = new LinkedList<>();//to keep track of already connected devices in this receive attempt
        private AppManager appManager;

        FileReceiverStarter(AppManager appManager){
            this.appManager = appManager;
        }

        @Override
        public void update(UpdateEvent ev) {
            if(SendallNetWifiScanBroadcastReceiver.class.equals(ev.source)){
                //the update from wifi scanner
                @SuppressWarnings("unchecked") List<ScanResult> result = (List<ScanResult>) ev.getExtra(SendallNetWifiScanBroadcastReceiver.UPDATE_EXTRA_RESULT);
                appManager.stopWifiScanning();
                /*Start with the first connection available which is not contacted yet.
                * Start client for that SSID. If file is available then receive it. If
                * no not contacted connections are available then stop for a while and restart scanning.
                * An update must be got from the Client System when its operation has finished
                * On receiving that update try connecting to another SSID iff wifi is on*/
                ScanResult sr = getNextConn(result);
                if(sr != null) {
                    FileTransferClient client = new FileTransferClient(
                            sr.SSID,
                            appManager.getDefaultWifiPass(),
                            getPortNo(sr.SSID), appManager);
                    client.setOpFinisedListener(this);
                    FileTransferClientService.start(appManager.context, client);
                } else {
                    try {
                        Thread.sleep(3000);//wait 3 seconds
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    //now retry scanning if wifi is still enabled
                    if(appManager.isWifiEnabled()){
                        appManager.startScanningWifi(this, SharedPrefConstants.CURR_STATUS_TRANSFERRING);
                    }
                }
            } else if(FileTransferClient.class.equals(ev.source)){
                if(FileTransferClient.OP_FINISHED.equals(ev.action)){
                    String clientStatus = (String) ev.getExtra(FileTransferClient.CLIENT_STATUS);
                    if(FileTransferClient.FAILED_AUTH_ERR.equals(clientStatus)){
                        //no need to connect to this device again
                        contactedSSIDs.add((String) ev.getExtra(FileTransferClient.SSID));
                    } else if(FileTransferClient.TRANSFER_SUCCESS.equals(clientStatus)){
                        //file transferred. No need to go further
                        appManager.stopHotspotAndScanning();
                        appManager.showTransferSuccessNotific();
                        return;
                    } else if(FileTransferClient.FAILED_IN_SUFF_SPACE.equals(clientStatus)){
                        appManager.showInsuffSpaceNotific();
                        appManager.stopHotspotAndScanning();
                        return;
                    } else if(FileTransferClient.FAILED_NO_EXT_MEDIA.equals(clientStatus)){
                        appManager.showNoExtMediaNotific();
                        appManager.stopHotspotAndScanning();
                        return;
                    }

                    //Nothing happened with this client. Go for the next one
                    if(appManager.isWifiEnabled()){
                        appManager.startScanningWifi(this, SharedPrefConstants.CURR_STATUS_TRANSFERRING);
                    }
                }
            }
        }

        private ScanResult getNextConn(List<ScanResult> results){
            for(ScanResult sr : results){
                if(!contactedSSIDs.contains(sr.SSID)){
                    return sr;
                }
            }
            return null;
        }

        private int getPortNo(String SSID){
            return appManager.getPortNo(SSID);
        }
    }
}
