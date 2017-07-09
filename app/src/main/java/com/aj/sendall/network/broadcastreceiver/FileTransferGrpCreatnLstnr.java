package com.aj.sendall.network.broadcastreceiver;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.db.contentprovidutil.ContentProviderUtil;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.dto.ConnectionsAndUris;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.network.runnable.FileTransferServer;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.network.utils.LocalWifiManager;
import com.aj.sendall.notification.util.NotificationUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by ajilal on 17/6/17.
 */

public class FileTransferGrpCreatnLstnr extends AbstractGroupCreationListener {
    private ConnectionsAndUris connectionsAndUris;
    @Inject
    public Handler handler;
    @Inject
    public SharedPrefUtil sharedPrefUtil;
    @Inject
    public NotificationUtil notificationUtil;
    @Inject
    public ContentProviderUtil contentProviderUtil;

    public FileTransferGrpCreatnLstnr(LocalWifiManager localWifiManager,
                                      ConnectionsAndUris connectionsAndUris) {
        super(localWifiManager);
        this.connectionsAndUris = connectionsAndUris;
    }

    @Override
    protected void onGroupInfoAvailable(final Context context, final String networkName, final String passPhrase) {
        ((AndroidApplication)context.getApplicationContext()).getDaggerInjector().inject(this);
        localWifiManager.wifiP2pManager.clearLocalServices(localWifiManager.channel, new WifiP2pManager.ActionListener() {
            private int clearServicesFailureCount = 0;//stop trying after 5 failures

            @Override
            public void onSuccess() {
                try {
                    final Map<String, String> record = new HashMap<>(2 + connectionsAndUris.connections.size());
                    record.put(Constants.ADV_KEY_NETWORK_NAME, networkName);
                    record.put(Constants.ADV_KEY_NETWORK_PASSPHRASE, passPhrase);
                    record.put(Constants.ADV_KEY_GROUP_PURPOSE, Constants.ADV_VALUE_PURPOSE_DATA_TRANSFER);
                    //creating an entry for each client to indicate that they have something to do with this group
                    for (ConnectionViewData connectionViewData : connectionsAndUris.connections) {
                        record.put(connectionViewData.uniqueId, Constants.ADV_VALUE_DATA_AVAILABLE);
                    }

                    //Creating the server socket and the port
                    final ServerSocket serverSocket = new ServerSocket(0);
                    final int port = serverSocket.getLocalPort();
                    Log.d(FileTransferGrpCreatnLstnr.class.getSimpleName(), "Created server => port:" + port);

                    record.put(Constants.ADV_KEY_SERVER_PORT, String.valueOf(port));

                    final WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo
                            .newInstance(Constants.P2P_SERVICE_INSTANCE_NAME, Constants.P2P_SERVICE_SERVICE_TYPE, record);
                    localWifiManager.wifiP2pManager.addLocalService(localWifiManager.channel, serviceInfo, new WifiP2pManager.ActionListener() {
                        private int addServiceFailureCount = 0;

                        @Override
                        public void onSuccess() {
                            Log.d(FileTransferGrpCreatnLstnr.class.getSimpleName(), "Service added successfully..");
                            Log.d(FileTransferGrpCreatnLstnr.class.getSimpleName(), "networkName : " + networkName);
                            Log.d(FileTransferGrpCreatnLstnr.class.getSimpleName(), "Passphrase : " + passPhrase);

                            //Starting the server.
                            FileTransferServer fileTransferServer = new FileTransferServer(serverSocket, port, localWifiManager, connectionsAndUris);
                            handler.post(fileTransferServer);
                            notificationUtil.removeToggleNotification();
                        }

                        @Override
                        public void onFailure(int reason) {
                            addServiceFailureCount++;
                            if (addServiceFailureCount < 5 && WifiP2pManager.BUSY == reason) {
                                int waitTime = addServiceFailureCount * 1000;
                                final WifiP2pManager.ActionListener enclosingActionListener = this;
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        localWifiManager.wifiP2pManager.addLocalService(localWifiManager.channel, serviceInfo, enclosingActionListener);
                                    }
                                }, waitTime);
                            } else {
                                sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                                sharedPrefUtil.commit();
                                Toast.makeText(context, "Sorry!! Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (IOException ioe){
                    ioe.printStackTrace();
                    sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                    sharedPrefUtil.commit();
                    Toast.makeText(context, "Sorry!! Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int reason) {
                clearServicesFailureCount++;
                if (clearServicesFailureCount < 5 && WifiP2pManager.BUSY == reason) {
                    int waitTime = clearServicesFailureCount * 1000;
                    final WifiP2pManager.ActionListener enclosingListener = this;
                    //Wait and resend the request
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            localWifiManager.wifiP2pManager.clearLocalServices(localWifiManager.channel, enclosingListener);
                        }
                    }, waitTime);
                } else {
                    sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                    sharedPrefUtil.commit();
                    Toast.makeText(context, "Sorry!! Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}