package com.aj.sendall.network.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import com.aj.sendall.application.AppManager;

import java.net.InetAddress;

/**
 * Created by ajilal on 9/7/17.
 */

public abstract class AbstractGroupCreationListener extends BroadcastReceiver {
    protected AppManager appManager;
    protected AppManager.BroadcastReceiverAutoUnregister unregister;

    public AbstractGroupCreationListener(AppManager appManager){
        this.appManager = appManager;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            NetworkInfo netInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if(netInfo.isConnected()) {
                //fetch the group info
                appManager.wifiP2pManager.requestGroupInfo(appManager.channel, new WifiP2pManager.GroupInfoListener() {
                    @Override
                    public void onGroupInfoAvailable(final WifiP2pGroup group) {
                        if (group != null && group.isGroupOwner()) {
                            //This Receiver is used only for getting the group info.
                            //So unregister now
                            if (unregister != null) {
                                unregister.unregNow();
                            } else {
                                context.unregisterReceiver(AbstractGroupCreationListener.this);
                            }
                            appManager.wifiP2pManager.requestConnectionInfo(appManager.channel, new WifiP2pManager.ConnectionInfoListener() {
                                @Override
                                public void onConnectionInfoAvailable(WifiP2pInfo info) {
                                    InetAddress ownerAddress = info.groupOwnerAddress;
                                    AbstractGroupCreationListener.this.onGroupInfoAvailable(context, group.getNetworkName(), group.getPassphrase(), ownerAddress);

                                }
                            });
                        }
                    }
                });
            }
        }
    }

    public void setUnregister(AppManager.BroadcastReceiverAutoUnregister unregister){
        this.unregister = unregister;
    }

    protected abstract void onGroupInfoAvailable(Context context, String networkName, String passPhrase, InetAddress grpOwnerAdd);
}
