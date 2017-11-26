package com.aj.sendall.services.abstr;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.aj.sendall.events.EventRouter;
import com.aj.sendall.events.EventRouterFactory;
import com.aj.sendall.events.event.CloseAllSocketsCommand;
import com.aj.sendall.controller.AppController;
import com.aj.sendall.nw.comms.abstr.AbstractClient;

public abstract class AbstractClientService extends IntentService {
    private static final String ACTION_START_NEW = "com.aj.sendall.services.abstr.AbstractClientService.START_NEW";
    private static final String ACTION_STOP = "com.aj.sendall.services.abstr.AbstractClientService.STOP";


    private static AbstractClient client;
    private EventRouter eventRouter = EventRouterFactory.getInstance();

    public AbstractClientService(String service) {
        super(service);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    protected abstract AppController getAppController();

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_NEW.equals(action)) {
                if (client != null) {
                    new Thread(client).start();
                    client = null;
                }
            } else if (ACTION_STOP.equals(action)) {
                CloseAllSocketsCommand event = new CloseAllSocketsCommand();
                eventRouter.broadcast(event);
            }
        }
    }

    protected static void start(Context context, AbstractClient client, Class<?> serviceClass) {
        AbstractClientService.client = client;
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
