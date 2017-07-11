package com.aj.sendall.ui.activity;

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
import com.aj.sendall.network.runnable.NewConnCreationSender;
import com.aj.sendall.network.runnable.NewConnCreationServer;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.adapter.ConnectorAdapter;
import com.aj.sendall.ui.interfaces.Updatable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.inject.Inject;

public class Connector extends AppCompatActivity implements Updatable {
    private RecyclerView availableConns;
    private LinearLayout transBgLayout;
    private ProgressBar pBarLoadingConns;
    private LinearLayout buttonLayout;
    private ImageView imgBtnInitConn;
    private ImageView imgBtnScanConn;
    private Updatable connCreatorServer;
    private Map<String, UpdateEvent> usernameToUpdateEvent = new HashMap<>();
    private Action selectedAction;
    @Inject
    AppManager appManager;

    private ConnectorAdapter connectorAdapter;
    private LinkedList<ConnectionViewData> connectionViewDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connector);
        ((AndroidApplication)getApplication()).getDaggerInjector().inject(this);
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
                if(appManager.createGroupAndAdvertise(listener, SharedPrefConstants.CURR_STATUS_CEATING_CONNECTION)){
                    selectedAction = Action.CREATE;
                    animateViewsOnButtonClicked(v);
                }
            }
        });

        imgBtnScanConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        UpdateEvent closeEvent = new UpdateEvent();
        closeEvent.source = this.getClass();
        closeEvent.data.put(Constants.ACTION, Constants.CLOSE_SOCKET);

        //Close the server
        connCreatorServer.update(closeEvent);

        //Close all open Sockets
        for(UpdateEvent eventFromSender : usernameToUpdateEvent.values()){
            Updatable sender = (Updatable) eventFromSender.data.get(NewConnCreationSender.UPDATE_CONST_SENDER);
            if(sender != null){
                sender.update(closeEvent);
            }
        }

        finish();
    }

    @Override
    public void update(UpdateEvent updateEvent) {
        if(NewConnCreationServer.class.equals(updateEvent.source)){
            connCreatorServer = (Updatable) updateEvent.data.get(NewConnCreationServer.UPDATE_CONST_SERVER);
        } else if(NewConnCreationSender.class.equals(updateEvent.source)){
            String username = (String) updateEvent.data.get(SharedPrefConstants.USER_NAME);
            usernameToUpdateEvent.put(username, updateEvent);
        } else if(ConnectorAdapter.class.equals(updateEvent.source)){
            String selectedConnection = (String) updateEvent.data.get(ConnectorAdapter.UPDATE_CONST_SELECTED_USERNAME);
        }
    }

    private enum Action{
        CREATE, JOIN
    }
}
