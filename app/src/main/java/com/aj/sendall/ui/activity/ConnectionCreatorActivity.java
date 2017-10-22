package com.aj.sendall.ui.activity;

import android.content.Intent;
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
import com.aj.sendall.application.ThisApplication;
import com.aj.sendall.events.EventRouter;
import com.aj.sendall.events.EventRouterFactory;
import com.aj.sendall.events.event.NewClientAvailable;
import com.aj.sendall.events.event.NewConnCreationFinished;
import com.aj.sendall.events.event.NewConnSelected;
import com.aj.sendall.events.event.SendallNetsAvailable;
import com.aj.sendall.controller.AppConsts;
import com.aj.sendall.controller.AppController;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.ui.adapter.ConnectorAdapter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class ConnectionCreatorActivity extends AppCompatActivity{
    private RecyclerView availableConns;
    private LinearLayout transBgLayout;
    private ProgressBar pBarLoadingConns;
    private LinearLayout buttonLayout;
    private ImageView imgBtnInitConn;
    private ImageView imgBtnScanConn;
    private Map<String, Acceptable> usernameToClientCommunicator;
    private EventRouter eventRouter = EventRouterFactory.getInstance();
    private Action selectedAction;
    private UpdateUI updateUI;
    @Inject
    AppController appController;

    private ConnectorAdapter connectorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connector);
        ((ThisApplication)getApplication()).getDaggerInjector().inject(this);

        usernameToClientCommunicator = new HashMap<>();

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

        updateUI = new UpdateUI(new Handler());
    }

    private void setListeners(){
        imgBtnInitConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appController.startNewConnCreationServer();
                selectedAction = Action.CREATE;
                animateViewsOnButtonClicked(v);
            }
        });

        imgBtnScanConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appController.scanForConnCreationServer();
                selectedAction = Action.JOIN;
                animateViewsOnButtonClicked(v);
            }
        });
        subscribeEvents();
    }

    public void subscribeEvents() {
        eventRouter.subscribe(NewConnSelected.class, new EventRouter.Receiver<NewConnSelected>() {
            @Override
            public void receive(NewConnSelected event) {
                if(Action.CREATE.equals(selectedAction)){
                    Acceptable clientConn = usernameToClientCommunicator.get(event.selectedConn.profileName);
                    if(clientConn != null){
                        clientConn.accept();
                    }
                    transBgLayout.setVisibility(View.VISIBLE);
                } else if(Action.JOIN.equals(selectedAction)){
                    transBgLayout.setVisibility(View.VISIBLE);
                    appController.connectToConnCreationServer(event.selectedConn.uniqueId);
                }
                eventRouter.unsubscribe(NewConnSelected.class, this);
            }
        });
        eventRouter.subscribe(NewConnCreationFinished.class, new EventRouter.Receiver<NewConnCreationFinished>() {
            @Override
            public void receive(NewConnCreationFinished event) {
                if(AppConsts.SUCCESS.equals(event.status)){
                    goHome();
                } else if(AppConsts.FAILED.equals(event.status)){
                    Log.i(this.getClass().getSimpleName(), "Socket connection failed");
                }
                eventRouter.unsubscribe(NewConnCreationFinished.class, this);
            }
        });
        eventRouter.subscribe(SendallNetsAvailable.class, new EventRouter.Receiver<SendallNetsAvailable>() {
            @Override
            public void receive(SendallNetsAvailable event) {
                List<String> SSIDs = event.availableSSIDs;
                if (SSIDs != null) {
                    for (String scanResult : SSIDs) {
                        updateUI.addNew(scanResult);
                    }
                }
            }
        });
        eventRouter.subscribe(NewClientAvailable.class, new EventRouter.Receiver<NewClientAvailable>() {
            @Override
            public void receive(NewClientAvailable event) {
                //A new connection request arrived. add it to the ui
                usernameToClientCommunicator.put(event.username, event.acceptable);
                updateUI.addNew(event.username, event.deviceId);
            }
        });
    }

    private void unsubscribeEvents(){
        eventRouter.clearListeners(SendallNetsAvailable.class);
        eventRouter.clearListeners(NewClientAvailable.class);
        eventRouter.clearListeners(NewConnSelected.class);
        eventRouter.clearListeners(NewConnCreationFinished.class);
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
        appController.setSystemIdle();
        unsubscribeEvents();
        finish();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    private void goHome(){
        Intent home = new Intent(this, HomeActivity.class);
        startActivity(home);
    }

    private enum Action{
        CREATE, JOIN
    }

    /*The server and client are runUpdate on a separeate thread. That thread cannot update ui. So
    * update the ui with this runnable*/
    private class UpdateUI{
        private final LinkedList<ConnectionViewData> connectionViewDatas;
        private Handler handler;

        private UpdateUI(Handler handler){
            this.handler = handler;
            connectionViewDatas = new LinkedList<>();
            connectorAdapter.setData(connectionViewDatas);
        }

        private void runUpdate(){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (connectionViewDatas) {
                        transBgLayout.setVisibility(View.VISIBLE);

                        connectorAdapter.setData(connectionViewDatas);
                        availableConns.setAdapter(connectorAdapter);
                        if (!connectionViewDatas.isEmpty()) {
                            transBgLayout.setVisibility(View.GONE);
                        }
                    }
                }
            });
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
                runUpdate();
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
                runUpdate();
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

    public interface Acceptable{
        void accept();
    }
}