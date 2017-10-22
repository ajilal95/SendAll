package com.aj.sendall.services;

import android.content.Context;

import com.aj.sendall.application.ThisApplication;
import com.aj.sendall.controller.AppController;
import com.aj.sendall.nw.comms.abstr.AbstractClient;
import com.aj.sendall.services.abstr.AbstractClientService;

import javax.inject.Inject;

public class NewConnCreationClientService extends AbstractClientService {
    @Inject
    public AppController appController;

    @Override
    public void onCreate() {
        super.onCreate();
        ((ThisApplication) getApplication()).getDaggerInjector().inject(this);
    }

    public NewConnCreationClientService() {
        super("ConnCreationClientService");
    }

    protected AppController getAppController(){
        return appController;
    }

    public static void start(Context context, AbstractClient client){
        start(context, client, NewConnCreationClientService.class);
    }

    public static void stop(Context context){
        stop(context, NewConnCreationClientService.class);
    }
}
