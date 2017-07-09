package com.aj.sendall.network.runnable;

import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.network.utils.LocalWifiManager;
import com.aj.sendall.notification.util.NotificationUtil;

/**
 * Created by ajilal on 16/6/17.
 */

public class GroupCreator implements Runnable {
    private LocalWifiManager localWifiManager;
    private NotificationUtil notificationUtil;
    private SharedPrefUtil sharedPrefUtil;

    public GroupCreator(LocalWifiManager localWifiManager,
                        NotificationUtil notificationUtil,
                        SharedPrefUtil sharedPrefUtil){
        this.localWifiManager = localWifiManager;
        this.notificationUtil = notificationUtil;
        this.sharedPrefUtil = sharedPrefUtil;
    }

    @Override
    public void run() {
        localWifiManager.wifiP2pManager.requestGroupInfo(localWifiManager.channel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if(group != null){
                    //group exists. remove it
                    localWifiManager.wifiP2pManager.removeGroup(localWifiManager.channel, new WifiP2pManager.ActionListener() {
                        private int groupRemoveFailureCount = 0;

                        @Override
                        public void onSuccess() {
                            //Success.. Now create your group
                            localWifiManager.wifiP2pManager.createGroup(localWifiManager.channel, new WifiP2pManager.ActionListener() {
                                int groupCreationFailureCount = 0;

                                @Override
                                public void onSuccess() {
                                    Toast.makeText(localWifiManager.context, "Success", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onFailure(int reason) {
                                    groupCreationFailureCount++;
                                    Toast.makeText(localWifiManager.context, ""+ reason, Toast.LENGTH_LONG).show();
                                    if(WifiP2pManager.BUSY == reason && groupCreationFailureCount < 5) {
                                        final WifiP2pManager.ActionListener enclosingListener = this;
                                        localWifiManager.handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                localWifiManager.wifiP2pManager.createGroup(localWifiManager.channel, enclosingListener);
                                            }
                                        }, 1000);
                                    } else {
                                        sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                                        sharedPrefUtil.commit();
                                        if(localWifiManager.isWifiEnabled()){
                                            notificationUtil.showToggleReceivingNotification();
                                        }
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFailure(int reason) {
                            groupRemoveFailureCount++;
                            Toast.makeText(localWifiManager.context, ""+ reason, Toast.LENGTH_LONG).show();
                            if(WifiP2pManager.BUSY == reason && groupRemoveFailureCount < 5) {
                                final WifiP2pManager.ActionListener enclosingListener = this;
                                localWifiManager.handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        localWifiManager.wifiP2pManager.removeGroup(localWifiManager.channel, enclosingListener);
                                    }
                                }, 5000);
                            } else {
                                sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                                sharedPrefUtil.commit();
                                if(localWifiManager.isWifiEnabled()){
                                    notificationUtil.showToggleReceivingNotification();
                                }
                            }
                        }
                    });
                } else {
                    //No group exists. create the group
                    localWifiManager.wifiP2pManager.createGroup(localWifiManager.channel, new WifiP2pManager.ActionListener() {
                        int groupCreationFailureCount = 0;

                        @Override
                        public void onSuccess() {
                            Toast.makeText(localWifiManager.context, "Success", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(int reason) {
                            groupCreationFailureCount++;
                            Toast.makeText(localWifiManager.context, ""+ reason, Toast.LENGTH_LONG).show();
                            if(WifiP2pManager.BUSY == reason && groupCreationFailureCount < 5) {
                                final WifiP2pManager.ActionListener enclosingListener = this;
                                localWifiManager.handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        localWifiManager.wifiP2pManager.createGroup(localWifiManager.channel, enclosingListener);
                                    }
                                }, 1000);
                            } else {
                                sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                                sharedPrefUtil.commit();
                                if(localWifiManager.isWifiEnabled()){
                                    notificationUtil.showToggleReceivingNotification();
                                }
                            }
                        }
                    });
                }
            }
        });
    }
}
