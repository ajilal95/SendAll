package com.aj.sendall.network.broadcastreceiver;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.util.Log;
import android.widget.Toast;

import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.network.runnable.NewConnCreationServer;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.interfaces.Updatable;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ajilal on 10/7/17.
 */

public class NewConnCreationGrpCreatnLstnr extends AbstractGroupCreationListener {
    private Updatable updatableActivity;

    public NewConnCreationGrpCreatnLstnr(AppManager appManager, Updatable updatableActivity){
        super(appManager);
        this.updatableActivity = updatableActivity;
    }

    @Override
    protected void onGroupInfoAvailable(final Context context, final String networkName, final String passPhrase) {
        appManager.wifiP2pManager.clearLocalServices(appManager.channel, new WifiP2pManager.ActionListener() {
            private int clearServicesFailureCount = 0;//stop trying after 5 failures

            @Override
            public void onSuccess() {
                try {
                    String thisUserName = appManager.sharedPrefUtil.getUserName();
                    final Map<String, String> record = new HashMap<>();
                    record.put(Constants.ADV_KEY_NETWORK_NAME, networkName);
                    record.put(Constants.ADV_KEY_NETWORK_PASSPHRASE, passPhrase);
                    record.put(Constants.ADV_KEY_USERNAME, thisUserName);
                    record.put(Constants.ADV_KEY_GROUP_PURPOSE, Constants.ADV_VALUE_PURPOSE_CONNECTION_CREATION);

                    //Creating the server socket and the port
                    final ServerSocket serverSocket = new ServerSocket(0);
                    final int port = serverSocket.getLocalPort();
                    Log.d(FileTransferGrpCreatnLstnr.class.getSimpleName(), "Created server => port:" + port);

                    record.put(Constants.ADV_KEY_SERVER_PORT, String.valueOf(port));

                    final WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo
                            .newInstance(Constants.P2P_SERVICE_INSTANCE_NAME, Constants.P2P_SERVICE_SERVICE_TYPE, record);
                    appManager.wifiP2pManager.addLocalService(appManager.channel, serviceInfo, new WifiP2pManager.ActionListener() {
                        private int addServiceFailureCount = 0;

                        @Override
                        public void onSuccess() {
                            Log.d(FileTransferGrpCreatnLstnr.class.getSimpleName(), "Service added successfully..");
                            Log.d(FileTransferGrpCreatnLstnr.class.getSimpleName(), "networkName : " + networkName);
                            Log.d(FileTransferGrpCreatnLstnr.class.getSimpleName(), "Passphrase : " + passPhrase);

                            //Starting the server.
                            NewConnCreationServer newConnCreationServer = new NewConnCreationServer(serverSocket, port, appManager, updatableActivity);
                            appManager.handler.post(newConnCreationServer);
                            appManager.notificationUtil.removeToggleNotification();
                        }

                        @Override
                        public void onFailure(int reason) {
                            addServiceFailureCount++;
                            if (addServiceFailureCount < 5 && WifiP2pManager.BUSY == reason) {
                                int waitTime = addServiceFailureCount * 1000;
                                final WifiP2pManager.ActionListener enclosingActionListener = this;
                                appManager.handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        appManager.wifiP2pManager.addLocalService(appManager.channel, serviceInfo, enclosingActionListener);
                                    }
                                }, waitTime);
                            } else {
                                appManager.sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                                appManager.sharedPrefUtil.commit();
                                Toast.makeText(context, "Sorry!! Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (IOException ioe){
                    ioe.printStackTrace();
                    appManager.sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                    appManager.sharedPrefUtil.commit();
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
                    appManager.handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            appManager.wifiP2pManager.clearLocalServices(appManager.channel, enclosingListener);
                        }
                    }, waitTime);
                } else {
                    appManager.sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                    appManager.sharedPrefUtil.commit();
                    Toast.makeText(context, "Sorry!! Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
