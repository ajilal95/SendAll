package com.aj.sendall.network.services;

import android.app.IntentService;
import android.content.Intent;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.services.abstr.AbstractClientService;

import javax.inject.Inject;

public class FileTransferClientService extends AbstractClientService {
    @Inject
    AppManager appManager;

    public FileTransferClientService(){
        super("FileTransferClientService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((AndroidApplication)this.getApplication()).getDaggerInjector().inject(this);
    }

    @Override
    protected AppManager getAppManager() {
        return appManager;
    }
}
