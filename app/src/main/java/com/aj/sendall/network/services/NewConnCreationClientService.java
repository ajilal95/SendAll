package com.aj.sendall.network.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.runnable.abstr.AbstractClient;

import javax.inject.Inject;

public class NewConnCreationClientService extends IntentService {
    private static final String ACTION_START_NEW = "START_NEW";

    @Inject
    public AppManager appManager;

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
        NewConnCreationClientService.client = client;
        Intent intent = new Intent(context, NewConnCreationClientService.class);
        intent.setAction(ACTION_START_NEW);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_NEW.equals(action)) {
                new Handler().post(client);
            }
        }
    }
}
