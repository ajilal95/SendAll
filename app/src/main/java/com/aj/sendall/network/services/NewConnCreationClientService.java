package com.aj.sendall.network.services;

import android.content.Context;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.network.runnable.NewConnCreationClient;
import com.aj.sendall.network.runnable.abstr.AbstractClient;
import com.aj.sendall.network.services.abstr.AbstractClientService;

import javax.inject.Inject;

public class NewConnCreationClientService extends AbstractClientService {
    @Inject
    public AppManager appManager;

    @Override
    public void onCreate() {
        super.onCreate();
        ((AndroidApplication) getApplication()).getDaggerInjector().inject(this);
    }

    public NewConnCreationClientService() {
        super("ConnCreationClientService");
    }

    @Override
    protected AppManager getAppManager(){
        return appManager;
    }

    @Override
    protected int getAppStatusOnCreatingClient() {
        return SharedPrefConstants.CURR_STATUS_CEATING_CONNECTION;
    }

    public static void start(Context context, AbstractClient client){
        start(context, client, NewConnCreationClientService.class);
    }

    public static void stop(Context context){
        stop(context, NewConnCreationClientService.class);
    }
}
