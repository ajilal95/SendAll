package com.aj.sendall.network.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pGroup;
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
import com.aj.sendall.network.runnable.Server;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.notification.util.NotificationUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by ajilal on 17/6/17.
 */

public class BroadcastReceiverForSender extends BroadcastReceiver {
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private ConnectionsAndUris connectionsAndUris;
    @Inject
    public Handler handler;
    @Inject
    public SharedPrefUtil sharedPrefUtil;
    @Inject
    public NotificationUtil notificationUtil;
    @Inject
    public ContentProviderUtil contentProviderUtil;

    public BroadcastReceiverForSender(WifiP2pManager wifiP2pManager
            , WifiP2pManager.Channel channel
            , ConnectionsAndUris connectionsAndUris) {
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.connectionsAndUris = connectionsAndUris;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        ((AndroidApplication)context.getApplicationContext()).getDaggerInjector().inject(this);
        final String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            //fetch the group info
            wifiP2pManager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(final WifiP2pGroup group) {
                    if (group != null && group.isGroupOwner()) {

                        wifiP2pManager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
                            private int clearServicesFailureCount = 0;//stop trying after 5 failures

                            @Override
                            public void onSuccess() {
                                try {
                                    //advertise the group so that other peers can connect to it
                                    final String networkName = group.getNetworkName();
                                    final String passPhrase = group.getPassphrase();

                                    final Map<String, String> record = new HashMap<>(2 + connectionsAndUris.connections.size());
                                    record.put(Constants.ADV_KEY_NETWORK_NAME, networkName);
                                    record.put(Constants.ADV_KEY_NETWORK_PASSPHRASE, passPhrase);
                                    //creating an entry for each client to indicate that they have something to do with this group
                                    for (ConnectionViewData connectionViewData : connectionsAndUris.connections) {
                                        record.put(connectionViewData.uniqueId, Constants.ADV_VALUE_DATA_AVAILABLE);
                                    }

                                    //Creating the server socket and the port
                                    final ServerSocket serverSocket = new ServerSocket(0);
                                    final int port = serverSocket.getLocalPort();
                                    Log.d(BroadcastReceiverForSender.class.getSimpleName(), "Created server => port:" + port);

                                    record.put(Constants.ADV_KEY_SERVER_PORT, String.valueOf(port));

                                    final WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo
                                            .newInstance(Constants.P2P_SERVICE_INSTANCE_NAME, Constants.P2P_SERVICE_SERVICE_TYPE, record);
                                    wifiP2pManager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
                                        private int addServiceFailureCount = 0;

                                        @Override
                                        public void onSuccess() {
                                            Log.d(BroadcastReceiverForSender.class.getSimpleName(), "Service added successfully..");
                                            Log.d(BroadcastReceiverForSender.class.getSimpleName(), "networkName : " + networkName);
                                            Log.d(BroadcastReceiverForSender.class.getSimpleName(), "Passphrase : " + passPhrase);

                                            //Starting the server.
                                            Server server = new Server(serverSocket, port, wifiP2pManager, channel, connectionsAndUris, context);
                                            handler.post(server);

                                            context.unregisterReceiver(BroadcastReceiverForSender.this);
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
                                                        wifiP2pManager.addLocalService(channel, serviceInfo, enclosingActionListener);
                                                    }
                                                }, waitTime);
                                            } else {
                                                sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                                                sharedPrefUtil.commit();
                                                Toast.makeText(context, "Sorry!! Failed", Toast.LENGTH_SHORT).show();
                                                context.unregisterReceiver(BroadcastReceiverForSender.this);
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
                                            wifiP2pManager.clearLocalServices(channel, enclosingListener);
                                        }
                                    }, waitTime);
                                } else {
                                    sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                                    sharedPrefUtil.commit();
                                    Toast.makeText(context, "Sorry!! Failed", Toast.LENGTH_SHORT).show();
                                    context.unregisterReceiver(BroadcastReceiverForSender.this);
                                }
                            }
                        });
                    }
                }
            });
        }
    }
}