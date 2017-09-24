package com.aj.sendall.ui.activity;

import android.content.Intent;
import android.net.wifi.ScanResult;
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
import com.aj.sendall.network.broadcastreceiver.SendallNetWifiScanBroadcastReceiver;
import com.aj.sendall.network.runnable.NewConnCreationClient;
import com.aj.sendall.network.runnable.NewConnCreationClientConnector;
import com.aj.sendall.network.services.NewConnCreationClientService;
import com.aj.sendall.network.services.NewConnCreationServerService;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.adapter.ConnectorAdapter;
import com.aj.sendall.network.monitor.Updatable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class ConnectionCreatorActivity extends AppCompatActivity implements Updatable {
    private RecyclerView availableConns;
    private LinearLayout transBgLayout;
    private ProgressBar pBarLoadingConns;
    private LinearLayout buttonLayout;
    private ImageView imgBtnInitConn;
    private ImageView imgBtnScanConn;
    private Map<String, NewConnCreationClientConnector> usernameToConnSender;
    private Action selectedAction;
    private UpdateUI updateUI;
    @Inject
    AppManager appManager;

    private ConnectorAdapter connectorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connector);
        ((AndroidApplication)getApplication()).getDaggerInjector().inject(this);

        usernameToConnSender = new HashMap<>();

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

                selectedAction = Action.CREATE;
                animateViewsOnButtonClicked(v);
                NewConnCreationServerService.start(ConnectionCreatorActivity.this, ConnectionCreatorActivity.this);
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

                appManager.startScanningWifi(ConnectionCreatorActivity.this, SharedPrefConstants.CURR_STATUS_CEATING_CONNECTION);
                selectedAction = Action.JOIN;
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

        if(updateUI != null) {
            updateUI.setInactive();
        }
        appManager.stopHotspotAndScanning();
        finish();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    public void update(UpdateEvent updateEvent) {
        if(NewConnCreationClientConnector.class.equals(updateEvent.source)){
            if(Constants.ACCEPT_CONN.equals(updateEvent.action)) {
                String username = (String) updateEvent.getExtra(SharedPrefConstants.USER_NAME);
                //A new connection request arrived. add it to the ui
                usernameToConnSender.put(username, (NewConnCreationClientConnector) updateEvent.getExtra(NewConnCreationClientConnector.UPDATE_CONST_SENDER));
                updateUI.addNew(username, (String) updateEvent.getExtra(SharedPrefConstants.DEVICE_ID));
            } else if(Constants.SUCCESS.equals(updateEvent.action)){
                goHome();
            }

        } else if(ConnectorAdapter.class.equals(updateEvent.source)){
            ConnectionViewData conn = (ConnectionViewData) updateEvent.getExtra(ConnectorAdapter.UPDATE_CONST_SELECTED_CONN);
            if(Action.CREATE.equals(selectedAction)){
                NewConnCreationClientConnector clientConn = usernameToConnSender.get(conn.profileName);
                if(clientConn != null){
                    UpdateEvent event = new UpdateEvent();
                    event.source = this.getClass();
                    event.action = Constants.ACCEPT_CONN;
                    clientConn.update(event);
                }
                transBgLayout.setVisibility(View.VISIBLE);
            } else if(Action.JOIN.equals(selectedAction)){
                appManager.stopWifiScanning();
                String SSID = conn.uniqueId;
                String pass = appManager.getDefaultWifiPass();
//                int port = appManager.sharedPrefUtil.getDefServerPort();
                int port = Integer.valueOf(SSID.split("_")[1]);
                NewConnCreationClient client = new NewConnCreationClient(SSID, pass, port, this, appManager);
                transBgLayout.setVisibility(View.VISIBLE);
                NewConnCreationClientService.start(this, client);
            }
        } else if(NewConnCreationClient.class.equals(updateEvent.source)){
            if(Constants.SUCCESS.equals(updateEvent.action)){
                goHome();
            } else if(Constants.FAILED.equals(updateEvent.action)){
                Log.i(this.getClass().getSimpleName(), "Socket connection failed");
            }
        } else if(SendallNetWifiScanBroadcastReceiver.class.equals(updateEvent.source)){
            @SuppressWarnings({"unchecked"}) List<ScanResult> scanResults = (List<ScanResult>) updateEvent.getExtra(SendallNetWifiScanBroadcastReceiver.UPDATE_EXTRA_RESULT);
            if(scanResults != null){
                for(ScanResult scanResult : scanResults){
                    updateUI.addNew(scanResult.SSID);
                }
            }
        }
    }

    private void goHome(){
        Intent home = new Intent(this, HomeActivity.class);
        startActivity(home);
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
                    new Handler().postDelayed(this, 2000);
                }
            }
        }

        private void setInactive(){
            active = false;

        }

        //For JOIN Action, SSID must be containing the username separated by an '_'
        private void addNew(String SSID){
            synchronized (connectionViewDatas){
                String[] parts = SSID.split("_");
                ConnectionViewData conn = new ConnectionViewData();
                conn.profileName = parts[2];
                conn.uniqueId = SSID;

                int posToIns = getIndex(SSID);

                if(posToIns == -1){
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

                if(posToIns == -1){
                    connectionViewDatas.add(conn);
                } else {
                    connectionViewDatas.remove(posToIns);
                    connectionViewDatas.add(posToIns, conn);
                }
                changed = true;
            }
        }

        private int getIndex(String ssid){
            int matchIndex = -1;
            boolean itemExists = false;
            for(ConnectionViewData cvd : connectionViewDatas){
                matchIndex++;

                if(cvd.uniqueId.equals(ssid)){
                    itemExists = true;
                    break;
                }
            }
            if(itemExists){
                return matchIndex;
            } else {
                return -1;
            }
        }
    }
}