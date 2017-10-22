package com.aj.sendall.services;

import android.content.Context;

import com.aj.sendall.application.ThisApplication;
import com.aj.sendall.controller.AppController;
import com.aj.sendall.nw.comms.NewConnCreationServer;
import com.aj.sendall.nw.comms.abstr.AbstractServer;
import com.aj.sendall.services.abstr.AbstractServerService;

import javax.inject.Inject;

public class NewConnCreationServerService extends AbstractServerService {
    @Inject
    public AppController appController;

    public NewConnCreationServerService() {
        super("NewConnCreationServerService");
    }

    @Override
    protected AppController getAppManager(Context context){
        if(appController == null)
            ((ThisApplication)context.getApplicationContext()).getDaggerInjector().inject(this);

        return appController;
    }

    @Override
    protected AbstractServer getServer(AppController appController){
        return new NewConnCreationServer(appController);
    }

    @Override
    public void afterStopped(){
    }

    public static void start(Context context) {
        start(context, NewConnCreationServerService.class);
    }

    public static void stop(Context context) {
        stop(context, NewConnCreationServerService.class);
    }
}
