package com.aj.sendall.network.services;

import android.content.Context;
import android.content.Intent;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.runnable.AbstractServer;
import com.aj.sendall.network.runnable.NewConnCreationServer;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.interfaces.Updatable;

import java.net.ServerSocket;
import java.util.Map;

import javax.inject.Inject;

public class ConnCreationServerService extends AbstractServerService {
    public static final String NET = "net";
    public static final String PASS = "pass";
    public static final String GRP_OWN_ADD = "GRP_OWN_ADD";

    private static boolean serverRunning = false;

    @Inject
    public AppManager appManager;

    public static Updatable connectorActivity;

    private static NewConnCreationServer currServer = null;

    public ConnCreationServerService() {
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
        mapToAdv.put(NET, intent.getStringExtra(NET));
        mapToAdv.put(PASS, intent.getStringExtra(PASS));
        mapToAdv.put(GRP_OWN_ADD, intent.getStringExtra(GRP_OWN_ADD));
        return mapToAdv;
    }

    @Override
    protected String getServerPurpose(){
        return Constants.ADV_VALUE_PURPOSE_CONNECTION_CREATION;
    }

    @Override
    protected boolean createServerToStaticVariable(ServerSocket serverSocket, AppManager appManager){
        if(currServer == null){
            currServer = new NewConnCreationServer(serverSocket, appManager, connectorActivity);
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
        connectorActivity = null;
        currServer = null;
    }

    @Override
    protected boolean allowOperation(String action){
        if(AbstractServerService.ACTION_START_NEW.equals(action)){
            if(serverRunning){
                return false;
            } else {
                serverRunning = true;
                return true;
            }
        } else if(AbstractServerService.ACTION_STOP.equals(action)){
            serverRunning = false;
            return true;
        }
        return true;
    }
}
