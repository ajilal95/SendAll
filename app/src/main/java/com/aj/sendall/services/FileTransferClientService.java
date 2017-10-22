package com.aj.sendall.services;

import android.content.Context;

import com.aj.sendall.application.ThisApplication;
import com.aj.sendall.controller.AppController;
import com.aj.sendall.nw.comms.abstr.AbstractClient;
import com.aj.sendall.services.abstr.AbstractClientService;

import javax.inject.Inject;

public class FileTransferClientService extends AbstractClientService {
    @Inject
    AppController appController;

    public FileTransferClientService(){
        super("FileTransferClientService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((ThisApplication)this.getApplication()).getDaggerInjector().inject(this);
    }

    protected AppController getAppController() {
        return appController;
    }

    public static void start(Context context, AbstractClient client) {
        start(context, client, FileTransferClientService.class);
    }

    public static void stop(Context context){
        stop(context, FileTransferClientService.class);
    }
}
