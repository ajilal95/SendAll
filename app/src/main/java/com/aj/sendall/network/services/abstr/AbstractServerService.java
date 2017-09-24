package com.aj.sendall.network.services.abstr;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.network.monitor.SocketSystem;
import com.aj.sendall.network.runnable.abstr.AbstractServer;
import com.aj.sendall.network.services.NewConnCreationServerService;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.network.monitor.Updatable;

import java.net.ServerSocket;

public abstract class AbstractServerService extends IntentService {
    protected static final String ACTION_START_NEW = "com.aj.sendall.network.services.action.START_NEW";
    protected static final String ACTION_STOP = "com.aj.sendall.network.services.action.STOP";

    private static final Object syncObj = new Object();
    private AppManager appManager;
    private static Updatable activityToUpdate;

    private static SocketSystem socketSystem;

    public AbstractServerService(String serviceName) {
        super(serviceName);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        this.appManager = getAppManager(this);
        if(socketSystem == null){
            socketSystem = SocketSystem.getInstance();
            socketSystem.addServerModeStopListener(new Runnable() {
                @Override
                public void run() {
                    appManager.stopHotspotAndScanning();
                }
            });
            socketSystem.addServerModeStartListener(new Runnable() {
                @Override
                public void run() {
                    appManager.initHotspot(getHotspotInitAppStatus(), socketSystem.getServerPortNo());
                }
            });
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        synchronized (syncObj) {
            if (intent != null) {
                final String action = intent.getAction();
                if (ACTION_START_NEW.equals(action)) {
                    if(socketSystem.idle()) {
                        try {
                            socketSystem.clear();
                            socketSystem.startServerMode(0);
                            createServerToStaticVariable(appManager, activityToUpdate);
                            activityToUpdate = null;
                            startServerAction();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (ACTION_STOP.equals(action)) {
                    if(socketSystem.inServerMode()){
                        socketSystem.stopServerMode();
                    }
                    activityToUpdate = null;
                    stopCurrentServer();
                    afterStopped();
                }
            }
        }
    }

    abstract protected AppManager getAppManager(Context context);
    abstract protected boolean createServerToStaticVariable(AppManager appManager, Updatable updatableActivity);
    abstract protected AbstractServer getServerFromAStaticVariable();
    abstract protected void setServerFromStaticVariableToNull();
    abstract protected void afterStopped();
    abstract protected int getHotspotInitAppStatus();

    private void startServerAction(){
        try{
            new Handler().post(getServerFromAStaticVariable());
        } catch (Exception e){
            e.printStackTrace();
            socketSystem.stopServerMode();
            setServerFromStaticVariableToNull();
            Toast.makeText(getContext(), "Sorry!! Failed", Toast.LENGTH_SHORT).show();
        }
    }

    protected Context getContext(){
        return this;
    }

    private void stopCurrentServer() {
        if(getServerFromAStaticVariable() != null){
            Updatable.UpdateEvent event = getStopUpdateEvent();
            getServerFromAStaticVariable().update(event);
            setServerFromStaticVariableToNull();
        }
    }

    @NonNull
    private Updatable.UpdateEvent getStopUpdateEvent() {
        Updatable.UpdateEvent event = new Updatable.UpdateEvent();
        event.source = NewConnCreationServerService.class;
        event.action = Constants.CLOSE_SOCKET;
        return event;
    }

    protected static void start(Context context, Updatable activityToUpdate, Class<?> serviceClass) {
        Intent intent = new Intent(context, serviceClass);
        intent.setAction(ACTION_START_NEW);
        AbstractServerService.activityToUpdate = activityToUpdate;
        context.startService(intent);
    }

    protected static void stop(Context context, Class<?> serviceClass) {
        Intent intent = new Intent(context, serviceClass);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }
}