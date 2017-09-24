package com.aj.sendall.network.services;

import android.content.Context;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.db.dto.ConnectionsAndUris;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.network.runnable.FileTransferServer;
import com.aj.sendall.network.runnable.abstr.AbstractServer;
import com.aj.sendall.network.services.abstr.AbstractServerService;
import com.aj.sendall.network.monitor.Updatable;

import javax.inject.Inject;

public class FileTransferServerService extends AbstractServerService {
    private static FileTransferServer fileTransferServer;
    private static ConnectionsAndUris connectionsAndUris;

    @Inject
    public AppManager appManager;

    public FileTransferServerService(){
        super("FileTransferServerService");
    }

    @Override
    protected void afterStopped(){
        fileTransferServer = null;
    }

    @Override
    protected boolean createServerToStaticVariable(AppManager appManager, Updatable updatable){
        if(fileTransferServer == null){
            fileTransferServer = new FileTransferServer(appManager,connectionsAndUris);
            connectionsAndUris = null;//We don't need it anymore here
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected AbstractServer getServerFromAStaticVariable(){
        return fileTransferServer;
    }

    @Override
    protected void setServerFromStaticVariableToNull(){
        fileTransferServer = null;
    }

    @Override
    protected int getHotspotInitAppStatus() {
        return SharedPrefConstants.CURR_STATUS_TRANSFERRING;
    }

    @Override
    protected AppManager getAppManager(Context context){
        if(appManager == null){
            ((AndroidApplication) context.getApplicationContext()).getDaggerInjector().inject(this);;
        }
        return appManager;
    }

    public static void start(Context context, ConnectionsAndUris connectionsAndUris){
        FileTransferServerService.connectionsAndUris = connectionsAndUris;
        start(context, null, FileTransferServerService.class);
    }

    public static void stop(Context context) {
        stop(context, FileTransferServerService.class);
    }
}
