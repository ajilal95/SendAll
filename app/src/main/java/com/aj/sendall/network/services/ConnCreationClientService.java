package com.aj.sendall.network.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.runnable.NewConnCreationClient;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.interfaces.Updatable;

import javax.inject.Inject;

public class ConnCreationClientService extends IntentService {
    private static final String ACTION_START_NEW = "START_NEW";

    @Inject
    public AppManager appManager;

    private static NewConnCreationClient client = null;

    @Override
    public void onCreate() {
        super.onCreate();
        ((AndroidApplication) getApplication()).getDaggerInjector().inject(this);
    }

    public ConnCreationClientService() {
        super("ConnCreationClientService");
    }


    public static void start(Context context, NewConnCreationClient client) {
        ConnCreationClientService.client = client;
        Intent intent = new Intent(context, ConnCreationClientService.class);
        intent.setAction(ACTION_START_NEW);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_NEW.equals(action)) {
                appManager.handler.post(client);
            }
        }
    }
}
