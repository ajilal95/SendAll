package com.aj.sendall.network.runnable;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by ajilal on 16/6/17.
 */

public class GroupCreator implements Runnable {
    private Handler parentHandler;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private Context context;

    public GroupCreator(WifiP2pManager wifiP2pManager,
                        WifiP2pManager.Channel channel,
                        Context context,
                        Handler handler){
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.context = context;
        this.parentHandler = handler;
    }

    @Override
    public void run() {
        wifiP2pManager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if(group != null){
                    //group exists. remove it
                    wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            //Success.. Now create your group
                            wifiP2pManager.createGroup(channel, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(context, "Success", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onFailure(int reason) {
                                    Toast.makeText(context, ""+ reason, Toast.LENGTH_LONG).show();
                                    if(WifiP2pManager.BUSY == reason) {
                                        parentHandler.postDelayed(GroupCreator.this, 5000);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFailure(int reason) {
                            Toast.makeText(context, ""+ reason, Toast.LENGTH_LONG).show();
                            if(WifiP2pManager.BUSY == reason) {
                                parentHandler.postDelayed(GroupCreator.this, 5000);
                            }
                        }
                    });
                } else {
                    //No group exists. create the group
                    wifiP2pManager.createGroup(channel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(context, "Success", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(int reason) {
                            Toast.makeText(context, ""+ reason, Toast.LENGTH_LONG).show();
                            if(WifiP2pManager.BUSY == reason) {
                                parentHandler.postDelayed(GroupCreator.this, 5000);
                            }
                        }
                    });
                }
            }
        });
    }
}
