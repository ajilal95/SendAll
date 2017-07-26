package com.aj.sendall.ui.activity;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.aj.sendall.R;
import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.network.broadcastreceiver.NewConnCreationGrpCreatnLstnr;
import com.aj.sendall.network.runnable.NewConnCreationClient;
import com.aj.sendall.network.runnable.NewConnCreationClientConnector;
import com.aj.sendall.network.runnable.NewConnCreationServer;
import com.aj.sendall.network.services.ConnCreationClientService;
import com.aj.sendall.network.services.ConnCreationServerService;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.adapter.ConnectorAdapter;
import com.aj.sendall.ui.interfaces.Updatable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class Connector extends AppCompatActivity implements Updatable {
    private RecyclerView availableConns;
    private LinearLayout transBgLayout;
    private ProgressBar pBarLoadingConns;
    private LinearLayout buttonLayout;
    private ImageView imgBtnInitConn;
    private ImageView imgBtnScanConn;
    private Map<String, NewConnCreationClientConnector> usernameToConnSender;
    private Map<String, Map<String, String>> userNameToConnectionData;
    private List<NewConnCreationClient> clientList;
    private Action selectedAction;
    @Inject
    AppManager appManager;

    private ConnectorAdapter connectorAdapter;
    private LinkedList<ConnectionViewData> connectionViewDatas = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connector);
        ((AndroidApplication)getApplication()).getDaggerInjector().inject(this);

        appManager.notificationUtil.removeToggleNotification();

        usernameToConnSender = new HashMap<>();
        userNameToConnectionData = new HashMap<>();
        clientList = new ArrayList<>();

        findViews();
        initViews();
        setListeners();
    }

    private void findViews(){
        availableConns = (RecyclerView) findViewById(R.id.recyclr_vw_connector_available_conns);
        transBgLayout = (LinearLayout) findViewById(R.id.lyt_connector_trans_bg);
        pBarLoadingConns = (ProgressBar) findViewById(R.id.prg_bar_connector_loading_conns);
        buttonLayout = (LinearLayout) findViewById(R.id.lyt_connector_create_connection_buttons);
        imgBtnInitConn = (ImageView) findViewById(R.id.img_btn_connector_initiate_connection);
        imgBtnScanConn = (ImageView) findViewById(R.id.img_btn_connector_search_connection);
    }

    private void initViews(){
        availableConns.setLayoutManager(new LinearLayoutManager(this));
        connectorAdapter = new ConnectorAdapter(this);
        availableConns.setAdapter(connectorAdapter);

        pBarLoadingConns.setAlpha(0);
    }

    private void setListeners(){
        imgBtnInitConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewConnCreationGrpCreatnLstnr listener = new NewConnCreationGrpCreatnLstnr(appManager, Connector.this);
                appManager.createGroupAndAdvertise(listener, SharedPrefConstants.CURR_STATUS_CEATING_CONNECTION);
                selectedAction = Action.CREATE;
                animateViewsOnButtonClicked(v);
            }
        });

        imgBtnScanConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAction = Action.JOIN;
                startP2pServiceDiscovery(appManager);
                animateViewsOnButtonClicked(v);
            }
        });
    }

    private void animateViewsOnButtonClicked(View clickedButton){
        if(clickedButton.equals(imgBtnInitConn)){
            imgBtnInitConn.animate().setDuration(500).translationX(imgBtnInitConn.getWidth()).withEndAction(new OnButtonClickPrimaryAnimationFinished());
            imgBtnScanConn.animate().setDuration(500).translationX(imgBtnScanConn.getWidth());
        } else {
            imgBtnScanConn.animate().setDuration(500).translationX(-imgBtnScanConn.getWidth()).withEndAction(new OnButtonClickPrimaryAnimationFinished());
            imgBtnInitConn.animate().setDuration(500).translationX(-imgBtnInitConn.getWidth());
        }
    }

    private class OnButtonClickPrimaryAnimationFinished implements Runnable{
        @Override
        public void run() {
            buttonClickTransitionFinished();
        }
    }

    private void buttonClickTransitionFinished() {
        buttonLayout.animate().alpha(0).translationY(buttonLayout.getHeight()).setDuration(500);
        pBarLoadingConns.animate().alpha(1).translationY(buttonLayout.getHeight()).setDuration(500);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(Action.CREATE.equals(selectedAction)) {
            ConnCreationServerService.stop(this);
        } else if(Action.JOIN.equals(selectedAction)){
            UpdateEvent closeEvent = new UpdateEvent();
            closeEvent.source = this.getClass();
            closeEvent.data.put(Constants.ACTION, Constants.CLOSE_SOCKET);
            for(NewConnCreationClient client : clientList){
                client.update(closeEvent);
            }
        }
        appManager.stopAllWifiOps();
        appManager.notificationUtil.showToggleReceivingNotification();
        finish();
    }

    @Override
    protected void onResume(){
        super.onResume();
        appManager.notificationUtil.removeToggleNotification();
    }

    @Override
    public void update(UpdateEvent updateEvent) {
        if(NewConnCreationServer.class.equals(updateEvent.source)){
            Toast.makeText(this, "Server started",Toast.LENGTH_SHORT).show();
        } else if(NewConnCreationClientConnector.class.equals(updateEvent.source)){
            if(Constants.ACCEPT_CONN.equals(updateEvent.data.get(Constants.ACTION))) {
                String username = (String) updateEvent.data.get(SharedPrefConstants.USER_NAME);
                if (usernameToConnSender.containsKey(username)) {
                    transBgLayout.setVisibility(View.GONE);
                    //A new connection request arrived. add it to the ui
                    usernameToConnSender.put(username, (NewConnCreationClientConnector) updateEvent.data.get(NewConnCreationClientConnector.UPDATE_CONST_SENDER));
                    ConnectionViewData newConn = new ConnectionViewData();
                    newConn.profileName = username;
                    newConn.uniqueId = (String) updateEvent.data.get(SharedPrefConstants.DEVICE_ID);
                    connectionViewDatas.add(newConn);
                    //update the UI
                    connectorAdapter.setData(connectionViewDatas);
                    availableConns.setAdapter(connectorAdapter);
                }
            } else if(Constants.SUCCESS.equals(updateEvent.data.get(Constants.ACTION))){
                goHome();
            }

        } else if(ConnectorAdapter.class.equals(updateEvent.source)){
            ConnectionViewData conn = (ConnectionViewData) updateEvent.data.get(ConnectorAdapter.UPDATE_CONST_SELECTED_CONN);
            if(Action.CREATE.equals(selectedAction)){
                NewConnCreationClientConnector sender = usernameToConnSender.get(conn.profileName);
                if(sender != null){
                    UpdateEvent event = new UpdateEvent();
                    event.source = this.getClass();
                    event.data.put(Constants.ACTION, Constants.ACCEPT_CONN);
                    sender.update(updateEvent);
                    transBgLayout.setVisibility(View.VISIBLE);
                }
            } else if(Action.JOIN.equals(selectedAction)){
                Map<String, String> connData = userNameToConnectionData.get(conn.profileName);
                if(connData != null) {
                    String SSID = connData.get(Constants.ADV_KEY_NETWORK_NAME);
                    String pass = connData.get(Constants.ADV_KEY_NETWORK_PASSPHRASE);
                    String portString = connData.get(Constants.ADV_KEY_SERVER_PORT);
                    int port = -1;
                    if(portString != null && !portString.isEmpty()){
                        try{
                            port = Integer.valueOf(portString);
                        }catch (Exception e){
                        }
                    }
                    NewConnCreationClient client = new NewConnCreationClient(SSID, pass, port, this, appManager);
                    clientList.add(client);
                    transBgLayout.setVisibility(View.VISIBLE);
                    ConnCreationClientService.start(this, client);
                }
            }
        } else if(NewConnCreationClient.class.equals(updateEvent.source)){
            if(Constants.SUCCESS.equals(updateEvent.data.get(Constants.ACTION))){
                goHome();
            } else if(Constants.FAILED.equals(updateEvent.data.get(Constants.ACTION))){
                connectionViewDatas.clear();
                connectorAdapter.setData(connectionViewDatas);
                availableConns.setAdapter(connectorAdapter);
                transBgLayout.setVisibility(View.GONE);
            }
        }
    }

    public void startP2pServiceDiscovery(AppManager appManager){
        SharedPrefUtil sharedPrefUtil = appManager.sharedPrefUtil;

        if(sharedPrefUtil.getCurrentAppStatus() == SharedPrefConstants.CURR_STATUS_IDLE) {
            sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_RECEIVABLE);
            sharedPrefUtil.commit();
            final WifiP2pManager.DnsSdTxtRecordListener txtRecordListener = new WifiP2pManager.DnsSdTxtRecordListener() {
                @Override
                public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
                if(Constants.P2P_SERVICE_FULL_DOMAIN_NAME.equals(fullDomainName)) {
                    if(Constants.ADV_VALUE_PURPOSE_CONNECTION_CREATION.equals(txtRecordMap.get(Constants.ADV_KEY_GROUP_PURPOSE))){
                        transBgLayout.setVisibility(View.GONE);
                        String userName = txtRecordMap.get(Constants.ADV_KEY_USERNAME);
                        if(!userNameToConnectionData.containsKey(userName)) {
                            userNameToConnectionData.put(userName, txtRecordMap);
                            ConnectionViewData conn = new ConnectionViewData();
                            conn.profileName = userName;
                            connectionViewDatas.add(conn);
                            connectorAdapter.setData(connectionViewDatas);
                            availableConns.setAdapter(connectorAdapter);
                        }
                    }
                }
                }
            };

            WifiP2pManager.DnsSdServiceResponseListener serviceResponseListener = new WifiP2pManager.DnsSdServiceResponseListener() {
                @Override
                public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                }
            };

            appManager.startP2pServiceDiscovery(txtRecordListener, serviceResponseListener);
        } else {
            Toast.makeText(this, "Wait for current operation to finish", Toast.LENGTH_SHORT).show();
        }
    }

    private void goHome(){
        if(Action.CREATE.equals(selectedAction)) {
            ConnCreationServerService.stop(this);
        } else if(Action.JOIN.equals(selectedAction)){
            UpdateEvent closeEvent = new UpdateEvent();
            closeEvent.source = this.getClass();
            closeEvent.data.put(Constants.ACTION, Constants.CLOSE_SOCKET);
            for(NewConnCreationClient client : clientList){
                client.update(closeEvent);
            }
        }
        appManager.stopAllWifiOps();
        appManager.notificationUtil.showToggleReceivingNotification();
        finish();
    }

    private enum Action{
        CREATE, JOIN
    }
}
