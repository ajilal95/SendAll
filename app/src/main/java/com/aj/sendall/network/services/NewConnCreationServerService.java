package com.aj.sendall.network.services;

import android.content.Context;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.network.runnable.NewConnCreationServer;
import com.aj.sendall.network.runnable.abstr.AbstractServer;
import com.aj.sendall.network.services.abstr.AbstractServerService;
import com.aj.sendall.network.monitor.Updatable;

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
    protected boolean createServerToStaticVariable(AppManager appManager, Updatable connectorActivity){
        if(currServer == null){
            currServer = new NewConnCreationServer(appManager, connectorActivity);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected int getHotspotInitAppStatus() {
        return SharedPrefConstants.CURR_STATUS_CEATING_CONNECTION;
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

    public static void start(Context context, Updatable activityToUpdate) {
        start(context, activityToUpdate, NewConnCreationServerService.class);
    }

    public static void stop(Context context) {
        stop(context, NewConnCreationServerService.class);
    }
}
