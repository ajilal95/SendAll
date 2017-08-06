package com.aj.sendall.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.aj.sendall.R;
import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
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
//    private Map<String, Map<String, String>> userNameToConnectionData;
    private List<NewConnCreationClient> clientList;
    private Action selectedAction;
    private UpdateUI updateUI;
    private WifiScannerRunnable wifiScannerRunnable;
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
//        userNameToConnectionData = new HashMap<>();
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
                if(updateUI != null){
                    updateUI.setInactive();
                }
                updateUI = new UpdateUI();

                new Handler().postDelayed(updateUI, 2000);

               // NewConnCreationGrpCreatnLstnr listener = new NewConnCreationGrpCreatnLstnr(appManager, Connector.this);
                appManager.initConnection(SharedPrefConstants.CURR_STATUS_CEATING_CONNECTION);
                selectedAction = Action.CREATE;
                animateViewsOnButtonClicked(v);
                NewConnCreationServerService.start(Connector.this, Connector.this);
            }
        });

        imgBtnScanConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(updateUI != null){
                    updateUI.setInactive();
                }
                updateUI = new UpdateUI();

                new Handler().postDelayed(updateUI, 2000);

                if(wifiScannerRunnable != null){
                    wifiScannerRunnable.setInactive();
                }

                wifiScannerRunnable = new WifiScannerRunnable();

                new Handler().post(wifiScannerRunnable);

                selectedAction = Action.JOIN;
//                startP2pServiceDiscovery(appManager);
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
            NewConnCreationClientService.stop(this);
        }

        if(updateUI != null){
            updateUI.setInactive();
        }

        if(wifiScannerRunnable != null){
            wifiScannerRunnable.setInactive();
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
//                appManager.showShortToast(this, "Connect now..");
            }
        } else if(NewConnCreationClientConnector.class.equals(updateEvent.source)){
            if(Constants.ACCEPT_CONN.equals(updateEvent.data.get(Constants.ACTION))) {
                String username = (String) updateEvent.data.get(SharedPrefConstants.USER_NAME);
                //A new connection request arrived. add it to the ui
                usernameToConnSender.put(username, (NewConnCreationClientConnector) updateEvent.data.get(NewConnCreationClientConnector.UPDATE_CONST_SENDER));
                updateUI.addNew(username, (String) updateEvent.data.get(SharedPrefConstants.DEVICE_ID));
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
//                Map<String, String> connData = userNameToConnectionData.get(conn.profileName);
//                if(connData != null) {
                    /*String SSID = connData.get(Constants.ADV_KEY_NETWORK_NAME);
                    String pass = connData.get(Constants.ADV_KEY_NETWORK_PASSPHRASE);
                    String portString = connData.get(Constants.ADV_KEY_SERVER_PORT);
                    int port = -1;*/
                    /*if(portString != null && !portString.isEmpty()){
                        try{
                            port = Integer.valueOf(portString);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }*/
                String SSID = conn.uniqueId;
                String pass = appManager.sharedPrefUtil.getDefaultWifiPass();
                int port = appManager.sharedPrefUtil.getDefServerPort();
                NewConnCreationClient client = new NewConnCreationClient(SSID, pass, port, this, appManager);
                clientList.add(client);
                transBgLayout.setVisibility(View.VISIBLE);
                NewConnCreationClientService.start(this, client);
//                }
            }
        } else if(NewConnCreationClient.class.equals(updateEvent.source)){
            if(Constants.SUCCESS.equals(updateEvent.data.get(Constants.ACTION))){
                goHome();
            } else if(Constants.FAILED.equals(updateEvent.data.get(Constants.ACTION))){
                Log.i(this.getClass().getSimpleName(), "Socket connection failed");
            }
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

    /*The server and client are run on a separeate thread. That thread cannot update ui. So
    * update the ui with this runnable*/
    private class UpdateUI implements Runnable{
        private boolean active = true;
        private final LinkedList<ConnectionViewData> connectionViewDatas;
        private boolean changed = false;

        private UpdateUI(){
            connectionViewDatas = new LinkedList<>();
            connectorAdapter.setData(connectionViewDatas);
        }

        public void run(){
            synchronized (connectionViewDatas) {
                if(active){
                    if(changed) {
                        transBgLayout.setVisibility(View.VISIBLE);

                        connectorAdapter.setData(connectionViewDatas);
                        availableConns.setAdapter(connectorAdapter);
                        if (!connectionViewDatas.isEmpty()) {
                            transBgLayout.setVisibility(View.GONE);
                        }
                        changed = false;
                    }
                    new Handler().postDelayed(this, 5000);
                }
            }
        }

        private void setInactive(){
            active = false;

        }

        //For JOIN Action, SSID must be containing the username separated by an '_'
        private void addNew(String SSID){
            synchronized (connectionViewDatas){
                int sepIndex = SSID.indexOf('_');
                ConnectionViewData conn = new ConnectionViewData();
                conn.profileName = SSID.substring(sepIndex + 1, SSID.length());
                conn.uniqueId = SSID;

                int posToIns = getIndex(SSID);

                if((connectionViewDatas.size() - 1) == posToIns){
                    connectionViewDatas.add(conn);
                } else {
                    connectionViewDatas.remove(posToIns);
                    connectionViewDatas.add(posToIns, conn);
                }
                changed = true;
            }
        }

        private void addNew(String username, String SSID){
            synchronized (connectionViewDatas){
                ConnectionViewData conn = new ConnectionViewData();
                conn.profileName = username;
                conn.uniqueId = SSID;

                int posToIns = getIndex(SSID);

                if((connectionViewDatas.size() - 1) == posToIns){
                    connectionViewDatas.add(conn);
                } else {
                    connectionViewDatas.remove(posToIns);
                    connectionViewDatas.add(posToIns, conn);
                }
                changed = true;
            }
        }
        private void remove(String SSID){
            synchronized (connectionViewDatas){
                int posToDelete = getIndex(SSID);
                if(posToDelete != -1){
                    connectionViewDatas.remove(posToDelete);
                    changed = true;
                }
            }
        }

        /*private void clear(){
            synchronized (connectionViewDatas){
                connectionViewDatas.clear();
            }
        }*/

        private int getIndex(String ssid){
            int matchIndex = -1;
            for(ConnectionViewData cvd : connectionViewDatas){
                matchIndex++;

                if(cvd.uniqueId.equals(ssid)){
                    break;
                }
            }
            return matchIndex;
        }
    }

    class WifiScannerRunnable implements Runnable {
        Map<String, String> currentSsidToPass = new HashMap<>();
        private final Object syncObj = new Object();
        private boolean active = true;
        @Override
        public void run() {
            synchronized (syncObj) {
                if(active) {
                    appManager.enableWifi(true);
                    Map<String, String> tempSsidToPass = appManager.getAllActiveSendallNets();
                    for (Map.Entry<String, String> ssidToPassEntry : tempSsidToPass.entrySet()) {
                        String newSSID = ssidToPassEntry.getKey();
                        if (!currentSsidToPass.containsKey(newSSID)) {
                            if(updateUI != null) {
                                updateUI.addNew(newSSID);
                            }
                            currentSsidToPass.remove(newSSID);//removing to get the non existing networks in the next for loop
                        }
                    }

                    if(updateUI != null) {
                        for (Map.Entry<String, String> ssidToPassEntry : tempSsidToPass.entrySet()) {
                            updateUI.remove(ssidToPassEntry.getKey());
                        }
                    }

                    currentSsidToPass = tempSsidToPass;

                    new Handler().postDelayed(this, 5000);
                }
            }
        }

        private void setInactive(){
            synchronized (syncObj){
                appManager.enableWifi(false);
                active = false;
            }
        }
    };


}
