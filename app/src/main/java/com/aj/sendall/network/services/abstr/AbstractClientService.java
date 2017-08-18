package com.aj.sendall.network.services.abstr;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.runnable.abstr.AbstractClient;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.interfaces.Updatable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractClientService extends IntentService {
    private static final String ACTION_START_NEW = "com.aj.sendall.network.services.NewConnCreationClientService.START_NEW";
    private static final String ACTION_STOP = "com.aj.sendall.network.services.NewConnCreationClientService.STOP";


    private final static List<AbstractClient> clients = new ArrayList<>();
    private AppManager appManager;
    private static AbstractClient client;

    public AbstractClientService(String service){
        super(service);
    }

    protected abstract AppManager getAppManager();

    @Override
    protected void onHandleIntent(Intent intent) {
        if(appManager == null){
            appManager = getAppManager();
        }
        if (intent != null) {
            synchronized (clients) {
                final String action = intent.getAction();
                if (ACTION_START_NEW.equals(action)) {
                    if (client != null) {
                        clients.add(client);
                        new Handler().post(client);
                        client = null;
                    }
                } else if(ACTION_STOP.equals(action)){
                    Updatable.UpdateEvent event = new Updatable.UpdateEvent();
                    event.data.put(Constants.ACTION, Constants.CLOSE_SOCKET);
                    for(AbstractClient client : clients){
                        client.update(event);
                    }
                    clients.clear();
                }
            }
        }
    }

    public static void start(Context context, AbstractClient client, Class<?> serviceClass) {
        synchronized (clients) {
            AbstractClientService.client = client;
            Intent intent = new Intent(context, serviceClass);
            intent.setAction(ACTION_START_NEW);
            context.startService(intent);
        }
    }

    public static void stop(Context context, Class<?> serviceClass){
        synchronized (clients) {
            Intent intent = new Intent(context, serviceClass);
            intent.setAction(ACTION_STOP);
            context.startService(intent);
        }
    }

}
