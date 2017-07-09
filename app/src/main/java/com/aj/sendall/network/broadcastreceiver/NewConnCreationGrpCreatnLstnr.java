package com.aj.sendall.network.broadcastreceiver;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.util.Log;
import android.widget.Toast;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.network.runnable.FileTransferServer;
import com.aj.sendall.network.runnable.NewConnCreationServer;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.network.utils.LocalWifiManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ajilal on 10/7/17.
 */

public class NewConnCreationGrpCreatnLstnr extends AbstractGroupCreationListener {

    public NewConnCreationGrpCreatnLstnr(LocalWifiManager localWifiManager){
        super(localWifiManager);
    }

    @Override
    protected void onGroupInfoAvailable(final Context context, final String networkName, final String passPhrase) {
        localWifiManager.wifiP2pManager.clearLocalServices(localWifiManager.channel, new WifiP2pManager.ActionListener() {
            private int clearServicesFailureCount = 0;//stop trying after 5 failures

            @Override
            public void onSuccess() {
                try {
                    final Map<String, String> record = new HashMap<>();
                    record.put(Constants.ADV_KEY_NETWORK_NAME, networkName);
                    record.put(Constants.ADV_KEY_NETWORK_PASSPHRASE, passPhrase);
                    record.put(Constants.ADV_KEY_GROUP_PURPOSE, Constants.ADV_VALUE_PURPOSE_CONNECTION_CREATION);

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
                            NewConnCreationServer newConnCreationServer = new NewConnCreationServer(serverSocket, port,  localWifiManager);
                            localWifiManager.handler.post(newConnCreationServer);
                            localWifiManager.notificationUtil.removeToggleNotification();
                        }

                        @Override
                        public void onFailure(int reason) {
                            addServiceFailureCount++;
                            if (addServiceFailureCount < 5 && WifiP2pManager.BUSY == reason) {
                                int waitTime = addServiceFailureCount * 1000;
                                final WifiP2pManager.ActionListener enclosingActionListener = this;
                                localWifiManager.handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        localWifiManager.wifiP2pManager.addLocalService(localWifiManager.channel, serviceInfo, enclosingActionListener);
                                    }
                                }, waitTime);
                            } else {
                                localWifiManager.sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                                localWifiManager.sharedPrefUtil.commit();
                                Toast.makeText(context, "Sorry!! Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (IOException ioe){
                    ioe.printStackTrace();
                    localWifiManager.sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                    localWifiManager.sharedPrefUtil.commit();
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
                    localWifiManager.handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            localWifiManager.wifiP2pManager.clearLocalServices(localWifiManager.channel, enclosingListener);
                        }
                    }, waitTime);
                } else {
                    localWifiManager.sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                    localWifiManager.sharedPrefUtil.commit();
                    Toast.makeText(context, "Sorry!! Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
