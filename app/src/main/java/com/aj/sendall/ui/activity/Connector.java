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
import com.aj.sendall.network.utils.LocalWifiManager;
import com.aj.sendall.ui.adapter.ConnectorAdapter;

import java.util.LinkedList;

import javax.inject.Inject;

public class Connector extends AppCompatActivity {
    private RecyclerView availableConns;
    private LinearLayout transBgLayout;
    private ProgressBar pBarLoadingConns;
    private LinearLayout buttonLayout;
    private ImageView imgBtnInitConn;
    private ImageView imgBtnScanConn;
    @Inject
    LocalWifiManager localWifiManager;

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
        connectorAdapter = new ConnectorAdapter(this, new ClickListenerForConnectorItem());
        availableConns.setAdapter(connectorAdapter);

        pBarLoadingConns.setAlpha(0);
    }

    private void setListeners(){
        imgBtnInitConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateViewsOnButtonClicked(v);
            }
        });

        imgBtnScanConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        finish();
    }

    private class ClickListenerForConnectorItem implements View.OnClickListener{
        @Override
        public void onClick(View v) {

        }
    }
}
