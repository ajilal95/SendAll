package com.aj.sendall.network.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.network.runnable.AbstractServer;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.interfaces.Updatable;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractServerService extends IntentService {
    protected static final String ACTION_START_NEW = "com.aj.sendall.network.services.action.START_NEW";
    protected static final String ACTION_STOP = "com.aj.sendall.network.services.action.STOP";
    private static final String INTENT_EXTRA_KEYS = "intent.extra.keys";

    private ServerSocket serverSocket;
    private int port;
    private Handler handler;
    private AppManager appManager;

    private Map<String, String> recToAdv = new HashMap<>();

    public AbstractServerService(String serviceName) {
        super(serviceName);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        this.appManager = getAppManager(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if(allowOperation(action)) {
                if (ACTION_START_NEW.equals(action)) {
                    try {
                        serverSocket = new ServerSocket(0);
                        createServerToStaticVariable(serverSocket, appManager);
                        port = serverSocket.getLocalPort();
                        handler = new Handler();

                        String[] keys = intent.getStringArrayExtra(INTENT_EXTRA_KEYS);
                        for (String key : keys) {
                            recToAdv.put(key, intent.getStringExtra(key));
                        }
                        final String thisUserName = appManager.sharedPrefUtil.getUserName();
                        recToAdv.put(Constants.ADV_KEY_USERNAME, thisUserName);
                        recToAdv.put(Constants.ADV_KEY_GROUP_PURPOSE, getServerPurpose());
                        recToAdv.put(Constants.ADV_KEY_SERVER_PORT, String.valueOf(port));
                        recToAdv = updateRecordToAdv(recToAdv, intent);

                        startServerAction();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (ACTION_STOP.equals(action)) {
                    stopCurrentServer();
                    afterStopped();
                }
            }
        }
    }

    abstract protected AppManager getAppManager(Context context);
    abstract protected Map<String, String> updateRecordToAdv(Map<String, String> mapToAdv, Intent intent);
    abstract protected String getServerPurpose();
    abstract protected boolean createServerToStaticVariable(ServerSocket serverSocket, AppManager appManager);
    abstract protected AbstractServer getServerFromAStaticVariable();
    abstract protected void setServerFromStaticVariableToNull();
    abstract protected boolean allowOperation(String action);
    abstract public void afterStopped();

    private void startServerAction(){
        try{
            new Handler().postDelayed(ScanPeersToKeepAdvActive.getInstance(appManager), 5000);
            handler.post(getServerFromAStaticVariable());
            appManager.notificationUtil.removeToggleNotification();
            appManager.wifiP2pManager.clearLocalServices(appManager.channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    clearedLocalServices();
                }

                @Override
                public void onFailure(int reason) {
                    clearedLocalServices();
                }

                private void clearedLocalServices() {
                    final WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo
                            .newInstance(Constants.P2P_SERVICE_INSTANCE_NAME, Constants.P2P_SERVICE_SERVICE_TYPE, recToAdv);
                    appManager.wifiP2pManager.addLocalService(appManager.channel, serviceInfo, new WifiP2pManager.ActionListener() {
                        private int addServiceFailureCount = 0;

                        @Override
                        public void onSuccess() {
                            Log.i(getContext().getClass().getSimpleName(), "Server : " + serverSocket.getLocalSocketAddress() + " Port : " + port);
                        }

                        @Override
                        public void onFailure(int reason) {
                            addServiceFailureCount++;
                            if (addServiceFailureCount < 5 && WifiP2pManager.BUSY == reason) {
                                if (addServiceFailureCount == 1) {
                                    Toast.makeText(getContext(), "Something's not right. Plese turn on wifi.", Toast.LENGTH_SHORT).show();
                                }
                                int waitTime = addServiceFailureCount * 1000;
                                final WifiP2pManager.ActionListener enclosingActionListener = this;
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        appManager.wifiP2pManager.addLocalService(appManager.channel, serviceInfo, enclosingActionListener);
                                    }
                                }, waitTime);
                            } else {
                                stopCurrentServer();
                                appManager.sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                                appManager.sharedPrefUtil.commit();
                                Toast.makeText(getContext(), "Sorry!! Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            });
        } catch (Exception e){
            e.printStackTrace();
            appManager.sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
            appManager.sharedPrefUtil.commit();
            Toast.makeText(getContext(), "Sorry!! Failed", Toast.LENGTH_SHORT).show();
        }
    }

    protected Context getContext(){
        return this;
    }

    private void stopCurrentServer() {
        ScanPeersToKeepAdvActive.setInactive();
        if(getServerFromAStaticVariable() != null){
            appManager.wifiP2pManager.clearLocalServices(appManager.channel, null);
            Updatable.UpdateEvent event = new Updatable.UpdateEvent();
            event.source = ConnCreationServerService.class;
            event.data.put(Constants.ACTION, Constants.CLOSE_SOCKET);
            getServerFromAStaticVariable().update(event);
            if(serverSocket != null && !serverSocket.isClosed()){
                try {
                    serverSocket.close();
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
            setServerFromStaticVariableToNull();
        }
    }

    public static void start(Context context, Map<String, String> recordToAdv) {
        String[] keys = new String[recordToAdv.keySet().size()];
        recordToAdv.keySet().toArray(keys);

        Intent intent = new Intent(context, ConnCreationServerService.class);
        intent.putExtra(INTENT_EXTRA_KEYS, keys);
        for(String key : keys){
            intent.putExtra(key, recordToAdv.get(key));
        }
        intent.setAction(ACTION_START_NEW);
        context.startService(intent);
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, ConnCreationServerService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }
}

class ScanPeersToKeepAdvActive implements Runnable{
    private Handler handler = new Handler();
    private int wakeCount = 6;
    private boolean active = true;
    private AppManager appManager;

    private static ScanPeersToKeepAdvActive newObj = null;

    static ScanPeersToKeepAdvActive getInstance(AppManager appManager){
        if (newObj == null) {
            newObj = new ScanPeersToKeepAdvActive(appManager);
            newObj.wakeCount = 6;
            newObj.active = true;
        }
        return newObj;
    }

    private ScanPeersToKeepAdvActive(AppManager appManager){
        this.appManager = appManager;
    }

    public void run(){
        if(active) {
            wakeCount--;
            if (wakeCount >= 0) {
                appManager.wifiP2pManager.discoverPeers(appManager.channel, null);
                handler.postDelayed(this, 5000);
            } else {
                newObj = null;
                active = false;
            }
        } else {
            wakeCount = -1;
        }
    }

    static void setInactive(){
        if(newObj != null){
            newObj.active = false;
            newObj = null;
        }
    }
}
