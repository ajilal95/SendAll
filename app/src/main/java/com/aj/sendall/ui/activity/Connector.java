package com.aj.sendall.ui.activity;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

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
import com.aj.sendall.network.services.NewConnCreationClientService;
import com.aj.sendall.network.services.NewConnCreationServerService;
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
    private UpdateUIForAction updateUIForAction;
    @Inject
    AppManager appManager;

    private ConnectorAdapter connectorAdapter;

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
                if(updateUIForAction != null){
                    updateUIForAction.setInactive();
                }
                updateUIForAction = new UpdateUIForAction();

                new Handler().postDelayed(updateUIForAction, 2000);

                NewConnCreationGrpCreatnLstnr listener = new NewConnCreationGrpCreatnLstnr(appManager, Connector.this);
                appManager.createGroupAndAdvertise(listener, SharedPrefConstants.CURR_STATUS_CEATING_CONNECTION);
                selectedAction = Action.CREATE;
                animateViewsOnButtonClicked(v);
            }
        });

        imgBtnScanConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(updateUIForAction != null){
                    updateUIForAction.setInactive();
                }
                updateUIForAction = new UpdateUIForAction();

                new Handler().postDelayed(updateUIForAction, 2000);

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
            NewConnCreationServerService.stop(this);
        } else if(Action.JOIN.equals(selectedAction)){
            UpdateEvent closeEvent = new UpdateEvent();
            closeEvent.source = this.getClass();
            closeEvent.data.put(Constants.ACTION, Constants.CLOSE_SOCKET);
            for(NewConnCreationClient client : clientList){
                client.update(closeEvent);
            }
        }

        if(updateUIForAction != null){
            updateUIForAction.setInactive();
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
            if(Constants.ACCEPT_CONN.equals(updateEvent.data.get(Constants.ACTION))) {
                appManager.showShortToast(this, "Connect now..");
            }
        } else if(NewConnCreationClientConnector.class.equals(updateEvent.source)){
            if(Constants.ACCEPT_CONN.equals(updateEvent.data.get(Constants.ACTION))) {
                String username = (String) updateEvent.data.get(SharedPrefConstants.USER_NAME);
                //A new connection request arrived. add it to the ui
                usernameToConnSender.put(username, (NewConnCreationClientConnector) updateEvent.data.get(NewConnCreationClientConnector.UPDATE_CONST_SENDER));
                ConnectionViewData newConn = new ConnectionViewData();
                newConn.profileName = username;
                newConn.uniqueId = (String) updateEvent.data.get(SharedPrefConstants.DEVICE_ID);
                updateUIForAction.addNew(newConn);
            } else if(Constants.SUCCESS.equals(updateEvent.data.get(Constants.ACTION))){
                goHome();
            }

        } else if(ConnectorAdapter.class.equals(updateEvent.source)){
            ConnectionViewData conn = (ConnectionViewData) updateEvent.data.get(ConnectorAdapter.UPDATE_CONST_SELECTED_CONN);
            if(Action.CREATE.equals(selectedAction)){
                appManager.stopP2pServiceAdv();
                NewConnCreationClientConnector clientConn = usernameToConnSender.get(conn.profileName);
                if(clientConn != null){
                    UpdateEvent event = new UpdateEvent();
                    event.source = this.getClass();
                    event.data.put(Constants.ACTION, Constants.ACCEPT_CONN);
                    clientConn.update(event);
                }
            } else if(Action.JOIN.equals(selectedAction)){
                appManager.stopP2pServiceDiscovery();
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
                            e.printStackTrace();
                        }
                    }
                    NewConnCreationClient client = new NewConnCreationClient(SSID, pass, port, this, appManager);
                    clientList.add(client);
                    transBgLayout.setVisibility(View.VISIBLE);
                    NewConnCreationClientService.start(this, client);
                }
            }
        } else if(NewConnCreationClient.class.equals(updateEvent.source)){
            if(Constants.SUCCESS.equals(updateEvent.data.get(Constants.ACTION))){
                goHome();
            } else if(Constants.FAILED.equals(updateEvent.data.get(Constants.ACTION))){
                updateUIForAction.clear();
                availableConns.setAdapter(connectorAdapter);
                transBgLayout.setVisibility(View.GONE);
                appManager.showShortToast(this, "Connection failed. Please try again..");
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
                        String newUserName = txtRecordMap.get(Constants.ADV_KEY_USERNAME);
                        String newPort = txtRecordMap.get(Constants.ADV_KEY_SERVER_PORT);
                        if(newUserName != null && newPort != null) {
                            String oldPort = null;
                            Map<String, String> existingRecord = userNameToConnectionData.get(newUserName);
                            if (existingRecord != null) {
                                oldPort = existingRecord.get(Constants.ADV_KEY_SERVER_PORT);
                            }

                            if (oldPort == null || !newPort.equals(oldPort)) {
                                userNameToConnectionData.put(newUserName, txtRecordMap);
                                ConnectionViewData newConn = new ConnectionViewData();
                                newConn.profileName = newUserName;
                                updateUIForAction.addNew(newConn);
                            }
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
            appManager.showShortToast(this, "Wait for current operation to finish");
        }
    }

    private void goHome(){
        if(Action.CREATE.equals(selectedAction)) {
            NewConnCreationServerService.stop(this);
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

    /*The server is run on a separeate thread. That thread cannot update ui. So
    * update the ui with this runnable*/
    private class UpdateUIForAction implements Runnable{
        private boolean active = true;
        private final LinkedList<ConnectionViewData> connectionViewDatas;
        private boolean changed = false;

        public UpdateUIForAction(){
            connectionViewDatas = new LinkedList<>();
            connectorAdapter.setData(connectionViewDatas);
        }

        public void run(){
            if(active){
                synchronized (connectionViewDatas) {
                    if(changed) {
                        transBgLayout.setVisibility(View.VISIBLE);

                        connectorAdapter.setData(connectionViewDatas);
                        availableConns.setAdapter(connectorAdapter);
                        if (!connectionViewDatas.isEmpty()) {
                            transBgLayout.setVisibility(View.GONE);
                        }
                        changed = false;
                    }
                    new Handler().postDelayed(this, 4000);
                }
            }
        }

        private void setInactive(){
            active = false;

        }

        public void addNew(ConnectionViewData conn){
            synchronized (connectionViewDatas){
                int posToIns = getIndex(conn);
                if((connectionViewDatas.size() - 1) == posToIns){
                    connectionViewDatas.add(conn);
                    changed = true;
                } else {
                    connectionViewDatas.remove(posToIns);
                    connectionViewDatas.add(posToIns, conn);
                }
                changed = true;
            }
        }

        public void remove(ConnectionViewData conn){
            synchronized (connectionViewDatas){
                connectionViewDatas.remove(conn);
            }
        }

        public void clear(){
            synchronized (connectionViewDatas){
                connectionViewDatas.clear();
            }
        }

        private int getIndex(ConnectionViewData conn){
            int matchIndex = -1;
            for(ConnectionViewData cvd : connectionViewDatas){
                matchIndex++;

                if(cvd.profileName.equals(conn.profileName)){
                    break;
                }
            }
            return matchIndex;
        }
    }
}
