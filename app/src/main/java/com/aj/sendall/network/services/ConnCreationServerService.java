package com.aj.sendall.network.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;

import com.aj.sendall.network.runnable.NewConnCreationServer;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.interfaces.Updatable;

public class ConnCreationServerService extends IntentService {
    private static final String ACTION_START_NEW = "com.aj.sendall.network.services.action.START_NEW";
    private static final String ACTION_STOP = "com.aj.sendall.network.services.action.STOP";

    private static NewConnCreationServer connServer = null;

    public ConnCreationServerService() {
        super("ConnCreationServerService");
    }

    public static void start(Context context, NewConnCreationServer newServer) {
        stopCurrentServer();
        connServer = newServer;
        Intent intent = new Intent(context, ConnCreationServerService.class);
        intent.setAction(ACTION_START_NEW);
        context.startService(intent);
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, ConnCreationServerService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_NEW.equals(action)) {
                new Handler().post(connServer);
            } else if (ACTION_STOP.equals(action)) {
                stopCurrentServer();
                connServer = null;
            }
        }
    }

    private static void stopCurrentServer() {
        if(connServer != null){
            Updatable.UpdateEvent event = new Updatable.UpdateEvent();
            event.source = ConnCreationServerService.class;
            event.data.put(Constants.ACTION, Constants.CLOSE_SOCKET);
            connServer.update(event);
        }
    }
}
