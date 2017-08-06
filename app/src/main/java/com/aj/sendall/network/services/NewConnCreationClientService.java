package com.aj.sendall.network.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.runnable.NewConnCreationClient;
import com.aj.sendall.network.runnable.abstr.AbstractClient;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.interfaces.Updatable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class NewConnCreationClientService extends IntentService {
    private static final String ACTION_START_NEW = "START_NEW";
    private static final String ACTION_STOP = "STOP";

    @Inject
    public AppManager appManager;

    private final static List<AbstractClient> clients = new ArrayList<>();

    private static AbstractClient client = null;

    @Override
    public void onCreate() {
        super.onCreate();
        ((AndroidApplication) getApplication()).getDaggerInjector().inject(this);
    }

    public NewConnCreationClientService() {
        super("ConnCreationClientService");
    }


    public static void start(Context context, AbstractClient client) {
        synchronized (clients) {
            NewConnCreationClientService.client = client;
            Intent intent = new Intent(context, NewConnCreationClientService.class);
            intent.setAction(ACTION_START_NEW);
            context.startService(intent);
        }
    }

    public static void stop(Context context){
        synchronized (clients) {
            Intent intent = new Intent(context, NewConnCreationClientService.class);
            intent.setAction(ACTION_STOP);
            context.startService(intent);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            synchronized (clients) {
                final String action = intent.getAction();
                if (ACTION_START_NEW.equals(action)) {
                    if (client != null) {
                        clients.add(client);
                        new Handler().post(client);
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
}
