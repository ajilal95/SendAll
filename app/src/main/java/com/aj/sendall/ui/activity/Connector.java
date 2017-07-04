package com.aj.sendall.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.aj.sendall.R;
import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.network.utils.LocalWifiManager;
import com.aj.sendall.ui.adapter.ConnectorAdapter;

import java.util.LinkedList;

import javax.inject.Inject;

public class Connector extends AppCompatActivity {
    private RecyclerView availableConns;
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
    }

    private void findViews(){
        availableConns = (RecyclerView) findViewById(R.id.recyclr_vw_available_conns);
    }

    private void initViews(){
        availableConns.setLayoutManager(new LinearLayoutManager(this));
        connectorAdapter = new ConnectorAdapter(this, new ClickListenerForConnectorItem());
        availableConns.setAdapter(connectorAdapter);
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
