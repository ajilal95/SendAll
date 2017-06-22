package com.aj.sendall.network.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.widget.Toast;

import com.aj.sendall.db.dto.ConnectionsAndUris;
import com.aj.sendall.db.util.DBUtil;
import com.aj.sendall.depndency.dagger.DModule;
import com.aj.sendall.network.asych.GroupCreator;
import com.aj.sendall.network.asych.ServiceAdvertiser;
import com.aj.sendall.network.broadcastreceiver.BroadcastReceiverForSender;
import com.aj.sendall.network.services.ToggleReceiverService;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Singleton;

/**
 * Created by ajilal on 25/5/17.
 */

@Singleton
public class LocalWifiManager implements Serializable{
    private Context context;
    private DBUtil dbUtil;
    private boolean initialised = false;
    private WifiManager wifiManager;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private Handler groupHandler;

    @Inject
    public LocalWifiManager(Context context, DBUtil dbUtil, @Named(DModule.NAME_WIFI_GROUP_HANDLER) Handler groupHandler){
        this.context = context;
        this.dbUtil = dbUtil;
        this.groupHandler = groupHandler;
        init();
    }

    private void init() {
        if(!initialised) {
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
            channel = wifiP2pManager.initialize(context, context.getMainLooper(), new WifiP2pManager.ChannelListener() {
                @Override
                public void onChannelDisconnected() {

                }
            });
            initialised = true;
        }
    }

    public boolean isWifiEnabled(){
        return  wifiManager.isWifiEnabled();
    }

    public void enableWifi(boolean enable){
        wifiManager.setWifiEnabled(enable);
    }

    public void scanPeersAndNotifyBroadcastReceiver(WifiP2pManager.ActionListener listener){
        wifiP2pManager.discoverPeers(channel, listener);
    }

    public void stopScanning(){
        wifiP2pManager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }

    public void requestPeers(WifiP2pManager.PeerListListener listener){
        wifiP2pManager.requestPeers(channel, listener);
    }

    public void connectAndReceiveFiles(WifiP2pDevice device){
        if(dbUtil.isConnectedSendAllDevice(device.deviceName)) {
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int reason) {

                }
            });
        }
    }

    public void createGroupAndAdvertise(ConnectionsAndUris connectionsAndUris){
        enableWifi(true);

        //Start the receiver to receive the connection data
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        BroadcastReceiverForSender broadcastReceiverForSender = new BroadcastReceiverForSender(wifiP2pManager, channel, connectionsAndUris, groupHandler);
        context.registerReceiver(broadcastReceiverForSender, intentFilter);

        //Delay for the service to start
        groupHandler.postDelayed(new GroupCreator(wifiP2pManager, channel, context, groupHandler), 2000);
    }
}
