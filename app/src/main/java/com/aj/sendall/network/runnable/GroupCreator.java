package com.aj.sendall.network.runnable;

import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.notification.util.NotificationUtil;

/**
 * Created by ajilal on 16/6/17.
 */

public class GroupCreator implements Runnable {
    private AppManager appManager;
    private NotificationUtil notificationUtil;
    private SharedPrefUtil sharedPrefUtil;

    public GroupCreator(AppManager appManager,
                        NotificationUtil notificationUtil,
                        SharedPrefUtil sharedPrefUtil){
        this.appManager = appManager;
        this.notificationUtil = notificationUtil;
        this.sharedPrefUtil = sharedPrefUtil;
    }

    @Override
    public void run() {
        appManager.wifiP2pManager.requestGroupInfo(appManager.channel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if(group != null){
                    //group exists. remove it
                    appManager.wifiP2pManager.removeGroup(appManager.channel, new WifiP2pManager.ActionListener() {
                        private int groupRemoveFailureCount = 0;

                        @Override
                        public void onSuccess() {
                            //Success.. Now create your group
                            appManager.wifiP2pManager.createGroup(appManager.channel, new WifiP2pManager.ActionListener() {
                                int groupCreationFailureCount = 0;

                                @Override
                                public void onSuccess() {
                                    Toast.makeText(appManager.context, "Success", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onFailure(int reason) {
                                    groupCreationFailureCount++;
                                    Toast.makeText(appManager.context, ""+ reason, Toast.LENGTH_LONG).show();
                                    if(WifiP2pManager.BUSY == reason && groupCreationFailureCount < 5) {
                                        final WifiP2pManager.ActionListener enclosingListener = this;
                                        appManager.handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                appManager.wifiP2pManager.createGroup(appManager.channel, enclosingListener);
                                            }
                                        }, 1000);
                                    } else {
                                        sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                                        sharedPrefUtil.commit();
                                        if(appManager.isWifiEnabled()){
                                            notificationUtil.showToggleReceivingNotification();
                                        }
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFailure(int reason) {
                            groupRemoveFailureCount++;
                            Toast.makeText(appManager.context, ""+ reason, Toast.LENGTH_LONG).show();
                            if(WifiP2pManager.BUSY == reason && groupRemoveFailureCount < 5) {
                                final WifiP2pManager.ActionListener enclosingListener = this;
                                appManager.handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        appManager.wifiP2pManager.removeGroup(appManager.channel, enclosingListener);
                                    }
                                }, 5000);
                            } else {
                                sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                                sharedPrefUtil.commit();
                                if(appManager.isWifiEnabled()){
                                    notificationUtil.showToggleReceivingNotification();
                                }
                            }
                        }
                    });
                } else {
                    //No group exists. create the group
                    appManager.wifiP2pManager.createGroup(appManager.channel, new WifiP2pManager.ActionListener() {
                        int groupCreationFailureCount = 0;

                        @Override
                        public void onSuccess() {
                            Toast.makeText(appManager.context, "Success", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(int reason) {
                            groupCreationFailureCount++;
                            if(groupCreationFailureCount == 1) {
                                Toast.makeText(appManager.context, "Please turn on wifi", Toast.LENGTH_LONG).show();
                            }
                            if(WifiP2pManager.BUSY == reason && groupCreationFailureCount <= 5) {
                                final WifiP2pManager.ActionListener enclosingListener = this;
                                appManager.handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        appManager.wifiP2pManager.createGroup(appManager.channel, enclosingListener);
                                    }
                                }, groupCreationFailureCount * 1000);
                            } else {
                                sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                                sharedPrefUtil.commit();
                                if(appManager.isWifiEnabled()){
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
