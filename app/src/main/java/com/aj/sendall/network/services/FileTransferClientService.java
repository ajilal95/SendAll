package com.aj.sendall.network.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.network.runnable.abstr.AbstractClient;
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

    @Override
    protected int getAppStatusOnCreatingClient() {
        return SharedPrefConstants.CURR_STATUS_TRANSFERRING;
    }

    public static void start(Context context, AbstractClient client) {
        start(context, client, FileTransferClientService.class);
    }

    public static void stop(Context context){
        stop(context, FileTransferClientService.class);
    }
}
