package com.aj.sendall.services;

import android.content.Context;

import com.aj.sendall.application.ThisApplication;
import com.aj.sendall.controller.AppController;
import com.aj.sendall.db.dto.ConnectionsAndUris;
import com.aj.sendall.nw.comms.FileTransferServer;
import com.aj.sendall.nw.comms.abstr.AbstractServer;
import com.aj.sendall.services.abstr.AbstractServerService;

import javax.inject.Inject;

public class FileTransferServerService extends AbstractServerService {
    private static ConnectionsAndUris connectionsAndUris;

    @Inject
    public AppController appController;

    public FileTransferServerService(){
        super("FileTransferServerService");
    }

    @Override
    protected void afterStopped(){
    }

    @Override
    protected AbstractServer getServer(AppController appController){
        FileTransferServer server = new FileTransferServer(appController,connectionsAndUris);
        connectionsAndUris = null;//We don't need it anymore here
        return server;
    }

    @Override
    protected AppController getAppManager(Context context){
        if(appController == null){
            ((ThisApplication) context.getApplicationContext()).getDaggerInjector().inject(this);
        }
        return appController;
    }

    public static void start(Context context, ConnectionsAndUris connectionsAndUris){
        FileTransferServerService.connectionsAndUris = connectionsAndUris;
        start(context, FileTransferServerService.class);
    }

    public static void stop(Context context) {
        stop(context, FileTransferServerService.class);
    }
}
