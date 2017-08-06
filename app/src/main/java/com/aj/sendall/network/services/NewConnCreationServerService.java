package com.aj.sendall.network.services;

import android.content.Context;
import android.content.Intent;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.runnable.NewConnCreationServer;
import com.aj.sendall.network.runnable.abstr.AbstractServer;
import com.aj.sendall.network.services.abstr.AbstractServerService;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.interfaces.Updatable;

import java.net.ServerSocket;
import java.util.Map;

import javax.inject.Inject;

public class NewConnCreationServerService extends AbstractServerService {

    private static boolean serverRunning = false;

    @Inject
    public AppManager appManager;

    private static NewConnCreationServer currServer = null;

    public NewConnCreationServerService() {
        super("ConnCreationServerService");
    }

    @Override
    protected AppManager getAppManager(Context context){
        if(appManager == null)
            ((AndroidApplication)context.getApplicationContext()).getDaggerInjector().inject(this);

        return appManager;
    }

    @Override
    protected Map<String, String> updateRecordToAdv(Map<String, String> mapToAdv, Intent intent){
        return mapToAdv;
    }

    @Override
    protected String getServerPurpose(){
        return Constants.ADV_VALUE_PURPOSE_CONNECTION_CREATION;
    }

    @Override
    protected boolean createServerToStaticVariable(ServerSocket serverSocket, AppManager appManager, Updatable connectorActivity){
        if(currServer == null){
            currServer = new NewConnCreationServer(serverSocket, appManager, connectorActivity);
            serverRunning = true;
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected AbstractServer getServerFromAStaticVariable(){
        return currServer;
    }

    @Override
    protected void setServerFromStaticVariableToNull(){
        serverRunning = false;
        currServer = null;
    }

    @Override
    public void afterStopped(){
        serverRunning = false;
        currServer = null;
    }
}
