package com.aj.sendall.network.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Handler;
import android.util.Log;

import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.dto.ConnectionsAndUris;
import com.aj.sendall.network.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ajilal on 17/6/17.
 */

public class BroadcastReceiverForSender extends BroadcastReceiver {
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private ConnectionsAndUris connectionsAndUris;
    private Handler handler;

    public BroadcastReceiverForSender(WifiP2pManager wifiP2pManager
            , WifiP2pManager.Channel channel
            , ConnectionsAndUris connectionsAndUris
            , Handler handler){
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.connectionsAndUris = connectionsAndUris;
        this.handler = handler;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();
        if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            //fetch the group info
            wifiP2pManager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(final WifiP2pGroup group) {
                    if(group != null && group.isGroupOwner()){

                        wifiP2pManager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
                            private int clearServicesFailureCount = 0;//stop trying after 5 failures
                            @Override
                            public void onSuccess() {
                                //advertise the group so that other peers can connect to it
                                final String networkName = group.getNetworkName();
                                final String passPhrase = group.getPassphrase();

                                final Map<String, String> record = new HashMap<>(2 + connectionsAndUris.connections.size());
                                record.put(Constants.ADV_KEY_NETWORK_NAME, networkName);
                                record.put(Constants.ADV_KEY_NETWORK_PASSPHRASE, passPhrase);
                                //creating an entry for each client to indicate that they have something to do with this group
                                for(ConnectionViewData connectionViewData : connectionsAndUris.connections){
                                    record.put(connectionViewData.uniqueId, Constants.ADV_VALUE_DATA_AVAILABLE);
                                }
                                final WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo
                                        .newInstance(Constants.P2P_SERVICE_ISTANCE_NAME, Constants.P2P_SERVICE_SERVICE_TYPE, record);
                                wifiP2pManager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
                                    private int addServiceFailureCount = 0;
                                    @Override
                                    public void onSuccess() {
                                        Log.i(BroadcastReceiverForSender.class.getSimpleName(), "Service added successfuy..");
                                        Log.i(BroadcastReceiverForSender.class.getSimpleName(), "networkName : " + networkName);
                                        Log.i(BroadcastReceiverForSender.class.getSimpleName(), "Passphrase : " + passPhrase);
                                        context.unregisterReceiver(BroadcastReceiverForSender.this);
                                    }

                                    @Override
                                    public void onFailure(int reason) {
                                        addServiceFailureCount++;
                                        if(addServiceFailureCount < 5 && WifiP2pManager.BUSY == reason){
                                            int waitTime = addServiceFailureCount * 1000;
                                            final WifiP2pManager.ActionListener enclosingActionListener = this;
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    wifiP2pManager.addLocalService(channel, serviceInfo, enclosingActionListener);
                                                }
                                            }, waitTime);
                                        } else {
                                            context.unregisterReceiver(BroadcastReceiverForSender.this);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onFailure(int reason) {
                                clearServicesFailureCount++;
                                if(clearServicesFailureCount < 5 && WifiP2pManager.BUSY == reason){
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