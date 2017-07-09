package com.aj.sendall.network.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;

import com.aj.sendall.network.utils.LocalWifiManager;

/**
 * Created by ajilal on 9/7/17.
 */

public abstract class AbstractGroupCreationListener extends BroadcastReceiver {
    protected LocalWifiManager localWifiManager;

    public AbstractGroupCreationListener(LocalWifiManager localWifiManager){
        this.localWifiManager = localWifiManager;
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        final String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            //fetch the group info
            localWifiManager.wifiP2pManager.requestGroupInfo(localWifiManager.channel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(final WifiP2pGroup group) {
                    if (group != null && group.isGroupOwner()) {
                        //This Receiver is used only for getting the group info.
                        //So unregister now
                        context.unregisterReceiver(AbstractGroupCreationListener.this);
                        AbstractGroupCreationListener.this.onGroupInfoAvailable(context, group.getNetworkName(), group.getPassphrase());
                    }
                }
            });
        }
    }

    protected abstract void onGroupInfoAvailable(Context context, String networkName, String passPhrase);
}
