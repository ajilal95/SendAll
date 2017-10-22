package com.aj.sendall.services.abstr;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.aj.sendall.events.EventRouter;
import com.aj.sendall.events.EventRouterFactory;
import com.aj.sendall.events.event.CloseServerSocketCommand;
import com.aj.sendall.controller.AppController;
import com.aj.sendall.nw.comms.abstr.AbstractServer;

public abstract class AbstractServerService extends IntentService {
    protected static final String ACTION_START_NEW = "com.aj.sendall.network.services.action.START_NEW";
    protected static final String ACTION_STOP = "com.aj.sendall.network.services.action.STOP";

    private AppController appController;
    private EventRouter eventRouter = EventRouterFactory.getInstance();

    public AbstractServerService(String serviceName) {
        super(serviceName);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        this.appController = getAppManager(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_NEW.equals(action)) {
                if(appController.isSystemFree()) {
                    try {
                        startServerAction();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (ACTION_STOP.equals(action)) {
                stopCurrentServer();
                afterStopped();
            }
        }
    }

    abstract protected AppController getAppManager(Context context);
    abstract protected AbstractServer getServer(AppController appController);
    abstract protected void afterStopped();

    private void startServerAction(){
        try{
            new Handler().post(getServer(appController));
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getContext(), "Sorry!! Failed", Toast.LENGTH_SHORT).show();
        }
    }

    protected Context getContext(){
        return this;
    }

    private void stopCurrentServer() {
        CloseServerSocketCommand event = new CloseServerSocketCommand();
        eventRouter.broadcast(event);
    }

    protected static void start(Context context, Class<?> serviceClass) {
        Intent intent = new Intent(context, serviceClass);
        intent.setAction(ACTION_START_NEW);
        context.startService(intent);
    }

    protected static void stop(Context context, Class<?> serviceClass) {
        Intent intent = new Intent(context, serviceClass);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }
}