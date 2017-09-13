package com.aj.sendall.network.services.abstr;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.network.runnable.abstr.AbstractServer;
import com.aj.sendall.network.services.NewConnCreationServerService;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.interfaces.Updatable;

import java.net.ServerSocket;

public abstract class AbstractServerService extends IntentService {
    protected static final String ACTION_START_NEW = "com.aj.sendall.network.services.action.START_NEW";
    protected static final String ACTION_STOP = "com.aj.sendall.network.services.action.STOP";

    private static final Object syncObj = new Object();
    private static boolean serverRunning = false;
    private AppManager appManager;
    private static Updatable activityToUpdate;

    private int port;

    public AbstractServerService(String serviceName) {
        super(serviceName);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        this.appManager = getAppManager(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        synchronized (syncObj) {
            if (intent != null) {
                final String action = intent.getAction();
                if (ACTION_START_NEW.equals(action)) {
                    if(!serverRunning) {
                        serverRunning = true;
                        try {
                            createHotspot(appManager, port);
                            ServerSocket serverSocket = new ServerSocket(0);
                            port = serverSocket.getLocalPort();
                            createServerToStaticVariable(serverSocket, appManager, activityToUpdate);
                            activityToUpdate = null;
                            startServerAction();
                        } catch (Exception e) {
                            serverRunning = false;
                            e.printStackTrace();
                        }
                    }
                } else if (ACTION_STOP.equals(action)) {
                    activityToUpdate = null;
                    stopCurrentServer();
                    afterStopped();
                }
            }
        }
    }

    abstract protected AppManager getAppManager(Context context);
    abstract protected boolean createServerToStaticVariable(ServerSocket serverSocket, AppManager appManager, Updatable updatableActivity);
    abstract protected AbstractServer getServerFromAStaticVariable();
    abstract protected void setServerFromStaticVariableToNull();
    abstract protected void afterStopped();
    abstract protected void createHotspot(AppManager appManager, int port);
    abstract protected void shutdownHotspot(AppManager appManager);

    private void startServerAction(){
        try{
            new Handler().post(getServerFromAStaticVariable());
            appManager.notificationUtil.removeToggleNotification();
        } catch (Exception e){
            e.printStackTrace();
            appManager.sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
            appManager.sharedPrefUtil.commit();
            setServerFromStaticVariableToNull();
            Toast.makeText(getContext(), "Sorry!! Failed", Toast.LENGTH_SHORT).show();
        }
    }

    protected Context getContext(){
        return this;
    }

    private void stopCurrentServer() {
        shutdownHotspot(appManager);
        if(getServerFromAStaticVariable() != null){
            Updatable.UpdateEvent event = new Updatable.UpdateEvent();
            event.source = NewConnCreationServerService.class;
            event.data.put(Constants.ACTION, Constants.CLOSE_SOCKET);
            getServerFromAStaticVariable().update(event);
            setServerFromStaticVariableToNull();
        }
        serverRunning = false;
    }

    public static void start(Context context, Updatable activityToUpdate, Class<?> serviceClass) {
        Intent intent = new Intent(context, serviceClass);
        intent.setAction(ACTION_START_NEW);
        AbstractServerService.activityToUpdate = activityToUpdate;
        context.startService(intent);
    }

    public static void stop(Context context, Class<?> serviceClass) {
        Intent intent = new Intent(context, serviceClass);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }
}