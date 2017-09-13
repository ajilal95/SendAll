package com.aj.sendall.network.services;

import android.content.Context;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.network.runnable.NewConnCreationServer;
import com.aj.sendall.network.runnable.abstr.AbstractServer;
import com.aj.sendall.network.services.abstr.AbstractServerService;
import com.aj.sendall.ui.interfaces.Updatable;

import java.net.ServerSocket;

import javax.inject.Inject;

public class NewConnCreationServerService extends AbstractServerService {
    @Inject
    public AppManager appManager;

    private static NewConnCreationServer currServer = null;

    public NewConnCreationServerService() {
        super("NewConnCreationServerService");
    }

    @Override
    protected AppManager getAppManager(Context context){
        if(appManager == null)
            ((AndroidApplication)context.getApplicationContext()).getDaggerInjector().inject(this);

        return appManager;
    }

    @Override
    protected boolean createServerToStaticVariable(ServerSocket serverSocket, AppManager appManager, Updatable connectorActivity){
        if(currServer == null){
            currServer = new NewConnCreationServer(serverSocket, appManager, connectorActivity);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void createHotspot(AppManager appManager, int port){
        appManager.initHotspotForNewConnCreation(SharedPrefConstants.CURR_STATUS_CEATING_CONNECTION, port);
    }

    @Override
    protected void shutdownHotspot(AppManager appManager){
        appManager.stopHotspotAndScanning();
    }

    @Override
    protected AbstractServer getServerFromAStaticVariable(){
        return currServer;
    }

    @Override
    protected void setServerFromStaticVariableToNull(){
        currServer = null;
    }

    @Override
    public void afterStopped(){
        currServer = null;
    }
}
