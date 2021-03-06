package com.aj.sendall.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.aj.sendall.R;
import com.aj.sendall.application.ThisApplication;
import com.aj.sendall.controller.AppConsts;
import com.aj.sendall.controller.AppController;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.dto.FileInfoDTO;
import com.aj.sendall.db.model.Connections;
import com.aj.sendall.db.model.PersonalInteraction;
import com.aj.sendall.streams.FileUtil;
import com.aj.sendall.ui.adapter.PersonalInteractionsAdapter;
import com.aj.sendall.ui.utils.FileTransferUIUtil;
import com.aj.sendall.ui.utils.PersonalInteractionsUtil;
import com.aj.sendall.ui.interfaces.ItemSelectableView;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

public class PersonalInteractionsActivity extends AppCompatActivity implements ItemSelectableView{
    @Inject
    public PersonalInteractionsUtil personalInteractionsUtil;
    @Inject
    public FileTransferUIUtil fileTransferUIUtil;
    @Inject
    public AppController appController;
    private FloatingActionButton fltActionButtonSend;
    private RecyclerView recyclrVwPersInteractions;
    private PersonalInteractionsAdapter personalInteractionsAdapter;

    private long userDBId;

    private int numberOfSelecedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((ThisApplication)getApplication()).getDaggerInjector().inject(this);
        setContentView(R.layout.activity_pesonal_interaction_view);
        userDBId = getIntent().getLongExtra(AppConsts.INTENT_EXTRA_KEY_1, 0);
        String userName = getIntent().getStringExtra(AppConsts.INTENT_EXTRA_KEY_2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(userName);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        this.numberOfSelecedItems = 0;

        findViews();
        initViews();
    }

    private void findViews(){
        fltActionButtonSend = (FloatingActionButton) findViewById(R.id.flt_btn_send_personally);
        recyclrVwPersInteractions = (RecyclerView) findViewById(R.id.recycl_vw_personal_inter);
    }

    private void initViews(){
        initSendButton();
        initPersonalInteractionView();
    }

    private void initSendButton(){
        if(numberOfSelecedItems > 0) {
            setSendButtonClickListener(SendButtonAction.FORWARD);
            setSendButtonImage(SendButtonAction.FORWARD);
        } else {
            setSendButtonClickListener(SendButtonAction.SEND);
            setSendButtonImage(SendButtonAction.SEND);
        }
    }

    private void initPersonalInteractionView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        recyclrVwPersInteractions.setLayoutManager(layoutManager);
        personalInteractionsAdapter = new PersonalInteractionsAdapter(userDBId, this, this, personalInteractionsUtil, new Handler(getMainLooper()));
        recyclrVwPersInteractions.setAdapter(personalInteractionsAdapter);
    }

    private void setSendButtonClickListener(SendButtonAction action) {
        switch (action){
            case SEND:
                fltActionButtonSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fileTransferUIUtil.clean();//clean the existing file transfers
                        ConnectionViewData cvd = appController.getConnectionViewData(userDBId);
                        Set<ConnectionViewData> receivers = new HashSet<>();
                        receivers.add(cvd);
                        if(!FileTransferUIUtil.SendOperationResult.SENDING.equals(fileTransferUIUtil.send_to(receivers))){
                            Intent i = new Intent(PersonalInteractionsActivity.this, SelectMediaActivity.class);
                            PersonalInteractionsActivity.this.startActivity(i);
                        } else {
                            Snackbar.make(view, "Sending", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case FORWARD:
                fltActionButtonSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fileTransferUIUtil.clean();
                        Set<FileInfoDTO> files = personalInteractionsAdapter.getSelectedFiles();
                        if(!FileTransferUIUtil.SendOperationResult.SENDING.equals(fileTransferUIUtil.send_items(files))){
                            Intent i = new Intent(PersonalInteractionsActivity.this, SelectReceiversActivity.class);
                            PersonalInteractionsActivity.this.startActivity(i);
                        } else {
                            Snackbar.make(view, "Sending", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
        }

    }

    private void setSendButtonImage(SendButtonAction action){
        switch (action){
            case SEND:
                fltActionButtonSend.setImageResource(R.mipmap.ic_add_white_plus);
                break;

            case FORWARD:
                fltActionButtonSend.setImageResource(R.mipmap.send_white_up_arrow);
                break;
        }
    }

    @Override
    public void incrementTotalNoOfSelections() {
        numberOfSelecedItems++;
        initSendButton();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeAllListeners();
        finish();
    }

    private void removeAllListeners(){
        personalInteractionsAdapter.unsubscribeFileTransferStatusEvents();
    }

    @Override
    public void decrementTotalNoOfSelections() {
        numberOfSelecedItems--;
        initSendButton();
    }

    private enum SendButtonAction{
        SEND, FORWARD
    }
}
